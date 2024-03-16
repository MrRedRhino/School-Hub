package org.pipeman.substitution_plan;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.pipeman.Config;
import org.pipeman.Database;
import org.pipeman.ilaw.ILAW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PlanCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanCache.class);
    private final Map<PlanIdentifier, byte[]> hashes = new HashMap<>();

    private final LoadingCache<String, Optional<PlanAccount>> userCache = Caffeine.newBuilder()
            .maximumSize(10)
            .evictionListener((String key, Optional<PlanAccount> value, RemovalCause cause) -> value.ifPresent(account -> account.ilaw.close()))
            .build(this::getAccount);

    private final LoadingCache<PlanIdentifier, Plan> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.of(5, ChronoUnit.MINUTES))
            .maximumSize(10)
            .build(this::downloadPlan);

    private final LoadingCache<PlanIdentifier, Plan> staleCache = Caffeine.newBuilder()
            .maximumSize(10)
            .build(k -> downloadPlan(k, false));

    public PlanCache() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    getPlan(new PlanIdentifier("", Day.TODAY));
                    getPlan(new PlanIdentifier("", Day.TOMORROW));
                } catch (Exception e) {
                    LOGGER.warn("Failed to auto fetch plan", e);
                }
            }
        };

        long interval = Duration.of(15, ChronoUnit.MINUTES).toMillis();
        new Timer(true).schedule(task, interval, interval);
    }

    public void removeAccount(String clazz) {
        userCache.invalidate(clazz);
    }

    private Optional<PlanAccount> getAccount(String clazz) {
        return Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT username, pgp_sym_decrypt(password, :encryption_password) AS password, today_plan_id, tomorrow_plan_id
                        FROM substitution_accounts
                        WHERE class = :class
                        """)
                .bind("class", clazz)
                .bind("encryption_password", Config.get().encryptionPassword)
                .mapTo(PlanAccount.class)
                .findFirst());
    }

    public Plan downloadPlan(PlanIdentifier identifier, boolean addToStaleCache) {
        long start = System.nanoTime();

        Optional<PlanAccount> optionalAccount = userCache.get(identifier.clazz());
        if (optionalAccount.isEmpty()) return null;

        PlanAccount account = optionalAccount.get();
        String fileId = account.planId(identifier.day());
        Plan plan = new Plan(account.ilaw().downloadOnedriveFile(fileId));

        LOGGER.info("Took {}ms to download plan", (System.nanoTime() - start) / 1_000_000);

        byte[] oldHash = hashes.get(identifier);
        byte[] newHash = plan.getHash();
        if (oldHash != null && !Arrays.equals(oldHash, newHash)) {
            planChanged(identifier, plan);
        }
        hashes.put(identifier, newHash);
        if (addToStaleCache) staleCache.put(identifier, plan);
        return plan;
    }

    private Plan downloadPlan(PlanIdentifier identifier) {
        return downloadPlan(identifier, true);
    }

    private static void planChanged(PlanIdentifier identifier, Plan data) {
//        NotificationHandler.handlePlanUpdate(identifier.day(), data);
    }

    public Plan getPlan(PlanIdentifier id) {
        return cache.get(id);
    }

    public Plan getCachedPlan(PlanIdentifier plan) {
        return staleCache.get(plan);
    }

    public record PlanAccount(String username, String password, String todayPlanId, String tomorrowPlanId, ILAW ilaw) {
        @ConstructorProperties({"username", "password", "today_plan_id", "tomorrow_plan_id"})
        public PlanAccount(String username, String password, String todayPlanId, String tomorrowPlanId) {
            this(username, password, todayPlanId, tomorrowPlanId, getIlaw(username, password));
        }

        private static ILAW getIlaw(String username, String password) {
            return ILAW.login(Config.get().ilUrl, username, password);
        }

        public String planId(Day day) {
            return day == Day.TODAY ? todayPlanId : tomorrowPlanId;
        }
    }
}

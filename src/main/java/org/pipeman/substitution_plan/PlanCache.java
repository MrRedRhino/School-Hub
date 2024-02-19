package org.pipeman.substitution_plan;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.pipeman.Config;
import org.pipeman.Main;
import org.pipeman.substitution_plan.notifications.NotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PlanCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanCache.class);
    private final Map<PlanIdentifier, byte[]> hashes = new HashMap<>();

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

    public Plan downloadPlan(PlanIdentifier identifier, boolean addToStaleCache) {
        long start = System.nanoTime();

        Config config = Config.get();
        String fileId = identifier.day() == Day.TODAY ? config.ilTodayPlanId : config.ilTomorrowPlanId;
        Plan plan = new Plan(Main.getIlaw().downloadOnedriveFile(fileId));

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
}

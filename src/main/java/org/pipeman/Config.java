package org.pipeman;

import org.pipeman.pconf.AbstractConfig;
import org.pipeman.utils.Utils;

import java.nio.file.Path;

public class Config extends AbstractConfig {
    private static final Config INSTANCE = new Config("config.properties");

    public final String dbUser = get("db-user", "");
    public final String dbPassword = get("db-password", "");
    public final String dbUrl = get("db-url", "");
    public final int apiPort = get("api-port", 4000);
    public final String ilUrl = this.get("its-learning-url", "https://your-school.itslearning.com");
    public final String ilPassword = this.get("its-learning-password", "Your password");
    public final String ilUser = this.get("its-learning-user", "Your name");
    public final String vapidPublicKey = this.get("vapid-public-key", "");
    public final String vapidPrivateKey = this.get("vapid-private-key", "");
    public final String vapidSubject = this.get("vapid-subject", "");
    public final String aiKey = this.get("ai-key", "");
    public final int dailyUsageLimit = this.get("daily-usage-limit", 233_333);
    public final int adminPassword = this.get("admin-password", 42069);
    public final String adminUsername = this.get("admin-username", "");
    public final int maxSeatsPerPerson = this.get("max-seats-per-person", 5);
    public final String encryptionPassword = this.get("encryption-password", Utils.generateRandomString(32));
    public final String googleBucket = this.get("google-bucket", "");
    public final Path googleCredentialFile = this.get("google-credential-file", Path.of("google-credentials.json"));
    public final boolean production = this.get("production", false);

    public Config(String file) {
        super(file);
        store(Path.of("config.properties"), "");
    }

    public static Config get() {
        return INSTANCE;
    }
}

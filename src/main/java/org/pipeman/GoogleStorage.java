package org.pipeman;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class GoogleStorage {
    private static final String bucket = Config.get().googleBucket;
    private static final Storage STORAGE = getStorage();

    private static Storage getStorage() {
        try {
            return StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(Files.newInputStream(Config.get().googleCredentialFile)))
                    .build()
                    .getService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Upload createNewUpload() {
        long id = Main.ID_GENERATOR.next();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, String.valueOf(id)).build();
        String url = STORAGE.signUrl(blobInfo,
                        15,
                        TimeUnit.MINUTES,
                        Storage.SignUrlOption.httpMethod(HttpMethod.PUT))
                .toExternalForm();
        return new Upload(id, url);
    }

    public static boolean removeObject(long id) {
        return STORAGE.delete(bucket, String.valueOf(id));
    }

    public static String getObjectUrl(long id) {
        return "https://storage.googleapis.com/" + bucket + "/" + id;
    }

    public record Upload(@JsonSerialize(using = ToStringSerializer.class) long id, String uploadUrl) {
    }
}

package org.pipeman.ilaw;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public interface ILAW {

    /**
     * ILAW is an experimental Itslearning "API" wrapper.
     *
     * @param url      Your it's-learning instance URL, e.g. https://your-school.itslearning.com
     * @param username Your itslearning-account username
     * @param password Your itslearning-account password
     * @return A new ILAW instance
     * @throws LoginException When the login failed
     */
    static ILAW login(String url, String username, String password) throws LoginException {
        try {
            return new ILAWImpl(url, username, password);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    <T> HttpResponse<T> sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> handler);

    void sendMessage(long recipientId, String content);

    List<Recipient> getMessageRecipients(String search);

    byte[] downloadOnedriveFile(String fileId);
}

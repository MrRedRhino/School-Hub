package org.pipeman.rest.reservation;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SES {
    private static final SesClient SES_CLIENT = SesClient.builder()
            .region(Region.EU_NORTH_1)
            .build();

    public static void sendReservationConfirmation(String recipient, String subject, String bodyHTML, byte[] plan) throws MessagingException, IOException {
        Session session = Session.getDefaultInstance(new Properties());

        MimeMessage message = new MimeMessage(session);
        message.setSubject(subject, "UTF-8");
        message.setFrom("Tickets <tickets@pipeman.org>");
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

        MimeMultipart msg = new MimeMultipart("mixed");
        message.setContent(msg);
        msg.addBodyPart(buildBody(bodyHTML));

        MimeBodyPart attachment = new MimeBodyPart();
        DataSource fds = new ByteArrayDataSource(plan, "image/png");
        attachment.setDataHandler(new DataHandler(fds));
        attachment.setFileName("plan.png");

        msg.addBodyPart(attachment);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        SdkBytes data = SdkBytes.fromByteArray(outputStream.toByteArray());
        RawMessage rawMessage = RawMessage.builder()
                .data(data)
                .build();

        SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                .rawMessage(rawMessage)
                .build();

        SES_CLIENT.sendRawEmail(rawEmailRequest);
    }

    private static MimeBodyPart buildBody(String bodyHTML) throws MessagingException {
        MimeMultipart msgBody = new MimeMultipart("alternative");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");
        msgBody.addBodyPart(htmlPart);

        MimeBodyPart wrap = new MimeBodyPart();
        wrap.setContent(msgBody);
        return wrap;
    }
}

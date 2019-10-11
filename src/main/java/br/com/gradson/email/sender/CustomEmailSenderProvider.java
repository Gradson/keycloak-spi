package br.com.gradson.email.sender;

import com.sun.mail.smtp.SMTPMessage;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.truststore.HostnameVerificationPolicy;
import org.keycloak.truststore.JSSETruststoreConfigurator;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLSocketFactory;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class CustomEmailSenderProvider implements EmailSenderProvider {

    private final KeycloakSession session;

    public CustomEmailSenderProvider(KeycloakSession session) {
        this.session = session;
    }

    public void send(Map<String, String> config, UserModel user, String subject, String textBody, String htmlBody) throws EmailException {
        Transport transport = null;

        try {
            String address = this.retrieveEmailAddress(user);
            Properties props = new Properties();
            if (config.containsKey("host")) {
                props.setProperty("mail.smtp.host", (String) config.get("host"));
            }

            boolean auth = "true".equals(config.get("auth"));
            boolean ssl = "true".equals(config.get("ssl"));
            boolean starttls = "true".equals(config.get("starttls"));
            if (config.containsKey("port") && config.get("port") != null) {
                props.setProperty("mail.smtp.port", (String) config.get("port"));
            }

            if (auth) {
                props.setProperty("mail.smtp.auth", "true");
            }

            if (ssl) {
                props.setProperty("mail.smtp.ssl.enable", "true");
            }

            if (starttls) {
                props.setProperty("mail.smtp.starttls.enable", "true");
            }

            if (ssl || starttls) {
                this.setupTruststore(props);
            }

            props.setProperty("mail.smtp.timeout", "10000");
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            String from = (String) config.get("from");
            String fromDisplayName = (String) config.get("fromDisplayName");
            String replyTo = (String) config.get("replyTo");
            String replyToDisplayName = (String) config.get("replyToDisplayName");
            String envelopeFrom = (String) config.get("envelopeFrom");
            Session session = Session.getInstance(props);
            Multipart multipart = new MimeMultipart("alternative");
            MimeBodyPart htmlPart;
            if (textBody != null) {
                htmlPart = new MimeBodyPart();
                htmlPart.setText(textBody, "UTF-8");
                multipart.addBodyPart(htmlPart);
            }

            if (htmlBody != null) {
                htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
                multipart.addBodyPart(htmlPart);
            }

            SMTPMessage msg = new SMTPMessage(session);
            msg.setFrom(this.toInternetAddress(from, fromDisplayName));
            msg.setReplyTo(new Address[]{this.toInternetAddress(from, fromDisplayName)});
            if (replyTo != null && !replyTo.isEmpty()) {
                msg.setReplyTo(new Address[]{this.toInternetAddress(replyTo, replyToDisplayName)});
            }

            if (envelopeFrom != null && !envelopeFrom.isEmpty()) {
                msg.setEnvelopeFrom(envelopeFrom);
            }

            msg.setHeader("To", address);
            msg.setSubject(subject, "utf-8");
            msg.setContent(multipart);
            msg.saveChanges();
            msg.setSentDate(new Date());
            transport = session.getTransport("smtp");
            if (auth) {
                transport.connect((String) config.get("user"), (String) config.get("password"));
            } else {
                transport.connect();
            }

            transport.sendMessage(msg, new InternetAddress[]{new InternetAddress(address)});
        } catch (Exception var27) {
            ServicesLogger.LOGGER.failedToSendEmail(var27);
            throw new EmailException(var27);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException var26) {
                }
            }

        }

    }

    protected InternetAddress toInternetAddress(String email, String displayName) throws UnsupportedEncodingException, AddressException, EmailException {
        if (email != null && !"".equals(email.trim())) {
            return displayName != null && !"".equals(displayName.trim()) ? new InternetAddress(email, displayName, "utf-8") : new InternetAddress(email);
        } else {
            throw new EmailException("Please provide a valid address", (Throwable) null);
        }
    }

    protected String retrieveEmailAddress(UserModel user) {
        //TODO override method
        return user.getEmail();
    }

    private void setupTruststore(Properties props) throws NoSuchAlgorithmException, KeyManagementException {
        JSSETruststoreConfigurator configurator = new JSSETruststoreConfigurator(this.session);
        SSLSocketFactory factory = configurator.getSSLSocketFactory();
        if (factory != null) {
            props.put("mail.smtp.ssl.socketFactory", factory);
            if (configurator.getProvider().getPolicy() == HostnameVerificationPolicy.ANY) {
                props.setProperty("mail.smtp.ssl.trust", "*");
            }
        }

    }

    public void close() {
    }
}

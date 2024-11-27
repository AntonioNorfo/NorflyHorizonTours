package antonionorfo.norflyHorizonTours.tools;

import antonionorfo.norflyHorizonTours.entities.User;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailgunSender {

    private static final Logger logger = LoggerFactory.getLogger(MailgunSender.class);

    private final String apiKey;
    private final String domain;

    public MailgunSender(@Value("${mailgun.apikey}") String apiKey,
                         @Value("${mailgun.domain}") String domain) {
        this.apiKey = apiKey;
        this.domain = domain;
    }

    public void sendPasswordResetEmail(User recipient, String token) {
        String resetLink = "https://example.com/reset-password?token=" + token;

        HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + domain + "/messages")
                .basicAuth("api", apiKey)
                .queryString("from", "NorFly Horizon Tours <noreply@" + domain + ">")
                .queryString("to", recipient.getEmail())
                .queryString("subject", "Password Reset Request")
                .queryString("text", "Hello " + recipient.getFirstName() + ",\n\n"
                        + "Click the link below to reset your password:\n"
                        + resetLink + "\n\n"
                        + "If you did not request a password reset, please ignore this email.")
                .asJson();

        if (response.isSuccess()) {
            System.out.println("Password reset email sent to " + recipient.getEmail());
        } else {
            System.err.println("Failed to send password reset email: " + response.getStatus() + " - " + response.getBody());
        }
    }


    public void sendRegistrationEmail(User recipient) {
        String subject = "Registrazione completata!";
        String message = String.format("Benvenuto %s sulla piattaforma NorFly Horizon Tours!", recipient.getFirstName());
        sendPlainTextEmail(recipient.getEmail(), subject, message);
    }

    public void sendBookingConfirmationEmail(User recipient, String bookingDetails) {
        String subject = "Conferma Prenotazione - NorFly Horizon Tours";
        String message = String.format(
                "Ciao %s,\n\nLa tua prenotazione è stata confermata! Ecco i dettagli:\n%s\n\nGrazie per aver scelto NorFly Horizon Tours!",
                recipient.getFirstName(),
                bookingDetails
        );
        sendPlainTextEmail(recipient.getEmail(), subject, message);
    }

    public void sendCancellationEmail(User recipient, String bookingDetails) {
        String subject = "Cancellazione Prenotazione - NorFly Horizon Tours";
        String message = String.format(
                "Ciao %s,\n\nLa tua prenotazione è stata cancellata. Ecco i dettagli:\n%s\n\nSe hai bisogno di assistenza, contattaci.",
                recipient.getFirstName(),
                bookingDetails
        );
        sendPlainTextEmail(recipient.getEmail(), subject, message);
    }

    public void sendReminderEmail(User recipient, String excursionDetails) {
        String subject = "Promemoria Escursione - NorFly Horizon Tours";
        String message = String.format(
                "Ciao %s,\n\nQuesto è un promemoria per la tua escursione imminente:\n%s\n\nBuona avventura!",
                recipient.getFirstName(),
                excursionDetails
        );
        sendPlainTextEmail(recipient.getEmail(), subject, message);
    }

    public void sendPlainTextEmail(String recipientEmail, String subject, String message) {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + domain + "/messages")
                    .basicAuth("api", apiKey)
                    .queryString("from", "NorFly Horizon Tours <noreply@" + domain + ">")
                    .queryString("to", recipientEmail)
                    .queryString("subject", subject)
                    .queryString("text", message)
                    .queryString("h:Content-Type", "text/plain; charset=utf-8")
                    .asJson();

            handleResponse(response, recipientEmail);
        } catch (Exception e) {
            logger.error("Errore nell'invio dell'email a {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    public void sendHtmlEmail(String recipientEmail, String subject, String htmlContent) {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + domain + "/messages")
                    .basicAuth("api", apiKey)
                    .queryString("from", "NorFly Horizon Tours <noreply@" + domain + ">")
                    .queryString("to", recipientEmail)
                    .queryString("subject", subject)
                    .queryString("html", htmlContent)
                    .queryString("h:Content-Type", "text/html; charset=utf-8")
                    .asJson();

            handleResponse(response, recipientEmail);
        } catch (Exception e) {
            logger.error("Errore nell'invio dell'email HTML a {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    private void handleResponse(HttpResponse<JsonNode> response, String recipientEmail) {
        if (response.isSuccess()) {
            logger.info("Email inviata con successo a {}", recipientEmail);
        } else {
            logger.error("Errore nell'invio dell'email a {}: Status {} - Body {}",
                    recipientEmail, response.getStatus(), response.getBody());
        }
    }
}

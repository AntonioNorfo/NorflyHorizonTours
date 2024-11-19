package antonionorfo.norflyHorizonTours.tools;

import antonionorfo.norflyHorizonTours.entities.User;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailgunSender {

    private final String apiKey;
    private final String domain;

    public MailgunSender(@Value("${mailgun.apikey}") String apiKey,
                         @Value("${mailgun.domain}") String domain) {
        this.apiKey = apiKey;
        this.domain = domain;
    }

    public void sendRegistrationEmail(User recipient) {
        HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + domain + "/messages")
                .basicAuth("api", apiKey)
                .queryString("from", "NorFly Horizon Tours <noreply@" + domain + ">")
                .queryString("to", recipient.getEmail())
                .queryString("subject", "Registrazione completata!")
                .queryString("text", "Benvenuto " + recipient.getFirstName() + " sulla piattaforma NorFly Horizon Tours!")
                .asJson();

        if (response.isSuccess()) {
            System.out.println("Email inviata con successo a " + recipient.getEmail());
        } else {
            System.err.println("Errore nell'invio dell'email: Status " + response.getStatus() + " - " + response.getBody());
        }
    }
}

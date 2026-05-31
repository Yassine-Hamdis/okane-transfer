package com.okanetransfer.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String twilioPhoneNumber;

    // Cette méthode s'exécute automatiquement au démarrage de l'application
    // pour initialiser la connexion avec les serveurs de Twilio.
    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    public void sendOtpSms(String toPhoneNumber, String otpCode) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    "Votre code de vérification Okane Transfer est : " + otpCode // 3. Le texte
            ).create();

            System.out.println("SMS envoyé avec succès ! ID: " + message.getSid());

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS : " + e.getMessage());
            // En production, on pourrait lancer une exception personnalisée ici
        }
    }
}
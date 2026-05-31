package com.okanetransfer.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionService {

    // On récupère la clé secrète depuis application.properties
    @Value("${aes.secret.key}")
    private String aesSecretKey;

    private static final String ALGORITHM = "AES";

    /**
     * Chiffre une chaîne de caractères en AES
     */
    public String encrypt(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }
        try {
            // Création de la clé à partir du secret
            SecretKeySpec keySpec = new SecretKeySpec(aesSecretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // Chiffrement
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Encodage en Base64 pour pouvoir le stocker facilement en BDD (String)
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement de la donnée", e);
        }
    }

    /**
     * Déchiffre une chaîne de caractères chiffrée en AES (Utile pour plus tard)
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            return null;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(aesSecretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Décodage du Base64 puis déchiffrement
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du déchiffrement de la donnée", e);
        }
    }
}

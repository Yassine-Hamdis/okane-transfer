package com.okanetransfer.service;

import com.okanetransfer.entity.enums.OtpType;

public interface OtpService {
    String generateAndSave(Long userId, OtpType type); // retourne le code brut (simulation d’envoi)
    boolean verify(Long userId, String rawCode, OtpType type);
}
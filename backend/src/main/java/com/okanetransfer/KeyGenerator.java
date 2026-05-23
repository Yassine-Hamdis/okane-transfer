package com.okanetransfer;

import com.okanetransfer.util.AesEncryptionUtil;

public class KeyGenerator {
    public static void main(String[] args) {
        System.out.println(AesEncryptionUtil.generateBase64Key());
    }
}
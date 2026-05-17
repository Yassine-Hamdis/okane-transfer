package com.okanetransfer.security;

public class SecurityConstants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final long ACCESS_TOKEN_EXPIRATION  = 3_600_000L;     // 1 hour
    public static final long REFRESH_TOKEN_EXPIRATION = 604_800_000L;   // 7 days
}
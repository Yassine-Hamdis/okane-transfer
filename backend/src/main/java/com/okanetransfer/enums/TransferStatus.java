package com.okanetransfer.enums;

public enum TransferStatus {
    EN_ATTENTE,   // Waiting to be picked up
    PAYE,         // Paid out to recipient
    ANNULE,       // Cancelled
    EXPIRE        // Expired (not picked up in time)
}
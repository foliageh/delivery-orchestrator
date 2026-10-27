package com.delivery.bitrix;

import jakarta.resource.cci.ConnectionMetaData;

public class BitrixConnectionMetaData implements ConnectionMetaData {
    @Override
    public String getEISProductName() {
        return "Bitrix";
    }

    @Override
    public String getEISProductVersion() {
        return "REST API";
    }

    @Override
    public String getUserName() {
        return "Webhook";
    }
}
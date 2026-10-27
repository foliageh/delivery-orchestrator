package com.delivery.bitrix;

import jakarta.resource.cci.InteractionSpec;
import lombok.Getter;

@Getter
public class BitrixInteractionSpec implements InteractionSpec {
    private final String method;
    private final String webhookUrl;

    public BitrixInteractionSpec(String method, String webhookUrl) {
        this.method = method;
        this.webhookUrl = webhookUrl;
    }
}
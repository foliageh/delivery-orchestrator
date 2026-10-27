package com.delivery.bitrix;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import jakarta.resource.cci.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@PropertySource("classpath:bitrix.properties")
public class BitrixConnector {
    private final ConnectionFactory connectionFactory;
    private final RecordFactory recordFactory;

    @Value("${bitrix.webhook.url}")
    private String webhookUrl;

    public BitrixConnector(ConnectionFactory connectionFactory, RecordFactory recordFactory) {
        this.connectionFactory = connectionFactory;
        this.recordFactory = recordFactory;
    }

    public String executeMethod(String method, java.util.Map<String, Object> params) throws ResourceException {
        log.debug("Executing Bitrix method: {} with params: {}", method, params);

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            Interaction interaction = connection.createInteraction();
            InteractionSpec interactionSpec = new BitrixInteractionSpec(method, webhookUrl);

            BitrixMappedRecord inputRecord = (BitrixMappedRecord) recordFactory.createMappedRecord("BitrixRequest");
            inputRecord.setRecordName("BitrixRequest");
            inputRecord.setParameters(params);

            Record outputRecord = recordFactory.createMappedRecord("BitrixResponse");

            if (!interaction.execute(interactionSpec, inputRecord, outputRecord))
                throw new ResourceException("Failed to execute Bitrix method: " + method);

            String result = ((BitrixMappedRecord) outputRecord).getResponse();
            log.debug("Bitrix response: {}", result);

            return result;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (ResourceException e) {
                    log.error("Error closing connection", e);
                }
            }
        }
    }
}
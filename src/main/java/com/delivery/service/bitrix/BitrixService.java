package com.delivery.service.bitrix;

import com.delivery.bitrix.BitrixConnector;
import com.delivery.model.Courier;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitrixService {
    public final BitrixConnector connector;

    // not working: access denied
    public String createCourierDismissalDocument(Courier courier) throws ResourceException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("templateId", 39);
            params.put("value", "delivery_app");
            params.put("values", Map.of("name", courier.getName()));
            return connector.executeMethod("documentgenerator.document.add", params);
        } catch (Exception e) {
            log.error("Error creating courier dismissal document", e);
            throw new ResourceException("Failed to create courier dismissal document", e);
        }
    }

    public String createCourierDismissalTask(Courier courier) throws ResourceException {
        Map<String, Object> params = new HashMap<>();
        var fields = Map.of(
                "TITLE", "Уволить курьера",
                "DESCRIPTION", "Уволить курьера по имени " + courier.getName(),
                "RESPONSIBLE_ID", 1
        );
        params.put("fields", fields);
        return connector.executeMethod("tasks.task.add", params);
    }
}
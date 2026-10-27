package com.delivery.bitrix;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import jakarta.resource.cci.Record;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BitrixInteraction implements Interaction {
    private final CloseableHttpClient httpClient;
    private final Connection connection;
    private ResourceWarning warnings;

    public BitrixInteraction() {
        this.httpClient = HttpClients.createDefault();
        this.connection = null;
    }

    @Override
    public boolean execute(InteractionSpec ispec, Record input, Record output) throws ResourceException {
        if (!(ispec instanceof BitrixInteractionSpec))
            throw new ResourceException("Expected BitrixInteractionSpec");
        if (!(input instanceof BitrixMappedRecord))
            throw new ResourceException("Expected BitrixMappedRecord as input");
        if (!(output instanceof BitrixMappedRecord))
            throw new ResourceException("Expected BitrixMappedRecord as output");

        BitrixInteractionSpec spec = (BitrixInteractionSpec) ispec;
        BitrixMappedRecord inputRecord = (BitrixMappedRecord) input;
        BitrixMappedRecord outputRecord = (BitrixMappedRecord) output;

        try {
            String url = spec.getWebhookUrl() + spec.getMethod();
            log.debug("Executing request to URL: {}", url);

            HttpPost httpPost = new HttpPost(new URI(url));
            httpPost.setHeader("Content-Type", "application/json");
            String jsonBody = new ObjectMapper().writeValueAsString(inputRecord.getParameters());
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    log.warn("HTTP status code: {}", statusCode);
                    addWarning("HTTP status code: " + statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseString = EntityUtils.toString(entity);
                    outputRecord.setResponse(responseString);
                    return true;
                } else {
                    log.error("No response entity from Bitrix");
                    addWarning("No response entity from Bitrix");
                    return false;
                }
            }
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            log.error("Error preparing request to Bitrix", e);
            addWarning("Error preparing request: " + e.getMessage());
            throw new ResourceException("Error preparing request to Bitrix", e);
        } catch (IOException e) {
            log.error("Error executing request to Bitrix", e);
            addWarning("Error executing request: " + e.getMessage());
            throw new ResourceException("Error executing request to Bitrix", e);
        }
    }

    @Override
    public Record execute(InteractionSpec ispec, Record input) throws ResourceException {
        throw new ResourceException("This method is not supported, use execute(InteractionSpec, Record, Record) instead");
    }

    @Override
    public void close() throws ResourceException {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new ResourceException("Error closing HTTP client", e);
        }
    }

    @Override
    public Connection getConnection() {
        if (connection == null)
            throw new RuntimeException("No connection associated with this interaction");
        return connection;
    }

    @Override
    public ResourceWarning getWarnings() {
        return warnings;
    }

    @Override
    public void clearWarnings() {
        warnings = null;
    }

    private void addWarning(String message) {
        log.warn("Interaction warning: {}", message);
    }
}
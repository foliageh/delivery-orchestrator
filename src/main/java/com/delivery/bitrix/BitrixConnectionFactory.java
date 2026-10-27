package com.delivery.bitrix;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.Reference;

@Component
public class BitrixConnectionFactory implements ConnectionFactory {
    private final RecordFactory recordFactory;

    public BitrixConnectionFactory(RecordFactory recordFactory) {
        this.recordFactory = recordFactory;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        return new BitrixConnection();
    }

    @Override
    public Connection getConnection(ConnectionSpec properties) throws ResourceException {
        return getConnection();
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return recordFactory;
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new BitrixResourceAdapterMetaData();
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException("JNDI lookup not supported");
    }

    @Override
    public void setReference(Reference reference) {
        throw new UnsupportedOperationException("JNDI lookup not supported");
    }
}
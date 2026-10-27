package com.delivery.bitrix;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BitrixConnection implements Connection {
    private boolean closed = false;

    @Override
    public Interaction createInteraction() throws ResourceException {
        checkIfClosed();
        return new BitrixInteraction();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        checkIfClosed();
        throw new ResourceException("Local transactions are not supported");
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        checkIfClosed();
        return new BitrixConnectionMetaData();
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        checkIfClosed();
        throw new ResourceException("ResultSet is not supported");
    }

    @Override
    public void close() throws ResourceException {
        log.debug("Closing Bitrix connection");
        closed = true;
    }

    private void checkIfClosed() throws ResourceException {
        if (closed) throw new ResourceException("Connection is already closed");
    }
}
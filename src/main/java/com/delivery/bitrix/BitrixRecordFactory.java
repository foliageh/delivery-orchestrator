package com.delivery.bitrix;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.RecordFactory;
import org.springframework.stereotype.Component;

@Component
public class BitrixRecordFactory implements RecordFactory {
    @Override
    public MappedRecord createMappedRecord(String recordName) throws ResourceException {
        return new BitrixMappedRecord(recordName);
    }

    @Override
    public IndexedRecord createIndexedRecord(String recordName) throws ResourceException {
        throw new ResourceException("IndexedRecord not supported by Bitrix");
    }
}
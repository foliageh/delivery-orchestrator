package com.delivery.bitrix;

import jakarta.resource.cci.ResourceAdapterMetaData;

public class BitrixResourceAdapterMetaData implements ResourceAdapterMetaData {
    @Override
    public String getAdapterVersion() {
        return "1.0";
    }

    @Override
    public String getAdapterVendorName() {
        return "Delivery App";
    }

    @Override
    public String getAdapterName() {
        return "Bitrix JCA Connector";
    }

    @Override
    public String getAdapterShortDescription() {
        return "JCA bitrix for Bitrix CRM";
    }

    @Override
    public String getSpecVersion() {
        return "1.7";
    }

    @Override
    public String[] getInteractionSpecsSupported() {
        return new String[]{"com.delivery.bitrix.record.BitrixInteractionSpec"};
    }

    @Override
    public boolean supportsExecuteWithInputAndOutputRecord() {
        return true;
    }

    @Override
    public boolean supportsExecuteWithInputRecordOnly() {
        return false;
    }

    @Override
    public boolean supportsLocalTransactionDemarcation() {
        return false;
    }
}
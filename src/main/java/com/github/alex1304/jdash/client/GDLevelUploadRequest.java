package com.github.alex1304.jdash.client;

import com.github.alex1304.jdash.exception.GDClientException;

import java.util.Map;

class GDLevelUploadRequest extends AbstractAuthenticatedGDRequest<Long> {

    GDLevelUploadRequest(AuthenticatedGDClient client) {
        super(client);
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    void putParams(Map<String, String> params) {

    }

    @Override
    Long parseResponse0(String response) throws GDClientException {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

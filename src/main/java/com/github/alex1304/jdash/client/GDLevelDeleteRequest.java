package com.github.alex1304.jdash.client;

import com.github.alex1304.jdash.exception.BlockingUserFailedException;
import com.github.alex1304.jdash.exception.GDClientException;
import com.github.alex1304.jdash.util.Routes;

import java.util.Map;

class GDLevelDeleteRequest extends AbstractAuthenticatedGDRequest<Void> {

    private final long levelId;

    GDLevelDeleteRequest(AuthenticatedGDClient client, long levelId) {
        super(client);
        this.levelId = levelId;
    }

    @Override
    public String getPath() {
        return Routes.DELETE_LEVEL;
    }

    @Override
    void putParams(Map<String, String> params) {
        params.put("levelID", ""+levelId);
        params.put("secret", "Wmfv2898gc9"); // Overrides the default one
    }

    @Override
    Void parseResponse0(String response) throws GDClientException {
        if (!response.equals("1")) {
            throw new BlockingUserFailedException();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GDLevelDeleteRequest && ((GDLevelDeleteRequest) obj).levelId == levelId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(levelId);
    }
}

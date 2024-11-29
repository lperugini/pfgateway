package com.sap.periziafacile.pfgateway.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ServiceContainer {
    private static final Map<String, String> services = new HashMap<>();

    private ServiceContainer() {}

    public static Boolean registerService(String name, String uri) {
        if (services.containsKey(name)) {
            return false;
        }

        services.put(name, uri);
        return true;
    }

    public static Optional<String> getService(String name) {
        if (services.containsKey(name)) {
            return Optional.of(services.get(name));
        }
        return Optional.empty();
    }

}

package com.konfyrm.gigatester.features.service;

import com.konfyrm.gigatester.features.domain.FeatureFlag;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory feature flags. Nothing is persisted: a restart resets every flag to its default. */
@Service
public class FeatureFlagService {

    private final Map<FeatureFlag, Boolean> values = new ConcurrentHashMap<>();

    public FeatureFlagService() {
        for (FeatureFlag flag : FeatureFlag.values()) {
            values.put(flag, flag.getDefaultValue());
        }
    }

    public boolean isEnabled(FeatureFlag flag) {
        return values.getOrDefault(flag, flag.getDefaultValue());
    }

    public void set(FeatureFlag flag, boolean enabled) {
        values.put(flag, enabled);
    }

    /** Guard for gated endpoints: behaves as if the endpoint does not exist while the flag is off. */
    public void require(FeatureFlag flag) {
        if (!isEnabled(flag)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

package com.rudderstack.android.integration.branch;

import com.rudderstack.android.sdk.core.RudderLogger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.branch.referral.util.ProductCategory;

public class Utils {
    @Nullable
    static String getBranchKey(Object config) {
        if (config instanceof Map) {
            Map<String, Object> configMap = (Map<String, Object>) config;
            return getString(configMap.get("branchKey"));
        }
        return null;
    }
    static String getString(Object value) {
        return value == null ? null : value.toString();
    }

    static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    static String truncateUserIdIfExceedsLimit(String userId, int limit) {
        if (userId.length() > limit) {
            userId = userId.substring(0, limit);
        }
        return userId;
    }

    @Nullable
    static ArrayList<Object> getProducts(Object products) {
        try {
            if (products == null) {
                return null;
            }
            if (products instanceof List) {
                return new ArrayList<>((Collection<?>) products);
            }
            if (products instanceof Map) {
                Map<String, Object> propertiesMap = (Map<String, Object>) products;
                if (propertiesMap.containsKey("products") && propertiesMap.get("products") != null) {
                    return new ArrayList<>((Collection<?>) propertiesMap.get("products"));
                }
            }
            RudderLogger.logError("RudderBranchIntegration: Invalid products value");
            return null;
        } catch (Exception e) {
            RudderLogger.logError("RudderBranchIntegration: Invalid products value");
            return null;
        }
    }

    @Nullable
    public static ProductCategory getProductCategory(Object category) {
        if (category == null) {
            return null;
        }
        ProductCategory categoryValue = null;
        if (category instanceof String) {
            categoryValue = ProductCategory.getValue((String) category);
        }
        if (categoryValue == null) {
            RudderLogger.logError("RudderBranchIntegration: Invalid product category");
            return null;
        }
        return categoryValue;
    }
}

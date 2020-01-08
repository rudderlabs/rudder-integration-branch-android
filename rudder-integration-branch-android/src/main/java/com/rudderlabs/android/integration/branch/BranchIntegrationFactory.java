package com.rudderlabs.android.integration.branch;

import android.app.Application;

import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.rudderlabs.android.sdk.core.MessageType;
import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderConfig;
import com.rudderlabs.android.sdk.core.RudderIntegration;
import com.rudderlabs.android.sdk.core.RudderLogger;
import com.rudderlabs.android.sdk.core.RudderMessage;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchContentUrlBuilder;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.ProductCategory;

public class BranchIntegrationFactory extends RudderIntegration<Branch> {
    static final String BRANCH_KEY = "Branch Metrics";
    static final String BRANCH_DISPLAY_NAME = "Branch Metrics";
    private Branch branchInstance;
    private Application applicationContext;
    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig config) {
            return new BranchIntegrationFactory(settings, client, config);
        }

        @Override
        public String key() {
            return BRANCH_KEY;
        }
    };

    private Map<String, String> eventMap = new HashMap<>();

    private BranchIntegrationFactory(Object config, RudderClient client, RudderConfig rudderConfig) {
        applicationContext = client.getApplication();

        Map<String, Object> destinationConfig = (Map<String, Object>) config;

        // initiate branch SDK
        String branchKey = (String) destinationConfig.get("branchKey");
        if (branchKey != null && !branchKey.isEmpty() && client.getApplication() != null) {
            if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG) {
                Branch.enableDebugMode();
            }
            branchInstance = Branch.getAutoInstance(client.getApplication().getApplicationContext(), branchKey);
        }
    }

    @Override
    public void reset() {
        Branch.getInstance().logout();
    }

    @Override
    public void dump(RudderMessage element) {
        if (element == null) return;

        String eventType = element.getType();
        if (eventType != null) {
            if (eventType.equalsIgnoreCase(MessageType.IDENTIFY)) {
                String userId = element.getUserId();
                if (userId != null) {
                    // branch supports userId to be max 127 characters
                    if (userId.length() > 127) userId = userId.substring(0, 127);

                    branchInstance.setIdentity(userId);
                }
            } else if (eventType.equalsIgnoreCase(MessageType.TRACK)) {
                String eventName = element.getEventName();
                if (eventName != null) {
                    Map<String, Object> property = element.getProperties();
                    Map<String, Object> userProperty = element.getUserProperties();
                    switch (eventName) {
                        case ECommerceEvents.PRODUCT_ADDED:
                            ContentMetadata pa_cmd = this.getSingleProductMetaData(property);
                            if (property != null && property.containsKey("cart_id")) {
                                pa_cmd.addCustomMetadata("cart_id", (String) property.get("cart_id"));
                            }
                            this.sendTrackToBranch(BRANCH_STANDARD_EVENT.ADD_TO_CART, pa_cmd);
                            break;
                        case ECommerceEvents.PRODUCT_ADDED_TO_WISH_LIST:
                            ContentMetadata pawl_cmd = this.getSingleProductMetaData(property);
                            if (property != null) {
                                if (property.containsKey("wishlist_id")) {
                                    pawl_cmd.addCustomMetadata("wishlist_id", (String) property.get("wishlist_id"));
                                }
                                if (property.containsKey("wishlist_name")) {
                                    pawl_cmd.addCustomMetadata("wishlist_name", (String) property.get("wishlist_name"));
                                }
                            }
                            this.sendTrackToBranch(BRANCH_STANDARD_EVENT.ADD_TO_WISHLIST, pawl_cmd);
                            break;
                        case ECommerceEvents.CART_VIEWED:
                            
                            break;
                        default:

                    }
                }
            } else if (eventType.equalsIgnoreCase(MessageType.SCREEN)) {

            }
        }
    }

    private void sendTrackToBranch(BRANCH_STANDARD_EVENT bse, ContentMetadata cmd) {
        BranchUniversalObject buo = new BranchUniversalObject().setContentMetadata(cmd);
        BranchEvent be = new BranchEvent(bse).addContentItems(buo);
        be.logEvent(applicationContext);
    }

    private ContentMetadata getSingleProductMetaData(Map<String, Object> property) {
        ContentMetadata cmd = new ContentMetadata();
        if (property != null) {
            if (property.containsKey("price") && property.containsKey("currency")) {
                cmd.setPrice((Double) property.get("price"), CurrencyType.getValue((String) property.get("currency")));
            }
            if (property.containsKey("brand")) {
                cmd.setProductBrand((String) property.get("brand"));
            }
            if (property.containsKey("name")) {
                cmd.setProductName((String) property.get("name"));
            }
            if (property.containsKey("category")) {
                cmd.setProductCategory((ProductCategory) property.get("category"));
            }
            if (property.containsKey("sku")) {
                cmd.setSku((String) property.get("sku"));
            }
            if (property.containsKey("quantity")) {
                cmd.setQuantity((Double) property.get("quantity"));
            }
            if (property.containsKey("variant")) {
                cmd.setProductVariant((String) property.get("variant"));
            }
        }
        return cmd;
    }

    @Override
    public Branch getUnderlyingInstance() {
        return branchInstance;
    }
}

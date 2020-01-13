package com.rudderlabs.android.integration.branch;

import android.app.Application;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderConfig;
import com.rudderlabs.android.sdk.core.RudderIntegration;
import com.rudderlabs.android.sdk.core.RudderLogger;
import com.rudderlabs.android.sdk.core.RudderMessage;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.ProductCategory;

public class BranchIntegrationFactory extends RudderIntegration<Branch> {
    private static final String BRANCH_KEY = "Branch Metrics";

    private Branch branchInstance;
    private Application applicationContext;

    private List<String> predefinedKeysList;

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

        String[] predefinedKeys = {
                "cart_id", "wishlist_id", "wishlist_name", "products", "cart_id", "affiliation",
                "currency", "coupon", "revenue", "shipping", "tax", "order_id", "price", "brand",
                "name", "category", "sku", "quantity", "variant", "product_id", "rating"
        };
        predefinedKeysList = Arrays.asList(predefinedKeys);

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
            switch (eventType) {
                case "identify":
                    String userId = element.getUserId();
                    if (userId != null) {
                        // branch supports userId to be max 127 characters
                        if (userId.length() > 127) userId = userId.substring(0, 127);
                        branchInstance.setIdentity(userId);
                    }
                    break;
                case "track":
                    String eventName = element.getEventName();
                    if (eventName != null) {
                        Map<String, Object> property = element.getProperties();
                        Map<String, Object> userProperty = element.getUserProperties();
                        if (property != null) {
                            switch (eventName) {
                                case ECommerceEvents.PRODUCT_ADDED:
                                    ContentMetadata pa_cmd = this.getSingleProductMetaData(property);
                                    BranchEvent pa_be = new BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_CART);
                                    pa_be.addContentItems(new BranchUniversalObject().setContentMetadata(pa_cmd));
                                    this.logEventToBranch(pa_be, property);
                                    break;
                                case ECommerceEvents.PRODUCT_ADDED_TO_WISH_LIST:
                                    BranchEvent pawl_be = new BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_WISHLIST);
                                    this.logEventToBranch(pawl_be, property);
                                    break;
                                case ECommerceEvents.CART_VIEWED:
                                    BranchEvent cv_be = new BranchEvent(BRANCH_STANDARD_EVENT.VIEW_CART);
                                    this.appendOrderProperty(cv_be, property);
                                    this.logEventToBranch(cv_be, property);
                                    break;
                                case ECommerceEvents.CHECKOUT_STARTED:
                                    BranchEvent cs_be = new BranchEvent(BRANCH_STANDARD_EVENT.INITIATE_PURCHASE);
                                    this.appendOrderProperty(cs_be, property);
                                    this.logEventToBranch(cs_be, property);
                                    break;
                                case ECommerceEvents.PAYMENT_INFO_ENTERED:
                                    BranchEvent pie_be = new BranchEvent(BRANCH_STANDARD_EVENT.ADD_PAYMENT_INFO);
                                    this.appendOrderProperty(pie_be, property);
                                    this.logEventToBranch(pie_be, property);
                                    break;
                                case ECommerceEvents.ORDER_COMPLETED:
                                    BranchEvent oc_be = new BranchEvent(BRANCH_STANDARD_EVENT.PURCHASE);
                                    this.appendOrderProperty(oc_be, property);
                                    this.logEventToBranch(oc_be, property);
                                    break;
                                case "Spend Credits":
                                    BranchEvent sc_be = new BranchEvent(BRANCH_STANDARD_EVENT.SPEND_CREDITS);
                                    this.appendOrderProperty(sc_be, property);
                                    this.logEventToBranch(sc_be, property);
                                    break;
                                case ECommerceEvents.PRODUCTS_SEARCHED:
                                    BranchEvent ps_be = new BranchEvent(BRANCH_STANDARD_EVENT.SEARCH);
                                    if (property.containsKey("query")) {
                                        ps_be.addContentItems(new BranchUniversalObject().addKeyWord((String) property.get("query")));
                                    }
                                    this.logEventToBranch(ps_be, property);
                                    break;
                                case ECommerceEvents.PRODUCT_VIEWED:
                                    BranchEvent pv_be = new BranchEvent(BRANCH_STANDARD_EVENT.VIEW_ITEM);
                                    pv_be.addContentItems(new BranchUniversalObject()
                                            .setContentMetadata(this.getSingleProductMetaData(property))
                                    );
                                    this.logEventToBranch(pv_be, property);
                                    break;
                                case ECommerceEvents.PRODUCT_LIST_VIEWED:
                                    BranchEvent plv_be = new BranchEvent(BRANCH_STANDARD_EVENT.VIEW_ITEMS);
                                    this.appendOrderProperty(plv_be, property);
                                    this.logEventToBranch(plv_be, property);
                                    break;
                                case ECommerceEvents.PRODUCT_REVIEWED:
                                    BranchEvent prv_be = new BranchEvent(BRANCH_STANDARD_EVENT.RATE);
                                    prv_be.addContentItems(new BranchUniversalObject().setContentMetadata(this.getSingleProductMetaData(property)));
                                    this.logEventToBranch(prv_be, property);
                                    break;
                                case ECommerceEvents.PRODUCT_SHARED:
                                    ContentMetadata prs_cmd = this.getSingleProductMetaData(property);
                                    BranchEvent prs_be = new BranchEvent(BRANCH_STANDARD_EVENT.SHARE);
                                    prs_be.addContentItems(new BranchUniversalObject().setContentMetadata(prs_cmd));
                                    this.logEventToBranch(prs_be, property);
                                    break;
                                case "Complete Registration":
                                    BranchEvent cr_be = new BranchEvent(BRANCH_STANDARD_EVENT.COMPLETE_REGISTRATION);
                                    this.logEventToBranch(cr_be, property);
                                    break;
                                case "Complete Tutorial":
                                    BranchEvent ct_be = new BranchEvent(BRANCH_STANDARD_EVENT.COMPLETE_TUTORIAL);
                                    this.logEventToBranch(ct_be, property);
                                    break;
                                case "Achieve Level":
                                    BranchEvent al_be = new BranchEvent(BRANCH_STANDARD_EVENT.ACHIEVE_LEVEL);
                                    this.logEventToBranch(al_be, property);
                                    break;
                                case "Unlock Achievement":
                                    BranchEvent ua_be = new BranchEvent(BRANCH_STANDARD_EVENT.UNLOCK_ACHIEVEMENT);
                                    this.logEventToBranch(ua_be, property);
                                    break;
                                default:
                                    // generic track event. send custom event
                                    BranchEvent ge_be = new BranchEvent(eventName);
                                    this.logEventToBranch(ge_be, property);
                                    break;
                            }
                        }
                    }
                    break;
                case "screen":
                    // nothing to do as of now
                    break;
                default:
                    break;
            }
        }
    }

    private void logEventToBranch(BranchEvent be, Map<String, Object> property) {
        Gson gson = new Gson();
        Set<String> keySet = property.keySet();
        for (String key : keySet) {
            if (!predefinedKeysList.contains(key)) {
                Object value = property.get(key);
                if (value instanceof String) {
                    be.addCustomDataProperty(key, (String) value);
                } else if (value instanceof Integer) {
                    be.addCustomDataProperty(key, Integer.toString((Integer) value));
                } else if (value instanceof Double) {
                    be.addCustomDataProperty(key, Double.toString((Double) value));
                } else if (value instanceof Float) {
                    be.addCustomDataProperty(key, Float.toString((Float) value));
                } else if (value instanceof Boolean) {
                    be.addCustomDataProperty(key, Boolean.toString((Boolean) value));
                } else {
                    be.addCustomDataProperty(key, gson.toJson(value));
                }
            }
        }
        be.logEvent(applicationContext);
    }

    private void appendOrderProperty(BranchEvent be, Map<String, Object> property) {
        ArrayList<BranchUniversalObject> buos = new ArrayList<>();
        if (property.containsKey("products")) {
            ArrayList<Map<String, Object>> products = (ArrayList<Map<String, Object>>) property.get("products");
            if (products != null) {
                for (Map<String, Object> product : products) {
                    buos.add(new BranchUniversalObject().setContentMetadata(this.getSingleProductMetaData(product)));
                }
            }
        }
        if (!buos.isEmpty()) {
            be.addContentItems(buos);
        }
        if (property.containsKey("affiliation")) {
            be.setAffiliation((String) property.get("affiliation"));
        }
        if (property.containsKey("currency")) {
            be.setCurrency(CurrencyType.getValue((String) property.get("currency")));
        }
        if (property.containsKey("coupon")) {
            be.setCoupon((String) property.get("coupon"));
        }
        if (property.containsKey("revenue")) {
            be.setRevenue((Double) property.get("revenue"));
        }
        if (property.containsKey("shipping")) {
            be.setShipping((Double) property.get("shipping"));
        }
        if (property.containsKey("tax")) {
            be.setTax((Double) property.get("tax"));
        }
        if (property.containsKey("order_id")) {
            be.setTransactionID((String) property.get("order_id"));
        }
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
            // give sku higher priority. if sku is not present take productid as sku
            if (property.containsKey("sku")) {
                cmd.setSku((String) property.get("sku"));
            } else if (property.containsKey("product_id")) {
                cmd.setSku((String) property.get("product_id"));
            }
            if (property.containsKey("quantity")) {
                cmd.setQuantity((Double) property.get("quantity"));
            }
            if (property.containsKey("variant")) {
                cmd.setProductVariant((String) property.get("variant"));
            }
            if (property.containsKey("rating")) {
                cmd.setRating((Double) property.get("rating"), null, null, null);
            }
        }
        return cmd;
    }

    @Override
    public Branch getUnderlyingInstance() {
        return branchInstance;
    }
}

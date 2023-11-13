package com.rudderstack.android.integration.branch;

import android.app.Application;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.CurrencyType;

public class BranchIntegrationFactory extends RudderIntegration<Branch> {
    private static final String BRANCH_KEY = "Branch Metrics";

    private Branch branchInstance;
    private final Application applicationContext;
    private final List<String> predefinedKeysList = new ArrayList<>(Arrays.asList(
            "cart_id", "wishlist_id", "wishlist_name", "products", "cart_id", "affiliation",
            "currency", "coupon", "revenue", "shipping", "tax", "order_id", "price", "brand",
            "name", "category", "sku", "quantity", "variant", "product_id", "rating", "query"
    ));

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig config) {
            return new BranchIntegrationFactory(settings, config);
        }

        @Override
        public String key() {
            return BRANCH_KEY;
        }
    };

    @VisibleForTesting
    BranchIntegrationFactory(Branch branchInstance, Application applicationContext) {
        this.branchInstance = branchInstance;
        this.applicationContext = applicationContext;
    }

    private BranchIntegrationFactory(Object config, RudderConfig rudderConfig) {
        applicationContext = RudderClient.getApplication();
        String branchKey = Utils.getBranchKey(config);
        if (Utils.isEmpty(branchKey)) {
            RudderLogger.logError("RudderBranchIntegration: Branch Key is empty. Aborting initialization.");
            return;
        }

        if (RudderClient.getApplication() != null) {
            if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG) {
                Branch.enableLogging();
            }
            branchInstance = Branch.getAutoInstance(RudderClient.getApplication(), branchKey);
            RudderLogger.logDebug("RudderBranchIntegration: Branch SDK initialized");
        }
    }

    @Override
    public void dump(RudderMessage element) {
        if (element == null) return;
        String eventType = element.getType();
        if (eventType != null) {
            switch (eventType) {
                case "identify":
                    String userId = element.getUserId();
                    if (Utils.isEmpty(userId)) {
                        RudderLogger.logDebug("RudderBranchIntegration: User Id is empty. Aborting identify event.");
                        return;
                    }
                    // branch supports userId to be max 127 characters, refer here: https://help.branch.io/developers-hub/docs/android-advanced-features#track-users
                    userId = Utils.truncateUserIdIfExceedsLimit(userId, 127);
                    branchInstance.setIdentity(userId);
                    RudderLogger.logVerbose("RudderBranchIntegration: Branch Identity set for userId: " + userId);
                    break;
                case "track":
                    String eventName = element.getEventName();
                    if (eventName != null) {
                        Map<String, Object> property = element.getProperties();
                        switch (eventName) {
                            // Commerce Events
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
                            // Not supported by branch in v5.7.2
//                            case "Spend Credits":
//                                BranchEvent sc_be = new BranchEvent(BRANCH_STANDARD_EVENT.SPEND_CREDITS);
//                                this.appendOrderProperty(sc_be, property);
//                                this.logEventToBranch(sc_be, property);
//                                break;
                            case ECommerceEvents.PROMOTION_VIEWED:
                                BranchEvent prov_be = new BranchEvent(BRANCH_STANDARD_EVENT.VIEW_AD);
                                this.logEventToBranch(prov_be, property);
                                break;
                            case ECommerceEvents.PROMOTION_CLICKED:
                                BranchEvent pc_be = new BranchEvent(BRANCH_STANDARD_EVENT.CLICK_AD);
                                this.logEventToBranch(pc_be, property);
                                break;
                            case "Reserve":
                                BranchEvent r_be = new BranchEvent(BRANCH_STANDARD_EVENT.RESERVE);
                                this.logEventToBranch(r_be, property);
                                break;
                            // Content Events
                            case ECommerceEvents.PRODUCTS_SEARCHED:
                                BranchEvent ps_be = new BranchEvent(BRANCH_STANDARD_EVENT.SEARCH);
                                if (property != null && property.containsKey("query")) {
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
                            case "Initiate Stream":
                                BranchEvent is_be = new BranchEvent(BRANCH_STANDARD_EVENT.INITIATE_STREAM);
                                this.logEventToBranch(is_be, property);
                                break;
                            case "Complete Stream":
                                BranchEvent cs_be1 = new BranchEvent(BRANCH_STANDARD_EVENT.COMPLETE_STREAM);
                                this.logEventToBranch(cs_be1, property);
                                break;
                            // Lifecycle Events
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
                            case "Invite":
                                BranchEvent i_be = new BranchEvent(BRANCH_STANDARD_EVENT.INVITE);
                                this.logEventToBranch(i_be, property);
                                break;
                            case "Login":
                                BranchEvent l_be = new BranchEvent(BRANCH_STANDARD_EVENT.LOGIN);
                                this.logEventToBranch(l_be, property);
                                break;
                            case "Start Trial":
                                BranchEvent st_be = new BranchEvent(BRANCH_STANDARD_EVENT.START_TRIAL);
                                this.logEventToBranch(st_be, property);
                                break;
                            case "Subscribe":
                                BranchEvent s_be = new BranchEvent(BRANCH_STANDARD_EVENT.SUBSCRIBE);
                                this.logEventToBranch(s_be, property);
                                break;
                            default:
                                // handle custom events
                                BranchEvent ge_be = new BranchEvent(eventName);
                                this.logEventToBranch(ge_be, property);
                                break;
                        }
                    }
                    break;
                default:
                    RudderLogger.logDebug("RudderBranchIntegration: This " + eventType + " event type is not supported.");
                    break;
            }
        }
    }

    private void logEventToBranch(BranchEvent be, Map<String, Object> property) {
        if (property != null) {
            // add custom data properties
            Gson gson = new Gson();
            for (String key : property.keySet()) {
                if (!predefinedKeysList.contains(key)) {
                    Object value = property.get(key);
                    if (value instanceof String) {
                        be.addCustomDataProperty(key, Utils.getString(value));
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
        }
        be.logEvent(applicationContext);
    }

    private void appendOrderProperty(BranchEvent be, Map<String, Object> property) {
        if (property != null) {
            ArrayList<BranchUniversalObject> buos = new ArrayList<>();
            if (property.containsKey("products")) {
                ArrayList<Object> products = Utils.getProducts(property.get("products"));
                if (products != null) {
                    for (Object product : products) {
                        if (product instanceof Map) {
                            buos.add(new BranchUniversalObject().setContentMetadata(this.getSingleProductMetaData((Map<String, Object>) product)));
                        }
                    }
                }
            }
            if (!buos.isEmpty()) {
                be.addContentItems(buos);
            }
            if (property.containsKey("affiliation")) {
                be.setAffiliation(Utils.getString(property.get("affiliation")));
            }
            if (property.containsKey("currency")) {
                be.setCurrency(CurrencyType.getValue(Utils.getString(property.get("currency"))));
            }
            if (property.containsKey("coupon")) {
                be.setCoupon(Utils.getString(property.get("coupon")));
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
                be.setTransactionID(Utils.getString(property.get("order_id")));
            }
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
                cmd.setProductCategory(Utils.getProductCategory(property.get("category")));
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
    public void reset() {
        Branch.getInstance().logout();
    }

    @Override
    public Branch getUnderlyingInstance() {
        return branchInstance;
    }
}

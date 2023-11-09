package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            // This is required to register the device as a test device in the Branch dashboard
            val advertisingId = AdvertisingIdClient.getAdvertisingIdInfo(this).id
            println("AdvertisingId: $advertisingId")
        }.start()

        findViewById<Button>(R.id.identifyWithoutTraits).setOnClickListener { identifyWithoutTraits() }
        findViewById<Button>(R.id.identifyWithTraits).setOnClickListener { identifyWithTraits() }

        // Commerce Events
        findViewById<Button>(R.id.productAdded).setOnClickListener { productAdded() }
        findViewById<Button>(R.id.productAddedToWishList).setOnClickListener { productAddedToWishList() }
        findViewById<Button>(R.id.cartViewed).setOnClickListener { cartViewed() }
        findViewById<Button>(R.id.checkoutStarted).setOnClickListener { checkoutStarted() }
        findViewById<Button>(R.id.paymentInfoEntered).setOnClickListener { paymentInfoEntered() }
        findViewById<Button>(R.id.orderCompleted).setOnClickListener { orderCompleted() }
        findViewById<Button>(R.id.promotionViewed).setOnClickListener { promotionViewed() }
        findViewById<Button>(R.id.promotionClicked).setOnClickListener { promotionClicked() }
        findViewById<Button>(R.id.reserve).setOnClickListener { reserve() }

        // Content Events
        findViewById<Button>(R.id.productsSearched).setOnClickListener { productsSearched() }
        findViewById<Button>(R.id.productViewed).setOnClickListener { productViewed() }
        findViewById<Button>(R.id.productListViewed).setOnClickListener { productListViewed() }
        findViewById<Button>(R.id.productReviewed).setOnClickListener { productReviewed() }
        findViewById<Button>(R.id.productShared).setOnClickListener { productShared() }
        findViewById<Button>(R.id.initiateStream).setOnClickListener { initiateStream() }
        findViewById<Button>(R.id.completeStream).setOnClickListener { completeStream() }

        // Lifecycle Events
        findViewById<Button>(R.id.completeRegistration).setOnClickListener { completeRegistration() }
        findViewById<Button>(R.id.completeTutorial).setOnClickListener { completeTutorial() }
        findViewById<Button>(R.id.achieveLevel).setOnClickListener { achieveLevel() }
        findViewById<Button>(R.id.unlockAchievement).setOnClickListener { unlockAchievement() }
        findViewById<Button>(R.id.invite).setOnClickListener { invite() }
        findViewById<Button>(R.id.login).setOnClickListener { login() }
        findViewById<Button>(R.id.startTrial).setOnClickListener { startTrial() }
        findViewById<Button>(R.id.subscribe).setOnClickListener { subscribe() }

        // Custom Events
        findViewById<Button>(R.id.customTrackWithoutProperties).setOnClickListener { customTrackWithoutProperties() }
        findViewById<Button>(R.id.customTrackWithProperties).setOnClickListener { customTrackWithProperties() }

        // Reset
        findViewById<Button>(R.id.reset).setOnClickListener { reset() }
    }

    private fun identifyWithoutTraits() {
        RudderClient.getInstance()?.identify("some_user_id without traits")
    }

    private fun identifyWithTraits() {
        RudderClient.getInstance()?.identify(
            "some_user_id with traits",
            RudderTraits()
                .putEmail("test@example.com")
                .put("Key-1", "value-1"),
            null
        )
    }

    // Commerce Events

    private fun productAdded() {
        RudderClient.getInstance()?.track("Product Added", getSingleProductProperties())
    }

    private fun productAddedToWishList() {
        RudderClient.getInstance()?.track("Product Added to Wishlist", getCustomProperties())
    }

    private fun cartViewed() {
        RudderClient.getInstance()?.track("Cart Viewed", getMultipleProductProperties())
    }

    private fun checkoutStarted() {
        RudderClient.getInstance()?.track("Checkout Started", getMultipleProductProperties())
    }

    private fun paymentInfoEntered() {
        RudderClient.getInstance()?.track("Payment Info Entered", getMultipleProductProperties())
    }

    private fun orderCompleted() {
        RudderClient.getInstance()?.track("Order Completed", getMultipleProductProperties())
    }

    private fun promotionViewed() {
        RudderClient.getInstance()?.track("Promotion Viewed", getCustomProperties())
    }

    private fun promotionClicked() {
        RudderClient.getInstance()?.track("Promotion Clicked", getCustomProperties())
    }

    private fun reserve() {
        RudderClient.getInstance()?.track("Reserve", getCustomProperties())
    }

    // Content Events

    private fun productsSearched() {
        RudderClient.getInstance()?.track("Products Searched", getEComPropertiesWithoutProducts())
    }

    private fun productViewed() {
        RudderClient.getInstance()?.track("Product Viewed", getSingleProductProperties())
    }

    private fun productListViewed() {
        RudderClient.getInstance()?.track("Product List Viewed", getMultipleProductProperties())
    }

    private fun productReviewed() {
        RudderClient.getInstance()?.track("Product Reviewed", getSingleProductProperties())
    }

    private fun productShared() {
        RudderClient.getInstance()?.track("Product Shared", getSingleProductProperties())
    }

    private fun initiateStream() {
        RudderClient.getInstance()?.track("Initiate Stream", getCustomProperties())
    }

    private fun completeStream() {
        RudderClient.getInstance()?.track("Complete Stream", getCustomProperties())
    }

    // Lifecycle Events

    private fun completeRegistration() {
        RudderClient.getInstance()?.track("Complete Registration", getCustomProperties())
    }

    private fun completeTutorial() {
        RudderClient.getInstance()?.track("Complete Tutorial", getCustomProperties())
    }

    private fun achieveLevel() {
        RudderClient.getInstance()?.track("Achieve Level", getCustomProperties())
    }

    private fun unlockAchievement() {
        RudderClient.getInstance()?.track("Unlock Achievement", getCustomProperties())
    }

    private fun invite() {
        RudderClient.getInstance()?.track("Invite", getCustomProperties())
    }

    private fun login() {
        RudderClient.getInstance()?.track("Login", getCustomProperties())
    }

    private fun startTrial() {
        RudderClient.getInstance()?.track("Start Trial", getCustomProperties())
    }
    private fun subscribe() {
        RudderClient.getInstance()?.track("Subscribe", getCustomProperties())
    }

    // Custom Events

    private fun customTrackWithoutProperties() {
        RudderClient.getInstance()?.track("Custom Track Without Properties")
    }

    private fun customTrackWithProperties() {
        RudderClient.getInstance()
            ?.track("Custom Track With Properties", appendCustomProperties(RudderProperty()))
    }

    private fun reset() {
        RudderClient.getInstance()?.reset(true)
    }

    private fun getMultipleProductProperties(): RudderProperty {
        val product1 = mapOf(
            "price" to 154.79,
            "currency" to "USD",
            "brand" to "some_brand_1",
            "name" to "some_name_1",
            "category" to "Animals & Pet Supplies", // Make sure this is a valid value, refer to the Branch "ProductCategory" class.
            "sku" to "some_sku_1",
            "product_id" to "some_product_id_1",
            "quantity" to 10.0,
            "variant" to "some_variant_1",
            "rating" to 4.5
        )
        val product2 = mapOf(
            "price" to 254.79,
            "currency" to "INR",
            "brand" to "some_brand_2",
            "name" to "some_name_2",
            "category" to "some_category_2",
            "sku" to "some_sku_2",
            "product_id" to "some_product_id_2",
            "quantity" to 20.0,
            "variant" to "some_variant_2",
            "rating" to 3.5
        )

        val products = listOf(product1, product2)

        val property = RudderProperty()
            .putValue("products", products)

        appendStandardProperties(property)
        return appendCustomProperties(property)
    }

    private fun getSingleProductProperties(): RudderProperty {
        val property = RudderProperty()
            .putValue("price", 154.79)
            .putValue("currency", "USD")
            .putValue("brand", "some_brand")
            .putValue("name", "some_name")
            .putValue("category", "some_category")
            .putValue("sku", "some_sku")
            .putValue("product_id", "some_product_id")
            .putValue("quantity", 10.0)
            .putValue("variant", "some_variant")
            .putValue("rating", 4.5)
        return appendCustomProperties(property)
    }

    private fun getEComPropertiesWithoutProducts(): RudderProperty {
        val property = RudderProperty()
        appendStandardProperties(property)
        return appendCustomProperties(property)
    }

    private fun appendStandardProperties(property: RudderProperty) =
        property
            .putValue("affiliation", "some_affiliation")
            .putValue("currency", "USD")
            .putValue("coupon", "some_coupon")
            .putValue("revenue", 754.79)
            .putValue("shipping", 100.0)
            .putValue("tax", 5.0)
            .putValue("order_id", "some_order_id")
            .putValue("query", "some_query")


    private fun getCustomProperties() = appendCustomProperties(RudderProperty())
    private fun appendCustomProperties(property: RudderProperty) =
        property
            .putValue("key-1", "value-1")
            .putValue("key-2", 123)
}

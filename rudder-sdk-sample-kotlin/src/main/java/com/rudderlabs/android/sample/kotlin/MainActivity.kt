package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sdk.core.RudderMessageBuilder
import com.rudderlabs.android.sdk.core.TrackPropertyBuilder
import com.rudderlabs.android.sdk.core.ecomm.ECommerceCart
import com.rudderlabs.android.sdk.core.ecomm.ECommerceProduct
import com.rudderlabs.android.sdk.core.ecomm.ECommerceWishList
import com.rudderlabs.android.sdk.core.ecomm.events.CartViewedEvent
import com.rudderlabs.android.sdk.core.ecomm.events.ProductAddedToCartEvent
import com.rudderlabs.android.sdk.core.ecomm.events.ProductAddedToWishListEvent

class MainActivity : AppCompatActivity() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ECommerce Product
        val productA = ECommerceProduct.Builder()
            .withProductId("some_product_id_a")
            .withSku("some_product_sku_a")
            .withCurrency("USD")
            .withPrice(2.99f)
            .withName("Some Product Name A")
            .withQuantity(1f)
            .build()

        val productB = ECommerceProduct.Builder()
            .withProductId("some_product_id_b")
            .withSku("some_product_sku_b")
            .withCurrency("USD")
            .withPrice(3.99f)
            .withName("Some Product Name B")
            .withQuantity(1f)
            .build()

        val productC = ECommerceProduct.Builder()
            .withProductId("some_product_id_c")
            .withSku("some_product_sku_c")
            .withCurrency("USD")
            .withPrice(4.99f)
            .withName("Some Product Name C")
            .withQuantity(1f)
            .build()

        // ECommerce WishList
        val wishList = ECommerceWishList.Builder()
            .withWishListId("some_wish_list_id")
            .withWishListName("Some Wish List Name")
            .build()

        // ECommerce Cart
        val cart = ECommerceCart.Builder()
            .withCartId("some_cart_id")
            .withProduct(productA)
            .withProduct(productB)
            .withProduct(productC)
            .build()

        val productAddedToCartEvent = ProductAddedToCartEvent()
            .withCartId("some_cart_id")
            .withProduct(productA)
        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName(productAddedToCartEvent.event())
                .setProperty(productAddedToCartEvent.build())
                .build()
        )

        val productAddedToWishListEvent = ProductAddedToWishListEvent()
            .withWishList(wishList)
            .withProduct(productA)
        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setProperty(productAddedToWishListEvent.build())
                .setEventName(productAddedToWishListEvent.event())
        )

        val cartViewedEvent = CartViewedEvent().withCart(cart)
        MainApplication.rudderClient.track(cartViewedEvent.event(), cartViewedEvent.build())
    }
}

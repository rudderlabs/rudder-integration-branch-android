package com.rudderstack.android.integration.branch

import com.google.gson.GsonBuilder
import com.rudderstack.android.sdk.core.RudderMessage
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.CurrencyType
import io.branch.referral.util.ProductCategory
import io.mockk.CapturingSlot
import org.junit.Assert
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList

class TestUtils {
    private val gson = GsonBuilder().create()

    fun getMessage(fileName: String): RudderMessage {
        val inputJson = getJsonFromPath(fileName)
        return parseJson(inputJson!!)
    }

    fun getJsonFromPath(path: String?): String? {
        val inputStream: InputStream =
            this.javaClass.classLoader?.getResourceAsStream(path) ?: return null
        val reader = BufferedReader(InputStreamReader(inputStream))
        val builder = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return builder.toString()
    }

    private fun parseJson(json: String): RudderMessage {
        return gson.fromJson(json, RudderMessage::class.java)
    }

    fun verifySingleProductMetadata(contentMetadata: ContentMetadata) {
        Assert.assertEquals(154.79, contentMetadata.price, 0.0)
        Assert.assertEquals(CurrencyType.USD, contentMetadata.currencyType)
        Assert.assertEquals("some_brand", contentMetadata.productBrand)
        Assert.assertEquals("some_name", contentMetadata.productName)
        Assert.assertEquals(
            ProductCategory.ANIMALS_AND_PET_SUPPLIES,
            contentMetadata.productCategory
        )
        Assert.assertEquals("some_sku", contentMetadata.sku)
        Assert.assertEquals(10.0, contentMetadata.quantity, 0.0)
        Assert.assertEquals("some_variant", contentMetadata.productVariant)
        Assert.assertEquals(4.5, contentMetadata.rating, 0.0)
    }

    fun verifyStandardProperties(
        affiliationSlot: CapturingSlot<String>,
        currencySlot: CapturingSlot<CurrencyType>,
        couponSlot: CapturingSlot<String>,
        revenueSlot: CapturingSlot<Double>,
        shippingSlot: CapturingSlot<Double>,
        taxSlot: CapturingSlot<Double>,
        orderIdSlot: CapturingSlot<String>
    ) {
        Assert.assertEquals("some_affiliation", affiliationSlot.captured)
        Assert.assertEquals(CurrencyType.USD, currencySlot.captured)
        Assert.assertEquals("some_coupon", couponSlot.captured)
        Assert.assertEquals(754.79, revenueSlot.captured, 0.0)
        Assert.assertEquals(100.0, shippingSlot.captured, 0.0)
        Assert.assertEquals(5.0, taxSlot.captured, 0.0)
        Assert.assertEquals("some_order_id", orderIdSlot.captured)
    }

    fun verifyProduct1(product1: ContentMetadata) {
        Assert.assertEquals(154.79, product1.price, 0.0)
        Assert.assertEquals(CurrencyType.USD, product1.currencyType)
        Assert.assertEquals("some_brand_1", product1.productBrand)
        Assert.assertEquals("some_name_1", product1.productName)
        Assert.assertEquals(ProductCategory.ANIMALS_AND_PET_SUPPLIES, product1.productCategory)
        Assert.assertEquals("some_sku_1", product1.sku)
        Assert.assertEquals(10.0, product1.quantity, 0.0)
        Assert.assertEquals("some_variant_1", product1.productVariant)
        Assert.assertEquals(4.5, product1.rating, 0.0)
    }

    fun verifyProduct2(product1: ContentMetadata) {
        Assert.assertEquals(254.79, product1.price, 0.0)
        Assert.assertEquals(CurrencyType.INR, product1.currencyType)
        Assert.assertEquals("some_brand_2", product1.productBrand)
        Assert.assertEquals("some_name_2", product1.productName)
        Assert.assertEquals(ProductCategory.ANIMALS_AND_PET_SUPPLIES, product1.productCategory)
        Assert.assertEquals("some_sku_2", product1.sku)
        Assert.assertEquals(20.0, product1.quantity, 0.0)
        Assert.assertEquals("some_variant_2", product1.productVariant)
        Assert.assertEquals(3.5, product1.rating, 0.0)
    }

    fun verifyCustomProperty(
        propertyNameSlot: MutableList<String>,
        propertyValueSlot: MutableList<String>
    ) {
        Assert.assertEquals(mutableListOf("key-1", "key-2"), propertyNameSlot)
        Assert.assertEquals(mutableListOf("value-1", "123.0"), propertyValueSlot)
    }

    fun verifyQueryProperty(captureBranchUniversalObject: ArrayList<String>) {
        Assert.assertEquals("some_query", captureBranchUniversalObject[0])
    }

    fun getUserIdOfLength(length: Int): String? {
        val sb = StringBuilder()
        for (j in 0 until length - 1) {
            sb.append("a")
        }
        sb.append(".")
        return sb.toString()
    }

    fun verifyIdentify(userId: String) {
        Assert.assertEquals("some_user_id", userId)
    }

    fun verifyIdentifyWhenUserIdIsTruncated(userId: String) {
        Assert.assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            userId)
    }
}
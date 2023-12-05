package com.rudderstack.android.integration.branch

import android.app.Application
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.BRANCH_STANDARD_EVENT
import io.branch.referral.util.BranchEvent
import io.branch.referral.util.CurrencyType
import io.mockk.OfTypeMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import org.junit.Before
import org.junit.Test

class BranchIntegrationFactoryUnitTest {
    val testUtils = TestUtils()
    private var branchIntegrationFactory: BranchIntegrationFactory? = null

    private val branchInstance = mockk<Branch>()
    private val applicationContext = mockk<Application>()

    @Before
    fun setUp() {
        this.branchIntegrationFactory = BranchIntegrationFactory(branchInstance, applicationContext)
    }

    @Test
    fun testProductAdded() {
        val branchUniversalObjectSlot = slot<BranchUniversalObject>()
        val propertyNameSlot = mutableListOf<String>()
        val propertyValueSlot = mutableListOf<String>()
        val mockBranchEvent = mockk<BranchEvent>()

        mockkConstructor(BranchEvent::class)
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addContentItems(capture(branchUniversalObjectSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addCustomDataProperty(capture(propertyNameSlot), capture(propertyValueSlot)) } returns mockBranchEvent

        val message = testUtils.getMessage("productAdded.json")
        branchIntegrationFactory?.dump(message)

        val captureBranchUniversalObject = branchUniversalObjectSlot.captured
        testUtils.verifySingleProductMetadata(captureBranchUniversalObject.contentMetadata)
        testUtils.verifyCustomProperty(propertyNameSlot, propertyValueSlot)
    }

    @Test
    fun testProductAddedToWishList() {
        val propertyNameSlot = mutableListOf<String>()
        val propertyValueSlot = mutableListOf<String>()
        val mockBranchEvent = mockk<BranchEvent>()

        mockkConstructor(BranchEvent::class)
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addCustomDataProperty(capture(propertyNameSlot), capture(propertyValueSlot)) } returns mockBranchEvent

        val message = testUtils.getMessage("productAddedToWishList.json")
        branchIntegrationFactory?.dump(message)

        testUtils.verifyCustomProperty(propertyNameSlot, propertyValueSlot)
    }

    @Test
    fun testCartViewed() {
        val listOfBranchUniversalObjectSlot = slot<List<BranchUniversalObject>>()
        val propertyNameSlot = mutableListOf<String>()
        val propertyValueSlot = mutableListOf<String>()
        val mockBranchEvent = mockk<BranchEvent>()

        val affiliationSlot = slot<String>()
        val currencySlot = slot<CurrencyType>()
        val couponSlot = slot<String>()
        val revenueSlot = slot<Double>()
        val shippingSlot = slot<Double>()
        val taxSlot = slot<Double>()
        val orderIdSlot = slot<String>()

        mockkConstructor(BranchEvent::class)
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addContentItems(capture(listOfBranchUniversalObjectSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addCustomDataProperty(capture(propertyNameSlot), capture(propertyValueSlot)) } returns mockBranchEvent

        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setAffiliation(capture(affiliationSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setCurrency(capture(currencySlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setCoupon(capture(couponSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setRevenue(capture(revenueSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setShipping(capture(shippingSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setTax(capture(taxSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).setTransactionID(capture(orderIdSlot)) } returns mockBranchEvent

        val message = testUtils.getMessage("cartViewed.json")
        branchIntegrationFactory?.dump(message)

        val captureBranchUniversalObject = listOfBranchUniversalObjectSlot.captured
        testUtils.verifyStandardProperties(affiliationSlot, currencySlot, couponSlot, revenueSlot, shippingSlot, taxSlot, orderIdSlot)
        testUtils.verifyProduct1(captureBranchUniversalObject[0].getContentMetadata())
        testUtils.verifyProduct2(captureBranchUniversalObject[1].getContentMetadata())
        testUtils.verifyCustomProperty(propertyNameSlot, propertyValueSlot)
    }

    @Test
    fun testProductsSearched() {
        val branchUniversalObjectSlot = slot<BranchUniversalObject>()
        val propertyNameSlot = mutableListOf<String>()
        val propertyValueSlot = mutableListOf<String>()
        val mockBranchEvent = mockk<BranchEvent>()

        mockkConstructor(BranchEvent::class)
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addContentItems(capture(branchUniversalObjectSlot)) } returns mockBranchEvent
        every { constructedWith<BranchEvent>(OfTypeMatcher<BRANCH_STANDARD_EVENT>(BRANCH_STANDARD_EVENT::class)).addCustomDataProperty(capture(propertyNameSlot), capture(propertyValueSlot)) } returns mockBranchEvent

        val message = testUtils.getMessage("productsSearched.json")
        branchIntegrationFactory?.dump(message)

        val captureBranchUniversalObject = branchUniversalObjectSlot.captured
        testUtils.verifyQueryProperty(captureBranchUniversalObject.keywords)
        testUtils.verifyCustomProperty(propertyNameSlot, propertyValueSlot)
    }

    @Test
    fun testCustomTrackEvent() {
        val propertyNameSlot = mutableListOf<String>()
        val propertyValueSlot = mutableListOf<String>()
        val mockBranchEvent = mockk<BranchEvent>()

        mockkConstructor(BranchEvent::class)
        every { constructedWith<BranchEvent>(OfTypeMatcher<String>(String::class)).addCustomDataProperty(capture(propertyNameSlot), capture(propertyValueSlot)) } returns mockBranchEvent

        val message = testUtils.getMessage("customTrackEvent.json")
        branchIntegrationFactory?.dump(message)

        testUtils.verifyCustomProperty(propertyNameSlot, propertyValueSlot)
    }

    @Test
    fun testIdentifyEvent() {
        val userIdSlot = slot<String>()

        every { branchInstance.setIdentity(capture(userIdSlot)) } returns Unit

        val message = testUtils.getMessage("identify.json")
        branchIntegrationFactory?.dump(message)

        testUtils.verifyIdentify(userIdSlot.captured)
    }

    @Test
    fun testIdentifyEventWith128CharacterUserId() {
        val userIdSlot = slot<String>()

        every { branchInstance.setIdentity(capture(userIdSlot)) } returns Unit

        val message = testUtils.getMessage("identifyWith128CharacterUserId.json")
        branchIntegrationFactory?.dump(message)

        testUtils.verifyIdentifyWhenUserIdIsTruncated(userIdSlot.captured)
    }
}
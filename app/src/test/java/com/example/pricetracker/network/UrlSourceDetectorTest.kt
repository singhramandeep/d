package com.example.pricetracker.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlSourceDetectorTest {

    @Test
    fun `detects amazon source`() {
        val source = UrlSourceDetector.detect("https://www.amazon.in/dp/B0ABC12345")
        assertEquals(ProductSource.AMAZON, source)
    }

    @Test
    fun `detects flipkart source`() {
        val source = UrlSourceDetector.detect("https://www.flipkart.com/item/p/itm123")
        assertEquals(ProductSource.FLIPKART, source)
    }

    @Test
    fun `detects myntra source`() {
        val source = UrlSourceDetector.detect("https://www.myntra.com/shoes/nike/123456")
        assertEquals(ProductSource.MYNTRA, source)
    }

    @Test
    fun `returns unknown for unsupported host`() {
        val source = UrlSourceDetector.detect("https://example.com/product/1")
        assertNull(source)
    }
}

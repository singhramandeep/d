package com.example.pricetracker.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProductPageParserTest {

    private val parser = ProductPageParser()

    @Test
    fun `parses data from amazon html`() {
        val html = """
            <html>
              <head>
                <title>Test Product - Amazon.in</title>
                <meta property="og:title" content="Test Running Shoe"/>
                <meta property="og:image" content="https://img.example.com/p.jpg"/>
              </head>
              <body>
                <span class="a-price-whole">1,999</span>
              </body>
            </html>
        """.trimIndent()

        val parsed = parser.parse(
            source = ProductSource.AMAZON,
            url = "https://www.amazon.in/dp/B0ABC12345",
            html = html
        )

        assertEquals("Test Running Shoe", parsed.title)
        assertEquals("https://img.example.com/p.jpg", parsed.imageUrl)
        assertEquals(1999.0, parsed.price, 0.0)
    }

    @Test
    fun `parses json ld fallback`() {
        val html = """
            <html>
              <head>
                <script type="application/ld+json">
                {
                  "name": "JSON Product Name",
                  "image": "https://img.example.com/json.jpg",
                  "offers": { "price": "2499" }
                }
                </script>
              </head>
              <body></body>
            </html>
        """.trimIndent()

        val parsed = parser.parse(
            source = ProductSource.FLIPKART,
            url = "https://www.flipkart.com/item/p/itm123",
            html = html
        )

        assertEquals("JSON Product Name", parsed.title)
        assertEquals("https://img.example.com/json.jpg", parsed.imageUrl)
        assertEquals(2499.0, parsed.price, 0.0)
    }

    @Test
    fun `throws on missing required fields`() {
        val html = "<html><head></head><body>No product data</body></html>"

        try {
            parser.parse(
                source = ProductSource.MYNTRA,
                url = "https://www.myntra.com/item/1",
                html = html
            )
        } catch (error: IllegalArgumentException) {
            assertNotNull(error.message)
            return
        }
        throw AssertionError("Expected IllegalArgumentException")
    }
}

package com.kabhawi.admin

import com.kabhawi.admin.data.remote.ServerUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerUrlTest {

    @Test
    fun addsSchemeAndTrailingSlash() {
        assertEquals("http://192.168.1.10:8080/", ServerUrl.normalize("192.168.1.10:8080"))
        assertEquals("https://shop.example.com/", ServerUrl.normalize(" https://shop.example.com "))
    }

    @Test
    fun keepsPathPrefix() {
        assertEquals("https://example.com/gateway/", ServerUrl.normalize("https://example.com/gateway"))
    }

    @Test
    fun rejectsInvalidUrls() {
        assertNull(ServerUrl.normalize(""))
        assertNull(ServerUrl.normalize("   "))
        assertNull(ServerUrl.normalize("http://"))
    }
}

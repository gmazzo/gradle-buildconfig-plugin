package com.github.gmazzo.buildconfig.demos.kmp

import com.eygraber.uri.Uri
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class CommonBuildConfigTest(
    private val expectedPlatform: String,
    private val expectedMobile: Boolean = false,
    private val expectedDebug: Boolean = false,
    private val expectedUri: Uri = Uri.parse("https://api.example.com"),
    private val expectedProduct: String? = null,
) {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals(expectedPlatform, BuildConfig.PLATFORM)
        assertEquals(expectedDebug, BuildConfig.DEBUG)
        assertEquals(expectedUri, BuildConfig.ENDPOINT)
        assertEquals(expectedProduct, BuildConfig.PRODUCT_VALUE)
        assertEquals("aLazyProvidedValue", BuildConfig.PROVIDED_VALUE)
    }

    @Test
    fun testI18nProperties() {
        assertEquals("Hello", i18n_hello)
        assertEquals(expectedPlatform, i18n_kind)
    }

    @Test
    fun testSingleProperties() {
        assertEquals(expectedMobile, Single.IS_MOBILE)
    }

    @Test
    fun testTestProperties() {
        assertEquals("aTestValue", TestBuildConfig.TEST_VALUE)
        assertEquals("aLazyProvidedValue", TestBuildConfig.PROVIDED_VALUE)
    }

}

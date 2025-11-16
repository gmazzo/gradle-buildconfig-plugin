package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonBuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
    }

    @Test
    fun testI18nProperties() {
        assertEquals("Hello", i18n_hello)
        assertEquals("Greetings", i18n_greetings)
        assertEquals(expected_i18n_kind, i18n_kind)
    }

}

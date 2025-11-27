package com.github.gmazzo.buildconfig.demos.kts_android

import com.github.gmazzo.buildconfig.demos.android.BuildConfig as AndroidBuildConfig
import com.github.gmazzo.buildconfig.demos.groovy.BuildConfigBaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildConfigTest : BuildConfigBaseTest() {

    override fun extraCases() = arrayOf(
        // properties cases
        arrayOf("com.github.gmazzo.buildconfig.demos.android", AndroidBuildConfig.APPLICATION_ID),

        arrayOf("kts-android", BuildConfig.APP_NAME),
        arrayOf("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET),
        arrayOf(true, BuildConfig.FEATURE_ENABLED),
        arrayOf(listOf(1, 2, 3, 4), BuildConfig.MAGIC_NUMBERS.toList()),

        // flavor cases
        arrayOf(AndroidBuildConfig.DEBUG, BuildConfig.IS_DEBUG),
        arrayOf(AndroidBuildConfig.FLAVOR, BuildConfig.BRAND),
    )

    @Test
    fun testBuildConfigTestProperties() {
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT)
    }

}

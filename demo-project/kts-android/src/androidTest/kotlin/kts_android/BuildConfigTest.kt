package com.github.gmazzo.buildconfig.demos.kts_android

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildConfigTest {

    @Test
    fun testBuildConfigTestProperties() {
        assertEquals("aTestValue", AndroidTestBuildConfig.TEST_CONSTANT)
    }

}

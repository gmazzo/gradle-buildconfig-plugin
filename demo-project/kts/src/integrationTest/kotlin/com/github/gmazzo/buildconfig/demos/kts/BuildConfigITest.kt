package com.github.gmazzo.buildconfig.demos.kts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildConfigITest {

    @Test
    fun testBuildConfigIntegrationTestProperties() {
        assertEquals("aIntTestValue", IntegrationTestBuildConfig.INTEGRATION_TEST_CONSTANT)
    }

}

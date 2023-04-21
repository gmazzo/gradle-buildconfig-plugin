package com.github.gmazzo.buildconfig.demos.kts

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildConfigITest {

    @Test
    fun testBuildConfigIntegrationTestProperties() {
        assertEquals("aIntTestValue", IntegrationTestBuildConfig.INTEGRATION_TEST_CONSTANT)
    }

}

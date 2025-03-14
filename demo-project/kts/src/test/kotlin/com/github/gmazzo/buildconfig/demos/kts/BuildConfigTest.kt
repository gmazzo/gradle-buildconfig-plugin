package com.github.gmazzo.buildconfig.demos.kts

import com.github.gmazzo.buildconfig.demos.groovy.BuildConfigBaseTest
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildConfigTest : BuildConfigBaseTest() {

    @Test
    fun testBuildConfigTestProperties() {
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT)
    }

    @Test
    fun testBuildConfigVersionsProperties() {
        assertEquals("1.0.1", myDependencyVersion)
    }

    @Test
    fun testResourcesConfigProperties() {
        assertEquals("aConstant", BuildResources.A_CONSTANT)
        assertEquals("file1.json", BuildResources.FILE1_JSON.path)
        assertEquals("file2.json", BuildResources.FILE2_JSON.path)
        assertEquals("config/local.properties", BuildResources.CONFIG_LOCAL_PROPERTIES.path)
        assertEquals("config/prod.properties", BuildResources.CONFIG_PROD_PROPERTIES.path)
    }

    @Test
    fun testCustomXMLGeneratorProperties() {
        val props = javaClass.getResourceAsStream("/PropertiesBuildConfig.xml").use {
            Properties().apply { loadFromXML(it) }
        }

        assertEquals("AAA", props["value1"])
        assertEquals("BBB", props["value2"])
        assertEquals("CCC", props["value3"])
    }

}

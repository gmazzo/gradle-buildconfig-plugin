package com.github.gmazzo.buildconfig.demos.kts

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("kts", BuildConfig.APP_NAME)
        assertEquals("0.1.0-demo", BuildConfig.APP_VERSION)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET)
        assertEquals(null, BuildConfig.OPTIONAL)
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME)
        assertTrue(BuildConfig.FEATURE_ENABLED)
        assertArrayEquals(intArrayOf(1, 2, 3, 4), BuildConfig.MAGIC_NUMBERS)
        assertEquals(SomeData("a", 1), BuildConfig.MY_DATA)
    }

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

package com.github.gmazzo.buildconfig.demos.kts

import com.github.gmazzo.buildconfig.demos.groovy.BuildConfigBaseTest
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildConfigTest : BuildConfigBaseTest() {

    private val props by lazy {
        javaClass.getResourceAsStream("/PropertiesBuildConfig.xml").use {
            Properties().apply { loadFromXML(it) }
        }
    }

    override fun extraCases() = arrayOf(
        // test properties
        arrayOf("aTestValue", TestBuildConfig.TEST_CONSTANT),

        // version properties
        arrayOf("1.0.1", myDependencyVersion),

        // resource properties
        arrayOf("aConstant", BuildResources.A_CONSTANT),
        arrayOf("file1.json", BuildResources.FILE1_JSON.path),
        arrayOf("file2.json", BuildResources.FILE2_JSON.path),
        arrayOf("config/local.properties", BuildResources.CONFIG_LOCAL_PROPERTIES.path),
        arrayOf("config/prod.properties", BuildResources.CONFIG_PROD_PROPERTIES.path),

        // custom XML properties
        arrayOf("AAA", props["value1"]),
        arrayOf("BBB", props["value2"]),
        arrayOf("CCC", props["value3"]),
    )

}

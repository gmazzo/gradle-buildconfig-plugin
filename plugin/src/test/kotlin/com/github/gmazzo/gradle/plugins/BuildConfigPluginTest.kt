package com.github.gmazzo.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.FileOutputStream

@RunWith(Parameterized::class)
class BuildConfigPluginTest(
    private val gradleVersion: String,
    private val kotlinVersion: String?,
    private val withPackage: Boolean
) {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val projectDir by lazy { temporaryFolder.newFolder(PROJECT_NAME) }

    private val runner by lazy {
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
    }

    @Before
    fun setUp() {
        temporaryFolder.create()

        writeGradleProperties()
        writeBuildGradle()
        writeTest()
    }

    @Test
    fun testBuild() {
        val result = runner
            .withArguments("build", "-s")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":build")?.outcome)
    }

    private fun writeBuildGradle() {
        projectDir.resolve("build.gradle").writeText("""
plugins {
    id ${kotlinVersion?.let { "'org.jetbrains.kotlin.jvm' version '$kotlinVersion'" } ?: "'java'"}
    id 'com.github.gmazzo.buildconfig' version '<latest>'
}
""" + (if (withPackage) """
group = 'gs.test'
""" else "") + """

repositories {
    jcenter()
}

dependencies {
""" + (if (kotlinVersion != null) """
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion'
""" else "") + """
    testImplementation 'junit:junit:4.12'
}

buildConfig {""" +
                (if (withPackage) """
    packageName(group)
    
""" else "") + """
    
    buildConfigField('String', 'APP_NAME', "\"${'$'}{project.name}\"")
    buildConfigField('String', 'APP_SECRET', "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField('long', 'BUILD_TIME', "${'$'}{System.currentTimeMillis()}L")
    buildConfigField('boolean', 'FEATURE_ENABLED', "${'$'}{true}")

    forClass("BuildResources") {
        buildConfigField('String', 'A_CONSTANT', '"aConstant"')
    }
}

sourceSets {
    test {
        buildConfigField('String', 'TEST_CONSTANT', '"aTestValue"')
    }
}
        """.trimIndent())
    }

    private fun writeTest() {
        projectDir.resolve("src/test/java/gs/test/BuildConfigTest.java").apply {
            parentFile.mkdirs()
            writeText(
                """
package gs.test;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
""" + (if (!withPackage) """

import test_project.*;
""".trimIndent() else "") + """

public class BuildConfigTest {

    @Test
    public void testBuildConfigProperties() {
        assertEquals("$PROJECT_NAME", BuildConfig.APP_NAME);
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET);
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME);
        assertTrue(BuildConfig.FEATURE_ENABLED);
    }

    @Test
    public void testBuildConfigTestProperties() {
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT);
    }

    @Test
    public void testResourcesConfigProperties() {
        assertEquals("aConstant", BuildResources.A_CONSTANT);
    }

}
            """.trimIndent()
            )
        }

    }

    // This allows to coverage data to be collected from GradleRunner instance
    // https://github.com/koral--/jacoco-gradle-testkit-plugin
    private fun writeGradleProperties() {
        javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")!!
            .copyTo(FileOutputStream(File(projectDir, "gradle.properties")))
    }

    companion object {

        private const val PROJECT_NAME = "test-project"

        @JvmStatic
        @Parameterized.Parameters(name = "gradle={0}, kotlin={1}, withPackage={2}")
        fun versions() =
            listOf("4.10.1", "5.4.1", "6.1.1").flatMap { gradleVersion ->
                listOf(null, "1.2.41", "1.3.72").flatMap { kotlinVersion ->
                    listOf(true, false).map { withPackage ->
                        arrayOf(gradleVersion, kotlinVersion, withPackage)
                    }
                }
            }

    }

}

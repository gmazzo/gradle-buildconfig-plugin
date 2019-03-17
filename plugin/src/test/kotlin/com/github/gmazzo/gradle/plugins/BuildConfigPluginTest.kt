package com.github.gmazzo.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BuildConfigPluginTest(
    private val gradleVersion: String,
    private val kotlinVersion: String?
) {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val projectDir by lazy { temporaryFolder.newFolder(PROJECT_NAME) }

    private val runner by lazy {
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath(PluginUnderTestMetadataReading.readImplementationClasspath() + readCompileOnlyClasspath())
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
    }

    @Before
    fun setUp() {
        temporaryFolder.create()

        writeBuildGradle(kotlinVersion)
        writeTest()
    }

    @Test
    fun testBuild() {
        val result = runner
            .withArguments("build", "-s")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":build")?.outcome)
    }

    private fun writeBuildGradle(kotlinVersion: String?) {
        projectDir.resolve("build.gradle").writeText("""
plugins {
    id ${kotlinVersion?.let { "'org.jetbrains.kotlin.jvm' version '$kotlinVersion'" } ?: "'java'"}
    id 'com.github.gmazzo.buildconfig' version '<local>'
}

group = 'gs.test'

repositories {
    jcenter()
}

dependencies {
    testImplementation 'junit:junit:4.12'
}

buildConfig {
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

    companion object {

        private const val PROJECT_NAME = "test-project"

        @JvmStatic
        @Parameterized.Parameters(name = "gradle={0}, kotlin={1}")
        fun versions() =
            listOf("3.5", "4.1", "4.8", "4.10.1", "5.2.1").flatMap { gradleVersion ->
                listOf(null, "1.1.61", "1.2.41", "1.3.21").map { kotlinVersion ->
                    arrayOf(gradleVersion, kotlinVersion)
                }
            }
    }

}

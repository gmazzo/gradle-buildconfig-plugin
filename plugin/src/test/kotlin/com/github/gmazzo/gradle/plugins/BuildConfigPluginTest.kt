package com.github.gmazzo.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.io.FileOutputStream
import java.util.stream.Stream
import kotlin.streams.asStream

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildConfigPluginTest {

    fun testBuild(): Stream<Args> =
        sequenceOf("6.9.4", "7.6", "8.1.1").flatMap { gradleVersion ->
            sequenceOf(null, "1.6.20", "1.7.20", "1.8.20").flatMap { kotlinVersion ->
                sequenceOf(true, false).map { withPackage ->
                    Args(gradleVersion, kotlinVersion, withPackage)
                }
            }
        }.asStream()

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun Args.testBuild() {
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        writeGradleProperties()
        writeBuildGradle()
        writeTest()

        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withArguments("build", "-s")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":build")?.outcome)
    }

    private fun Args.writeBuildGradle() {
        projectDir.resolve("settings.gradle").writeText(
            """
            rootProject.name = "$PROJECT_NAME"
        """.trimIndent()
        )

        projectDir.resolve("build.gradle").writeText("""
        plugins {
            id ${kotlinVersion?.let { "'org.jetbrains.kotlin.jvm' version '$kotlinVersion'" } ?: "'java'"}
            id 'com.github.gmazzo.buildconfig' version '<latest>'
        }
        """ + (if (withPackage) """
        group = 'gs.test'
        """ else "") + """
                
        """ + (if (kotlinVersion != null) """
        assert "$kotlinVersion" == org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapperKt.getKotlinPluginVersion(project)
        
        """ else "") + """
        repositories {
            mavenCentral()
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
                buildConfig {
                    buildConfigField('String', 'TEST_CONSTANT', '"aTestValue"')
                }
            }
        }
        """.trimIndent())
    }

    private fun Args.writeTest() {
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
    private fun Args.writeGradleProperties() = File(projectDir, "gradle.properties").also { file ->
        javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")!!.use {
            FileOutputStream(file, true).use(it::copyTo)
        }

        file.appendText(
            """
            org.gradle.caching=true
            org.gradle.configuration-cache=true
        """.trimIndent()
        )
    }

    data class Args(
        val gradleVersion: String,
        val kotlinVersion: String?,
        val withPackage: Boolean,
    ) {

        val projectDir = File(
            PROJECT_NAME,
            "gradle-$gradleVersion/kotlin-${kotlinVersion ?: "none"}/${if (withPackage) "withPackage" else "withoutPackage"}"
        )

    }

    companion object {
        private const val PROJECT_NAME = "test-project"
    }

}

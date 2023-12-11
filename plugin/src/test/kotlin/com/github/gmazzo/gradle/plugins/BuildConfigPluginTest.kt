package com.github.gmazzo.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildConfigPluginTest {

    fun testBuild(): Stream<Args> {
        val gradleMin = "7.0"
        val gradle7 = "7.6"
        val gradle8 = "8.1.1"

        val kotlin6 = "1.6.20"
        val kotlin7 = "1.7.20"
        val kotlin8 = "1.8.20"

        return Stream.of(
            Args(gradleMin, null),
            Args(gradleMin, kotlin6),
            Args(gradleMin, kotlin7),
            Args(gradleMin, kotlin8),

            Args(gradle7, null),
            Args(gradle7, kotlin6),
            Args(gradle7, kotlin7),
            Args(gradle7, kotlin8),

            Args(gradle8, null),
            Args(gradle8, kotlin6),
            Args(gradle8, kotlin7),
            Args(gradle8, kotlin8),
        ).flatMap { Stream.of(it.copy(withPackage = true), it.copy(withPackage = false)) }
    }

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
        import com.github.gmazzo.gradle.plugins.FieldType

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
        
        java {
            withSourcesJar()
            withJavadocJar()
        }
        
        dependencies {
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
            // Test collections
            buildConfigField(FieldType.create('${collectionsPackage}.List', 'String'), 'LIST_OF_STRING', "java.util.Collections.emptyList()")
            buildConfigField(FieldType.create('${collectionsPackage}.Set', 'String'), 'SET_OF_STRING', "java.util.Collections.emptySet()")
            buildConfigField(FieldType.create('${collectionsPackage}.Collection', 'String'), 'COLLECTION_OF_STRING', "java.util.Collections.emptyList()")
            // Test boxing of primitives
            buildConfigField(FieldType.create('${collectionsPackage}.List', 'boolean'), 'LIST_OF_BOOLS', "java.util.Collections.emptyList()")
        
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
            file.outputStream().use(it::copyTo)
        }

        file.appendText("org.gradle.caching=true")
        if (configurationCache) {
            file.appendText("org.gradle.configuration-cache=true")
        }
    }

    @AfterAll
    fun tearDownGradleDaemon() {
        DefaultGradleConnector.close()
    }

    data class Args(
        val gradleVersion: String,
        val kotlinVersion: String?,
        val withPackage: Boolean = true,
    ) {

        // TODO there is a check on Gradle 8 preventing agents (like JaCoCo) to be added with CC
        //  https://docs.gradle.org/8.1.1/userguide/configuration_cache.html#config_cache:not_yet_implemented:testkit_build_with_java_agent
        val configurationCache = GradleVersion.version(gradleVersion) < GradleVersion.version("8.0")

        val projectDir =
            File(
                "${BuildConfigPluginTest::class.simpleName}/" +
                        "gradle-$gradleVersion/" +
                        "kotlin-${kotlinVersion ?: "none"}/" +
                        (if (withPackage) "withPackage/" else "withoutPackage/") +
                        (if (configurationCache) "withCC" else "withoutCC")
            )

        val collectionsPackage = if (kotlinVersion == null) "java.util" else "kotlin.collections"

    }

    companion object {
        private const val PROJECT_NAME = "test-project"
    }

}

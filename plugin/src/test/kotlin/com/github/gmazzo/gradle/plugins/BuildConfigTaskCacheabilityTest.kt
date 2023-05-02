package com.github.gmazzo.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.File
import kotlin.test.assertEquals

@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildConfigTaskCacheabilityTest {

    private val projectDir = File("${BuildConfigTaskCacheabilityTest::class.simpleName}")

    private val buildScript = File(projectDir, "build.gradle.kts")

    private val runner = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("clean", "jar")
        .forwardOutput()

    @BeforeAll
    fun setup() {
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        File(projectDir, "gradle.properties").writeText(
            """
            org.gradle.caching=true
            org.gradle.configuration-cache=true
        """.trimIndent()
        )

        File(projectDir, "settings.gradle.kts").writeText("""
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                }
            }
        """.trimIndent())

        buildScript.writeText(
            """
            plugins {
                kotlin("jvm") version embeddedKotlinVersion
                id("com.github.gmazzo.buildconfig")
            }
            
            buildConfig {
                buildConfigField("String", "SOME_FIELD", "\"aValue\"")
            }
            
        """.trimIndent()
        )
    }

    @Test
    fun `using java, cacheability should succeeded`() {
        buildScript.appendText("buildConfig.useJavaOutput()\n")

        val firstRun = runner.build()
        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":generateBuildConfig")?.outcome)

        val secondRun = runner.build()
        assertEquals(TaskOutcome.FROM_CACHE, secondRun.task(":generateBuildConfig")?.outcome)
    }

    @Test
    fun `using kotlin, cacheability should succeeded`() {
        buildScript.appendText("buildConfig.useKotlinOutput()\n")

        val firstRun = runner.build()
        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":generateBuildConfig")?.outcome)

        val secondRun = runner.build()
        assertEquals(TaskOutcome.FROM_CACHE, secondRun.task(":generateBuildConfig")?.outcome)

        buildScript.appendText("buildConfig.useKotlinOutput { topLevelConstants = true }\n")

        val thirdRun = runner.build()
        assertEquals(TaskOutcome.SUCCESS, thirdRun.task(":generateBuildConfig")?.outcome)
    }

}
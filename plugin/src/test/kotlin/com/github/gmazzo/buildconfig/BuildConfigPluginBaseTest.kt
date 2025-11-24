package com.github.gmazzo.buildconfig

import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BuildConfigPluginBaseTest(private val isKMP: Boolean = false) {

    private val baseDir = File(
        System.getenv("TEMP_DIR"),
        javaClass.simpleName
    ).absoluteFile

    private val noTests = object : Args(gradleVersion = "0") {
        override fun toString() = "no tests"
    }

    protected val gradleMin = BuildConfigPlugin.MIN_GRADLE_VERSION
    protected val gradleLatest: String = GradleVersion.current().baseVersion.version
    protected val kotlinMin = "1.9.+"
    protected val kotlinCurrent = KotlinVersion.CURRENT.toString()
    protected val androidCurrent: String = ANDROID_GRADLE_PLUGIN_VERSION

    protected abstract fun Args.buildConfigFieldsContent(): String
    protected open fun Args.extraBuildContent() = ""
    protected abstract fun Args.writeTests()

    open fun testBuild() = listOf(
        Args(gradleVersion = gradleLatest),
        Args(gradleVersion = gradleLatest, kotlinVersion = kotlinMin),
        Args(gradleVersion = gradleLatest, kotlinVersion = kotlinCurrent),
        Args(gradleVersion = gradleLatest, androidVersion = androidCurrent),
        Args(gradleVersion = gradleLatest, kotlinVersion = kotlinCurrent, androidVersion = androidCurrent),

        Args(gradleVersion = gradleLatest, withPackage = false),
        Args(gradleVersion = gradleLatest, kotlinVersion = kotlinCurrent, withPackage = false),

        Args(gradleVersion = gradleMin),
        Args(gradleVersion = gradleMin, kotlinVersion = kotlinMin),
    )

    @Suppress("unused")
    private fun androidBuilds() = testBuild().filter { it.androidVersion != null }.ifEmpty { listOf(noTests) }

    @Suppress("unused")
    private fun otherBuilds() = testBuild().filter { it.androidVersion == null }.ifEmpty { listOf(noTests) }

    @ResourceLock("android") // AGP may install SDK components, doing this concurrently will break the builds
    @ParameterizedTest(name = "{0}")
    @MethodSource("androidBuilds")
    fun Args.testAndroidBuild() = testBuild()

    @ParameterizedTest(name = "{0}")
    @MethodSource("otherBuilds")
    fun Args.testOtherBuild() = testBuild()

    private fun Args.testBuild() {
        if (this == noTests) return

        projectDir.deleteRecursively()
        projectDir.mkdirs()

        writeGradleProperties()
        writeBuildGradle()
        writeTests()

        val task = if (isKMP) "allTests" else "test"
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withTestKitDir(projectDir.resolve(".testkit"))
            .withGradleVersion(gradleVersion)
            .withArguments(task, "-s", "--build-cache")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
    }

    private fun Args.writeBuildGradle() {
        val userBuildCacheDir = File(System.getProperty("user.home"))
            .resolve(".gradle/caches/build-cache-1")
            .takeIf { it.isDirectory }

        projectDir.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    google()
                }
            }

            plugins {
                id("jacoco-testkit-coverage")
            }

            ${if (userBuildCacheDir != null) "buildCache.local.directory = file(\"$userBuildCacheDir\")" else ""}

            rootProject.name = "$PROJECT_NAME"
            """.trimIndent()
        )

        val plugins = when (isKMP) {
            true -> when (androidVersion) {
                null -> listOf("org.jetbrains.kotlin.multiplatform" to kotlinVersion)
                else -> listOf(
                    "org.jetbrains.kotlin.multiplatform" to kotlinVersion,
                    "com.android.kotlin.multiplatform.library" to androidVersion,
                )
            }

            else -> when (androidVersion) {
                null -> when (kotlinVersion) {
                    null -> listOf("java" to null)
                    else -> listOf("org.jetbrains.kotlin.jvm" to kotlinVersion)
                }

                else -> when (kotlinVersion) {
                    null -> listOf("com.android.application" to androidVersion)
                    else -> listOf(
                        "org.jetbrains.kotlin.android" to kotlinVersion,
                        "com.android.application" to androidVersion,
                    )
                }
            }
        }

        if (androidVersion != null) {
            projectDir.resolve("src/${if (isKMP) "androidMain" else "main"}/AndroidManifest.xml")
                .apply { parentFile.mkdirs() }
                .writeText("<manifest/>")
        }

        projectDir.resolve("build.gradle.kts").writeText(
            (if (kotlinVersion != null) "import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion\n\n" else "") +
                """
        plugins {
        ${plugins.joinToString(separator = "\n") { (id, version) -> "    id(\"$id\")" + (version?.let { " version \"$it\"" } ?: "") }}
            id("com.github.gmazzo.buildconfig")
        }
        """ + (if (withPackage) """
        group = "gs.test"
        """ else "") + """

        """ + (if (kotlinVersion != null) """
        check(getKotlinPluginVersion().matches(Regex("${
                kotlinVersion.replace(".", "\\\\.").replace("+", "\\\\d+")
            }"))) {
            "Kotlin plugin version (${'$'}{getKotlinPluginVersion()}) does not match the required version ($kotlinVersion)"
        }

        """ else "") + """
        repositories {
            mavenCentral()
            google()
        }

        java.toolchain.languageVersion = JavaLanguageVersion.of(17)

        """ + (if (androidVersion != null) """
        ${if (isKMP) "kotlin.androidLibrary" else "android "}{
            compileSdk = 33
            namespace = "org.test"
        }
        """ else """
        java {
            withSourcesJar()
            withJavadocJar()
        }
        """) + """

        buildConfig {""" +
                (if (withPackage) """
            packageName("${'$'}{project.group}")

        """ else "") + """
            ${buildConfigFieldsContent()}
        }

        ${extraBuildContent()}
        """.trimIndent()
        )
    }

    private fun Args.writeGradleProperties() = File(projectDir, "gradle.properties").writeText(
        """
        org.gradle.jvmargs=-Xmx1g
        org.gradle.caching=true
    """.trimIndent()
    )

    open inner class Args(
        val gradleVersion: String,
        val kotlinVersion: String? = null,
        val androidVersion: String? = null,
        val withPackage: Boolean = true,
    ) {

        val projectDir: File = baseDir
            .resolve("gradle-$gradleVersion")
            .resolve("kotlin-${kotlinVersion ?: "none"}")
            .resolve("android-${androidVersion ?: "none"}")
            .resolve(if (withPackage) "withPackage" else "withoutPackage")

        override fun toString() = buildString {
            append("Gradle $gradleVersion")
            if (kotlinVersion != null) append(", Kotlin $kotlinVersion")
            if (androidVersion != null) append(", Android $androidVersion")
            if (!withPackage) append(", no package")
        }

    }

    companion object {
        const val PROJECT_NAME = "test-project"
    }

}

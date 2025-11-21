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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BuildConfigPluginBaseTest {

    private val baseDir = File(
        System.getenv("TEMP_DIR"),
        javaClass.simpleName
    ).absoluteFile

    protected val gradleMin = BuildConfigPlugin.MIN_GRADLE_VERSION
    protected val gradleLatest: String = GradleVersion.current().baseVersion.version
    protected val kotlinMin = "1.9.+"
    protected val kotlinCurrent = KotlinVersion.CURRENT.toString()
    protected val androidCurrent: String = ANDROID_GRADLE_PLUGIN_VERSION

    protected open val kotlinPluginId = "org.jetbrains.kotlin.jvm"
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

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun Args.testBuild() {
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        writeGradleProperties()
        writeBuildGradle()
        writeTests()

        val result = synchronizedIfAndroid {
            GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withTestKitDir(projectDir.resolve(".testkit"))
                .withGradleVersion(gradleVersion)
                .withArguments("build", "-s", "--build-cache")
                .build()
        }

        assertEquals(TaskOutcome.SUCCESS, result.task(":build")?.outcome)
    }

    /**
     * AGP may download and install SDK components, doing this concurrently will break one of the builds
     */
    private fun <Result> Args.synchronizedIfAndroid(block: () -> Result) =
        if (androidVersion != null) synchronized(this@BuildConfigPluginBaseTest, block) else block()

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

            ${ if (userBuildCacheDir != null) "buildCache.local.directory = file(\"$userBuildCacheDir\")" else "" }

            rootProject.name = "$PROJECT_NAME"
            """.trimIndent()
        )

        val plugins = when (androidVersion) {
            null -> when (kotlinVersion) {
                null -> listOf("java")
                else -> listOf("id(\"$kotlinPluginId\") version \"$kotlinVersion\"")
            }

            else -> when (kotlinVersion) {
                null -> listOf("id(\"com.android.application\") version \"$androidVersion\"")
                else -> listOf(
                    "id(\"com.android.application\") version \"$androidVersion\"",
                    "id(\"org.jetbrains.kotlin.android\") version \"$kotlinVersion\"",
                )
            }
        }

        if (androidVersion != null) {
            projectDir.resolve("src/main/AndroidManifest.xml")
                .apply { parentFile.mkdirs() }
                .writeText("<manifest/>")
        }

        projectDir.resolve("build.gradle.kts").writeText(
            (if (kotlinVersion != null) "import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion\n\n" else  "") +
            """
        plugins {
        ${plugins.joinToString(separator = "\n") { "    $it" }}
            id("com.github.gmazzo.buildconfig")
        }
        """ + (if (withPackage) """
        group = "gs.test"
        """ else "") + """

        """ + (if (kotlinVersion != null) """
        check(getKotlinPluginVersion().matches(Regex("${kotlinVersion.replace(".", "\\\\.").replace("+", "\\\\d+")}"))) {
            "Kotlin plugin version (${'$'}{getKotlinPluginVersion()}) does not match the required version ($kotlinVersion)"
        }

        """ else "") + """
        repositories {
            mavenCentral()
            google()
        }

        java.toolchain.languageVersion = JavaLanguageVersion.of(17)

        """ + (if (androidVersion != null) """
        android {
            compileSdkVersion(33)
            namespace = "org.test"

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            publishing {
                singleVariant("release") {
                    withSourcesJar()
                    withJavadocJar()
                }
            }
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

    private fun Args.writeGradleProperties() = File(projectDir, "gradle.properties").also { file ->
        file.appendText("org.gradle.caching=true")
        file.appendText("org.gradle.configuration-cache=true")
    }

    inner class Args(
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

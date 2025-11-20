package com.github.gmazzo.buildconfig

import org.junit.jupiter.api.Disabled

@Disabled // FIXME there is an issue that we'll tackle separately
class BuildConfigPluginKMPExpectActualTest : BuildConfigPluginBaseTest() {

    private val targets = listOf("android", "jvm", "iosArm64", "js")

    override val kotlinPluginId = "org.jetbrains.kotlin.multiplatform"

    override fun testBuild() = listOf(
        Args(gradleVersion = gradleLatest, kotlinVersion = kotlinCurrent, androidVersion = androidCurrent),
        Args(gradleVersion = gradleMin, kotlinVersion = kotlinMin, androidVersion = androidCurrent),
    )

    override fun Args.buildConfigFieldsContent() = """
        buildConfigField("API_BASE_URL", expect("\"https://localhost:8080/\""))

        sourceSets.named("androidDebug") {
            buildConfigField("API_BASE_URL", "\"https://10.0.2.2:8080/\"")
        }
    """.trimIndent()

    override fun Args.extraBuildContent() = """
        kotlin {
            androidTarget()
            for (iosTarget in listOf(iosArm64(), iosSimulatorArm64())) {
                iosTarget.binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
            wasmJs { browser() }

            sourceSets.commonTest.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    """.trimIndent()

    override fun Args.writeTests() {
        projectDir.resolve("src/commonTest/kotlin/gs/test/BuildConfigBaseTest.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
            package gs.test

            import kotlin.test.*

            abstract class BuildConfigBaseTest(private val expectedBaseUrl: String) {

                @Test
                fun testBuildConfigProperties() {
                    assertEquals(expectedBaseUrl, BuildConfig.API_BASE_URL)
                }

            }
            """.trimIndent()
            )
        }
        for (target in targets) {
            val className = "BuildConfig${target.replaceFirstChar { it.uppercase() }}Test"
            val host = if (target == "android") "10.0.2.2" else "localhost"

            projectDir.resolve("src/${target}Test/kotlin/$className.kt").apply {
                parentFile.mkdirs()
                writeText(
                """
                    package gs.test

                    import kotlin.test.*

                    class $className : BuildConfigBaseTest(
                        expectedBaseUrl = "\"https://$host:8080/\""
                    )
                    """.trimIndent()
                )
            }
        }
    }

}

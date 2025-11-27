package com.github.gmazzo.buildconfig

class BuildConfigPluginKMPExpectActualTest : BuildConfigPluginBaseTest(isKMP = true) {

    override fun testBuild() = listOf(
        Args(gradleVersion = gradleLatest, kotlinVersion = kotlinCurrent, androidVersion = androidCurrent),
        Args(gradleVersion = gradleMin, kotlinVersion = kotlinMin, androidVersion = androidMin),
    )

    override fun Args.buildConfigFieldsContent() = """
        buildConfigField("String", "API_BASE_URL", expect(expression("\"https://localhost:8080/\"")))

        sourceSets.named("androidMain") {
            buildConfigField("String", "API_BASE_URL", "\"https://10.0.2.2:8080/\"")
        }
    """.trimIndent()

    override fun Args.extraBuildContent() = """
        kotlin {
            jvm()
            androidLibrary {
                withAndroidTestOnJvm { }
            }
            js { browser() }

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

            expect val expectedBaseUrl: String

            class BuildConfigBaseTest {

                @Test
                fun testBuildConfigProperties() {
                    assertEquals(expectedBaseUrl, BuildConfig.API_BASE_URL)
                }

            }
            """.trimIndent()
            )
        }

        fun writeActuals(target: String, host: String = "localhost") {
            projectDir.resolve("src/$target/kotlin/gs/test/TestActuals.kt").apply {
                parentFile.mkdirs()
                writeText(
                    """
            package gs.test

            actual val expectedBaseUrl = "https://$host:8080/"
            """.trimIndent()
                )
            }
        }

        writeActuals("jvmTest")
        writeActuals("androidHostTest", "10.0.2.2")
        writeActuals("jsTest")
    }

}

package com.github.gmazzo.buildconfig

import java.io.File
import java.util.stream.Stream
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
class BuildConfigPluginTest {

    fun testBuild(): Stream<Args> {
        val gradleMin = BuildConfigPlugin.MIN_GRADLE_VERSION
        val gradleLatest = GradleVersion.current().baseVersion.version

        val kotlin8 = "1.8.+"
        val kotlin9 = "1.9.+"
        val kotlin20 = "2.0.+"

        return Stream.of(

            Args(gradleMin, null),
            Args(gradleMin, kotlin8),
            Args(gradleMin, kotlin9),
            Args(gradleMin, kotlin20),

            Args(gradleLatest, null),
            Args(gradleLatest, kotlin8),
            Args(gradleLatest, kotlin9),
            Args(gradleLatest, kotlin20),

            ).flatMap {
            Stream.of(
                it.copy(withPackage = true),
                it.copy(withPackage = false),
            )
        }
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
            plugins {
                id("jacoco-testkit-coverage")
            }

            rootProject.name = "$PROJECT_NAME"
            """.trimIndent()
        )

        projectDir.resolve("build.gradle").writeText(
            """
        plugins {
            id ${kotlinVersion?.let { "'org.jetbrains.kotlin.jvm' version '$kotlinVersion'" } ?: "'java'"}
            id 'com.github.gmazzo.buildconfig'
        }
        """ + (if (withPackage) """
        group = 'gs.test'
        """ else "") + """

        """ + (if (kotlinVersion != null) """
        assert "$kotlinVersion" == org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapperKt.getKotlinPluginVersion(project).replaceFirst(/\.\d+(-\w+)?$/, '.+')

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

            forClass("BuildResources") {
                buildConfigField('String', 'A_CONSTANT', '"aConstant"')
            }

            // all possible kind for String
            buildConfigField(String, "STRING", "aString")
            buildConfigField(String, "STRING_NULL", null)
            buildConfigField(String, "STRING_PROVIDER", provider { "aString" })
            buildConfigField(String[], "STRING_ARRAY", ["a", "b", "c"])
            buildConfigField(String[], "STRING_ARRAY_PROVIDER", provider { ["a", "b", "c"] })
            buildConfigField('String?[]', "STRING_ARRAY_NULLABLE", ["a", null, "c"])
            buildConfigField('String?[]', "STRING_ARRAY_NULLABLE_PROVIDER", provider { ["a", null, "c"] })
            buildConfigField('List<String?>', "STRING_LIST", ["a", null, "c"])
            buildConfigField('List<String?>', "STRING_LIST_PROVIDER", provider { ["a", null, "c"] })
            buildConfigField('Set<String?>', "STRING_SET", ["a", null, "c"])
            buildConfigField('Set<String?>', "STRING_SET_PROVIDER", provider { ["a", null, "c"] })

            // all possible kind for Byte
            buildConfigField(byte, "BYTE", (byte) 64)
            buildConfigField(Byte, "BYTE_NULL", null)
            buildConfigField(byte, "BYTE_PROVIDER", provider { (byte) 64 })
            buildConfigField(byte[], "BYTE_NATIVE_ARRAY", [1, 2, 3])
            buildConfigField(byte[], "BYTE_NATIVE_ARRAY_PROVIDER", provider { [1, 2, 3] })
            buildConfigField(Byte[], "BYTE_ARRAY", [1, 2, 3])
            buildConfigField(Byte[], "BYTE_ARRAY_PROVIDER", provider { [1, 2, 3] })
            buildConfigField('Byte?[]', "BYTE_ARRAY_NULLABLE", [1, null, 3])
            buildConfigField('Byte?[]', "BYTE_ARRAY_NULLABLE_PROVIDER", provider { [1, null, 3] })
            buildConfigField('List<Byte?>', "BYTE_LIST", [1, null, 3])
            buildConfigField('List<Byte?>', "BYTE_LIST_PROVIDER", provider { [1, null, 3] })
            buildConfigField('Set<Byte?>', "BYTE_SET", [1, null, 3])
            buildConfigField('Set<Byte?>', "BYTE_SET_PROVIDER", provider { [1, null, 3] })

            // all possible kind for Short
            buildConfigField(short, "SHORT", 64)
            buildConfigField(short, "SHORT_NULL", null)
            buildConfigField(short, "SHORT_PROVIDER", provider { 64 })
            buildConfigField(short[], "SHORT_NATIVE_ARRAY", [1, 2, 3])
            buildConfigField(short[], "SHORT_NATIVE_ARRAY_PROVIDER", provider { [1, 2, 3] })
            buildConfigField(Short[], "SHORT_ARRAY", [1, 2, 3])
            buildConfigField(Short[], "SHORT_ARRAY_PROVIDER", provider { [1, 2, 3] })
            buildConfigField('Short?[]', "SHORT_ARRAY_NULLABLE", [1, null, 3])
            buildConfigField('Short?[]', "SHORT_ARRAY_NULLABLE_PROVIDER", provider { [1, null, 3] })
            buildConfigField('List<Short?>', "SHORT_LIST", [1, null, 3])
            buildConfigField('List<Short?>', "SHORT_LIST_PROVIDER", provider { [1, null, 3] })
            buildConfigField('Set<Short?>', "SHORT_SET", [1, null, 3])
            buildConfigField('Set<Short?>', "SHORT_SET_PROVIDER", provider { [1, null, 3] })

            // all possible kind for Char
            buildConfigField(char, "CHAR", 'a' as char)
            buildConfigField(Character, "CHAR_NULL", null)
            buildConfigField(char, "CHAR_PROVIDER", provider { 'a' as char })
            buildConfigField(char[], "CHAR_NATIVE_ARRAY", ['a' as char, 'b' as char, 'c' as char])
            buildConfigField(char[], "CHAR_NATIVE_ARRAY_PROVIDER", provider { ['a' as char, 'b' as char, 'c' as char] })
            buildConfigField(Character[], "CHAR_ARRAY", ['a' as char, 'b' as char, 'c' as char])
            buildConfigField(Character[], "CHAR_ARRAY_PROVIDER", provider { ['a' as char, 'b' as char, 'c' as char] })
            buildConfigField('Char?[]', "CHAR_ARRAY_NULLABLE", ['a' as char, null, 'c' as char])
            buildConfigField('Char?[]', "CHAR_ARRAY_NULLABLE_PROVIDER", provider { ['a' as char, null, 'c' as char] })
            buildConfigField('List<Char?>', "CHAR_LIST", ['a' as char, null, 'c' as char])
            buildConfigField('List<Char?>', "CHAR_LIST_PROVIDER", provider { ['a' as char, null, 'c' as char] })
            buildConfigField('Set<Char?>', "CHAR_SET", ['a' as char, null, 'c' as char])
            buildConfigField('Set<Char?>', "CHAR_SET_PROVIDER", provider { ['a' as char, null, 'c' as char] })

            // all possible kind for Int
            buildConfigField(int, "INT", 1)
            buildConfigField(Integer, "INT_NULL", null)
            buildConfigField(int, "INT_PROVIDER", provider { 1 })
            buildConfigField(int[], "INT_NATIVE_ARRAY", [1, 2, 3])
            buildConfigField(int[], "INT_NATIVE_ARRAY_PROVIDER", provider { [1, 2, 3] })
            buildConfigField(Integer[], "INT_ARRAY", [1, 2, 3])
            buildConfigField(Integer[], "INT_ARRAY_PROVIDER", provider { [1, 2, 3] })
            buildConfigField('Integer?[]', "INT_ARRAY_NULLABLE", [1, null, 3])
            buildConfigField('Integer?[]', "INT_ARRAY_NULLABLE_PROVIDER", provider { [1, null, 3] })
            buildConfigField('List<Integer?>', "INT_LIST", [1, null, 3])
            buildConfigField('List<Integer?>', "INT_LIST_PROVIDER", provider { [1, null, 3] })
            buildConfigField('Set<Integer?>', "INT_SET", [1, null, 3])
            buildConfigField('Set<Integer?>', "INT_SET_PROVIDER", provider { [1, null, 3] })

            // all possible kind for Long
            buildConfigField(long, "LONG", 1L)
            buildConfigField(Long, "LONG_NULL", null)
            buildConfigField(long, "LONG_PROVIDER", provider { 1L })
            buildConfigField(long[], "LONG_NATIVE_ARRAY", [1L, 2L, 3L])
            buildConfigField(long[], "LONG_NATIVE_ARRAY_PROVIDER", provider { [1L, 2L, 3L] })
            buildConfigField(Long[], "LONG_ARRAY", [1L, 2L, 3L])
            buildConfigField(Long[], "LONG_ARRAY_PROVIDER", provider { [1L, 2L, 3L] })
            buildConfigField('Long?[]', "LONG_ARRAY_NULLABLE", [1L, null, 3L])
            buildConfigField('Long?[]', "LONG_ARRAY_NULLABLE_PROVIDER", provider { [1L, null, 3L] })
            buildConfigField('List<Long?>', "LONG_LIST", [1L, null, 3L])
            buildConfigField('List<Long?>', "LONG_LIST_PROVIDER", provider { [1L, null, 3L] })
            buildConfigField('Set<Long?>', "LONG_SET", [1L, null, 3L])
            buildConfigField('Set<Long?>', "LONG_SET_PROVIDER", provider { [1L, null, 3L] })

            // all possible kind for Float
            buildConfigField(float, "FLOAT", 1f)
            buildConfigField(Float, "FLOAT_NULL", null)
            buildConfigField(float, "FLOAT_PROVIDER", provider { 1f })
            buildConfigField(float[], "FLOAT_NATIVE_ARRAY", [1f, 2f, 3f])
            buildConfigField(float[], "FLOAT_NATIVE_ARRAY_PROVIDER", provider { [1f, 2f, 3f] })
            buildConfigField(Float[], "FLOAT_ARRAY", [1f, 2f, 3f] )
            buildConfigField(Float[], "FLOAT_ARRAY_PROVIDER", provider { [1f, 2f , 3f] })
            buildConfigField('Float?[]', "FLOAT_ARRAY_NULLABLE", [1f, null, 3f] )
            buildConfigField('Float?[]', "FLOAT_ARRAY_NULLABLE_PROVIDER", provider { [1f, null , 3f]  })
            buildConfigField('List<Float?>', "FLOAT_LIST", [1f, null, 3f] )
            buildConfigField('List<Float?>', "FLOAT_LIST_PROVIDER", provider { [1f, null , 3f]  })
            buildConfigField('Set<Float?>', "FLOAT_SET", [1f, null, 3f] )
            buildConfigField('Set<Float?>', "FLOAT_SET_PROVIDER", provider { [1f, null , 3f]  })

            // all possible kind for Double
            buildConfigField(double, "DOUBLE", 1.0)
            buildConfigField(Double, "DOUBLE_NULL", null)
            buildConfigField(double, "DOUBLE_PROVIDER", provider { 1.0 })
            buildConfigField(double[], "DOUBLE_NATIVE_ARRAY", [1.0, 2.0, 3.0])
            buildConfigField(double[], "DOUBLE_NATIVE_ARRAY_PROVIDER", provider { [1.0, 2.0, 3.0] })
            buildConfigField(Double[], "DOUBLE_ARRAY", [1.0, 2.0, 3.0] )
            buildConfigField(Double[], "DOUBLE_ARRAY_PROVIDER", provider { [1.0, 2.0 , 3.0] })
            buildConfigField('Double?[]', "DOUBLE_ARRAY_NULLABLE", [1.0 as double, null, 3.0 as double] )
            buildConfigField('Double?[]', "DOUBLE_ARRAY_NULLABLE_PROVIDER", provider { [1.0 as double, null , 3.0 as double]})
            buildConfigField('List<Double?>', "DOUBLE_LIST", [1.0 as double, null, 3.0 as double] )
            buildConfigField('List<Double?>', "DOUBLE_LIST_PROVIDER", provider { [1.0 as double, null, 3.0 as double]})
            buildConfigField('Set<Double?>', "DOUBLE_SET", [1.0 as double, null, 3.0 as double] )
            buildConfigField('Set<Double?>', "DOUBLE_SET_PROVIDER", provider { [1.0 as double, null, 3.0 as double]})

            // all possible kind for Boolean
            buildConfigField(boolean, "BOOLEAN", true)
            buildConfigField(Boolean, "BOOLEAN_NULL", null)
            buildConfigField(boolean, "BOOLEAN_PROVIDER", provider { true })
            buildConfigField(boolean[], "BOOLEAN_NATIVE_ARRAY", [true, false, false])
            buildConfigField(boolean[], "BOOLEAN_NATIVE_ARRAY_PROVIDER", provider { [true, false, false] })
            buildConfigField(Boolean[], "BOOLEAN_ARRAY", [true, false, false] )
            buildConfigField(Boolean[], "BOOLEAN_ARRAY_PROVIDER", provider { [true, false , false ] })
            buildConfigField('Boolean?[]', "BOOLEAN_ARRAY_NULLABLE", [true, null, false] )
            buildConfigField('Boolean?[]', "BOOLEAN_ARRAY_NULLABLE_PROVIDER", provider { [true, null , false ] })
            buildConfigField('List<Boolean?>', "BOOLEAN_LIST", [true, null, false] )
            buildConfigField('List<Boolean?>', "BOOLEAN_LIST_PROVIDER", provider { [true, null , false ] })
            buildConfigField('Set<Boolean?>', "BOOLEAN_SET", [true, null, false] )
            buildConfigField('Set<Boolean?>', "BOOLEAN_SET_PROVIDER", provider { [true, null , false ] })

            // custom formats with expressions, including Map and custom types
            buildConfigField(
                    "${if (kotlinVersion != null) "kotlin.collections" else "java.util"}.Map<String, Integer>",
                    "MAP",
                    "${if (kotlinVersion != null) "mapOf(\\\"a\\\" to 1, \\\"b\\\" to 2)" else "java.util.Map.of(\\\"a\\\", 1, \\\"b\\\", 2)"}"
            )
            buildConfigField(
                    "${if (kotlinVersion != null) "kotlin.collections" else "java.util"}.Map<String, Integer>",
                    "MAP_PROVIDER",
                    provider { "${if (kotlinVersion != null) "mapOf(\\\"a\\\" to 1, \\\"b\\\" to 2)" else "java.util.Map.of(\\\"a\\\", 1, \\\"b\\\", 2)"}" }
            )

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

    private fun Args.writeGradleProperties() = File(projectDir, "gradle.properties").also { file ->
        file.appendText("org.gradle.caching=true")
        file.appendText("org.gradle.configuration-cache=true")
    }

    data class Args(
        val gradleVersion: String,
        val kotlinVersion: String?,
        val withPackage: Boolean = true,
    ) {

        val projectDir =
            File(
                "${BuildConfigPluginTest::class.simpleName}/" +
                    "gradle-$gradleVersion/" +
                    "kotlin-${kotlinVersion ?: "none"}/" +
                    (if (withPackage) "withPackage/" else "withoutPackage/")
            )

    }

    companion object {
        private const val PROJECT_NAME = "test-project"
    }

}

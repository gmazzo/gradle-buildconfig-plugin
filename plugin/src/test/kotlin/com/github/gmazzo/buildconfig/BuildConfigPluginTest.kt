package com.github.gmazzo.buildconfig

class BuildConfigPluginTest : BuildConfigPluginBaseTest() {

    override fun Args.buildConfigFieldsContent() = """
        useJavaOutput()
        documentation = "This is a generated BuildConfig class"

        buildConfigField("String", "APP_NAME", "\"${'$'}{project.name}\"")
        buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
        buildConfigField("boolean", "FEATURE_ENABLED", "${'$'}{true}")

        forClass("BuildResources") {
            buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
        }
        """

    override fun Args.extraBuildContent() = """
        dependencies {
            testImplementation("junit:junit:4.12")
        }

        ${when {
            androidVersion != null -> "android.sourceSets"
            kotlinVersion != null -> "kotlin.sourceSets"
            else -> "sourceSets"
        }} {
            named("test") {
                buildConfig {
                    buildConfigField("String", "TEST_CONSTANT", "\"aTestValue\"")
                }
            }
        }
        """

    override fun Args.writeTests() {
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

}

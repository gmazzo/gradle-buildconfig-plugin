import com.github.gmazzo.buildconfig.BuildConfigField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

buildscript {
    dependencies {
        classpath(platform(libs.junit5.bom))
        classpath(libs.junit5.params)
    }
}

plugins {
    base
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")

    forClass("BuildResources") {
        buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
    }

    buildConfigFields.addLater(provider {
        objects.newInstance<BuildConfigField>("PROVIDED").apply {
            type(String::class.java)
            value("byAddLater")
        }
    })
}

// everything below here are just helper code to allow testing the plugin as we can't rely on any framework like JUnit

val generateBuildConfigTest by tasks.registering(AssertGeneratedFile::class) {
    generatedDir.set(tasks.generateBuildConfigClasses.flatMap { it.outputDir })
    filePath.set("com/github/gmazzo/buildconfig/demos/generic/BuildConfig.java")
    expectedContent.set(
        """
        package com.github.gmazzo.buildconfig.demos.generic;

        import java.lang.String;

        public final class BuildConfig {
          public static final String APP_NAME = "generic";

          public static final String APP_SECRET = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu";

          public static final boolean FEATURE_ENABLED = true;

          public static final String PROVIDED = "byAddLater";

          private BuildConfig() {
          }
        }
        """
    )
}

val generateBuildResourcesBuildConfigTest by tasks.registering(AssertGeneratedFile::class) {
    generatedDir.set(tasks.generateBuildConfigClasses.flatMap { it.outputDir })
    filePath.set("com/github/gmazzo/buildconfig/demos/generic/BuildResources.java")
    expectedContent.set(
        """
        package com.github.gmazzo.buildconfig.demos.generic;

        import java.lang.String;

        public final class BuildResources {
          public static final String A_CONSTANT = "aConstant";

          private BuildResources() {
          }
        }
        """
    )
}

tasks {

    val test by registering {
        dependsOn(generateBuildConfigTest, generateBuildResourcesBuildConfigTest)
    }

    check {
        dependsOn(test)
    }

}

abstract class AssertGeneratedFile : DefaultTask() {

    @get:InputDirectory
    abstract val generatedDir: DirectoryProperty

    @get:Input
    abstract val filePath: Property<String>

    @get:Input
    abstract val expectedContent: Property<String>

    @TaskAction
    fun performAssert() {
        val actualFile = File(generatedDir.asFile.get(), filePath.get())

        assertTrue(actualFile.isFile, "Expected file doesn't exist: $actualFile")

        val expected = expectedContent.get().trimIndent().trim()
        val actualContent = actualFile.readText().trim()
        assertEquals(expected, actualContent)
    }

}

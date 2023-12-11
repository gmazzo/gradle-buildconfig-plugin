import kotlin.test.assertEquals
import kotlin.test.assertTrue

buildscript {
    dependencies {
        classpath(libs.kotlin.test)
    }
}

plugins {
    base
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    buildConfigField( "APP_NAME", project.name)
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("BUILD_TIME", TimeUnit.DAYS.toMillis(2))
    buildConfigField("FEATURE_ENABLED", true)

    forClass("BuildResources") {
        buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
    }
}

// everything below here are just helper code to allow testing the plugin as we can't rely on any framework like JUnit

val generateBuildConfigTest = task<AssertGeneratedFile>("generateBuildConfigTest") {
    generatedDir.set(tasks.generateBuildConfig.flatMap { it.outputDir })
    filePath.set("com/github/gmazzo/buildconfig/demos/generic/BuildConfig.java")
    expectedContent.set("""
        package com.github.gmazzo.buildconfig.demos.generic;
        
        import java.lang.String;
        
        public final class BuildConfig {
          public static final String APP_NAME = "generic";
        
          public static final String APP_SECRET = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu";
        
          public static final long BUILD_TIME = 172800000L;
        
          public static final boolean FEATURE_ENABLED = true;
        
          private BuildConfig() {
          }
        }
        """)
}

val generateBuildResourcesBuildConfigTest = task<AssertGeneratedFile>("generateBuildResourcesBuildConfigTest") {
    generatedDir.set(tasks.generateBuildConfig.flatMap { it.outputDir })
    filePath.set("com/github/gmazzo/buildconfig/demos/generic/BuildResources.java")
    expectedContent.set("""
        package com.github.gmazzo.buildconfig.demos.generic;
        
        import java.lang.String;
        
        public final class BuildResources {
          public static final String A_CONSTANT = "aConstant";
        
          private BuildResources() {
          }
        }
        """)
}

tasks {

    val test by registering {
        dependsOn(generateBuildConfigTest, generateBuildResourcesBuildConfigTest)
    }

    build {
        dependsOn(test)
    }

}

abstract class AssertGeneratedFile : DefaultTask() {

    @get:InputFiles
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

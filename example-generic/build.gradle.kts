import com.github.gmazzo.gradle.plugins.BuildConfigTask

plugins {
    id("com.github.gmazzo.buildconfig") version "<latest>"
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${TimeUnit.DAYS.toMillis(2)}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")

    forClass("BuildResources") {
        buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
    }
}

// everything below here are just helper code to allow testing the plugin as we can't rely on any framework like JUnit

val generateBuildConfig: BuildConfigTask by tasks
val generateBuildResourcesBuildConfig: BuildConfigTask by tasks

val generateBuildConfigTest = task<AssertGeneratedFile>("generateBuildConfigTest") {
    dependsOn(generateBuildConfig)

    task = generateBuildConfig
    filePath = "com/github/gmazzo/example_generic/BuildConfig.java"
    expectedContent = """
        package com.github.gmazzo.example_generic;
        
        import java.lang.String;
        
        public final class BuildConfig {
          public static final String APP_NAME = "example-generic";
        
          public static final String APP_SECRET = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu";
        
          public static final long BUILD_TIME = 172800000L;
        
          public static final boolean FEATURE_ENABLED = true;
        
          private BuildConfig() {
          }
        }
        """
}

val generateBuildResourcesBuildConfigTest = task<AssertGeneratedFile>("generateBuildResourcesBuildConfigTest") {
    dependsOn(generateBuildResourcesBuildConfig)

    task = generateBuildResourcesBuildConfig
    filePath = "com/github/gmazzo/example_generic/BuildResources.java"
    expectedContent = """
        package com.github.gmazzo.example_generic;
        
        import java.lang.String;
        
        public final class BuildResources {
          public static final String A_CONSTANT = "aConstant";
        
          private BuildResources() {
          }
        }
        """
}

task<Delete>("clean") {
    delete(buildDir)
}

task("test") {
    dependsOn(generateBuildConfigTest, generateBuildResourcesBuildConfigTest)
}

open class AssertGeneratedFile : DefaultTask() {
    lateinit var task: BuildConfigTask
    lateinit var filePath: String
    lateinit var expectedContent: String

    @TaskAction
    fun performAssert() {
        val actualFile = File(task.outputDir, filePath)
        if (!actualFile.isFile) {
            throw AssertionError("Expected file doesn't exist: $actualFile")
        }

        val content = expectedContent.trimIndent().trim()
        val actualContent = actualFile.readText().trim()
        if (actualContent != content) {
            throw AssertionError("Expected:\n$content\n\n but was:\n$actualContent")
        }
    }

}

import com.github.gmazzo.buildconfig.BuildConfigField

plugins {
    base
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${TimeUnit.DAYS.toMillis(2)}")
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
    generatedDir.set(tasks.generateBuildConfig.flatMap { it.outputDir })
    filePath.set("com/github/gmazzo/buildconfig/demos/generic/BuildConfig.java")
    expectedContent.set(
        """
        package com.github.gmazzo.buildconfig.demos.generic;

        import java.lang.String;

        public final class BuildConfig {
          public static final String APP_NAME = "generic";

          public static final String PROVIDED = "byAddLater";

          public static final String APP_SECRET = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu";

          public static final long BUILD_TIME = 172800000;

          public static final boolean FEATURE_ENABLED = true;

          private BuildConfig() {
          }
        }
        """
    )
}

val generateBuildResourcesBuildConfigTest by tasks.registering(AssertGeneratedFile::class) {
    generatedDir.set(tasks.generateBuildConfig.flatMap { it.outputDir })
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

        check(actualFile.isFile) { "Expected file doesn't exist: $actualFile" }

        val expected = expectedContent.get().trimIndent().trim()
        val actualContent = actualFile.readText().trim()
        check(expected == actualContent)
    }

}

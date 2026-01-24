import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.github.gmazzo.buildconfig.BuildConfigTask

plugins {
    alias(libs.plugins.android.library)
    id("com.github.gmazzo.buildconfig")
    `maven-publish`
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

android {
    namespace = "com.github.gmazzo.buildconfig.demos.android.lib"
    compileSdk = 36

    buildFeatures.buildConfig = true

    compileOptions {
        targetCompatibility(java.targetCompatibility)
        sourceCompatibility(java.sourceCompatibility)
    }

    publishing {
        multipleVariants {
            allVariants()
            withSourcesJar()
        }
    }
}

androidComponents.onVariants {

    // mimics the variant-aware buildConfigField behavior from Android, by declaring fields on the final variant sourceSet
    buildConfig.sourceSets.named(it.name) {
        className.set("BuildConfig")

        buildConfigField("APP_NAME", project.name)
        buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
        buildConfigField("FEATURE_ENABLED", true)
        buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))

        buildConfigField("BUILD_TYPE", it.buildType)
    }

}

dependencies {
    testImplementation(testFixtures(projects.demoProject.groovy))
}

// workaround of AGP issue failing to pick test sources correctly
tasks.withType<AndroidLintAnalysisTask>().configureEach {
    mustRunAfter(tasks.withType<BuildConfigTask>())
}
tasks.withType<LintModelWriterTask>().configureEach {
    mustRunAfter(tasks.withType<BuildConfigTask>())
}

afterEvaluate {
    publishing.publications.create<MavenPublication>("maven") {
        from(components["default"])
    }
}

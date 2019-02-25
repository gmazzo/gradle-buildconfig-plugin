import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.61"
    id("com.github.gmazzo.buildconfig") version "<local>"
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
    buildConfigField("com.github.gmazzo.SomeData", "MY_DATA", "SomeData(\"a\",1)")
}

sourceSets["test"].withConvention(BuildConfigSourceSet::class) {
    buildConfigField("String", "TEST_CONSTANT", "\"aTestValue\"")
}

/**
 *  A task that iterates over your classpath resources and generate constants for them
 */
task("generateResourcesConstants") {
    doFirst {
        val resources = sourceSets["main"].resources

        resources.files.forEach {
            val path = it.relativeTo(resources.srcDirs.iterator().next()).path
            val name = path.toUpperCase().replace("\\W".toRegex(), "_")

            buildConfig.buildConfigField("java.io.File", "RESOURCE_$name", "File(\"$path\")")
        }
    }

    tasks["generateBuildConfig"].dependsOn(this)
}

dependencies {
    implementation(kotlin("stdlib"))
}

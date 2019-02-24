plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.11"
    id("com.github.gmazzo.buildconfig") version "<local>"
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
}

task("generateResourcesConstants") {
    doFirst {
        val resources = sourceSets["main"].resources

        resources.files.forEach {
            val name = it.name.toUpperCase().replace("\\W".toRegex(), "_")
            val path = it.relativeTo(resources.srcDirs.iterator().next())

            buildConfig.buildConfigField("java.io.File", "RESOURCE_$name", "File(\"$path\")")
        }
    }

    tasks["generateBuildConfig"].dependsOn(this)
}

dependencies {
    implementation(kotlin("stdlib"))
}

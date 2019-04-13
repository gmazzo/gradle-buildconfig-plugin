import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.github.gmazzo.gradle.plugins.tasks.BuildConfigTask
import java.io.FileOutputStream
import java.util.*

plugins {
    kotlin("jvm") version "1.3.21"
    id("com.github.gmazzo.buildconfig") version "<latest>"
}

dependencies {
    implementation(kotlin("stdlib"))
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
val generateBuildConfig by tasks

task("generateResourcesConstants") {
    val buildResources = buildConfig.forClass("BuildResources") {
        buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
    }

    doFirst {
        sourceSets["main"].resources.asFileTree.visit(Action<FileVisitDetails> {
            val name = path.toUpperCase().replace("\\W".toRegex(), "_")

            buildResources.buildConfigField("java.io.File", name, "File(\"$path\")")
        })
    }

    generateBuildConfig.dependsOn(this)
}

// example of a custom language that builds into XML in a new generated resource folder
buildConfig.forClass("properties") {
    buildConfigField("String", "value1", "AAA")
    buildConfigField("String", "value2", "BBB")
    buildConfigField("String", "value3", "CCC")

    val generatePropertiesBuildConfig: BuildConfigTask by tasks
    val newOutputForRes = generatePropertiesBuildConfig.outputDir
        .let { File(it.parentFile, "res${it.name.capitalize()}") }

    language(object : BuildConfigGenerator {

        override fun execute(spec: BuildConfigTaskSpec) {
            newOutputForRes.mkdirs()

            Properties().apply {
                spec.fields.forEach { setProperty(it.name, it.value) }
                storeToXML(FileOutputStream(File(newOutputForRes, "${spec.className}.xml")), null)
            }
        }

    })

    sourceSets["main"].resources.srcDir(newOutputForRes)
    tasks["generateResourcesConstants"].dependsOn(generatePropertiesBuildConfig)
}

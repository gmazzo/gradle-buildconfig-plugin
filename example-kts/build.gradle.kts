import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import java.io.FileOutputStream
import java.util.*

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig") version "<latest>"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

task<Test>("integrationTest") {
    val sourceSet = sourceSets.create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }

    testClassesDirs = sourceSet.output.classesDirs
    classpath = sourceSet.runtimeClasspath

    tasks.named("check") {
        dependsOn(this@task)
    }
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
    buildConfigField("com.github.gmazzo.example_kts.SomeData", "MY_DATA", "SomeData(\"a\",1)")

    sourceSets["test"].buildConfigField("String", "TEST_CONSTANT", "\"aTestValue\"")
    sourceSets["integrationTest"].buildConfigField("String", "INTEGRATION_TEST_CONSTANT", "\"aIntTestValue\"")

    forClass("Versions") {
        useKotlinOutput { topLevelConstants = true }

        buildConfigField("String", "myDependencyVersion", "\"1.0.1\"")
    }
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

// example of a custom generator that builds into XML
buildConfig.forClass("properties") {
    buildConfigField("String", "value1", "AAA")
    buildConfigField("String", "value2", "BBB")
    buildConfigField("String", "value3", "CCC")

    generator(object : BuildConfigGenerator {

        override fun execute(spec: BuildConfigTaskSpec) {
            spec.outputDir.mkdirs()

            Properties().apply {
                spec.fields.forEach { setProperty(it.name, it.value) }
                storeToXML(FileOutputStream(File(spec.outputDir, "${spec.className}.xml")), null)
            }
        }

    })

    sourceSets["main"].resources.srcDir(generateTask)
}

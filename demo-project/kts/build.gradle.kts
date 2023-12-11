import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import java.io.FileOutputStream
import java.util.Properties

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
}

val integrationTest by testing.suites.registering(JvmTestSuite::class) {
    useJUnit()
}

dependencies {
    testImplementation(libs.kotlin.test)
    "integrationTestImplementation"(project)
    "integrationTestImplementation"(libs.kotlin.test)
}

tasks.check {
    dependsOn(integrationTest)
}

buildConfig {
    documentation = "This is a generated BuildConfig class"

    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("String?", "OPTIONAL", "null")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("int[]", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
    buildConfigField("Integer[]", "MAGIC_NUMBERS2", "intArrayOf(1, 2, 3, 4)")
    buildConfigField("kotlin.IntArray", "MAGIC_NUMBERS3", "intArrayOf(9, 10)")
    buildConfigField("com.github.gmazzo.buildconfig.demos.kts.SomeData", "MY_DATA", "SomeData(\"a\",1)")

    sourceSets["test"].buildConfigField("String", "TEST_CONSTANT", "\"aTestValue\"")
    sourceSets["integrationTest"].buildConfigField("String", "INTEGRATION_TEST_CONSTANT", "\"aIntTestValue\"")
}

val versionsSS = buildConfig.sourceSets.register ("Versions") {
    useKotlinOutput { topLevelConstants = true }

    documentation = "My list of versions"
    buildConfigField("String", "myDependencyVersion", "\"1.0.1\"")
}

val buildResources = buildConfig.forClass("BuildResources") {
    buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
}
val generateResourcesConstants by tasks.registering {
    doFirst {
        sourceSets["main"].resources.asFileTree.visit {
            val name = path.uppercase().replace("\\W".toRegex(), "_")

            buildResources.buildConfigField("java.io.File", name, "File(\"$path\")")
        }
    }
}

tasks.generateBuildConfig {
    dependsOn(generateResourcesConstants)
}

// example of a custom generator that builds into XML
val propertiesSS = buildConfig.sourceSets.register("properties") {
    buildConfigField("String", "value1", "AAA")
    buildConfigField("String", "value2", "BBB")
    buildConfigField("String", "value3", "CCC")

    generator(object : BuildConfigGenerator {

        override fun execute(spec: BuildConfigGeneratorSpec) {
            spec.outputDir.mkdirs()

            Properties().also { props ->
                spec.fields.forEach { props.setProperty(it.name, (it.value.get() as BuildConfigField.Expression).value) }
                props.storeToXML(FileOutputStream(File(spec.outputDir, "${spec.className}.xml")), null)
            }
        }

    })
}

sourceSets.main {
    kotlin.srcDir(versionsSS)
    resources.srcDir(propertiesSS)
}

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import java.io.FileOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

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

    buildConfigField("APP_NAME", project.name)
    buildConfigField<String>("APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField<String>("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
    buildConfigField<String>("OPTIONAL", null)
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("FEATURE_ENABLED", true)
    buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))
    buildConfigField("MAGIC_NUMBERS2", arrayOf(1, 2, null, 4))
    buildConfigField("MAGIC_NUMBERS3", listOf(1, 2, null, 4))
    buildConfigField("MAGIC_NUMBERS4", setOf(1, 2, null, 4))
    buildConfigField(typeOf("kotlin.IntArray"), "MAGIC_NUMBERS5", expression("intArrayOf(9, 10)"))
    buildConfigField<Map<String, Int>>("MAPPINGS", expression("mapOf(\"a\" to 1, \"b\" to 2)"))
    buildConfigField<Map<String, Int>>("PROVIDED_MAPPINGS", provider { expression("mapOf(\"a\" to 1, \"b\" to 2)") })
    buildConfigField(
        typeOf("com.github.gmazzo.buildconfig.demos.kts.SomeData"),
        "MY_DATA",
        expression("SomeData(\"a\",1)")
    )
    buildConfigField(
        typeOf("com.github.gmazzo.buildconfig.demos.kts.SomeData"),
        "MY_DATA2",
        expression("SomeData(\"a\",1)")
    )

    sourceSets["test"].buildConfigField( "TEST_CONSTANT", "aTestValue")
    sourceSets["integrationTest"].buildConfigField("INTEGRATION_TEST_CONSTANT", "aIntTestValue")
}

val versionsSS = buildConfig.sourceSets.register("Versions") {
    useKotlinOutput { topLevelConstants = true }

    documentation = "My list of versions"
    buildConfigField("myDependencyVersion", "1.0.1")
}

val buildResources = buildConfig.forClass("BuildResources") {
    buildConfigField("A_CONSTANT", "aConstant")
}
val generateResourcesConstants by tasks.registering {
    doFirst {
        sourceSets["main"].resources.asFileTree.visit {
            val name = path.uppercase().replace("\\W".toRegex(), "_")

            with(buildResources) {
                buildConfigField(File::class, name, expression("File(\"$path\")"))
            }
        }
    }
}

tasks.generateBuildConfig {
    dependsOn(generateResourcesConstants)
}

// example of a custom generator that builds into XML
val propertiesSS = buildConfig.sourceSets.register("properties") {
    buildConfigField("value1", "AAA")
    buildConfigField("value2", "BBB")
    buildConfigField("value3", "CCC")

    generator(object : BuildConfigGenerator {

        override fun execute(spec: BuildConfigGeneratorSpec) {
            spec.outputDir.mkdirs()

            Properties().also { props ->
                spec.fields.forEach {
                    props.setProperty(
                        it.name,
                        (it.value.get() as BuildConfigField.Literal).value.toString()
                    )
                }
                props.storeToXML(FileOutputStream(File(spec.outputDir, "${spec.className}.xml")), null)
            }
        }

    })
}

sourceSets.main {
    kotlin.srcDir(versionsSS)
    resources.srcDir(propertiesSS)
}

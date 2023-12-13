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

    // all possible kind for String
    buildConfigField("STRING", "aString")
    buildConfigField<String>("STRING_NULL", null)
    buildConfigField("STRING_PROVIDER", provider { "aString" })
    buildConfigField("STRING_ARRAY", arrayOf("a", "b", "c"))
    buildConfigField("STRING_ARRAY_PROVIDER", provider { arrayOf("a", "b", "c") })
    buildConfigField("STRING_ARRAY_NULLABLE", arrayOf("a", null, "c"))
    buildConfigField("STRING_ARRAY_NULLABLE_PROVIDER", provider { arrayOf("a", null, "c") })
    buildConfigField("STRING_LIST", listOf("a", "b", "c"))
    buildConfigField("STRING_LIST_PROVIDER", provider { listOf("a", "b", "c") })
    buildConfigField("STRING_SET", setOf("a", "b", "c"))
    buildConfigField("STRING_SET_PROVIDER", provider { setOf("a", "b", "c") })

    // all possible kind for Byte
    buildConfigField("BYTE", 128.toByte())
    buildConfigField<Byte>("BYTE_NULL", null)
    buildConfigField("BYTE_PROVIDER", provider { 128.toByte() })
    buildConfigField("BYTE_NATIVE_ARRAY", byteArrayOf(1, 2, 3))
    buildConfigField("BYTE_NATIVE_ARRAY_PROVIDER", provider { byteArrayOf(1, 2, 3) })
    buildConfigField("BYTE_ARRAY", arrayOf(1.toByte(), 2.toByte(), 3.toByte()))
    buildConfigField("BYTE_ARRAY_PROVIDER", provider { arrayOf(1.toByte(), 2.toByte(), 3.toByte()) })
    buildConfigField("BYTE_ARRAY_NULLABLE", arrayOf(1.toByte(), null, 3.toByte()))
    buildConfigField("BYTE_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1.toByte(), null, 3.toByte()) })
    buildConfigField("BYTE_LIST", listOf(1.toByte(), null, 3.toByte()))
    buildConfigField("BYTE_LIST_PROVIDER", provider { listOf(1.toByte(), null, 3.toByte()) })
    buildConfigField("BYTE_SET", setOf(1.toByte(), null, 3.toByte()))
    buildConfigField("BYTE_SET_PROVIDER", provider { setOf(1.toByte(), null, 3.toByte()) })

    // all possible kind for Short
    buildConfigField("SHORT", 128.toShort())
    buildConfigField<Short>("SHORT_NULL", null)
    buildConfigField("SHORT_PROVIDER", provider { 128.toShort() })
    buildConfigField("SHORT_NATIVE_ARRAY", shortArrayOf(1, 2, 3))
    buildConfigField("SHORT_NATIVE_ARRAY_PROVIDER", provider { shortArrayOf(1, 2, 3) })
    buildConfigField("SHORT_ARRAY", arrayOf(1.toShort(), 2.toShort(), 3.toShort()))
    buildConfigField("SHORT_ARRAY_PROVIDER", provider { arrayOf(1.toShort(), 2.toShort(), 3.toShort()) })
    buildConfigField("SHORT_ARRAY_NULLABLE", arrayOf(1.toShort(), null, 3.toShort()))
    buildConfigField("SHORT_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1.toShort(), null, 3.toShort()) })
    buildConfigField("SHORT_LIST", listOf(1.toShort(), null, 3.toShort()))
    buildConfigField("SHORT_LIST_PROVIDER", provider { listOf(1.toShort(), null, 3.toShort()) })
    buildConfigField("SHORT_SET", setOf(1.toShort(), null, 3.toShort()))
    buildConfigField("SHORT_SET_PROVIDER", provider { setOf(1.toShort(), null, 3.toShort()) })

    // all possible kind for Char
    buildConfigField("CHAR", 'a')
    buildConfigField<Char>("CHAR_NULL", null)
    buildConfigField("CHAR_PROVIDER", provider { 'a' })
    buildConfigField("CHAR_NATIVE_ARRAY", charArrayOf('a', 'b', 'c'))
    buildConfigField("CHAR_NATIVE_ARRAY_PROVIDER", provider { charArrayOf('a', 'b', 'c') })
    buildConfigField("CHAR_ARRAY", arrayOf('a', 'b', 'c'))
    buildConfigField("CHAR_ARRAY_PROVIDER", provider { arrayOf('a', 'b', 'c') })
    buildConfigField("CHAR_ARRAY_NULLABLE", arrayOf('a', null, 'c'))
    buildConfigField("CHAR_ARRAY_NULLABLE_PROVIDER", provider { arrayOf('a', null, 'c') })
    buildConfigField("CHAR_LIST", listOf('a', null, 'c'))
    buildConfigField("CHAR_LIST_PROVIDER", provider { listOf('a', null, 'c') })
    buildConfigField("CHAR_SET", setOf('a', null, 'c'))
    buildConfigField("CHAR_SET_PROVIDER", provider { setOf('a', null, 'c') })

    // all possible kind for Int
    buildConfigField("INT", 1)
    buildConfigField<Int>("INT_NULL", null)
    buildConfigField("INT_PROVIDER", provider { 1 })
    buildConfigField("INT_NATIVE_ARRAY", intArrayOf(1, 2, 3))
    buildConfigField("INT_NATIVE_ARRAY_PROVIDER", provider { intArrayOf(1, 2, 3) })
    buildConfigField("INT_ARRAY", arrayOf(1, 2, 3))
    buildConfigField("INT_ARRAY_PROVIDER", provider { arrayOf(1, 2, 3) })
    buildConfigField("INT_ARRAY_NULLABLE", arrayOf(1, null, 3))
    buildConfigField("INT_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1, null, 3) })
    buildConfigField("INT_LIST", listOf(1, null, 3))
    buildConfigField("INT_LIST_PROVIDER", provider { listOf(1, null, 3) })
    buildConfigField("INT_SET", setOf(1, null, 3))
    buildConfigField("INT_SET_PROVIDER", provider { setOf(1, null, 3) })

    // all possible kind for Long
    buildConfigField("LONG", 1L)
    buildConfigField<Long>("LONG_NULL", null)
    buildConfigField("LONG_PROVIDER", provider { 1L })
    buildConfigField("LONG_NATIVE_ARRAY", longArrayOf(1L, 2L, 3L))
    buildConfigField("LONG_NATIVE_ARRAY_PROVIDER", provider { longArrayOf(1L, 2L, 3L) })
    buildConfigField("LONG_ARRAY", arrayOf(1L, 2L, 3L))
    buildConfigField("LONG_ARRAY_PROVIDER", provider { arrayOf(1L, 2L, 3L) })
    buildConfigField("LONG_ARRAY_NULLABLE", arrayOf(1L, null, 3L))
    buildConfigField("LONG_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1L, null, 3L) })
    buildConfigField("LONG_LIST", listOf(1L, null, 3L))
    buildConfigField("LONG_LIST_PROVIDER", provider { listOf(1L, null, 3L) })
    buildConfigField("LONG_SET", setOf(1L, null, 3L))
    buildConfigField("LONG_SET_PROVIDER", provider { setOf(1L, null, 3L) })

    // all possible kind for Float
    buildConfigField("FLOAT", 1f)
    buildConfigField<Float>("FLOAT_NULL", null)
    buildConfigField("FLOAT_PROVIDER", provider { 1f })
    buildConfigField("FLOAT_NATIVE_ARRAY", floatArrayOf(1f, 2f, 3f))
    buildConfigField("FLOAT_NATIVE_ARRAY_PROVIDER", provider { floatArrayOf(1f, 2f, 3f) })
    buildConfigField("FLOAT_ARRAY", arrayOf(1f, 2f, 3f))
    buildConfigField("FLOAT_ARRAY_PROVIDER", provider { arrayOf(1f, 2f, 3f) })
    buildConfigField("FLOAT_ARRAY_NULLABLE", arrayOf(1f, null, 3f))
    buildConfigField("FLOAT_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1f, null, 3f) })
    buildConfigField("FLOAT_LIST", listOf(1f, null, 3f))
    buildConfigField("FLOAT_LIST_PROVIDER", provider { listOf(1f, null, 3f) })
    buildConfigField("FLOAT_SET", setOf(1f, null, 3f))
    buildConfigField("FLOAT_SET_PROVIDER", provider { setOf(1f, null, 3f) })

    // all possible kind for Double
    buildConfigField("DOUBLE", 1.0)
    buildConfigField<Double>("DOUBLE_NULL", null)
    buildConfigField("DOUBLE_PROVIDER", provider { 1.0 })
    buildConfigField("DOUBLE_NATIVE_ARRAY", doubleArrayOf(1.0, 2.0, 3.0))
    buildConfigField("DOUBLE_NATIVE_ARRAY_PROVIDER", provider { doubleArrayOf(1.0, 2.0, 3.0) })
    buildConfigField("DOUBLE_ARRAY", arrayOf(1.0, 2.0, 3.0))
    buildConfigField("DOUBLE_ARRAY_PROVIDER", provider { arrayOf(1.0, 2.0, 3.0) })
    buildConfigField("DOUBLE_ARRAY_NULLABLE", arrayOf(1.0, null, 3.0))
    buildConfigField("DOUBLE_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1.0, null, 3.0) })
    buildConfigField("DOUBLE_LIST", listOf(1.0, null, 3.0))
    buildConfigField("DOUBLE_LIST_PROVIDER", provider { listOf(1.0, null, 3.0) })
    buildConfigField("DOUBLE_SET", setOf(1.0, null, 3.0))
    buildConfigField("DOUBLE_SET_PROVIDER", provider { setOf(1.0, null, 3.0) })

    // all possible kind for Boolean
    buildConfigField("BOOLEAN", true)
    buildConfigField<Boolean>("BOOLEAN_NULL", null)
    buildConfigField("BOOLEAN_PROVIDER", provider { true })
    buildConfigField("BOOLEAN_NATIVE_ARRAY", booleanArrayOf(true, false, false))
    buildConfigField("BOOLEAN_NATIVE_ARRAY_PROVIDER", provider { booleanArrayOf(true, false, false) })
    buildConfigField("BOOLEAN_ARRAY", arrayOf(true, false, false))
    buildConfigField("BOOLEAN_ARRAY_PROVIDER", provider { arrayOf(true, false, false) })
    buildConfigField("BOOLEAN_ARRAY_NULLABLE", arrayOf(true, null, false))
    buildConfigField("BOOLEAN_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(true, null, false) })
    buildConfigField("BOOLEAN_LIST", listOf(true, null, false))
    buildConfigField("BOOLEAN_LIST_PROVIDER", provider { listOf(true, null, false) })
    buildConfigField("BOOLEAN_SET", setOf(true, null, false))
    buildConfigField("BOOLEAN_SET_PROVIDER", provider { setOf(true, null, false) })

    // custom formats with expressions, including Map and custom types
    buildConfigField(
        "kotlin.collections.Map<String, Int>",
        "MAP",
        "mapOf(\"a\" to 1, \"b\" to 2)"
    )
    buildConfigField(
        "kotlin.collections.Map<String, Int>",
        "MAP_PROVIDER",
        provider { "mapOf(\"a\" to 1, \"b\" to 2)" }
    )
    buildConfigField(
        "com.github.gmazzo.buildconfig.demos.kts.SomeData",
        "MY_DATA",
        "SomeData(\"a\", 1)"
    )
    buildConfigField(
        "com.github.gmazzo.buildconfig.demos.kts.SomeData",
        "MY_DATA2",
        "SomeData(\"a\", 1)"
    )

    sourceSets["test"].buildConfigField("TEST_CONSTANT", "aTestValue")
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
                buildConfigField("java.io.File", name, "File(\"$path\")")
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

import com.github.gmazzo.buildconfig.BuildConfigValue
import com.github.gmazzo.buildconfig.generators.BuildConfigGenerator
import com.github.gmazzo.buildconfig.generators.BuildConfigGeneratorSpec
import java.io.FileOutputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

val integrationTest by testing.suites.registering(JvmTestSuite::class) {
    useJUnit()
}

dependencies {
    ksp(libs.autoservice.ksp)
    compileOnly(libs.autoservice)
    testImplementation(testFixtures(projects.demoProject.groovy))
    "integrationTestImplementation"(project)
    "integrationTestImplementation"(platform(libs.junit5.bom))
    "integrationTestImplementation"(libs.junit5.params)
    "integrationTestRuntimeOnly"(libs.junit5.engine)
    "integrationTestRuntimeOnly"(libs.junit5.platformLauncher)
}

tasks.check {
    dependsOn(integrationTest)
}

buildConfig {
    documentation = "This is a generated BuildConfig class"

    buildConfigField("APP_NAME", project.name)
    buildConfigField("APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
    buildConfigField<String?>("OPTIONAL", null)
    buildConfigField("FEATURE_ENABLED", true)
    buildConfigField("MAGIC_NUMBERS", listOf(1, 2, 3))

    // all possible kinds for String
    buildConfigField("STRING", "aString")
    buildConfigField("STRING_NULL", null as String?)
    buildConfigField("STRING_PROVIDER", provider { "aString" })
    buildConfigField("STRING_ARRAY", arrayOf("a", "b", "c"))
    buildConfigField("STRING_ARRAY_PROVIDER", provider { arrayOf("a", "b", "c") })
    buildConfigField("STRING_ARRAY_NULLABLE", arrayOf("a", null, "c"))
    buildConfigField("STRING_ARRAY_NULLABLE_PROVIDER", provider { arrayOf("a", null, "c") })
    buildConfigField("STRING_ARRAY_NULL", null as Array<String>?)
    buildConfigField("STRING_LIST", listOf("a", null, "c"))
    buildConfigField("STRING_LIST_PROVIDER", provider { listOf("a", null, "c") })
    buildConfigField("STRING_SET", setOf("a", null, "c"))
    buildConfigField("STRING_SET_PROVIDER", provider { setOf("a", null, "c") })

    // all possible kinds for Byte
    buildConfigField("BYTE", 64.toByte())
    buildConfigField("BYTE_NULL", null as Byte?)
    buildConfigField("BYTE_PROVIDER", provider { 64.toByte() })
    buildConfigField("BYTE_NATIVE_ARRAY", byteArrayOf(1, 2, 3))
    buildConfigField("BYTE_NATIVE_ARRAY_PROVIDER", provider { byteArrayOf(1, 2, 3) })
    buildConfigField("BYTE_ARRAY", arrayOf(1.toByte(), 2.toByte(), 3.toByte()))
    buildConfigField("BYTE_ARRAY_PROVIDER", provider { arrayOf(1.toByte(), 2.toByte(), 3.toByte()) })
    buildConfigField("BYTE_ARRAY_NULLABLE", arrayOf(1.toByte(), null, 3.toByte()))
    buildConfigField("BYTE_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1.toByte(), null, 3.toByte()) })
    buildConfigField("BYTE_ARRAY_NULL", null as ByteArray?)
    buildConfigField("BYTE_LIST", listOf(1.toByte(), null, 3.toByte()))
    buildConfigField("BYTE_LIST_PROVIDER", provider { listOf(1.toByte(), null, 3.toByte()) })
    buildConfigField("BYTE_SET", setOf(1.toByte(), null, 3.toByte()))
    buildConfigField("BYTE_SET_PROVIDER", provider { setOf(1.toByte(), null, 3.toByte()) })

    // all possible kinds for Short
    buildConfigField("SHORT", 64.toShort())
    buildConfigField("SHORT_NULL", null as Short?)
    buildConfigField("SHORT_PROVIDER", provider { 64.toShort() })
    buildConfigField("SHORT_NATIVE_ARRAY", shortArrayOf(1, 2, 3))
    buildConfigField("SHORT_NATIVE_ARRAY_PROVIDER", provider { shortArrayOf(1, 2, 3) })
    buildConfigField("SHORT_ARRAY", arrayOf(1.toShort(), 2.toShort(), 3.toShort()))
    buildConfigField("SHORT_ARRAY_PROVIDER", provider { arrayOf(1.toShort(), 2.toShort(), 3.toShort()) })
    buildConfigField("SHORT_ARRAY_NULLABLE", arrayOf(1.toShort(), null, 3.toShort()))
    buildConfigField("SHORT_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1.toShort(), null, 3.toShort()) })
    buildConfigField("SHORT_ARRAY_NULL", null as ShortArray?)
    buildConfigField("SHORT_LIST", listOf(1.toShort(), null, 3.toShort()))
    buildConfigField("SHORT_LIST_PROVIDER", provider { listOf(1.toShort(), null, 3.toShort()) })
    buildConfigField("SHORT_SET", setOf(1.toShort(), null, 3.toShort()))
    buildConfigField("SHORT_SET_PROVIDER", provider { setOf(1.toShort(), null, 3.toShort()) })

    // all possible kinds for Char
    buildConfigField("CHAR", 'a')
    buildConfigField("CHAR_NULL", null as Char?)
    buildConfigField("CHAR_PROVIDER", provider { 'a' })
    buildConfigField("CHAR_NATIVE_ARRAY", charArrayOf('a', 'b', 'c'))
    buildConfigField("CHAR_NATIVE_ARRAY_PROVIDER", provider { charArrayOf('a', 'b', 'c') })
    buildConfigField("CHAR_ARRAY", arrayOf('a', 'b', 'c'))
    buildConfigField("CHAR_ARRAY_PROVIDER", provider { arrayOf('a', 'b', 'c') })
    buildConfigField("CHAR_ARRAY_NULLABLE", arrayOf('a', null, 'c'))
    buildConfigField("CHAR_ARRAY_NULLABLE_PROVIDER", provider { arrayOf('a', null, 'c') })
    buildConfigField("CHAR_ARRAY_NULL", null as CharArray?)
    buildConfigField("CHAR_LIST", listOf('a', null, 'c'))
    buildConfigField("CHAR_LIST_PROVIDER", provider { listOf('a', null, 'c') })
    buildConfigField("CHAR_SET", setOf('a', null, 'c'))
    buildConfigField("CHAR_SET_PROVIDER", provider { setOf('a', null, 'c') })

    // all possible kinds for Int
    buildConfigField("INT", 1)
    buildConfigField("INT_NULL", null as Int?)
    buildConfigField("INT_PROVIDER", provider { 1 })
    buildConfigField("INT_NATIVE_ARRAY", intArrayOf(1, 2, 3))
    buildConfigField("INT_NATIVE_ARRAY_PROVIDER", provider { intArrayOf(1, 2, 3) })
    buildConfigField("INT_ARRAY", arrayOf(1, 2, 3))
    buildConfigField("INT_ARRAY_PROVIDER", provider { arrayOf(1, 2, 3) })
    buildConfigField("INT_ARRAY_NULLABLE", arrayOf(1, null, 3))
    buildConfigField("INT_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1, null, 3) })
    buildConfigField("INT_ARRAY_NULL", null as IntArray?)
    buildConfigField("INT_LIST", listOf(1, null, 3))
    buildConfigField("INT_LIST_PROVIDER", provider { listOf(1, null, 3) })
    buildConfigField("INT_SET", setOf(1, null, 3))
    buildConfigField("INT_SET_PROVIDER", provider { setOf(1, null, 3) })

    // all possible kinds for Long
    buildConfigField("LONG", 1L)
    buildConfigField("LONG_NULL", null as Long?)
    buildConfigField("LONG_PROVIDER", provider { 1L })
    buildConfigField("LONG_NATIVE_ARRAY", longArrayOf(1L, 2L, 3L))
    buildConfigField("LONG_NATIVE_ARRAY_PROVIDER", provider { longArrayOf(1L, 2L, 3L) })
    buildConfigField("LONG_ARRAY", arrayOf(1L, 2L, 3L))
    buildConfigField("LONG_ARRAY_PROVIDER", provider { arrayOf(1L, 2L, 3L) })
    buildConfigField("LONG_ARRAY_NULLABLE", arrayOf(1L, null, 3L))
    buildConfigField("LONG_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1L, null, 3L) })
    buildConfigField("LONG_ARRAY_NULL", null as LongArray?)
    buildConfigField("LONG_LIST", listOf(1L, null, 3L))
    buildConfigField("LONG_LIST_PROVIDER", provider { listOf(1L, null, 3L) })
    buildConfigField("LONG_SET", setOf(1L, null, 3L))
    buildConfigField("LONG_SET_PROVIDER", provider { setOf(1L, null, 3L) })

    // all possible kinds for Float
    buildConfigField("FLOAT", 1f)
    buildConfigField("FLOAT_NULL", null as Float?)
    buildConfigField("FLOAT_PROVIDER", provider { 1f })
    buildConfigField("FLOAT_NATIVE_ARRAY", floatArrayOf(1f, 2f, 3f))
    buildConfigField("FLOAT_NATIVE_ARRAY_PROVIDER", provider { floatArrayOf(1f, 2f, 3f) })
    buildConfigField("FLOAT_ARRAY", arrayOf(1f, 2f, 3f))
    buildConfigField("FLOAT_ARRAY_PROVIDER", provider { arrayOf(1f, 2f, 3f) })
    buildConfigField("FLOAT_ARRAY_NULLABLE", arrayOf(1f, null, 3f))
    buildConfigField("FLOAT_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1f, null, 3f) })
    buildConfigField("FLOAT_ARRAY_NULL", null as FloatArray?)
    buildConfigField("FLOAT_LIST", listOf(1f, null, 3f))
    buildConfigField("FLOAT_LIST_PROVIDER", provider { listOf(1f, null, 3f) })
    buildConfigField("FLOAT_SET", setOf(1f, null, 3f))
    buildConfigField("FLOAT_SET_PROVIDER", provider { setOf(1f, null, 3f) })

    // all possible kinds for Double
    buildConfigField("DOUBLE", 1.0)
    buildConfigField("DOUBLE_NULL", null as Double?)
    buildConfigField("DOUBLE_PROVIDER", provider { 1.0 })
    buildConfigField("DOUBLE_NATIVE_ARRAY", doubleArrayOf(1.0, 2.0, 3.0))
    buildConfigField("DOUBLE_NATIVE_ARRAY_PROVIDER", provider { doubleArrayOf(1.0, 2.0, 3.0) })
    buildConfigField("DOUBLE_ARRAY", arrayOf(1.0, 2.0, 3.0))
    buildConfigField("DOUBLE_ARRAY_PROVIDER", provider { arrayOf(1.0, 2.0, 3.0) })
    buildConfigField("DOUBLE_ARRAY_NULLABLE", arrayOf(1.0, null, 3.0))
    buildConfigField("DOUBLE_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(1.0, null, 3.0) })
    buildConfigField("DOUBLE_ARRAY_NULL", null as DoubleArray?)
    buildConfigField("DOUBLE_LIST", listOf(1.0, null, 3.0))
    buildConfigField("DOUBLE_LIST_PROVIDER", provider { listOf(1.0, null, 3.0) })
    buildConfigField("DOUBLE_SET", setOf(1.0, null, 3.0))
    buildConfigField("DOUBLE_SET_PROVIDER", provider { setOf(1.0, null, 3.0) })

    // all possible kinds for Boolean
    buildConfigField("BOOLEAN", true)
    buildConfigField("BOOLEAN_NULL", null as Boolean?)
    buildConfigField("BOOLEAN_PROVIDER", provider { true })
    buildConfigField("BOOLEAN_NATIVE_ARRAY", booleanArrayOf(true, false, false))
    buildConfigField("BOOLEAN_NATIVE_ARRAY_PROVIDER", provider { booleanArrayOf(true, false, false) })
    buildConfigField("BOOLEAN_ARRAY", arrayOf(true, false, false))
    buildConfigField("BOOLEAN_ARRAY_PROVIDER", provider { arrayOf(true, false, false) })
    buildConfigField("BOOLEAN_ARRAY_NULLABLE", arrayOf(true, null, false))
    buildConfigField("BOOLEAN_ARRAY_NULLABLE_PROVIDER", provider { arrayOf(true, null, false) })
    buildConfigField("BOOLEAN_ARRAY_NULL", null as BooleanArray?)
    buildConfigField("BOOLEAN_LIST", listOf(true, null, false))
    buildConfigField("BOOLEAN_LIST_PROVIDER", provider { listOf(true, null, false) })
    buildConfigField("BOOLEAN_SET", setOf(true, null, false))
    buildConfigField("BOOLEAN_SET_PROVIDER", provider { setOf(true, null, false) })

    // custom formats with expressions, including Map and custom types
    buildConfigField("MAP", mapOf("a" to 1, "b" to 2))
    buildConfigField("MAP_PROVIDER", provider { mapOf("a" to 1, "b" to 2) })
    buildConfigField<Map<String, Int>>("MAP_BY_EXPRESSION", expression("mapOf(\"a\" to 1, \"b\" to 2)"))
    buildConfigField<Map<String, Int>>(
        "MAP_BY_EXPRESSION_PROVIDER",
        provider { expression("mapOf(\"a\" to 1, \"b\" to 2)") })
    buildConfigField<Map<*, *>>("MAP_GENERIC", expression("mapOf(\"a\" to 1, \"b\" to 2)"))
    buildConfigField<Map<*, *>>("MAP_GENERIC_PROVIDER", provider { expression("mapOf(\"a\" to 1, \"b\" to 2)") })
    buildConfigField("FILE", File("aFile"))
    buildConfigField("FILE_PROVIDER", provider { File("aFile") })
    buildConfigField("URI", uri("https://example.io"))
    buildConfigField("URI_PROVIDER", provider { uri("https://example.io") })
    buildConfigField(
        "com.github.gmazzo.buildconfig.demos.kts.SomeData",
        "DATA",
        "SomeData(\"a\", 1)"
    )
    buildConfigField(
        "com.github.gmazzo.buildconfig.demos.kts.SomeData",
        "DATA_PROVIDER",
        provider { "SomeData(\"a\", 1)" }
    )

    buildConfigField("VERY_LONG_LIST", (1..20).toList())
    buildConfigField("VERY_LONG_SET", (1..20).toSet())
    buildConfigField("VERY_LONG_MAP", (1..20).associateWith { it.toString() })
}

sourceSets.test { buildConfig.buildConfigField("TEST_CONSTANT", "aTestValue") }
kotlin.sourceSets.test {
    buildConfig.buildConfigField("TEST_CONSTANT2", "anotherValue")
    buildConfig {
        buildConfigField("TEST_CONSTANT3", "anotherValue")
    }
}

sourceSets["integrationTest"].buildConfig {
    buildConfigField("INTEGRATION_TEST_CONSTANT", "aIntTestValue")
}
kotlin.sourceSets["integrationTest"].buildConfig {
    buildConfigField("INTEGRATION_TEST_CONSTANT2", "anotherIntTestValue")
}

val versionsSS = buildConfig.sourceSets.register("Versions") {
    useKotlinOutput { topLevelConstants = true }

    documentation = "My list of versions"
    buildConfigField("myDependencyVersion", "1.0.1")
}

// Example: Custom generator that builds into XML
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
                        (it.value.get() as BuildConfigValue.Literal).value.toString()
                    )
                }
                props.storeToXML(FileOutputStream(File(spec.outputDir, "${spec.className}.xml")), null)
            }
        }

    })
}

// Example: Generate constants from resources files
buildConfig.forClass("BuildResources") {
    useKotlinOutput { internalVisibility = false }
    buildConfigField("A_CONSTANT", "aConstant")

    files(sourceSets["main"].resources.srcDirs).asFileTree.visit {
        if (!isDirectory) {
            val name = path.uppercase().replace("\\W".toRegex(), "_")

            buildConfigField(name, File(path))
        }
    }
}

sourceSets.main {
    kotlin.srcDir(versionsSS)
    resources.srcDir(propertiesSS)
}

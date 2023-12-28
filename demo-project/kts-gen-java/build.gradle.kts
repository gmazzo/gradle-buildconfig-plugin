plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

dependencies {
    implementation(projects.demoProject.kts)
    testImplementation(libs.kotlin.test)
    testImplementation(testFixtures(projects.demoProject.groovy))
}

buildConfig {
    useJavaOutput()
    documentation = "This is a generated BuildConfig class"

    buildConfigField("APP_NAME", project.name)
    buildConfigField("APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
    buildConfigField<String>("OPTIONAL", null)
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("FEATURE_ENABLED", true)
    buildConfigField("MAGIC_NUMBERS", listOf(1, 2, 3))

    // all possible kind for String
    buildConfigField("STRING", "aString")
    buildConfigField("STRING_NULL", null as String?)
    buildConfigField("STRING_PROVIDER", provider { "aString" })
    buildConfigField("STRING_ARRAY", arrayOf("a", "b", "c"))
    buildConfigField("STRING_ARRAY_PROVIDER", provider { arrayOf("a", "b", "c") })
    buildConfigField("STRING_ARRAY_NULLABLE", arrayOf("a", null, "c"))
    buildConfigField("STRING_ARRAY_NULLABLE_PROVIDER", provider { arrayOf("a", null, "c") })
    buildConfigField("STRING_LIST", listOf("a", null, "c"))
    buildConfigField("STRING_LIST_PROVIDER", provider { listOf("a", null, "c") })
    buildConfigField("STRING_SET", setOf("a", null, "c"))
    buildConfigField("STRING_SET_PROVIDER", provider { setOf("a", null, "c") })

    // all possible kind for Byte
    buildConfigField("BYTE", 64.toByte())
    buildConfigField("BYTE_NULL", null as Byte?)
    buildConfigField("BYTE_PROVIDER", provider { 64.toByte() })
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
    buildConfigField("SHORT", 64.toShort())
    buildConfigField("SHORT_NULL", null as Short?)
    buildConfigField("SHORT_PROVIDER", provider { 64.toShort() })
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
    buildConfigField("CHAR_NULL", null as Char?)
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
    buildConfigField("INT_NULL", null as Int?)
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
    buildConfigField("LONG_NULL", null as Long?)
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
    buildConfigField("FLOAT_NULL", null as Float?)
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
    buildConfigField("DOUBLE_NULL", null as Double?)
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
    buildConfigField("BOOLEAN_NULL", null as Boolean?)
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
    buildConfigField<Map<String, Int>>("MAP", expression("java.util.Map.of(\"a\", 1, \"b\", 2)"))
    buildConfigField<Map<String, Int>>("MAP_PROVIDER", provider { expression("java.util.Map.of(\"a\", 1, \"b\", 2)") })
    buildConfigField<Map<*, *>>("MAP_GENERIC", expression("java.util.Map.of(\"a\", 1, \"b\", 2)"))
    buildConfigField<Map<*, *>>("MAP_GENERIC_PROVIDER", provider { expression("java.util.Map.of(\"a\", 1, \"b\", 2)") })
    buildConfigField(
        "com.github.gmazzo.buildconfig.demos.kts.SomeData",
        "DATA",
        "new SomeData(\"a\", 1)"
    )
    buildConfigField(
        "com.github.gmazzo.buildconfig.demos.kts.SomeData",
        "DATA_PROVIDER",
        provider { "new SomeData(\"a\", 1)" }
    )
}

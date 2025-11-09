package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.internal.javaIdentifier
import com.github.gmazzo.buildconfig.internal.nameOf
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.FieldSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildConfigTypeUtilsTests {

    private val string = BuildConfigType("String")
    private val int = BuildConfigType("Int")
    private val mapStringInt = BuildConfigType("java.util.Map", listOf(string, int))
    private val mapStringListOfInt =
        BuildConfigType("java.util.Map", listOf(string, BuildConfigType("List", listOf(int.copy(nullable = true)))))

    @Suppress("unused")
    private val byJavaClassCases = listOf(
        arrayOf(string, String::class.java),
        arrayOf(string.copy(array = true), Array<String>::class.java),

        arrayOf(int, Int::class.java),
        arrayOf(int.copy(array = true), Array<Int>::class.java),
        arrayOf(int.copy(array = true), Array<Int?>::class.java),
        arrayOf(int.copy(array = true), IntArray::class.java),
    )

    @ParameterizedTest
    @FieldSource("byJavaClassCases")
    fun testNameOfByJavaClass(expected: BuildConfigType, input: Class<*>) {
        assertEquals(expected, nameOf(input))
    }

    @Suppress("unused")
    private val byKTypeCases = listOf(
        arrayOf(string, typeOf<String>()),
        arrayOf(string.copy(nullable = true), typeOf<String?>()),
        arrayOf(string.copy(array = true), typeOf<Array<String>>()),
        arrayOf(string.copy(nullable = true, array = true), typeOf<Array<String?>>()),
        arrayOf(string.copy(array = true, arrayNullable = true), typeOf<Array<String>?>()),
        arrayOf(string.copy(nullable = true, array = true, arrayNullable = true), typeOf<Array<String?>?>()),

        arrayOf(int, typeOf<Int>()),
        arrayOf(int.copy(nullable = true), typeOf<Int?>()),
        arrayOf(int.copy(array = true), typeOf<Array<Int>>()),
        arrayOf(int.copy(array = true), typeOf<IntArray>()),
        arrayOf(int.copy(nullable = true, array = true), typeOf<Array<Int?>>()),
        arrayOf(int.copy(array = true, arrayNullable = true), typeOf<Array<Int>?>()),
        arrayOf(int.copy(nullable = true, array = true, arrayNullable = true), typeOf<Array<Int?>?>()),

        arrayOf(mapStringInt, typeOf<Map<String, Int>>()),
        arrayOf(mapStringInt.copy(nullable = true), typeOf<Map<String, Int>?>()),
        arrayOf(mapStringInt.copy(array = true), typeOf<Array<Map<String, Int>>>()),
        arrayOf(mapStringInt.copy(nullable = true, array = true), typeOf<Array<Map<String, Int>?>>()),
        arrayOf(mapStringInt.copy(array = true, arrayNullable = true), typeOf<Array<Map<String, Int>>?>()),
        arrayOf(mapStringInt.copy(nullable = true, array = true, arrayNullable = true), typeOf<Array<Map<String, Int>?>?>()),

        arrayOf(mapStringListOfInt, typeOf<Map<String, List<Int?>>>()),
        arrayOf(mapStringListOfInt.copy(nullable = true), typeOf<Map<String, List<Int?>>?>()),
        arrayOf(mapStringListOfInt.copy(array = true), typeOf<Array<Map<String, List<Int?>>>>()),
        arrayOf(mapStringListOfInt.copy(nullable = true, array = true), typeOf<Array<Map<String, List<Int?>>?>>()),
        arrayOf(mapStringListOfInt.copy(array = true, arrayNullable = true), typeOf<Array<Map<String, List<Int?>>>?>()),
        arrayOf(mapStringListOfInt.copy(nullable = true, array = true, arrayNullable = true), typeOf<Array<Map<String, List<Int?>>?>?>()),
    )

    @ParameterizedTest
    @FieldSource("byKTypeCases")
    fun testNameOfByKType(expected: BuildConfigType, input: KType) {
        assertEquals(expected, nameOf(input))
    }

   @Suppress("unused")
   private val byNameCases = listOf(
        arrayOf(string, "String"),
        arrayOf(string.copy(nullable = true), "String?"),
        arrayOf(string.copy(array = true), "String[]"),
        arrayOf(string.copy(nullable = true, array = true), "String?[]"),
        arrayOf(string.copy(array = true, arrayNullable = true), "String[]?"),
        arrayOf(string.copy(nullable = true, array = true, arrayNullable = true), "String?[]?"),

        arrayOf(mapStringInt, "java.util.Map<String, Int>"),
        arrayOf(mapStringInt.copy(nullable = true), "java.util.Map<String, Int>?"),
        arrayOf(mapStringInt.copy(array = true), "java.util.Map<String, Int>[]"),
        arrayOf(mapStringInt.copy(nullable = true, array = true), "java.util.Map<String, Int>?[]"),
        arrayOf(mapStringInt.copy(array = true, arrayNullable = true), "java.util.Map<String, Int>[]?"),
        arrayOf(mapStringInt.copy(nullable = true, array = true, arrayNullable = true), "java.util.Map<String, Int>?[]?"),

        arrayOf(mapStringListOfInt, "java.util.Map<String, List<Int?>>"),
        arrayOf(mapStringListOfInt.copy(nullable = true), "java.util.Map<String, List<Int?>>?"),
        arrayOf(mapStringListOfInt.copy(array = true), "java.util.Map<String, List<Int?>>[]"),
        arrayOf(mapStringListOfInt.copy(nullable = true, array = true), "java.util.Map<String, List<Int?>>?[]"),
        arrayOf(mapStringListOfInt.copy(array = true, arrayNullable = true), "java.util.Map<String, List<Int?>>[]?"),
        arrayOf(mapStringListOfInt.copy(nullable = true, array = true, arrayNullable = true), "java.util.Map<String, List<Int?>>?[]?"),
   )

    @ParameterizedTest
    @FieldSource("byNameCases")
    fun testNameOfByName(expected: BuildConfigType, input: String) {
        assertEquals(expected, nameOf(input))
    }

    @ParameterizedTest
    @CsvSource(
        "com.example.app, com.example.app",
        "com.example.app10, com.example.app10",
        "com.example.my10app, com.example.my10app",
        "com.example._10app, com.example.10app",
        "com.example.app$10, com.example.app$10",
        "com.example_app, com.example-app"
    )
    fun testJavaIdentifier(expected: BuildConfigType, input: String) {
        arrayOf(expected, input.javaIdentifier)
    }

}

package com.github.gmazzo.buildconfig

import kotlin.reflect.typeOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildConfigTypeUtilsTests {

    private val string = BuildConfigType("String")
    private val int = BuildConfigType("Int")
    private val mapStringInt = BuildConfigType("java.util.Map", listOf(string, int))
    private val mapStringListOfInt =
        BuildConfigType("java.util.Map", listOf(string, BuildConfigType("List", listOf(int.copy(nullable = true)))))

    @Test
    fun testNameOfByJavaClass() {
        assertEquals(string, nameOf(String::class.java))
        assertEquals(string.copy(array = true), nameOf(Array<String>::class.java))

        assertEquals(int, nameOf(Int::class.java))
        assertEquals(int.copy(array = true), nameOf(Array<Int>::class.java))
        assertEquals(int.copy(array = true), nameOf(Array<Int?>::class.java))
        assertEquals(int.copy(array = true), nameOf(IntArray::class.java))
    }

    @Test
    fun testNameOfByKType() {
        assertEquals(string, nameOf(typeOf<String>()))
        assertEquals(string.copy(nullable = true), nameOf(typeOf<String?>()))
        assertEquals(string.copy(array = true), nameOf(typeOf<Array<String>>()))
        assertEquals(string.copy(nullable = true, array = true), nameOf(typeOf<Array<String?>>()))

        assertEquals(int, nameOf(typeOf<Int>()))
        assertEquals(int.copy(nullable = true), nameOf(typeOf<Int?>()))
        assertEquals(int.copy(array = true), nameOf(typeOf<Array<Int>>()))
        assertEquals(int.copy(array = true), nameOf(typeOf<IntArray>()))
        assertEquals(int.copy(nullable = true, array = true), nameOf(typeOf<Array<Int?>>()))

        assertEquals(mapStringInt, nameOf(typeOf<Map<String, Int>>()))
        assertEquals(mapStringInt.copy(nullable = true), nameOf(typeOf<Map<String, Int>?>()))
        assertEquals(mapStringInt.copy(array = true), nameOf(typeOf<Array<Map<String, Int>>>()))
        assertEquals(mapStringInt.copy(nullable = true, array = true), nameOf(typeOf<Array<Map<String, Int>?>>()))

        assertEquals(mapStringListOfInt, nameOf(typeOf<Map<String, List<Int?>>>()))
        assertEquals(mapStringListOfInt.copy(nullable = true), nameOf(typeOf<Map<String, List<Int?>>?>()))
        assertEquals(mapStringListOfInt.copy(array = true), nameOf(typeOf<Array<Map<String, List<Int?>>>>()))
        assertEquals(
            mapStringListOfInt.copy(nullable = true, array = true),
            nameOf(typeOf<Array<Map<String, List<Int?>>?>>())
        )
    }

    @Test
    fun testNameOfByName() {
        assertEquals(string, nameOf("String"))
        assertEquals(string.copy(nullable = true), nameOf("String?"))
        assertEquals(string.copy(array = true), nameOf("String[]"))
        assertEquals(string.copy(nullable = true, array = true), nameOf("String?[]"))

        assertEquals(mapStringInt, nameOf("java.util.Map<String, Int>"))
        assertEquals(mapStringInt.copy(nullable = true), nameOf("java.util.Map<String, Int>?"))
        assertEquals(mapStringInt.copy(array = true), nameOf("java.util.Map<String, Int>[]"))
        assertEquals(mapStringInt.copy(nullable = true, array = true), nameOf("java.util.Map<String, Int>?[]"))

        assertEquals(mapStringListOfInt, nameOf("java.util.Map<String, List<Int?>>"))
        assertEquals(mapStringListOfInt.copy(nullable = true), nameOf("java.util.Map<String, List<Int?>>?"))
        assertEquals(mapStringListOfInt.copy(array = true), nameOf("java.util.Map<String, List<Int?>>[]"))
        assertEquals(
            mapStringListOfInt.copy(nullable = true, array = true),
            nameOf("java.util.Map<String, List<Int?>>?[]")
        )
    }

    @Test
    fun testJavaIdentifier() {
        assertEquals("com.example.app", "com.example.app".javaIdentifier)
        assertEquals("com.example.app10", "com.example.app10".javaIdentifier)
        assertEquals("com.example.my10app", "com.example.my10app".javaIdentifier)
        assertEquals("com.example._10app", "com.example.10app".javaIdentifier)
        assertEquals("com.example.app$10", "com.example.app$10".javaIdentifier)
        assertEquals("com.example_app", "com.example-app".javaIdentifier)
    }

}

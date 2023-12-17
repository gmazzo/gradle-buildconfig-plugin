package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.BuildConfigType.NameRef
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTypeUtilsTests {

    private val paramsStringInt = listOf(NameRef("String"), NameRef("Int"))
    private val paramsStringListOfInt = listOf(NameRef("String"), NameRef("List", listOf(NameRef("Int?"))))

    @Test
    fun testNameOf() {
        assertEquals(NameRef("String"), nameOf("String"))
        assertEquals(NameRef("String?"), nameOf("String?"))
        assertEquals(NameRef("String[]"), nameOf("String[]"))
        assertEquals(NameRef("String?[]"), nameOf("String?[]"))
        assertEquals(NameRef("Map", paramsStringInt), nameOf("Map<String, Int>"))
        assertEquals(NameRef("Map?", paramsStringInt), nameOf("Map<String, Int>?"))
        assertEquals(NameRef("Map[]", paramsStringInt), nameOf("Map<String, Int>[]"))
        assertEquals(NameRef("Map?[]", paramsStringInt), nameOf("Map<String, Int>?[]"))
        assertEquals(NameRef("Map", paramsStringListOfInt), nameOf("Map<String, List<Int?>>"))
        assertEquals(NameRef("Map?", paramsStringListOfInt), nameOf("Map<String, List<Int?>>?"))
        assertEquals(NameRef("Map[]", paramsStringListOfInt), nameOf("Map<String, List<Int?>>[]"))
        assertEquals(NameRef("Map?[]", paramsStringListOfInt), nameOf("Map<String, List<Int?>>?[]"))
    }

}

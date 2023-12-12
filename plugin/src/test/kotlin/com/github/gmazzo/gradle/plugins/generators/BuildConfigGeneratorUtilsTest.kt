package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.parseTypename
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigGeneratorUtilsTest {

    @Test
    fun testParseTypename() {
        val cases = listOf(
            "int" to Triple("int", false, false),
            "int[]" to Triple("int", true, false),
            "int?" to Triple("int", false, true),
            "int[]?" to Triple("int", true, true),
            "java.lang.String" to Triple("java.lang.String", false, false),
            "java.lang.String[]" to Triple("java.lang.String", true, false),
            "java.lang.String?" to Triple("java.lang.String", false, true),
            "java.lang.String[]?" to Triple("java.lang.String", true, true),
        )

        cases.forEach { (input, expected) ->
           val actual = input.parseTypename()

            assertEquals(expected, actual)
        }
    }


}
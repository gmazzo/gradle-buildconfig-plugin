package com.github.gmazzo.gradle.plugins.generators

import com.github.gmazzo.gradle.plugins.parseTypename
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.expect

class BuildConfigGeneratorUtilsTest {

    @Test
    fun testParseTypename() {
        assertEquals(Triple("int", false, false), "int".parseTypename())
        assertEquals(Triple("int", false, true), "int[]".parseTypename())
        assertEquals(Triple("int", true, false), "int?".parseTypename())
        assertEquals(Triple("int", true, true), "int?[]".parseTypename())
        assertEquals(Triple("java.lang.String", false, false), "java.lang.String".parseTypename())
        assertEquals(Triple("java.lang.String", false, true), "java.lang.String[]".parseTypename())
        assertEquals(Triple("java.lang.String", true, false), "java.lang.String?".parseTypename())
        assertEquals(Triple("java.lang.String", true, true), "java.lang.String?[]".parseTypename())
    }

    @Test
    fun testParseTypenameError() {
        assertFailsWith<IllegalStateException> { "int[]?".parseTypename() }
    }


}
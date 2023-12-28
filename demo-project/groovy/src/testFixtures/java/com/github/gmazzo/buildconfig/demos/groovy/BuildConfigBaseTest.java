package com.github.gmazzo.buildconfig.demos.groovy;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

public abstract class BuildConfigBaseTest {

    @Test
    public void testBuildConfigProperties() {
        assertEquals("groovy", BuildConfig.APP_NAME);
        assertEquals("0.1.0-demo", BuildConfig.APP_VERSION);
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET);
        assertNull(BuildConfig.OPTIONAL);
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME);
        assertTrue(BuildConfig.FEATURE_ENABLED);
        assertEquals(Arrays.asList(1, 2, 3), BuildConfig.MAGIC_NUMBERS);
    }

    @Test
    public void testStrings() {
        assertEquals("aString", BuildConfig.STRING);
        assertNull(BuildConfig.STRING_NULL);
        assertEquals("aString", BuildConfig.STRING_PROVIDER);
        assertArrayEquals(new String[]{"a", "b", "c"}, BuildConfig.STRING_ARRAY);
        assertArrayEquals(new String[]{"a", "b", "c"}, BuildConfig.STRING_ARRAY_PROVIDER);
        assertArrayEquals(new String[]{"a", null, "c"}, BuildConfig.STRING_ARRAY_NULLABLE);
        assertArrayEquals(new String[]{"a", null, "c"}, BuildConfig.STRING_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList("a", null, "c"), BuildConfig.STRING_LIST);
        assertEquals(Arrays.asList("a", null, "c"), BuildConfig.STRING_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList("a", null, "c")), BuildConfig.STRING_SET);
        assertEquals(new HashSet<>(Arrays.asList("a", null, "c")), BuildConfig.STRING_SET_PROVIDER);
    }

    @Test
    public void testBytes() {
        assertEquals(64, BuildConfig.BYTE);
        assertNull(BuildConfig.BYTE_NULL);
        assertEquals(64, BuildConfig.BYTE_PROVIDER);
        assertArrayEquals(new byte[]{1, 2, 3}, BuildConfig.BYTE_NATIVE_ARRAY);
        assertArrayEquals(new byte[]{1, 2, 3}, BuildConfig.BYTE_NATIVE_ARRAY_PROVIDER);
        assertArrayEquals(new byte[]{1, 2, 3}, BuildConfig.BYTE_ARRAY);
        assertArrayEquals(new byte[]{1, 2, 3}, BuildConfig.BYTE_ARRAY_PROVIDER);
        assertArrayEquals(new Byte[]{1, null, 3}, BuildConfig.BYTE_ARRAY_NULLABLE);
        assertArrayEquals(new Byte[]{1, null, 3}, BuildConfig.BYTE_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList((byte) 1, null, (byte) 3), BuildConfig.BYTE_LIST);
        assertEquals(Arrays.asList((byte) 1, null, (byte) 3), BuildConfig.BYTE_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList((byte) 1, null, (byte) 3)), BuildConfig.BYTE_SET);
        assertEquals(new HashSet<>(Arrays.asList((byte) 1, null, (byte) 3)), BuildConfig.BYTE_SET_PROVIDER);
    }

    @Test
    public void testShorts() {
        assertEquals(64, BuildConfig.SHORT);
        assertNull(BuildConfig.SHORT_NULL);
        assertEquals(64, BuildConfig.SHORT_PROVIDER);
        assertArrayEquals(new short[]{1, 2, 3}, BuildConfig.SHORT_NATIVE_ARRAY);
        assertArrayEquals(new short[]{1, 2, 3}, BuildConfig.SHORT_NATIVE_ARRAY_PROVIDER);
        assertArrayEquals(new short[]{1, 2, 3}, BuildConfig.SHORT_ARRAY);
        assertArrayEquals(new short[]{1, 2, 3}, BuildConfig.SHORT_ARRAY_PROVIDER);
        assertArrayEquals(new Short[]{1, null, 3}, BuildConfig.SHORT_ARRAY_NULLABLE);
        assertArrayEquals(new Short[]{1, null, 3}, BuildConfig.SHORT_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList((short) 1, null, (short) 3), BuildConfig.SHORT_LIST);
        assertEquals(Arrays.asList((short) 1, null, (short) 3), BuildConfig.SHORT_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList((short) 1, null, (short) 3)), BuildConfig.SHORT_SET);
        assertEquals(new HashSet<>(Arrays.asList((short) 1, null, (short) 3)), BuildConfig.SHORT_SET_PROVIDER);
    }

    @Test
    public void testChars() {
        assertEquals('a', BuildConfig.CHAR);
        assertNull(BuildConfig.CHAR_NULL);
        assertEquals('a', BuildConfig.CHAR_PROVIDER);
        assertArrayEquals(new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_NATIVE_ARRAY);
        assertArrayEquals(new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_NATIVE_ARRAY_PROVIDER);
        assertArrayEquals(new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_ARRAY);
        assertArrayEquals(new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_ARRAY_PROVIDER);
        assertArrayEquals(new Character[]{'a', null, 'c'}, BuildConfig.CHAR_ARRAY_NULLABLE);
        assertArrayEquals(new Character[]{'a', null, 'c'}, BuildConfig.CHAR_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList('a', null, 'c'), BuildConfig.CHAR_LIST);
        assertEquals(Arrays.asList('a', null, 'c'), BuildConfig.CHAR_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList('a', null, 'c')), BuildConfig.CHAR_SET);
        assertEquals(new HashSet<>(Arrays.asList('a', null, 'c')), BuildConfig.CHAR_SET_PROVIDER);
    }

    @Test
    public void testIntegers() {
        assertEquals(1, BuildConfig.INT);
        assertNull(BuildConfig.INT_NULL);
        assertEquals(1, BuildConfig.INT_PROVIDER);
        assertArrayEquals(new int[]{1, 2, 3}, BuildConfig.INT_NATIVE_ARRAY);
        assertArrayEquals(new int[]{1, 2, 3}, BuildConfig.INT_NATIVE_ARRAY_PROVIDER);
        assertArrayEquals(new int[]{1, 2, 3}, BuildConfig.INT_ARRAY);
        assertArrayEquals(new int[]{1, 2, 3}, BuildConfig.INT_ARRAY_PROVIDER);
        assertArrayEquals(new Integer[]{1, null, 3}, BuildConfig.INT_ARRAY_NULLABLE);
        assertArrayEquals(new Integer[]{1, null, 3}, BuildConfig.INT_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList(1, null, 3), BuildConfig.INT_LIST);
        assertEquals(Arrays.asList(1, null, 3), BuildConfig.INT_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList(1, null, 3)), BuildConfig.INT_SET);
        assertEquals(new HashSet<>(Arrays.asList(1, null, 3)), BuildConfig.INT_SET_PROVIDER);
    }

    @Test
    public void testLongs() {
        assertEquals(1, BuildConfig.LONG);
        assertNull(BuildConfig.LONG_NULL);
        assertEquals(1, BuildConfig.LONG_PROVIDER);
        assertArrayEquals(new long[]{1L, 2L, 3L}, BuildConfig.LONG_NATIVE_ARRAY);
        assertArrayEquals(new long[]{1L, 2L, 3L}, BuildConfig.LONG_NATIVE_ARRAY_PROVIDER);
        assertArrayEquals(new long[]{1L, 2L, 3L}, BuildConfig.LONG_ARRAY);
        assertArrayEquals(new long[]{1L, 2L, 3L}, BuildConfig.LONG_ARRAY_PROVIDER);
        assertArrayEquals(new Long[]{1L, null, 3L}, BuildConfig.LONG_ARRAY_NULLABLE);
        assertArrayEquals(new Long[]{1L, null, 3L}, BuildConfig.LONG_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList(1L, null, 3L), BuildConfig.LONG_LIST);
        assertEquals(Arrays.asList(1L, null, 3L), BuildConfig.LONG_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList(1L, null, 3L)), BuildConfig.LONG_SET);
        assertEquals(new HashSet<>(Arrays.asList(1L, null, 3L)), BuildConfig.LONG_SET_PROVIDER);
    }

    @Test
    public void testFloats() {
        assertEquals(1, BuildConfig.FLOAT, 0);
        assertNull(BuildConfig.FLOAT_NULL);
        assertEquals(1, BuildConfig.FLOAT_PROVIDER, 0);
        assertArrayEquals(new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_NATIVE_ARRAY, 0);
        assertArrayEquals(new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_NATIVE_ARRAY_PROVIDER, 0);
        assertArrayEquals(new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_ARRAY, 0);
        assertArrayEquals(new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_ARRAY_PROVIDER, 0);
        assertArrayEquals(new Float[]{1f, null, 3f}, BuildConfig.FLOAT_ARRAY_NULLABLE);
        assertArrayEquals(new Float[]{1f, null, 3f}, BuildConfig.FLOAT_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList(1f, null, 3f), BuildConfig.FLOAT_LIST);
        assertEquals(Arrays.asList(1f, null, 3f), BuildConfig.FLOAT_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList(1f, null, 3f)), BuildConfig.FLOAT_SET);
        assertEquals(new HashSet<>(Arrays.asList(1f, null, 3f)), BuildConfig.FLOAT_SET_PROVIDER);
    }

    @Test
    public void testDoubles() {
        assertEquals(1, BuildConfig.DOUBLE, 0);
        assertNull(BuildConfig.DOUBLE_NULL);
        assertEquals(1, BuildConfig.DOUBLE_PROVIDER, 0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_NATIVE_ARRAY, 0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_NATIVE_ARRAY_PROVIDER, 0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_ARRAY, 0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_ARRAY_PROVIDER, 0);
        assertArrayEquals(new Double[]{1.0, null, 3.0}, BuildConfig.DOUBLE_ARRAY_NULLABLE);
        assertArrayEquals(new Double[]{1.0, null, 3.0}, BuildConfig.DOUBLE_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList(1.0, null, 3.0), BuildConfig.DOUBLE_LIST);
        assertEquals(Arrays.asList(1.0, null, 3.0), BuildConfig.DOUBLE_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList(1.0, null, 3.0)), BuildConfig.DOUBLE_SET);
        assertEquals(new HashSet<>(Arrays.asList(1.0, null, 3.0)), BuildConfig.DOUBLE_SET_PROVIDER);
    }

    @Test
    public void testBooleans() {
        assertTrue(BuildConfig.BOOLEAN);
        assertNull(BuildConfig.BOOLEAN_NULL);
        assertTrue(BuildConfig.BOOLEAN_PROVIDER);
        assertArrayEquals(new boolean[]{true, false, false}, BuildConfig.BOOLEAN_NATIVE_ARRAY);
        assertArrayEquals(new boolean[]{true, false, false}, BuildConfig.BOOLEAN_NATIVE_ARRAY_PROVIDER);
        assertArrayEquals(new boolean[]{true, false, false}, BuildConfig.BOOLEAN_ARRAY);
        assertArrayEquals(new boolean[]{true, false, false}, BuildConfig.BOOLEAN_ARRAY_PROVIDER);
        assertArrayEquals(new Boolean[]{true, null, false}, BuildConfig.BOOLEAN_ARRAY_NULLABLE);
        assertArrayEquals(new Boolean[]{true, null, false}, BuildConfig.BOOLEAN_ARRAY_NULLABLE_PROVIDER);
        assertEquals(Arrays.asList(true, null, false), BuildConfig.BOOLEAN_LIST);
        assertEquals(Arrays.asList(true, null, false), BuildConfig.BOOLEAN_LIST_PROVIDER);
        assertEquals(new HashSet<>(Arrays.asList(true, null, false)), BuildConfig.BOOLEAN_SET);
        assertEquals(new HashSet<>(Arrays.asList(true, null, false)), BuildConfig.BOOLEAN_SET_PROVIDER);
    }

    @Test
    public void testCustomTypes() {
        assertEquals(Map.of("a", 1, "b", 2), BuildConfig.MAP);
        assertEquals(Map.of("a", 1, "b", 2), BuildConfig.MAP_PROVIDER);
        assertEquals(Map.of("a", 1, "b", 2), BuildConfig.MAP_GENERIC);
        assertEquals(Map.of("a", 1, "b", 2), BuildConfig.MAP_GENERIC_PROVIDER);
        assertEquals(new File("aFile"), BuildConfig.FILE);
        assertEquals(new File("aFile"), BuildConfig.FILE_PROVIDER);
        assertEquals(URI.create("https://example.io"), BuildConfig.URI);
        assertEquals(URI.create("https://example.io"), BuildConfig.URI_PROVIDER);
        assertEquals(new SomeData("a", 1), BuildConfig.DATA);
        assertEquals(new SomeData("a", 1), BuildConfig.DATA_PROVIDER);
    }

}

package com.github.gmazzo.buildconfig.demos.groovy;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BuildConfigBaseTest {

    protected Object[][] extraCases() {
        return new Object[0][];
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("genericCases")
    @MethodSource("extraCases")
    public void testBuildConfigProperties(Object expected, Object actual) {
        if (expected instanceof Object[] && actual instanceof Object[]) {
            assertArrayEquals((Object[]) expected, (Object[]) actual);

        } else if (expected instanceof byte[] && actual instanceof byte[]) {
            assertArrayEquals((byte[]) expected, (byte[]) actual);

        } else if (expected instanceof short[] && actual instanceof short[]) {
            assertArrayEquals((short[]) expected, (short[]) actual);

        }  else if (expected instanceof char[] && actual instanceof char[]) {
            assertArrayEquals((char[]) expected, (char[]) actual);

        } else if (expected instanceof int[] && actual instanceof int[]) {
            assertArrayEquals((int[]) expected, (int[]) actual);

        } else if (expected instanceof long[] && actual instanceof long[]) {
            assertArrayEquals((long[]) expected, (long[]) actual);

        } else if (expected instanceof float[] && actual instanceof float[]) {
            assertArrayEquals((float[]) expected, (float[]) actual);

        } else if (expected instanceof double[] && actual instanceof double[]) {
            assertArrayEquals((double[]) expected, (double[]) actual);

        } else if (expected instanceof boolean[] && actual instanceof boolean[]) {
            assertArrayEquals((boolean[]) expected, (boolean[]) actual);

        } else {
            assertEquals(expected, actual);
        }
    }

    @SuppressWarnings("unused")
    private static final Object[][] genericCases = {

        // properties
        {"groovy", BuildConfig.APP_NAME},
        {"0.1.0-demo", BuildConfig.APP_VERSION},
        {"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET},
        {null, BuildConfig.OPTIONAL},
        {true, BuildConfig.FEATURE_ENABLED},
        {Arrays.asList(1, 2, 3), BuildConfig.MAGIC_NUMBERS},

        // strings
        {"aString", BuildConfig.STRING},
        {null, BuildConfig.STRING_NULL},
        {"aString", BuildConfig.STRING_PROVIDER},
        {new String[]{"a", "b", "c"}, BuildConfig.STRING_ARRAY},
        {new String[]{"a", "b", "c"}, BuildConfig.STRING_ARRAY_PROVIDER},
        {new String[]{"a", null, "c"}, BuildConfig.STRING_ARRAY_NULLABLE},
        {new String[]{"a", null, "c"}, BuildConfig.STRING_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList("a", null, "c"), BuildConfig.STRING_LIST},
        {Arrays.asList("a", null, "c"), BuildConfig.STRING_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList("a", null, "c")), BuildConfig.STRING_SET},
        {new HashSet<>(Arrays.asList("a", null, "c")), BuildConfig.STRING_SET_PROVIDER},

        // bytes
        {(byte) 64, BuildConfig.BYTE},
        {null, BuildConfig.BYTE_NULL},
        {(byte) 64, BuildConfig.BYTE_PROVIDER},
        {new byte[]{1, 2, 3}, BuildConfig.BYTE_NATIVE_ARRAY},
        {new byte[]{1, 2, 3}, BuildConfig.BYTE_NATIVE_ARRAY_PROVIDER},
        {new byte[]{1, 2, 3}, BuildConfig.BYTE_ARRAY},
        {new byte[]{1, 2, 3}, BuildConfig.BYTE_ARRAY_PROVIDER},
        {new Byte[]{1, null, 3}, BuildConfig.BYTE_ARRAY_NULLABLE},
        {new Byte[]{1, null, 3}, BuildConfig.BYTE_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList((byte) 1, null, (byte) 3), BuildConfig.BYTE_LIST},
        {Arrays.asList((byte) 1, null, (byte) 3), BuildConfig.BYTE_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList((byte) 1, null, (byte) 3)), BuildConfig.BYTE_SET},
        {new HashSet<>(Arrays.asList((byte) 1, null, (byte) 3)), BuildConfig.BYTE_SET_PROVIDER},

        // shorts
        {(short) 64, BuildConfig.SHORT},
        {null, BuildConfig.SHORT_NULL},
        {(short) 64, BuildConfig.SHORT_PROVIDER},
        {new short[]{1, 2, 3}, BuildConfig.SHORT_NATIVE_ARRAY},
        {new short[]{1, 2, 3}, BuildConfig.SHORT_NATIVE_ARRAY_PROVIDER},
        {new short[]{1, 2, 3}, BuildConfig.SHORT_ARRAY},
        {new short[]{1, 2, 3}, BuildConfig.SHORT_ARRAY_PROVIDER},
        {new Short[]{1, null, 3}, BuildConfig.SHORT_ARRAY_NULLABLE},
        {new Short[]{1, null, 3}, BuildConfig.SHORT_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList((short) 1, null, (short) 3), BuildConfig.SHORT_LIST},
        {Arrays.asList((short) 1, null, (short) 3), BuildConfig.SHORT_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList((short) 1, null, (short) 3)), BuildConfig.SHORT_SET},
        {new HashSet<>(Arrays.asList((short) 1, null, (short) 3)), BuildConfig.SHORT_SET_PROVIDER},

        // chars
        {'a', BuildConfig.CHAR},
        {null, BuildConfig.CHAR_NULL},
        {'a', BuildConfig.CHAR_PROVIDER},
        {new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_NATIVE_ARRAY},
        {new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_NATIVE_ARRAY_PROVIDER},
        {new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_ARRAY},
        {new char[]{'a', 'b', 'c'}, BuildConfig.CHAR_ARRAY_PROVIDER},
        {new Character[]{'a', null, 'c'}, BuildConfig.CHAR_ARRAY_NULLABLE},
        {new Character[]{'a', null, 'c'}, BuildConfig.CHAR_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList('a', null, 'c'), BuildConfig.CHAR_LIST},
        {Arrays.asList('a', null, 'c'), BuildConfig.CHAR_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList('a', null, 'c')), BuildConfig.CHAR_SET},
        {new HashSet<>(Arrays.asList('a', null, 'c')), BuildConfig.CHAR_SET_PROVIDER},

        // integers
        {1, BuildConfig.INT},
        {null, BuildConfig.INT_NULL},
        {1, BuildConfig.INT_PROVIDER},
        {new int[]{1, 2, 3}, BuildConfig.INT_NATIVE_ARRAY},
        {new int[]{1, 2, 3}, BuildConfig.INT_NATIVE_ARRAY_PROVIDER},
        {new int[]{1, 2, 3}, BuildConfig.INT_ARRAY},
        {new int[]{1, 2, 3}, BuildConfig.INT_ARRAY_PROVIDER},
        {new Integer[]{1, null, 3}, BuildConfig.INT_ARRAY_NULLABLE},
        {new Integer[]{1, null, 3}, BuildConfig.INT_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList(1, null, 3), BuildConfig.INT_LIST},
        {Arrays.asList(1, null, 3), BuildConfig.INT_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList(1, null, 3)), BuildConfig.INT_SET},
        {new HashSet<>(Arrays.asList(1, null, 3)), BuildConfig.INT_SET_PROVIDER},

        // longs
        {1L, BuildConfig.LONG},
        {null, BuildConfig.LONG_NULL},
        {1L, BuildConfig.LONG_PROVIDER},
        {new long[]{1L, 2L, 3L}, BuildConfig.LONG_NATIVE_ARRAY},
        {new long[]{1L, 2L, 3L}, BuildConfig.LONG_NATIVE_ARRAY_PROVIDER},
        {new long[]{1L, 2L, 3L}, BuildConfig.LONG_ARRAY},
        {new long[]{1L, 2L, 3L}, BuildConfig.LONG_ARRAY_PROVIDER},
        {new Long[]{1L, null, 3L}, BuildConfig.LONG_ARRAY_NULLABLE},
        {new Long[]{1L, null, 3L}, BuildConfig.LONG_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList(1L, null, 3L), BuildConfig.LONG_LIST},
        {Arrays.asList(1L, null, 3L), BuildConfig.LONG_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList(1L, null, 3L)), BuildConfig.LONG_SET},
        {new HashSet<>(Arrays.asList(1L, null, 3L)), BuildConfig.LONG_SET_PROVIDER},

        // floats
        {1f, BuildConfig.FLOAT, 0},
        {null, BuildConfig.FLOAT_NULL},
        {1f, BuildConfig.FLOAT_PROVIDER, 0},
        {new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_NATIVE_ARRAY, 0},
        {new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_NATIVE_ARRAY_PROVIDER, 0},
        {new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_ARRAY, 0},
        {new float[]{1f, 2f, 3f}, BuildConfig.FLOAT_ARRAY_PROVIDER, 0},
        {new Float[]{1f, null, 3f}, BuildConfig.FLOAT_ARRAY_NULLABLE},
        {new Float[]{1f, null, 3f}, BuildConfig.FLOAT_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList(1f, null, 3f), BuildConfig.FLOAT_LIST},
        {Arrays.asList(1f, null, 3f), BuildConfig.FLOAT_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList(1f, null, 3f)), BuildConfig.FLOAT_SET},
        {new HashSet<>(Arrays.asList(1f, null, 3f)), BuildConfig.FLOAT_SET_PROVIDER},

        // doubles
        {1.0, BuildConfig.DOUBLE, 0},
        {null, BuildConfig.DOUBLE_NULL},
        {1.0, BuildConfig.DOUBLE_PROVIDER, 0},
        {new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_NATIVE_ARRAY, 0},
        {new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_NATIVE_ARRAY_PROVIDER, 0},
        {new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_ARRAY, 0},
        {new double[]{1.0, 2.0, 3.0}, BuildConfig.DOUBLE_ARRAY_PROVIDER, 0},
        {new Double[]{1.0, null, 3.0}, BuildConfig.DOUBLE_ARRAY_NULLABLE},
        {new Double[]{1.0, null, 3.0}, BuildConfig.DOUBLE_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList(1.0, null, 3.0), BuildConfig.DOUBLE_LIST},
        {Arrays.asList(1.0, null, 3.0), BuildConfig.DOUBLE_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList(1.0, null, 3.0)), BuildConfig.DOUBLE_SET},
        {new HashSet<>(Arrays.asList(1.0, null, 3.0)), BuildConfig.DOUBLE_SET_PROVIDER},

        // booleans
        {true, BuildConfig.BOOLEAN},
        {null, BuildConfig.BOOLEAN_NULL},
        {true, BuildConfig.BOOLEAN_PROVIDER},
        {new boolean[]{true, false, false}, BuildConfig.BOOLEAN_NATIVE_ARRAY},
        {new boolean[]{true, false, false}, BuildConfig.BOOLEAN_NATIVE_ARRAY_PROVIDER},
        {new boolean[]{true, false, false}, BuildConfig.BOOLEAN_ARRAY},
        {new boolean[]{true, false, false}, BuildConfig.BOOLEAN_ARRAY_PROVIDER},
        {new Boolean[]{true, null, false}, BuildConfig.BOOLEAN_ARRAY_NULLABLE},
        {new Boolean[]{true, null, false}, BuildConfig.BOOLEAN_ARRAY_NULLABLE_PROVIDER},
        {Arrays.asList(true, null, false), BuildConfig.BOOLEAN_LIST},
        {Arrays.asList(true, null, false), BuildConfig.BOOLEAN_LIST_PROVIDER},
        {new HashSet<>(Arrays.asList(true, null, false)), BuildConfig.BOOLEAN_SET},
        {new HashSet<>(Arrays.asList(true, null, false)), BuildConfig.BOOLEAN_SET_PROVIDER},

        // custom types
        {Map.of("a", 1, "b", 2), BuildConfig.MAP},
        {Map.of("a", 1, "b", 2), BuildConfig.MAP_PROVIDER},
        {Map.of("a", 1, "b", 2), BuildConfig.MAP_BY_EXPRESSION},
        {Map.of("a", 1, "b", 2), BuildConfig.MAP_BY_EXPRESSION_PROVIDER},
        {Map.of("a", 1, "b", 2), BuildConfig.MAP_GENERIC},
        {Map.of("a", 1, "b", 2), BuildConfig.MAP_GENERIC_PROVIDER},
        {new File("aFile"), BuildConfig.FILE},
        {new File("aFile"), BuildConfig.FILE_PROVIDER},
        {URI.create("https://example.io"), BuildConfig.URI},
        {URI.create("https://example.io"), BuildConfig.URI_PROVIDER},
        {new SomeData("a", 1), BuildConfig.DATA},
        {new SomeData("a", 1), BuildConfig.DATA_PROVIDER},
    };

}

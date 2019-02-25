package com.github.gmazzo;

import org.junit.Test;

import static org.junit.Assert.*;

public class BuildConfigTest {

    @Test
    public void testBuildConfigProperties() {
        assertEquals("example-groovy", BuildConfig.APP_NAME);
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET);
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME);
        assertTrue(BuildConfig.FEATURE_ENABLED);
        assertArrayEquals(new int[]{1, 2, 3, 4}, BuildConfig.MAGIC_NUMBERS);
        assertEquals(new SomeData("a", 1), BuildConfig.MY_DATA);

        // test sourceSet buildConfig
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT);

        // resource files
        assertEquals("file1.json", BuildConfig.RESOURCE_FILE1_JSON.getPath());
        assertEquals("file2.json", BuildConfig.RESOURCE_FILE2_JSON.getPath());
        assertEquals("config/local.properties", BuildConfig.RESOURCE_CONFIG_LOCAL_PROPERTIES.getPath());
        assertEquals("config/prod.properties", BuildConfig.RESOURCE_CONFIG_PROD_PROPERTIES.getPath());
    }

}

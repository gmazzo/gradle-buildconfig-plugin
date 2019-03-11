package com.github.gmazzo;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildConfigTest {

    @Test
    public void testBuildConfigProperties() {
        assertEquals("example-groovy", BuildConfig.APP_NAME);
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET);
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME);
        assertTrue(BuildConfig.FEATURE_ENABLED);
        assertArrayEquals(new int[]{1, 2, 3, 4}, BuildConfig.MAGIC_NUMBERS);
        assertEquals(new SomeData("a", 1), BuildConfig.MY_DATA);
    }

    @Test
    public void testBuildConfigTestProperties() {
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT);
    }

    @Test
    public void testResourcesConfigProperties() {
        assertEquals("aConstant", BuildResources.A_CONSTANT);
        assertEquals("file1.json", BuildResources.FILE1_JSON.getPath());
        assertEquals("file2.json", BuildResources.FILE2_JSON.getPath());
        assertEquals("config/local.properties", BuildResources.CONFIG_LOCAL_PROPERTIES.getPath());
        assertEquals("config/prod.properties", BuildResources.CONFIG_PROD_PROPERTIES.getPath());
    }

}

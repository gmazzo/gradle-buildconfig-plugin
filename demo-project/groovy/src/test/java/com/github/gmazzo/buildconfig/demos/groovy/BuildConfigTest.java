package com.github.gmazzo.buildconfig.demos.groovy;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BuildConfigTest extends BuildConfigBaseTest {

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

    @Test
    public void testCustomBuildConfig() {
        assertEquals("aValue", Custom.CUSTOM_VALUE);
        assertFalse(Modifier.isPublic(Custom.class.getModifiers()));
    }

}

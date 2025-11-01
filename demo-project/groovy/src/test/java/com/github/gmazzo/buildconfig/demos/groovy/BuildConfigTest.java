package com.github.gmazzo.buildconfig.demos.groovy;

import java.lang.reflect.Modifier;

public class BuildConfigTest extends BuildConfigBaseTest {

    @Override
    protected Object[][] extraCases() {
        return new Object[][] {
            // test properties
            {"aTestValue", TestBuildConfig.TEST_CONSTANT},

            // resources properties
            {"aConstant", BuildResources.A_CONSTANT},
            {"file1.json", BuildResources.FILE1_JSON.getPath()},
            {"file2.json", BuildResources.FILE2_JSON.getPath()},
            {"config/local.properties", BuildResources.CONFIG_LOCAL_PROPERTIES.getPath()},
            {"config/prod.properties", BuildResources.CONFIG_PROD_PROPERTIES.getPath()},

            // custom properties
            {"aValue", Custom.CUSTOM_VALUE},
            {false, Modifier.isPublic(Custom.class.getModifiers())},
        };
    }

}

allprojects {
    group = "com.github.gmazzo.buildconfig.demos"
    version = "0.1.0-demo"
}

subprojects {
    fun addJUnit5(configuration: String = "testImplementation") {
        dependencies {
            configuration(platform(libs.junit5.bom))
            configuration(libs.junit5.params)
            "testRuntimeOnly"(libs.junit5.engine)
            "testRuntimeOnly"(libs.junit5.platformLauncher)
        }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    plugins.withId("java") { addJUnit5() }
    plugins.withId("java-test-fixtures") { addJUnit5("testFixturesImplementation") }
    plugins.withId("com.android.base") { addJUnit5() }
}

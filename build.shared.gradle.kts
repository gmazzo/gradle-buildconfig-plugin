allprojects {

    repositories {
        jcenter()
    }

    group = "com.github.gmazzo"
    version = "2.1.0"

    plugins.withType<JavaPlugin> {
        dependencies {
            "testImplementation"("junit:junit:4.12")
            "testImplementation"("org.mockito:mockito-core:2.27.0")
        }
    }

}

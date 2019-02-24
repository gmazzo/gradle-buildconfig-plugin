allprojects {

    repositories {
        jcenter()
    }

    group = "com.github.gmazzo"
    version = "0.2"

    project.plugins.withType(JavaPlugin::class.java) {
        dependencies.add("testImplementation", "junit:junit:4.12")
    }

}

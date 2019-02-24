allprojects {

    repositories {
        jcenter()
    }

    group = "com.github.gmazzo"
    version = "0.1"

    project.plugins.withType(JavaPlugin::class.java) {
        dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter:5.4.0")
    }

}

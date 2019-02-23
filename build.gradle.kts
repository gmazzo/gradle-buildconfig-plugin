apply(from = "buildShared.gradle.kts")

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

allprojects {

    repositories {
        jcenter()
    }

    group = "com.github.gmazzo"
    version = "0.3"

}

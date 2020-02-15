pluginManagement {
    // TODO this should not be required once Gradle supports variables on 'plugins' closure
    val kotlinVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                useVersion(kotlinVersion)
            }
        }
    }
}

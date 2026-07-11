pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repo.eclipse.org/content/repositories/paho-snapshots/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MqttDashboard"
include(":app")

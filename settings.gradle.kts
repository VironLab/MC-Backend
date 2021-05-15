//Repositorys for Plugins
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        jcenter()
        mavenCentral()
    }
}

rootProject.name = "Backend"


include(":backend")
include(":backend-api")


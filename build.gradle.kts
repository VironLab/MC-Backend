import kotlin.collections.*

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
        classpath("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    }
}

//Define Plugins
plugins {
    id("java")
    id("maven")
    kotlin("jvm") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
}

//Configure build of docs
tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(File(rootProject.buildDir.path, "vironlab-backend"))
}

//Define Variables for all Projects
allprojects {

    //Define Repositorys
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.vironlab.eu/repository/maven-snapshot/")
        maven("https://dl.bintray.com/kotlin/kotlinx")
        maven("https://repo.thesimplecloud.eu/artifactory/list/gradle-release-local/")
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.velocitypowered.com/snapshots/")
    }

    //Define Version and Group
    this.group = findProperty("group").toString()
    this.version = findProperty("version").toString()

}

//Default configuration for each module
subprojects {
    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")


    //Define Dependencies for all Modules
    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
        compileOnly("org.jetbrains.kotlin:kotlin-serialization:1.4.31")
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
        implementation("eu.vironlab.vextension:vextension-common:2.0.0-SNAPSHOT")
        compileOnly("com.google.code.gson:gson:2.8.6")
        compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
        compileOnly("com.velocitypowered:velocity-api:1.1.5")
        compileOnly("eu.thesimplecloud.simplecloud:simplecloud-api:2.1.0")
        compileOnly("eu.thesimplecloud.simplecloud:simplecloud-base:2.1.0")
        compileOnly("eu.thesimplecloud.simplecloud:simplecloud-plugin:2.1.0")
        compileOnly("eu.thesimplecloud.simplecloud:simplecloud-module-permission:2.1.0")
        implementation("joda-time:joda-time:2.9.9")
    }


    tasks {
        //Set the Name of the Sources Jar
        val sourcesJar by creating(Jar::class) {
            archiveFileName.set("${project.name}-sources.jar")
        }


        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        jar {
            archiveFileName.set("${project.name}.jar")

            if (project.name != "backend-api") {
                dependsOn(":backend-api:build")
                val buildDir = project(":backend-api").buildDir.path
                from("$buildDir/classes/java/main") {
                    include("**")
                }
                from("$buildDir/classes/kotlin/main") {
                    include("**")
                }
                from("$buildDir/resources/main") {
                    include("**")
                }
            }

            doFirst {
                //Set Manifest
                manifest {
                    attributes["Implementation-Title"] = project.name
                    attributes["Implementation-Version"] = findProperty("version").toString()
                    attributes["Specification-Version"] = findProperty("version").toString()
                    attributes["Implementation-Vendor"] = "VironLab.eu"
                    attributes["Built-By"] = System.getProperty("user.name")
                    attributes["Build-Jdk"] = System.getProperty("java.version")
                    attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
                }
            }
            doLast {
                //Generate the Pom file for the Repository
                maven.pom {
                    withGroovyBuilder {
                        "project" {
                            groupId = findProperty("group").toString()
                            artifactId = project.name
                            version = findProperty("version").toString()
                            this.setProperty("inceptionYear", "2021")
                            "licenses" {
                                "license" {
                                    setProperty("name", "General Public License (GPL v3.0)")
                                    setProperty("url", "https://www.gnu.org/licenses/gpl-3.0.txt")
                                    setProperty("distribution", "repo")
                                }
                            }
                            "developers" {
                                "developer" {
                                    setProperty("id", "Infinity_dev")
                                    setProperty("name", "Florin Dornig")
                                    setProperty("email", "infinitydev@vironlab.eu")
                                }
                            }
                        }
                    }

                }.writeTo("build/pom/pom.xml")
            }
        }

        withType<JavaCompile> {
            this.options.encoding = "UTF-8"
        }


    }
}
import org.gradle.kotlin.dsl.from

plugins {
    kotlin("jvm").apply(false)
    kotlin("plugin.serialization").apply(false)
    id("org.jetbrains.compose").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").apply(false)
}

version = "1.0"

tasks.register<Zip>("jar") {
    archiveFileName.set("extension.jar")
    destinationDirectory.set(file("$buildDir/libs/"))

    val desktopProject = project(":desktop")
    dependsOn(desktopProject.tasks.named("jar"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    doFirst {
        delete(
            "$buildDir/libs/extension.jar",
        )
    }

    from(zipTree("${desktopProject.buildDir}/libs/Output.jar"))

    from(rootDir) {
        include("info.toml")
    }

    from("assets/") {
        include("**")
    }
}

tasks.register<Zip>("deploy") {
    archiveFileName.set("extension-all.jar")
    destinationDirectory.set(file("$buildDir/libs/"))

    val desktopProject = project(":desktop")
    val androidProject = project(":android")

    dependsOn(androidProject.tasks.named("jarAndroid"))
    dependsOn(desktopProject.tasks.named("jar"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    doFirst {
        delete(
            "$buildDir/libs/extension.jar",
            "$buildDir/libs/extension-all.jar"
        )
    }

    from(rootDir) {
        include("info.toml")
    }

    from("assets/") {
        include("**")
    }

    from(androidProject.buildDir.absolutePath + "/generated") {
        include("config.toml")
        rename("config.toml", "inject_android.toml")
    }

    from(zipTree("${desktopProject.buildDir}/libs/Output.jar"))
    from(zipTree("${androidProject.buildDir}/libs/Output.jar"))
    from(zipTree("${androidProject.buildDir}/libs/Android.jar"))

    doLast {
        subprojects.forEach {
            delete(
                "${it.buildDir}/libs/Output.jar",
                "${it.buildDir}/libs/Android.jar"
            )
        }
    }
}
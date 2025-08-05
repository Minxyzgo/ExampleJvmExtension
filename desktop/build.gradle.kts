import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

group = "example"
version = "1.0"

ksp {
    arg("outputDir", project.buildDir.absolutePath + "/generated")
    arg("lib", "game-lib")
    arg("libDir", "$rootDir/lib")
}

dependencies {
    api(project(":common"))
    val rwppVersion = findProperty("rwpp.version") as String
    compileOnly("com.github.Minxyzgo.RWPP:core:$rwppVersion")
    compileOnly(fileTree(
        "dir" to rootDir.absolutePath + "/lib",
        "include" to "game-lib.jar",
    ))
    ksp("com.github.Minxyzgo.RWPP:ksp:$rwppVersion")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile> {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("Output.jar")

    from(configurations.runtimeClasspath.get().mapNotNull {
        if (it.name.contains("kotlin-stdlib") || it.name.startsWith("annotations-"))
            return@mapNotNull null
        if (it.isDirectory)
            it
        else zipTree(it)
    })

    from(project.buildDir.absolutePath + "/generated") {
        include("config.toml")
        rename("config.toml", "inject_desktop.toml")
    }
}
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
    arg("lib", "android-game-lib")
    arg("libDir", "$rootDir/lib")
}


dependencies {
    api(project(":common"))
    val rwppVersion = findProperty("rwpp.version") as String
    compileOnly("com.github.Minxyzgo.RWPP:core:$rwppVersion")
    compileOnly(fileTree(
        "dir" to rootDir.absolutePath + "/lib",
        "include" to "android-game-lib.jar",
    ))
    ksp("com.github.Minxyzgo.RWPP:ksp:$rwppVersion")
}

val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")

val buildToolsVersion = findProperty("build.tools.version") as String

val d8Path = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
    "$sdkRoot/build-tools/$buildToolsVersion/d8.bat"
} else {
    "$sdkRoot/build-tools/$buildToolsVersion/d8"
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
    archiveFileName.set("Android.jar")

    from(configurations.runtimeClasspath.get().mapNotNull {
        if (it.name.contains("kotlin-stdlib") || it.name.startsWith("annotations-"))
            return@mapNotNull null
        if (it.isDirectory)
            it
        else zipTree(it)
    })

    from(buildDir.absolutePath + "/generated") {
        include("config.toml")
        rename("config.toml", "inject_android.toml")
    }
}

val jarAndroid by tasks.registering {
    dependsOn(tasks.jar)

    doLast {
        if (sdkRoot.isNullOrBlank() || !File(sdkRoot).exists()) {
            throw GradleException("未找到有效的 Android SDK。请确保 ANDROID_HOME 或 ANDROID_SDK_ROOT 指向您的 Android SDK 目录。")
        }

        val platformRoot = File("$sdkRoot/platforms/").listFiles()
            ?.sortedDescending()
            ?.find { File(it, "android.jar").exists() }

        if (platformRoot == null) {
            throw GradleException("未找到 android.jar。请确保您已安装 Android 平台。")
        }

        val classpathFiles = configurations.compileClasspath.get().files +
                configurations.runtimeClasspath.get().files +
                File(platformRoot, "android.jar")

        val classpathArgs = classpathFiles.flatMap { listOf("--classpath", it.path) }

        project.exec {
            workingDir = file("$buildDir/libs")
            commandLine = listOf(d8Path) + classpathArgs + listOf(
                "--min-api", "26",
                "--output", "Output.jar",
                "Android.jar"
            )

            standardOutput = System.out
            errorOutput = System.err
        }
    }
}


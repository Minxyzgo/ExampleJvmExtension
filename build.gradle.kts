import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File


plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

version = "1.0"


val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")

// ||||||||||||||||||||||||||||||||||||||
// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
// ______________________________________
val rwppVersion = "eb600ba719"
// ______________________________________
val buildToolsVersion = "36.0.0" // 至少需要 36.0.0来支持kotlin 2.1+
// ______________________________________
// ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
// ||||||||||||||||||||||||||||||||||||||

val d8Path = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
    "$sdkRoot/build-tools/$buildToolsVersion/d8.bat"
} else {
    "$sdkRoot/build-tools/$buildToolsVersion/d8"
}

sourceSets.main.get().kotlin.srcDirs("src")

dependencies {
    compileOnly("com.github.Minxyzgo.RWPP:core:$rwppVersion")
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
    archiveFileName.set("Desktop.jar")

    from(configurations.runtimeClasspath.get().mapNotNull {
        if (it.name.contains("kotlin-stdlib") || it.name.startsWith("annotations-"))
            return@mapNotNull null
        if (it.isDirectory)
            it
        else zipTree(it)
    })

    from(rootDir) {
        include("info.toml")
    }

    from("assets/") {
        include("**")
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
                "--output", "Android.jar",
                "Desktop.jar"
            )

            standardOutput = System.out
            errorOutput = System.err
        }
    }
}

tasks.register<Jar>("deploy") {
    dependsOn(jarAndroid)
    dependsOn(tasks.jar)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("extension.jar")

    from(zipTree("$buildDir/libs/Desktop.jar"))
    from(zipTree("$buildDir/libs/Android.jar"))

    doLast {
        delete(
            "$buildDir/libs/Desktop.jar",
            "$buildDir/libs/Android.jar"
        )
    }
}
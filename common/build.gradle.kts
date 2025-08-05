
plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "example"
version = "1.0"

dependencies {
    val rwppVersion = findProperty("rwpp.version") as String
    compileOnly("com.github.Minxyzgo.RWPP:core:$rwppVersion")
}
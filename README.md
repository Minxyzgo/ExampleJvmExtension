# Example RWPP Jvm Extension

这是 [RWPP](https://github.com/Minxyzgo/rwpp) 的示例 Jvm 扩展.

此扩展使用 Java 和 Kotlin 共同编写, 并使用 Gradle 构建.

## 为桌面构建 Jar
1. 安装 JDK 17 或更高版本.
2. 通常情况下需要在`build.gradle.kts`中重新设置需要的`rwppVersion`
3. 运行 `gradlew jar`.
4. 构建的 JAR 文件位于 `build/libs` 目录下.

## 构建全平台 Jar
1. 安装 JDK 17 或更高版本.
2. 安装`Android Sdk` 并正确设置环境变量(`ANDROID_HOME`). 构建此项目所需的`build-tools`版本至少应为`36.0.0`.
3. 在`build.gradle.kts`中重新设置需要的`rwppVersion`和`build-tools`版本.
4. 运行 `gradlew deploy`.
5. 构建的 JAR 文件位于 `build/libs` 目录下.

## 可能遇到的问题
1. 可能在构建时遇见找不到 Jar 的错误，可尝试删除`build/libs`后重试
2. Dex 编译失败，可能是因为没有安装`Android Sdk`或`build-tools`版本太低，可尝试重新安装`Android Sdk`或更新`build-tools`版本
3. gradle脚本有时可能抽风全红，重载一下或许可以解决
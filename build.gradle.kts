plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

repositories {
    mavenCentral()
    maven("https://jcenter.bintray.com/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvnrepository.com/artifact/net.md-5/bungeecord-api")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.minebench.de/")
}

dependencies {
    annotationProcessor("com.velocitypowered", "velocity-api", "3.2.0-SNAPSHOT")
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", "1.8.21")
    implementation("club.minnced", "discord-webhooks", "0.4.0")
    implementation("net.kyori", "adventure-api", "4.8.1")
    implementation("net.kyori", "adventure-platform-bungeecord", "4.3.1")
    implementation("net.kyori", "adventure-text-minimessage", "4.14.0")
    implementation("net.kyori", "adventure-text-serializer-legacy", "4.14.0")
    implementation("net.kyori", "adventure-text-serializer-plain", "4.14.0")
    compileOnly("com.velocitypowered", "velocity-api", "3.2.0-SNAPSHOT")
    compileOnly("net.md-5", "bungeecord-api", "1.20-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc", "plugin-annotations", "1.2.2-SNAPSHOT")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<Jar> {
    version = "1.0-SNAPSHOT"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

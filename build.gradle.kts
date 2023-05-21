plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
}

repositories {
    mavenCentral()
    maven("https://jcenter.bintray.com/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvnrepository.com/artifact/net.md-5/bungeecord-api")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", "1.8.21")
    implementation("club.minnced", "discord-webhooks", "0.4.0")
    implementation("net.kyori", "adventure-api", "4.8.1")
    implementation("net.kyori", "adventure-text-serializer-legacy", "4.8.1")
    compileOnly("net.md-5", "bungeecord-api", "1.17-R0.1-SNAPSHOT")
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

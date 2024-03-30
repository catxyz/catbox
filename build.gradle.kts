plugins {
    id("xyz.jpenilla.run-paper") version "2.2.0"
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    implementation("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("com.comphenix.protocol:ProtocolLib:5.1.0")
}

tasks {
    runServer {
        minecraftVersion("1.20.1")
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+AllowRedefinitionToAddDeleteMethods")
}
plugins {
    java
}

group = "io.github.fisher2911"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly(files("libs/HMCLeaves-1.0.0.jar"))
}

java.sourceCompatibility = JavaVersion.toVersion(21)
java.targetCompatibility = JavaVersion.toVersion(21)

tasks {

    jar {
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

}

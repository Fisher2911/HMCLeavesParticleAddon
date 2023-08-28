plugins {
    java
}

group = "io.github.fisher2911"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly(files("libs/HMCLeaves-1.1.1.jar"))
}

tasks {

    jar {
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

}

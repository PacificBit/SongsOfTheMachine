plugins {
    `java-library`
}

base {
    archivesName.set("Songsofthemachine")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

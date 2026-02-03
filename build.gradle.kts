plugins {
    `java-library`
}

base {
    archivesName.set("SongsOfTheMachine")
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

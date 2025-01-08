plugins {
    id("groovy")
    id("java")
    id("jvm-test-suite")
    id("jacoco")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.groovy)

    // Use the awesome Spock testing and specification framework even with Java
    testImplementation(libs.spock.core)

    implementation(libs.guice)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock()
        }
    }
}

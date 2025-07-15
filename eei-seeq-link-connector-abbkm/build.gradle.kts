plugins {
    java
    distribution
}

group = "com.abbkmv4.seeq.link.connector"
version = "100.1.0"

dependencies {
    compileOnly("com.seeq.link:seeq-link-sdk:${project.properties["seeqLinkSDKVersion"]}")
    testImplementation("com.seeq.link:seeq-link-sdk:${project.properties["seeqLinkSDKVersion"]}")
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation(testFixtures("com.seeq.link:seeq-link-sdk:${project.properties["seeqLinkSDKVersion"]}"))
}

tasks {
    jar {
        doFirst {
            val files = configurations.runtimeClasspath.get().files
            if (files.isNotEmpty()) {
                manifest.attributes["Class-Path"] = files.joinToString(" ") { "lib/${it.name}" }
            }
        }
    }

    withType<Jar>().configureEach {
        manifest.attributes(
            "Version" to project.version,
            "Minimum-Seeq-Link-SDK-Version" to "${project.properties["seeqLinkSDKVersion"]}"
        )
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

distributions {
    main {
        contents {
            from(tasks.jar)
            into("lib") {
                from(configurations.runtimeClasspath)
            }
        }
    }
}

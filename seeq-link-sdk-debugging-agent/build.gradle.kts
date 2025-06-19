plugins {
    application
}

dependencies {
    // Note: The version of the Seeq Link Agent is always set to the same version as the Seeq Link SDK it provides
    implementation("com.seeq.link:seeq-link-agent:${project.properties["seeqLinkSDKVersion"]}")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        // Comment out the mycompany below if not wanting to run it from the debug agent.
        // dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
        dependsOn(":eei-seeq-link-connector-abbkm:installDist")
    }
}


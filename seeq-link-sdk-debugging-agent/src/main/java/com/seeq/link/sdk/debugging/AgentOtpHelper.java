package com.seeq.link.sdk.debugging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.seeq.link.sdk.services.DefaultFileBasedSecretsManager;
import com.seeq.link.sdk.utilities.AgentHelper;
import com.seeq.utilities.SeeqNames;

/**
 * This helper now only “consumes” the OTP on the very first run.
 * If the secret is already in the SecretsManager, it does nothing.
 */

public class AgentOtpHelper {
    private static final String AGENT_ONE_TIME_PASSWORD_PLACEHOLDER = "<your_agent_one_time_password>";

    private static final Path OtpFilePath =
            Path.of(new File(Main.class.getClassLoader().getResource("data/").getPath()).getAbsolutePath())
                    .getParent().getParent().getParent().getParent()
                    .resolve("src").resolve("main").resolve("resources").resolve("data")
                    .resolve("keys").resolve("agent.otp");

    public static void setupAgentOtp(Path seeqDataFolder, String agentName) {
        AgentHelper agentHelper = new AgentHelper(agentName);
        String secretName = agentHelper.getProvisionedAgentUsername()
                + "|PRE_PROVISIONED_ONE_TIME_PASSWORD";

        Path secretsPath = seeqDataFolder
                .resolve(SeeqNames.Agents.AgentKeysFolderName)
                .resolve("agent.keys");
        DefaultFileBasedSecretsManager secretsManager;
        try {
            secretsManager = new DefaultFileBasedSecretsManager(secretsPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open SecretsManager", e);
        }

        String existingSecret = secretsManager.getSecret(secretName);
        if (existingSecret != null && !existingSecret.isEmpty()) {
            // Already provisioned → do nothing
            return;
        }

        // Read the OTP from the file (this method still throws RuntimeException if it fails)
        String agentOneTimePassword = readAgentOneTimePassword();
        if (agentOneTimePassword == null || agentOneTimePassword.isEmpty()) {
            throw new IllegalStateException(
                    "Expected a valid one-time password in " + OtpFilePath +
                            ", but found none or placeholder."
            );
        }

        // If putSecret(...) also doesn’t declare IOException, you can call it directly:
        secretsManager.putSecret(secretName, agentOneTimePassword);

        // Finally, wipe out the source so it doesn’t stay in version control
        resetAgentOneTimePasswordFile();
    }

    private static String readAgentOneTimePassword() {
        if (!Files.exists(OtpFilePath)) {
            return null;
        }
        try {
            return Files.readString(OtpFilePath, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read OTP file: " + OtpFilePath, e);
        }
    }

    private static void resetAgentOneTimePasswordFile() {
        try {
            Files.writeString(OtpFilePath, AGENT_ONE_TIME_PASSWORD_PLACEHOLDER);
        } catch (IOException e) {
            throw new RuntimeException("Cannot reset OTP file: " + OtpFilePath, e);
        }
    }
}

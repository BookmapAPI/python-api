package com.bookmap.python.api.addon.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Future;

/**
 * Validates python version returning error if it is not a third version
 */
public class PythonEnvironmentValidator implements BuildValidator<String> {

    @Override
    public Future<String> validate(String entityToValidate) {
        return VALIDATING_EXECUTOR.submit(() -> {
            var process = new ProcessBuilder(entityToValidate, "--version");
            Process versionCheck = process.start();
            versionCheck.waitFor();
            String versionLine = null;
            // python2 sends info about version to stderr, just tested it with local version
            try (var stream = new BufferedReader(new InputStreamReader(versionCheck.getErrorStream()))) {
                versionLine = stream.readLine();
            }

            // if this is python3, it sends version to stdin
            if (versionLine == null) {
                try (var stream = new BufferedReader(new InputStreamReader(versionCheck.getInputStream()))) {
                    versionLine = stream.readLine();
                }
            }

            if (versionLine == null) {
                return "Failed to validate Python version. Is the path '" + entityToValidate + "' correct?";
            }
            String[] versionTokens = versionLine.split(" ");

            if (versionTokens.length != 2 || !"Python".equalsIgnoreCase(versionTokens[0])) {
                return "Please click 'Set custom runtime' and select your Python 3.x executable.";
            }

            String exactVersion = versionTokens[1];
            if (!exactVersion.startsWith("3.")) {
                return "You selected Python " + versionTokens[1] + ". Please select a Python 3.x executable.";
            }

            return null;
        });
    }
}

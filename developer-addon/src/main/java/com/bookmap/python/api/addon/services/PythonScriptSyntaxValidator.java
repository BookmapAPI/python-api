package com.bookmap.python.api.addon.services;

import com.bookmap.python.api.addon.utils.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Tries to build script and see whether it has syntax errors
 */
public class PythonScriptSyntaxValidator implements BuildValidator<Path> {

    private final String pythonRuntime;

    public PythonScriptSyntaxValidator(String pathToPythonRuntime) {
        this.pythonRuntime = pathToPythonRuntime;
    }

    @Override
    public Future<String> validate(Path fileToValidate) {
        return VALIDATING_EXECUTOR.submit(() -> {
            Process process = null;
            ProcessBuilder processBuilder = new ProcessBuilder()
                .command(pythonRuntime, "-m", "py_compile", fileToValidate.toAbsolutePath().toString());
            try {
                process = processBuilder.start();
                process.waitFor();

                String result;
                try (var stream = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    result = stream.lines().collect(Collectors.joining("\n"));
                }

                if (result.isBlank()) {
                    return null;
                }
                Log.info("Script contains errors: " + result);
                return result;
            } catch (IOException e) {
                Log.error("Failed to validate script", e);
                return e.getMessage();
            } finally {
                if (process != null) {
                    process.destroyForcibly();
                }
            }
        });
    }
}

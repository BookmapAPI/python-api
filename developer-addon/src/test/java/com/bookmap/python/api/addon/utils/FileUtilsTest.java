package com.bookmap.python.api.addon.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {

    @BeforeAll
    public static void generateTmpDir() {
        var resourcesDir = Paths.get("src", "test", "resources");
        resourcesDir.resolve("tmp").toFile().mkdir();
    }

    @Test
    public void testAddingItemToJar() throws IOException {
        File serversideRpcJar = Path.of("src/test/resources/serverside-rpc.jar").toFile();
        File newItemPath = Path.of("src/test/resources/test.txt").toFile();
        Path copiedFile = Path.of("src/test/resources/tmp/serverside-rpc-copy.jar");
        copiedFile.toFile().createNewFile();
        Files.copy(serversideRpcJar.toPath(), new FileOutputStream(copiedFile.toFile()));

        try {
            // do test
            FileUtils.addItemToJar(serversideRpcJar.toPath(), newItemPath.toPath());

            JarFile withAddedItem = new JarFile(serversideRpcJar.getAbsoluteFile());
            JarFile withoutAddedItem = new JarFile(copiedFile.toFile());

            var entriesEnumeration = withAddedItem.entries();
            boolean isTestFileIncluded = false;
            String testFileText = null;
            while (entriesEnumeration.hasMoreElements()) {
                JarEntry entryFromJarWithTestFile = entriesEnumeration.nextElement();
                String name = entryFromJarWithTestFile.getName();
                JarEntry entryFromJarWithoutTestFile = withoutAddedItem.getJarEntry(name);
                if ("test.txt".equals(name)) {
                    isTestFileIncluded = true;
                    if (entryFromJarWithoutTestFile != null) {
                        // check that new item was not added to a basic jar
                        Assertions.fail("Seems like test.txt is inside the initial jar...");
                    }
                    testFileText = new String(withAddedItem.getInputStream(entryFromJarWithTestFile).readAllBytes());
                } else {
                    if (entryFromJarWithoutTestFile != null) {
                        Assertions.assertEquals(
                            entryFromJarWithTestFile.getCrc(),
                            entryFromJarWithoutTestFile.getCrc(),
                            "Entries " + name + "have different names"
                        );
                    } else {
                        Assertions.fail("Entry from jar without test file is null. Entry name = " + name);
                    }
                }
            }

            // check that file was included and that it was not changed
            Assertions.assertTrue(isTestFileIncluded);
            Assertions.assertEquals("I AM TESTING!!!", testFileText);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assertions.fail("Failed", ex);
        } finally {
            Files.copy(
                copiedFile,
                copiedFile.getParent().getParent().resolve("serverside-rpc.jar"),
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    @Test
    public void testRemoveDir() throws IOException {
        File serversideRpcJar = Path.of("src/test/resources/testDir").toFile();
        boolean isDirCreated = serversideRpcJar.mkdir();
        Assertions.assertTrue(isDirCreated, "Failed to create test dir");
        FileUtils.removeDirectory(serversideRpcJar);
        Assertions.assertFalse(serversideRpcJar.exists());
    }

    @AfterAll
    public static void cleanTmpDir() throws IOException {
        try (var stream = Files.walk(Path.of("src", "test", "resources", "tmp"))) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }
}

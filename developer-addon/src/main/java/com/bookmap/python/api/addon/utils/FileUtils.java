package com.bookmap.python.api.addon.utils;

import static java.nio.file.Files.copy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

public class FileUtils {

    // 4MB buffer
    private static final byte[] BUFFER = new byte[4096 * 1024];

    /**
     * Removes file or directory subsequently removing all its child files
     *
     * @param directory initial directory
     */
    public static void deleteFileTree(File directory) throws IOException {
        // taken with slight changes from https://www.baeldung.com/java-delete-directory
        try (Stream<Path> fileStream = Files.walk(directory.toPath())) {
            fileStream
                .sorted(Comparator.reverseOrder()) // reverse lexicographic order to delete leaves of the tree first
                .map(Path::toFile)
                .forEach(f -> {
                    boolean isDeleted = f.delete();
                    if (!isDeleted) {
                        Log.warn("Failed to delete " + f.getAbsolutePath());
                    }
                });
        }

        Files.deleteIfExists(directory.toPath());
    }

    /**
     * This method unpacks files from the Jar file to which {@link FileUtils} class is wrapped. If resource name
     * points to directory inside of a jar file, the same directory (including all subdirectory and files) will be created
     * under destination path. If files already exist in the destination directory, the will be overwritten.
     *
     * @param resourceName name of the file/directory inside of a jar
     * @param destination  destination file to which resource file/directory will be written
     * @throws IOException           if error occur during copying
     * @throws IllegalStateException if file can not be found inside of a jar or if jar file is not accesible
     */
    public static void copyResourceAndAddNewItemsToIt(String resourceName, Path destination) throws IOException {
        URL resourceUrlPath = FileUtils.class.getClassLoader().getResource(resourceName);
        if (resourceUrlPath == null) {
            throw new IllegalStateException("Failed to find resource");
        }

        // fetch jar file name from the connection to the resource
        JarURLConnection jarConnection = (JarURLConnection) resourceUrlPath.openConnection();
        JarFile currentJarFile = jarConnection.getJarFile();

        Enumeration<JarEntry> entries = currentJarFile.entries();
        // go through all jar entries recursively
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            // handle only jar entries related to the required resource
            if (name.startsWith(resourceName)) {
                if (jarEntry.isDirectory()) {
                    destination.resolve(jarEntry.getName()).toFile().mkdirs();
                } else {
                    copy(currentJarFile.getInputStream(jarEntry), destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * copy input to output stream - available in several StreamUtils or Streams classes
     */
    private static void copyStreams(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER)) != -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }

    // taken with slight changes from https://stackoverflow.com/questions/2223434/appending-files-to-a-zip-file-with-java
    public static void addItemToJar(Path pathToInitialJar, Path item) throws IOException {
        File tmpFile = pathToInitialJar.getParent().resolve("tmp.jar").toFile();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
        JarFile jarFile = new JarFile(pathToInitialJar.toFile());
        Enumeration<? extends JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            e.setCompressedSize(-1);
            jarOutputStream.putNextEntry(e);
            if (!e.isDirectory()) {
                copyStreams(jarFile.getInputStream(e), jarOutputStream);
            }
            jarOutputStream.closeEntry();
        }

        // now append some extra content
        JarEntry e = new JarEntry(item.toFile().getName());
        e.setCompressedSize(-1);
        jarOutputStream.putNextEntry(e);
        try (FileInputStream inputStream = new FileInputStream(item.toAbsolutePath().toFile())) {
            copyStreams(inputStream, jarOutputStream);
        }
        jarOutputStream.closeEntry();
        jarOutputStream.close();
        jarFile.close();

        Files.move(tmpFile.toPath(), pathToInitialJar.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
    }

    // taken with slight changes from https://stackoverflow.com/questions/2223434/appending-files-to-a-zip-file-with-java
    public static void addItemToJar(Path pathToInitialJar, String itemName, byte[] item) throws IOException {
        File tmpFile = pathToInitialJar.getParent().resolve("tmp.jar").toFile();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
        JarFile jarFile = new JarFile(pathToInitialJar.toFile());
        Enumeration<? extends JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            jarOutputStream.putNextEntry(e);
            if (!e.isDirectory()) {
                copyStreams(jarFile.getInputStream(e), jarOutputStream);
            }
            jarOutputStream.closeEntry();
        }

        // now append some extra content
        JarEntry e = new JarEntry(itemName);
        jarOutputStream.putNextEntry(e);
        copyStreams(new ByteArrayInputStream(item), jarOutputStream);
        jarOutputStream.closeEntry();

        jarOutputStream.close();
        jarFile.close();

        Files.move(tmpFile.toPath(), pathToInitialJar.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void removeEntryFromJar(Path pathToJar, String canonicalClassName) {
        try (FileSystem fs = FileSystems.newFileSystem(pathToJar, Map.of("create", "false"))) {
            var pathToFile = fs.getPath(canonicalClassName);
            Files.delete(pathToFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readClassFromJarToByteArray(Path jar, String className) throws IOException {
        try (var jarFile = new JarFile(jar.toFile())) {
            Enumeration<? extends JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (e.getName().equals(className)) {
                    try (var stream = jarFile.getInputStream(e)) {
                        return stream.readAllBytes();
                    }
                }
            }
        }
        return null;
    }

    public static void removeDirectory(File file) throws IOException {
        try (var stream = Files.walk(file.toPath())) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }
}

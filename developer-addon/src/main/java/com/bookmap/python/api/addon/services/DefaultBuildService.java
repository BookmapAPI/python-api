package com.bookmap.python.api.addon.services;

import com.bookmap.python.api.addon.DeveloperAddon;
import com.bookmap.python.api.addon.asm.ChangeAddonNameVisitor;
import com.bookmap.python.api.addon.asm.RemoveTradingAnnotationVisitor;
import com.bookmap.python.api.addon.exceptions.FailedToBuildException;
import com.bookmap.python.api.addon.utils.FileUtils;
import com.bookmap.python.api.addon.utils.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

/**
 * Very simple service
 */
public class DefaultBuildService implements BuildService<Path> {

    private static final String BASIC_RPC_ADDON_PACKAGE_NAME = "com.bookmap.api.rpc.server.addon";
    private static final String BASIC_RPC_ADDON_CLASS_NAME = "RpcServerAddon";

    private final String tcpPort;
    private final String runtimePath;
    private final boolean isTradingStrategy;

    public DefaultBuildService(String tcpPort, String runtimePath, boolean isTradingStrategy) {
        this.tcpPort = tcpPort;
        this.runtimePath = runtimePath;
        this.isTradingStrategy = isTradingStrategy;
    }

    @Override
    public void build(String addonName, Path source) throws FailedToBuildException {
        var sourceFile = source.toFile();

        Path buildDirectory = DeveloperAddon.BUILD_DIR
            .toPath()
            .resolve(".tmp")
            .resolve(sourceFile.getName().replace(".py", ""));

        Path pathToScriptFromTmpDir = buildDirectory.resolve("script.py");
        buildDirectory.toFile().mkdirs();

        try {
            Files.copy(source, pathToScriptFromTmpDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FailedToBuildException(e);
        }

        mkdirIfDoesNotExist(buildDirectory.toFile());
        Path builtJarPath = DeveloperAddon.BUILD_DIR.toPath().resolve(sourceFile.getName().replace(".py", ".jar"));
        try {
            FileUtils.copyResourceAndAddNewItemsToIt("serverside-rpc.jar", builtJarPath);
            FileUtils.addItemToJar(builtJarPath, pathToScriptFromTmpDir);
            editClass(addonName, builtJarPath);
            createAndAddProperties(builtJarPath);
            Files.deleteIfExists(pathToScriptFromTmpDir);
        } catch (FileSystemException e) {
            Log.warn("Failed to build due to file system exception", e);
            throw new FailedToBuildException(
                String.format(
                    "%s.\nPlease, check if you have addon \"%s\" opened in Bookmap. Remove it and try to build again.\n" +
                    "If problem wasn't solved please contact support@bookmap.com",
                    e.getMessage(),
                    addonName
                ),
                e
            );
        } catch (IOException e) {
            Log.warn("Failed to build", e);
            throw new FailedToBuildException("Failed to build addon", e);
        }
    }

    private void mkdirIfDoesNotExist(File file) throws FailedToBuildException {
        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
            if (!isCreated) {
                throw new FailedToBuildException("Failed to build addon. Can't create directory " + file.toPath());
            }
        } else if (!file.isDirectory()) {
            throw new FailedToBuildException(
                "Can't create " + file.toPath() + ", there is a file with the same name and it is not a directory"
            );
        }
    }

    private void editClass(String addonName, Path addonPath) throws IOException, FailedToBuildException {
        String basicClassName = BASIC_RPC_ADDON_PACKAGE_NAME + "." + BASIC_RPC_ADDON_CLASS_NAME;
        String basicClassNameWithExtension = basicClassName.replaceAll("\\.", "/") + ".class";

        byte[] clazz = FileUtils.readClassFromJarToByteArray(addonPath, basicClassNameWithExtension);
        if (clazz == null) {
            Log.error("Addon file not found, is jar file corrupted?");
            throw new FailedToBuildException("File not found");
        }
        var classWriter = new ClassWriter(0);
        var classReader = new ClassReader(clazz);
        String buildAddonName = addonName.replaceAll("\\s", "_");
        var changeAddonNameVisitor = new ChangeAddonNameVisitor(addonName, classWriter);
        var removeTradingAnnotationVisitor = new RemoveTradingAnnotationVisitor(
            changeAddonNameVisitor,
            isTradingStrategy
        );

        String newClassName = BASIC_RPC_ADDON_PACKAGE_NAME.replaceAll("\\.", "/") + "/" + buildAddonName;
        var mapNameClassVisitor = new ClassRemapper(
            removeTradingAnnotationVisitor,
            new SimpleRemapper(basicClassName.replaceAll("\\.", "/"), newClassName)
        );

        classReader.accept(mapNameClassVisitor, 0);
        var newClass = classWriter.toByteArray();
        FileUtils.removeEntryFromJar(addonPath, basicClassNameWithExtension);

        String newClassNameWithExtension = newClassName + ".class";
        FileUtils.addItemToJar(addonPath, newClassNameWithExtension, newClass);
    }

    private void createAndAddProperties(Path builtJarPath) throws IOException {
        Properties properties = new Properties();
        properties.put("python_runtime", runtimePath);
        properties.put("tcp_port", tcpPort);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream(2048);
        properties.store(byteBuffer, null);
        FileUtils.addItemToJar(builtJarPath, "addon.properties", byteBuffer.toByteArray());
        byteBuffer.close();
    }
}

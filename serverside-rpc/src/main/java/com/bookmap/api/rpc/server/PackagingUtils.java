package com.bookmap.api.rpc.server;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackagingUtils {

	/**
	 * This method unpacks files from the Jar file to which {@link PackagingUtils} class is wrapped. If resource name
	 * points to directory inside of a jar file, the same directory (including all subdirectory and files) will be created
	 * under destination path. If files already exist in the destination directory, the will be overwritten.
	 *
	 * @param resourceName name of the file/directory inside of a jar
	 * @param destination  destination file to which resource file/directory will be written
	 * @throws IOException           if error occur during copying
	 * @throws IllegalStateException if file can not be found inside of a jar or if jar file is not accesible
	 */
	public static void unpackSpecificResourceFilesFromTheCurrentJar(String resourceName, Path destination)
			throws IOException {
		URL resourceUrlPath = PackagingUtils.class.getClassLoader().getResource(resourceName);
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
					Files.copy(
							currentJarFile.getInputStream(jarEntry),
							destination.resolve(jarEntry.getName()),
							StandardCopyOption.REPLACE_EXISTING
					);
				}
			}
		}
	}
}

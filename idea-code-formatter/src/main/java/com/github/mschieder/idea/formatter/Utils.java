package com.github.mschieder.idea.formatter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Utils {

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static void deleteDir(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    public static void unzipZippedFileFromResource(InputStream is, File outputDir) throws IOException {
        long started = System.currentTimeMillis();
        File zippedFile = new File(outputDir, "ide.zip");
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(zippedFile))) {
            is.transferTo(os);
        }
        unzipFromFile(zippedFile, outputDir);

        log.log(Level.FINE, "unzipped in {0} ms", System.currentTimeMillis() - started);
    }

    public static void unzipFromFile(File zippedFile, File outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zippedFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = new BufferedInputStream(zipFile.getInputStream(entry)); OutputStream out = new BufferedOutputStream(new FileOutputStream(entryDestination))) {
                        in.transferTo(out);
                    }
                }
            }
        }

    }

    public static String getJarName() {
        return getJarName(Utils.class);
    }

    public static boolean isPackagedInJar() {
        return getJarName().endsWith("jar");
    }


    public static String getJarName(Class<?> theClass) {
        return new File(theClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    public static String getJarPath(Class<?> theClass) {
        return theClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}

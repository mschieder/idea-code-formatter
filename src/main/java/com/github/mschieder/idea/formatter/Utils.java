package com.github.mschieder.idea.formatter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Utils {

    public static void deleteDir(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static void unzip(InputStream is, File outputDir) throws IOException {
        File zippedFile = new File(outputDir, "ide.zip");
        try (FileOutputStream os = new FileOutputStream(zippedFile)) {
            is.transferTo(os);
        }
        unzip(zippedFile, outputDir);
    }

    public static void unzip(File zippedFile, File outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zippedFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        in.transferTo(out);
                    }
                }
            }
        }
    }

}

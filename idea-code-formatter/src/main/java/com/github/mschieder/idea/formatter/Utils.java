package com.github.mschieder.idea.formatter;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class.getName());

    public static void deleteDir(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    public static void unzipZippedFileFromResource(InputStream is, File outputDir) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        File zippedFile = new File(outputDir, "ide.zip");
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(zippedFile))) {
            is.transferTo(os);
        }
        unzipFromFile(zippedFile, outputDir);

        stopwatch.stop();
        log.info("unzipped in {} ms", stopwatch.elapsed().toMillis());
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

    public static void unzipFromStream(InputStream is, File outputDir) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try (ZipInputStream zipInputstream = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zipInputstream.getNextEntry()) != null) {
                File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(entryDestination)) {
                        copy(zipInputstream, out);
                    }
                }
            }
        }
        stopwatch.stop();
        log.info("unzipped in {] ms", stopwatch.elapsed().toMillis());
    }

    private static void copy(final InputStream source, final OutputStream target) throws IOException {
        final int bufferSize = 4 * 1024;
        final byte[] buffer = new byte[bufferSize];

        int nextCount;
        while ((nextCount = source.read(buffer)) >= 0) {
            target.write(buffer, 0, nextCount);
        }
    }


    public static String getJarName() {
        return getJarName(Utils.class);
    }

    public static boolean isPackagedInJar() {
        return getJarName().endsWith("jar");
    }


    public static String getJarName(Class theClass) {
        return new File(theClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    public static String getJarPath(Class theClass) {
        return theClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}

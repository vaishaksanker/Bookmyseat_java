package com.seminarhall.ui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads the web app's event photos (Unsplash URLs) in the background, like the browser does.
 * Images are cached in memory and in ~/.bookmyseat/image-cache so later starts work offline.
 * Callers get null until an image is ready, then their callback runs on the Swing thread.
 */
public final class RemoteImages {

    private static final Path DISK_CACHE = Path.of(System.getProperty("user.home"), ".bookmyseat", "image-cache");
    private static final Map<String, BufferedImage> MEMORY = new ConcurrentHashMap<>();
    private static final Map<String, List<Runnable>> PENDING = new ConcurrentHashMap<>();
    private static final ExecutorService LOADER = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });

    private RemoteImages() {
    }

    /** The image if it is already loaded, otherwise null (and starts loading it, calling onLoaded when done). */
    public static BufferedImage get(String url, Runnable onLoaded) {
        if (url == null || url.isBlank()) {
            return null;
        }
        BufferedImage image = MEMORY.get(url);
        if (image != null) {
            return image;
        }
        boolean[] first = {false};
        PENDING.compute(url, (k, callbacks) -> {
            if (callbacks == null) {
                callbacks = new ArrayList<>();
                first[0] = true;
            }
            if (onLoaded != null) {
                callbacks.add(onLoaded);
            }
            return callbacks;
        });
        if (first[0]) {
            LOADER.submit(() -> load(url));
        }
        return null;
    }

    /** Number of images still downloading (used by screenshot tests to wait for photos). */
    public static int pendingCount() {
        return PENDING.size();
    }

    private static void load(String url) {
        BufferedImage image = null;
        try {
            image = readFromDisk(url);
            if (image == null) {
                image = download(url);
            }
        } catch (Exception e) {
            System.err.println("Image unavailable (" + e.getMessage() + "): " + url);
        }
        if (image != null) {
            MEMORY.put(url, image);
        }
        List<Runnable> callbacks = PENDING.remove(url);
        if (image != null && callbacks != null) {
            SwingUtilities.invokeLater(() -> callbacks.forEach(Runnable::run));
        }
    }

    private static BufferedImage readFromDisk(String url) throws IOException {
        Path file = cacheFile(url);
        return Files.exists(file) ? ImageIO.read(file.toFile()) : null;
    }

    private static BufferedImage download(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Accept", "image/jpeg,image/png");   // ImageIO cannot decode WebP/AVIF
        conn.setRequestProperty("User-Agent", "SeminarX-Desktop");
        byte[] bytes;
        try (InputStream in = conn.getInputStream()) {
            bytes = in.readAllBytes();
        }
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
        if (image != null) {
            try {
                Files.createDirectories(DISK_CACHE);
                Files.write(cacheFile(url), bytes);
            } catch (IOException ignored) {
                // cache is optional
            }
        }
        return image;
    }

    private static Path cacheFile(String url) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(url.getBytes(StandardCharsets.UTF_8));
            return DISK_CACHE.resolve(HexFormat.of().formatHex(hash) + ".img");
        } catch (Exception e) {
            return DISK_CACHE.resolve(Integer.toHexString(url.hashCode()) + ".img");
        }
    }
}

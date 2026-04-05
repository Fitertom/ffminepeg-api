package info.sigmaclient.ffminepeg.api;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class FfminepegApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("ffminepeg-api");
    private static final Object INIT_LOCK = new Object();
    private static volatile boolean initAttempted = false;
    private static volatile boolean available = false;
    private static volatile Path ffmpegPath = null;

    private static final String[] WIN_FILES = new String[] {
            "ffmpeg.exe",
            "avcodec.dll",
            "avcodec-62.dll",
            "avdevice.dll",
            "avdevice-62.dll",
            "avfilter.dll",
            "avfilter-11.dll",
            "avformat.dll",
            "avformat-62.dll",
            "avutil.dll",
            "avutil-60.dll",
            "swresample.dll",
            "swresample-6.dll",
            "swscale.dll",
            "swscale-9.dll",
            "libbz2-1.dll",
            "libcrypto-3-x64.dll",
            "libssl-3-x64.dll",
            "libgcc_s_seh-1.dll",
            "libiconv-2.dll",
            "libmp3lame-0.dll",
            "libwinpthread-1.dll",
            "zlib1.dll"
    };

    private FfminepegApi() {
    }

    public static void ensureReady() {
        if (initAttempted) {
            return;
        }
        synchronized (INIT_LOCK) {
            if (initAttempted) {
                return;
            }
            initAttempted = true;
            initializeInternal();
        }
    }

    public static boolean isAvailable() {
        ensureReady();
        return available;
    }

    public static Path getFfmpegPath() {
        ensureReady();
        if (!available || ffmpegPath == null) {
            throw new IllegalStateException("FFMinepeg API is not available on this platform or failed to initialize.");
        }
        return ffmpegPath;
    }

    public static ProcessBuilder buildFfmpegProcess(String... args) {
        Path exe = getFfmpegPath();
        java.util.List<String> cmd = new java.util.ArrayList<>(args.length + 1);
        cmd.add(exe.toString());
        java.util.Collections.addAll(cmd, args);
        return new ProcessBuilder(cmd);
    }

    private static void initializeInternal() {
        if (!isWindows()) {
            available = false;
            LOGGER.warn("FFMinepeg API: non-Windows OS detected, ffmpeg runtime not available.");
            return;
        }

        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path binDir = gameDir.resolve("ffminepeg").resolve("bin").resolve("win-x64");
        try {
            Files.createDirectories(binDir);
            for (String file : WIN_FILES) {
                String resourcePath = "assets/ffminepeg_api/bin/win-x64/" + file;
                Path out = binDir.resolve(file);
                copyResourceIfChanged(resourcePath, out);
            }
            ffmpegPath = binDir.resolve("ffmpeg.exe");
            available = Files.exists(ffmpegPath);
            if (available) {
                LOGGER.info("FFMinepeg API ready: {}", ffmpegPath);
            } else {
                LOGGER.error("FFMinepeg API failed to locate ffmpeg.exe after extraction.");
            }
        } catch (Exception e) {
            available = false;
            LOGGER.error("FFMinepeg API initialization failed.", e);
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private static void copyResourceIfChanged(String resourcePath, Path out) throws IOException {
        byte[] resourceData = readResource(resourcePath);
        if (resourceData == null || resourceData.length == 0) {
            throw new IOException("Missing bundled resource: " + resourcePath);
        }

        if (Files.exists(out)) {
            byte[] current = Files.readAllBytes(out);
            if (sha256(resourceData).equals(sha256(current))) {
                return;
            }
        }

        Files.write(out, resourceData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static byte[] readResource(String path) throws IOException {
        ClassLoader loader = FfminepegApi.class.getClassLoader();
        try (InputStream is = loader.getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            return is.readAllBytes();
        }
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }
}

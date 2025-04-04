package cn.stars.reversal.util.render.video;

import cn.stars.reversal.util.ReversalLogger;
import cn.stars.reversal.util.misc.FileUtil;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;

public class VideoManager {
    private static final File dictionary = new File(Minecraft.getMinecraft().mcDataDir, "Reversal/Background");
    private static final File backgroundFile = new File(dictionary, "background.mp4");
    private static final File splashFile = new File(dictionary, "splash.mp4");

    @SuppressWarnings("all")
    @SneakyThrows
    public static void loadFiles() {
        if (!dictionary.exists()) {
            dictionary.mkdirs();
        }
        if (!backgroundFile.exists()) {
            FileUtil.unpackFile(backgroundFile, "assets/minecraft/reversal/background.mp4");
        }

        if (!splashFile.exists()) {
            FileUtil.unpackFile(splashFile, "assets/minecraft/reversal/splash.mp4");
        }
    }

    @SneakyThrows
    public static void loadSplash() {
        if (!splashFile.exists()) {
            ReversalLogger.error("Splash file not found, this should not happen! Reload files.");
            loadFiles();
        }
        VideoUtil.init(splashFile);
    }

    @SneakyThrows
    public static void loadBackground() {
        if (!backgroundFile.exists()) {
            ReversalLogger.error("Background file not found, this should not happen! Reload files.");
            loadFiles();
        }
        VideoUtil.init(backgroundFile);
    }
}

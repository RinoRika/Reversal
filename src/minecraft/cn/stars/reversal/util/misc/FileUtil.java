package cn.stars.reversal.util.misc;

import cn.stars.reversal.Reversal;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Objects;

@SuppressWarnings("all")
public class FileUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final String SEPARATOR = File.separator;

    public static final String REVERSAL_PATH = mc.mcDataDir.getAbsolutePath() + SEPARATOR + "Reversal" + SEPARATOR;

    public static boolean coreDirectoryExists() {
        return new File(REVERSAL_PATH).exists();
    }

    public static boolean exists(final String fileName) {
        return getFileOrPath(fileName).exists();
    }

    public static void unpackFile(File file, String name) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(Objects.requireNonNull(Reversal.class.getClassLoader().getResourceAsStream(name)), fos);
        fos.close();
    }

    public static void saveFile(final String fileName, final boolean override, final String content) {
        BufferedWriter writer = null;
        try {
            final File file = getFileOrPath(fileName);
            if (!file.exists()) {
                createCoreDirectory();
                file.createNewFile();
            } else if (!override) {
                return;
            }

            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();
        } catch (final Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        }

    }

    public static String loadFile(final String fileName) {
        try {
            final File file = getFileOrPath(fileName);
            if (!file.exists()) return null;

            final BufferedReader reader = new BufferedReader(new FileReader(file));
            String content = reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                content += "\r\n" + line;
            }

            reader.close();

            return content;
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new IllegalStateException("Failed to read file!");
        }
    }

    public static void createCoreDirectory() {
        new File(REVERSAL_PATH).mkdirs();
    }

    public static void createDirectory(final String directoryName) {
        getFileOrPath(directoryName.replace("\\", SEPARATOR)).mkdirs();
    }

    public static void createFile(final String fileName) {
        try {
            getFileOrPath(fileName).mkdirs();
            getFileOrPath(fileName).createNewFile();
        } catch (final Throwable t) {
            throw new IllegalStateException("Unable to create Core directory!", t);
        }
    }

    public static File[] listFiles(final String path) {
        return getFileOrPath(path).listFiles();
    }

    public static File[] listFiles(final File file) {
        return file.listFiles();
    }

    public static File getFileOrPath(final String fileName) {
        return new File(REVERSAL_PATH + fileName.replace("\\", SEPARATOR));
    }

    public static void delete(final String fileName) {
        if (getFileOrPath(fileName).exists()) {
            if (!getFileOrPath(fileName).delete()) throw new IllegalStateException("Unable to delete file!");
        }
    }

    public static void delete(final File file) {
        if (file.exists()) {
            if (!file.delete()) throw new IllegalStateException("Unable to delete file!");
        }
    }
}

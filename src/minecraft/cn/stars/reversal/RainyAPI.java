/*
 * Reversal Client - A PVP Client with hack visual.
 * Copyright 2025 Aerolite Society, Some rights reserved.
 */
package cn.stars.reversal;

import cn.stars.reversal.ui.atmoic.mainmenu.AtomicMenu;
import cn.stars.reversal.ui.notification.NotificationType;
import cn.stars.reversal.util.ReversalLogger;
import cn.stars.reversal.util.math.MathUtil;
import cn.stars.reversal.util.math.RandomUtil;
import cn.stars.reversal.util.misc.FileUtil;
import cn.stars.reversal.util.render.video.BackgroundManager;
import cn.stars.reversal.util.render.video.VideoUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;

@Getter
@Setter
@SuppressWarnings("all")
public class RainyAPI {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static long window;
    public static int randomTitleId = -1;

    public static boolean hasJavaFX = true;

    /**
     * 客户端设置
     * 在AtomicMenu#ReversalSettingsGui里可以对选项进行修改，在游戏启动时使用loadAPI()加载保存的数据
     */
    public static int backgroundId = 9;
    public static boolean isShaderCompatibility = false;
    public static boolean isSplashScreenDisabled = false;
    public static boolean mainMenuDate = false;
    public static boolean guiSnow = false;
    public static boolean backgroundBlur = false;
    public static boolean imageScreen = false;
    public static boolean menuBubble = false;
    public static boolean asyncLoading = true;

    /**
     * 随机标题
     */
    public static final String[] randomTitle = new String[]
            {"当一个人做出一个决定时,想必他已做好了觉悟", "一个没有错的人,有什么需要挽回的呢?", "我们见证时代的兴衰,我们感叹人生的轮回", "Tough. Complex. Incomprehensible.", "时间永远是最难跨过的分界线", "自己选择的路终究由你自己跨过",
            "渴望陪伴,又渴望孤独", "Heaven will not have mercy on the hateful.", "可惜我不是你,没能成为你这样的有成之人", "昨夜西风凋碧树,独上高楼,望尽天涯路", "明天和死亡哪个会先到来?",
            "虚伪才是绝对的真实", "为了一个人,你会付出自己的一切吗?", "你希望有一个人,为你付出他的一切吗?", "Metamorphosis.", "衣带渐宽终不悔,为伊消得人憔悴", "No everlasting love.", "我们就如平行宇宙,近在眼前却又永不相遇",
            "混乱不应成为常态,错误不应理所当然", "希望本是无所谓有,无所谓无的", "无聊生者不生,即使厌见者不见,为人为己,也还都不错", "当世界要求你反省,你可以选择不配合这场审判", "疼痛的目的不是让你查看伤口,而是教会你如何站立",
            "当友谊成为了冰冷的符号,这段关系或许已经失衡", "放下助人情结,尊重他人命运", "Be responsible for your own life.", "如何达成理解?", "利益是人类建立关系的基础", "有时,我们注定只能成为别人生活中的配角",
            "揭开伪装你的面具,露出你真实的另一面", "人都会变,莫谈永远", "Helpfully help the helpless.", "我知道你没去,所以我也没来", "没有理由的伤害,到底谁来决定对错?", "用自己创造的麻烦来博取怜惜,最终或许只会被唾弃",
            "没想过你把我放在第一位,可惜我根本就不在列", "其一切奇怪不可迩之状,皆贫病怨恨,不得已诈而遁焉者也", "Intergration. Assimilation. Trust. Understanding.", "纵使星霜能倒转,终遗碎影落人间",
            "Your comedy, my tragedy.", "总有人在卖着惨,总有人想利用善", "看罢了浮华万千幕,看白了沉默是迷", "「不会离开」是你的谎言", "君子论迹不论心,论心世上无完人"};

    public static String getRandomTitle() {
        return randomTitleId == -1 ? randomTitle[RandomUtil.INSTANCE.nextInt(0, randomTitle.length)] : randomTitle[randomTitleId];
    }

    /**
     * LWJGL3: 初始化GLFW
     * 获取窗口GL Context, 允许使用GLFW操作, 防止无法得到上下文导致JVM崩溃
     * @author Stars
     */
    public static void setupGLFW() {
        window = Display.getWindow();
        GLFW.glfwMakeContextCurrent(window);
    }

    /**
     * 检测窗口拖入文件
     * 在主菜单检测是否有.mp4文件拖入并替换背景
     * @author Stars
     */
    public static void setupDrag() {
        GLFW.glfwSetDropCallback(window, (window, count, names) -> {
            String filePath = GLFWDropCallback.getName(names, 0);
            if (mc.currentScreen instanceof AtomicMenu) {
                if (count == 1) {
                    File droppedFile = new File(filePath);

                    // 检测后缀名
                    if (!droppedFile.exists()) return;
                    ReversalLogger.info("Dragged file detected: " + droppedFile.getAbsolutePath());
                    if (droppedFile.getName().toLowerCase().endsWith(".mp4")) {
                        try {
                            // 停止加载
                            VideoUtil.stop();
                            // 将获取的文件替换现有的文件
                            File tempFile = new File(Minecraft.getMinecraft().mcDataDir, "Reversal/Background");
                            File videoFile = new File(tempFile, "background.mp4");
                            Files.copy(droppedFile.toPath(), videoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            // 重新加载
                            VideoUtil.init(videoFile);
                            Reversal.notificationManager.registerNotification("成功更换视频背景!", "主菜单", 2000L, NotificationType.SUCCESS);
                        } catch (Exception e) {
                            ReversalLogger.error("Error while loading file.", e);
                            Reversal.notificationManager.registerNotification("更换视频背景时出现错误!", "主菜单", 2000L, NotificationType.ERROR);
                        }
                    } else if (droppedFile.getName().toLowerCase().endsWith(".png") || droppedFile.getName().toLowerCase().endsWith(".jpg")) {
                        try {
                            // 将获取的文件替换现有的文件
                            File tempFile = new File(Minecraft.getMinecraft().mcDataDir, "Reversal/Background");
                            File imageFile = new File(tempFile, "background.png");
                            Files.copy(droppedFile.toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            // 重新加载
                            BackgroundManager.backgroundImage = new DynamicTexture(ImageIO.read(imageFile));
                            Reversal.notificationManager.registerNotification("成功更换静态背景!", "主菜单", 2000L, NotificationType.SUCCESS);
                        } catch (Exception e) {
                            ReversalLogger.error("Error while loading file.", e);
                            Reversal.notificationManager.registerNotification("更换静态背景时出现错误!", "主菜单", 2000L, NotificationType.ERROR);
                        }
                    } else {
                        Reversal.notificationManager.registerNotification("不支持的文件类型!", "主菜单", 2000L, NotificationType.ERROR);
                        ReversalLogger.error("Dragged file read failed.");
                    }
                }
            }
        });
    }

    public static void readProperties() {
        if (System.getProperty("randomTitle.id") != null) {
            int id = Integer.parseInt(System.getProperty("randomTitle.id"));
            randomTitleId = MathUtil.between(id, 0, randomTitle.length - 1);
        } else {
            randomTitleId = -1;
        }
        if (System.getProperty("exMode") != null) {
            Reversal.EX_MODE = true;
        }
    }

    /**
     * 加载客户端设置
     */
    @SneakyThrows
    public static void loadAPI() {
        ReversalLogger.info("Loading RainyAPI...");
        ignoreSsl();
        loadAPIFromFile();
    }

    public static void loadAPIFromFile() {
        final String client = FileUtil.loadFile("client.txt");
        // re-save if not available on start.
        if (client == null || !client.contains("DisableShader") || !client.contains("DisableSplashScreen") || !client.contains("AsyncLoading")) {
            processAPI();
            loadAPI();
            return;
        }

        final String[] clientLines = client.split("\r\n");

        for (final String line : clientLines) {
            if (line == null) return;

            final String[] split = line.split("_");

            if (split[0].contains("DisableShader")) {
                isShaderCompatibility = Boolean.parseBoolean(split[1]);
            }
            if (split[0].contains("DisableSplashScreen")) {
                isSplashScreenDisabled = Boolean.parseBoolean(split[1]);
            }
            if (split[0].contains("GuiSnow")) {
                guiSnow = Boolean.parseBoolean(split[1]);
            }
            if (split[0].contains("BackgroundBlur")) {
                backgroundBlur = Boolean.parseBoolean(split[1]);
            }
            if (split[0].contains("ImageScreen")) {
                imageScreen = Boolean.parseBoolean(split[1]);
            }
            if (split[0].contains("MenuBubble")) {
                menuBubble = Boolean.parseBoolean(split[1]);
            }
            if (split[0].contains("AsyncLoading")) {
                asyncLoading = Boolean.parseBoolean(split[1]);
            }
        }
    }

    /**
     * 保存客户端设置
     */
    public static void processAPI() {
        final StringBuilder clientBuilder = new StringBuilder();
        clientBuilder.append("DisableShader_").append(isShaderCompatibility).append("\r\n");
        clientBuilder.append("DisableSplashScreen_").append(isSplashScreenDisabled).append("\r\n");
        clientBuilder.append("GuiSnow_").append(guiSnow).append("\r\n");
        clientBuilder.append("BackgroundBlur_").append(backgroundBlur).append("\r\n");
        clientBuilder.append("ImageScreen_").append(imageScreen).append("\r\n");
        clientBuilder.append("MenuBubble_").append(menuBubble).append("\r\n");
        clientBuilder.append("AsyncLoading_").append(asyncLoading).append("\r\n");

        FileUtil.saveFile("client.txt", true, clientBuilder.toString());
    }

    public static long createSubWindow() {
        GLFW.glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        return GLFW.glfwCreateWindow(1, 1, "SubWindow", MemoryUtil.NULL, Display.getWindow());
    }

    public static class CustomTrustManager implements TrustManager, X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }
    }

    @SneakyThrows
    public static void ignoreSsl() {
        TrustManager[] trustAllCerts = new TrustManager[] {new CustomTrustManager() };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
}

package cn.stars.reversal.engine;

import cn.stars.addons.dglab.DglabClient;
import cn.stars.reversal.RainyAPI;
import cn.stars.reversal.Reversal;
import cn.stars.reversal.font.FontUtil;
import cn.stars.reversal.util.render.video.VideoUtil;
import dev.yalan.live.LiveClient;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static cn.stars.reversal.util.ReversalLogger.*;
import static net.minecraft.client.Minecraft.getMinecraft;
import static net.minecraft.client.Minecraft.terminated;

/**
 * Deliver hope to the next.
 * @Author Stars
 */
@SuppressWarnings("all")
public class HopeEngine {
    public static final String version = "1.0.8";
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean terminationErrorFlag;

    @SneakyThrows
    public void initializeDisplay() {
        info("[HopeEngine] Handling display initialization!");
        Util.EnumOS os = Util.getOSType();

        info("[HopeEngine] Setting window icon...");
        if (os != Util.EnumOS.OSX)
        {
            InputStream inputstream = null;
            InputStream inputstream1 = null;
            try
            {
                inputstream = HopeEngine.class.getResourceAsStream("/assets/minecraft/reversal/images/logo/icon_512x512.png");
                inputstream1 = HopeEngine.class.getResourceAsStream("/assets/minecraft/reversal/images/logo/icon_256x256.png");

                if (inputstream != null && inputstream1 != null)
                {
                    Display.setIcon(new ByteBuffer[] { mc.readImageToBuffer(inputstream), mc.readImageToBuffer(inputstream1)});
                } else {
                    error("Couldn't find icon.");
                }
            }
            catch (IOException ioexception)
            {
                error("Couldn't set icon.", ioexception);
            }
            finally
            {
                IOUtils.closeQuietly(inputstream);
                IOUtils.closeQuietly(inputstream1);
            }
        }
        info("[HopeEngine] Transfroming information to Minecraft...");
        if (mc.fullscreen)
        {
            Display.setFullscreen(true);
            DisplayMode displaymode = Display.getDisplayMode();
            mc.displayWidth = Math.max(1, displaymode.getWidth());
            mc.displayHeight = Math.max(1, displaymode.getHeight());
        }
        else
        {
            Display.setDisplayMode(new DisplayMode(mc.displayWidth, mc.displayHeight));
        }
        info("[HopeEngine] Creating display...");
        Display.setResizable(true);
        Display.setTitle("Reversal Startup Progress | HopeEngine " + version);

        Display.create((new PixelFormat()).withDepthBits(24));

        RainyAPI.setupGLFW();
        RainyAPI.setupDrag();
        info("[HopeEngine] Finishing display initialization.");
        info("[HopeEngine] LWJGL Version: " + Sys.getVersion());
        info("[HopeEngine] Display Window: " + RainyAPI.window);
    }

    @SneakyThrows
    public void terminateSafely(CrashReport crashReport) {
        if (terminated) {
            warn("[HopeEngine] terminateSafely() called twice!");
            return;
        }

        // Reversal Termination
        info("[HopeEngine] Handling termination!");
        Display.setTitle("Reversal Termination Progress | HopeEngine " + version);

        stopClientServices:
        {
            try {
                if (terminationErrorFlag) break stopClientServices;
                VideoUtil.stop();
                Reversal.stop();
                LiveClient.INSTANCE.close();
                DglabClient.stop();
            } catch (Exception e) {
                error("Couldn't stop some elements. Unexpected result may happen. Retrying!", e);
                terminationErrorFlag = true;
                terminateSafely(crashReport);
            }
        }

        terminated = true;

        // Minecraft Termination
        if (crashReport != null) {
            warn("[HopeEngine] Terminating with crash!");

            try {
                info("[HopeEngine] Saving worlds...");
                mc.loadWorld(null);
            } catch (Throwable ignored) {}

            info("[HopeEngine] Unloading sound system...");
            mc.mcSoundHandler.unloadSounds();

            Display.destroy();
            System.gc();

            Bootstrap.printToSYSOUT(crashReport.getCompleteReport());

            File parent = new File(getMinecraft().mcDataDir, "crash-reports");
            File file = new File(parent, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
            crashReport.saveToFile(file);

            info("[HopeEngine] Crash report dialog displayed.");
            showCrashReportDialog(file);
        } else {
            info("[HopeEngine] Terminating normally!");

            try {
                info("[HopeEngine] Saving worlds...");
                mc.loadWorld(null);
            } catch (Throwable ignored) {}

            info("[HopeEngine] Unloading sound system...");
            mc.mcSoundHandler.unloadSounds();

            Display.destroy();
            System.gc();
            System.exit(0);
        }
    }

    public void terminateSafely() {
        terminateSafely(null);
    }

    @SneakyThrows
    public static void showCrashReportDialog(File crashReportFile) {
        if (!crashReportFile.exists()) {
            JOptionPane.showMessageDialog(null, "崩溃报告文件不存在！", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }

        StringBuilder crashReportContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(crashReportFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                crashReportContent.append(line).append("\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "读取崩溃报告文件失败！", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }

        JFrame frame = new JFrame("HopeEngine-崩溃报告");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Font font = FontUtil.getResource("reversal/font/regular.ttf", 12);
        UIManager.put("TextArea.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);

        JTextArea textArea = new JTextArea(crashReportContent.toString());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setBackground(new Color(240, 240, 240));
        textArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 250));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnOK = new JButton("确定");
        JButton btnOpenFile = new JButton("打开文件");
        JButton btnOpenDir = new JButton("打开目录");

        // 按钮功能
        btnOK.addActionListener(e -> System.exit(0));  // 点击确定退出游戏
        btnOpenFile.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(crashReportFile);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "打开文件失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnOpenDir.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(crashReportFile.getParentFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "打开文件夹失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnOK);
        buttonPanel.add(btnOpenFile);
        buttonPanel.add(btnOpenDir);

        frame.setLayout(new BorderLayout());
        frame.add(new JLabel("游戏已发生崩溃! 如果你不是专业人员,请将崩溃报告文件发送给开发者!", SwingConstants.CENTER), BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.addHierarchyListener(e -> {
            if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
                SwingUtilities.invokeLater(() -> {
                    Component[] components = frame.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel) {
                            JLabel label = (JLabel) comp;
                            label.setFont(font);
                            label.paintImmediately(label.getBounds());
                        }
                    }
                });
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

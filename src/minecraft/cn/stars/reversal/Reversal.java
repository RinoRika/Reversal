package cn.stars.reversal;

import cn.stars.addons.creativetab.ReversalTab;
import cn.stars.addons.fbp.FBP;
import cn.stars.reversal.command.Command;
import cn.stars.reversal.command.CommandManager;
import cn.stars.reversal.command.impl.*;
import cn.stars.reversal.config.DefaultHandler;
import cn.stars.reversal.config.MusicHandler;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleManager;
import cn.stars.reversal.module.impl.addons.*;
import cn.stars.reversal.module.impl.client.*;
import cn.stars.reversal.module.impl.combat.ClickSound;
import cn.stars.reversal.module.impl.combat.NoClickDelay;
import cn.stars.reversal.module.impl.hud.*;
import cn.stars.reversal.module.impl.misc.*;
import cn.stars.reversal.module.impl.movement.Sprint;
import cn.stars.reversal.module.impl.player.*;
import cn.stars.reversal.module.impl.render.*;
import cn.stars.reversal.module.impl.world.TimeTraveller;
import cn.stars.reversal.music.MusicManager;
import cn.stars.reversal.ui.atmoic.mainmenu.AtomicMenu;
import cn.stars.reversal.ui.atmoic.msgbox.AtomicMsgBox;
import cn.stars.reversal.ui.clickgui.modern.ModernClickGUI;
import cn.stars.reversal.ui.notification.NotificationManager;
import cn.stars.reversal.ui.splash.util.AsyncGLContentLoader;
import cn.stars.reversal.util.ReversalLogger;
import cn.stars.reversal.util.misc.FileUtil;
import cn.stars.reversal.util.misc.ModuleInstance;
import cn.stars.reversal.util.render.ThemeUtil;
import cn.stars.reversal.util.render.video.BackgroundManager;
import cn.stars.reversal.util.render.video.VideoUtil;
import cn.stars.reversal.util.reversal.Branch;
import cn.stars.reversal.util.reversal.Preloader;
import de.florianmichael.viamcp.ViaMCP;
import dev.yalan.live.LiveClient;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2025 Aerolite Society, Some rights reserved.
 * A Hack-visual PVP Client.
 */
@Getter
@SuppressWarnings("all")
public class Reversal {
    // Client Info
    public static final String NAME = "Reversal";

    public static final String VERSION = "v3.0.0.2";
    public static final String MINECRAFT_VERSION = "1.8.9";
    public static final String AUTHOR = "Stars, Ry4nnnnn";
    public static final Branch BRANCH = Branch.DEVELOPMENT;
    public static final String IDENTIFIER = "+alpha";
    public static boolean EX_MODE = false;

    // Init
    public static final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
    public static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(6);

    public static ModuleManager moduleManager;
    public static NotificationManager notificationManager;
    public static CommandManager cmdManager;
    public static MusicManager musicManager;

    public static ModernClickGUI modernClickGUI;
    public static AtomicMenu atomicMenu;
    public static AtomicMsgBox atomicMsgBox;

    public static CreativeTabs creativeTab;
    
    public static boolean firstBoot;

    // Core
    public static void start() {
        try {
            if (Thread.currentThread().getName().equals("Client thread")) {
                ReversalLogger.info("Loading client normally...");
            } else {
                ReversalLogger.info("Loading client asynchronously...");
            }

            RainyAPI.readProperties();

            // ViaMCP init
            threadPoolExecutor.execute(() -> {
                ViaMCP.create();
                ViaMCP.INSTANCE.initAsyncSlider();
            });

            initialize();

            ReversalLogger.info("Client loaded successfully.");
            ReversalLogger.info(NAME + " " + VERSION + " (Minecraft " + MINECRAFT_VERSION + "), made with love by " + AUTHOR + ".");
        } catch (Exception e) {
            ReversalLogger.error("An error has occurred while loading Reversal: ", e);
        }
    }

    public static void stop() {
        saveAll();
        threadExecutor.shutdownNow();
        threadPoolExecutor.shutdownNow();
    }

    // Usages
    public static void showMsg(Object msg) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            if (ModuleInstance.getModule(ClientSettings.class).clientMsgCustomName.enabled)
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§7[§b§l" + ThemeUtil.getCustomClientName() + "§r§7] §f" + msg));
            else
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§7[§b§l" + NAME + "§r§7] §f" + msg));
        }
    }

    public static void showCustomMsg(Object msg) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText((String) msg));
        }
    }

    // run required
    public static void saveAll() {
        DefaultHandler.saveConfig(false);
        RainyAPI.processAPI();
    }


    public static void initialize() {
        LiveClient.INSTANCE = new LiveClient();

        try {
            // Minecraft Pre-Initialize
            // Shut the fast render off
            Minecraft.getMinecraft().gameSettings.ofFastRender = false;

            // Reversal Initialize
            moduleManager = new ModuleManager();
            moduleManager.registerModules(modules);

            // Preload module resources
            Preloader preloader = new Preloader();
            AsyncGLContentLoader.loadGLContentAsync(preloader::loadResources);

            notificationManager = new NotificationManager();

            cmdManager = new CommandManager();
            cmdManager.registerCommands(commands);

            try {
                musicManager = new MusicManager();
                MusicHandler.load();
                musicManager.initGUI();
            } catch (NoClassDefFoundError e) {
                RainyAPI.hasJavaFX = false;
                ReversalLogger.warn("No JavaFX found in the current java version! Music player is disabled.");
            }
            modernClickGUI = new ModernClickGUI();
            //    mmtClickGUI = new MMTClickGUI();
            atomicMenu = new AtomicMenu();

            creativeTab = new ReversalTab();

            Minecraft.latch.countDown();
        } catch (final Exception e) {
            ReversalLogger.error("An error has occurred while loading Reversal: ", e);
        }


        try {
            if (!FileUtil.coreDirectoryExists()) {
                firstBoot = true;
                FileUtil.createCoreDirectory();
            }
            if (!FileUtil.exists("Config" + File.separator)) {
                FileUtil.createDirectory("Config" + File.separator);
            }
            if (!FileUtil.exists("Script" + File.separator)) {
                FileUtil.createDirectory("Script" + File.separator);
            }
            if (!FileUtil.exists("Cache" + File.separator)) {
                FileUtil.createDirectory("Cache" + File.separator);
            }
            if (!FileUtil.exists("Misc" + File.separator + "Dglab" + File.separator)) {
                FileUtil.createDirectory("Misc" + File.separator + "Dglab" + File.separator);
            }
            if (!FileUtil.exists("Background" + File.separator)) {
                FileUtil.createDirectory("Background" + File.separator);
            }
        } catch (final Exception e) {
            ReversalLogger.error("An error has occurred while loading Reversal: ", e);
        }
    }

    public static void postInitialize() {
        try {
            threadPoolExecutor.execute(DefaultHandler::loadConfigs);

            VideoUtil.stop();
            BackgroundManager.loadBackground();

            // Init it here because it causes crashes if it's initialized before the client is fully loaded.
            // Who knows why?
            FBP.init();

            setWindowTitle();

            ReversalLogger.info("Client finalized.");
        } catch (final Exception e) {
            ReversalLogger.error("An error has occurred while loading Reversal: ", e);
        }
    }

    public static void setWindowTitle() {
        StringBuilder title = new StringBuilder();

        title.append(NAME + " " + VERSION);
        if (BRANCH.equals(Branch.DEVELOPMENT)) title.append(IDENTIFIER);
        if (EX_MODE) title.append(" | EXPERIMENT MODE |");
        title.append(" " + Branch.getBranchName(BRANCH) + " | " + RainyAPI.getRandomTitle());

        Display.setTitle(title.toString());
    }

    public static boolean onSendChatMessage(final String s) {
        if (s.startsWith(".") && !s.startsWith("./") && !s.startsWith(".l") && !s.startsWith(".reg")) {
            cmdManager.callCommand(s.substring(1));
            return false;
        }
        return true;
    }

    public static Logger getLogger() {
        return ReversalLogger.logger;
    }

    private static final Command[] commands = new Command[] {
            new Bind(),
            new Chat(),
            new ClientName(),
            new ClientTitle(),
            new Config(),
            new Help(),
            new Say(),
            new Toggle(),
            new Test()
    };

    public static final Module[] modules = new Module[] {
            // Addons
            new FancyBlockParticles(),
            new FreeLook(),
            new MoBends(),
            new MusicPlayer(),
            new RealFirstPerson(),
            new SkinLayers3D(),
            new WaveyCapes(),
            new Dglab(),
            // Combat
            new ClickSound(),
            new NoClickDelay(),
            // Movement
            new Sprint(),
            // Misc
            new ClientSpoofer(),
            new CustomName(),
            new FakeFPS(),
            new NoAchievements(),
            new Protocol(),
            // World
            new TimeTraveller(),
            // Player
            new AutoGG(),
            new AutoPlay(),
            new AutoTip(),
            new Dinnerbone(),
            new HealthWarn(),
            new SmallPlayer(),
            new SmoothSneak(),
            new ThePitUtilities(),
            // Render
            new Animations(),
            new AppleSkin(),
            new BAHalo(),
            new BetterFont(),
            new BlockinDisplay(),
            new BlockOverlay(),
            new ChinaHat(),
            new ClickGui(),
            new Crosshair(),
            new DamageParticle(),
            new EnvironmentEffect(),
            new Fullbright(),
            new Hitbox(),
            new HitEffect(),
            new ItemIndicator(),
            new ItemPhysics(),
            new JumpCircle(),
            new LineGlyphs(),
            new MotionBlur(),
            new NoBob(),
            new ReachDisplay(),
            new TargetESP(),
            new TNTTimer(),
            new Trail(),
            new TrueSights(),
            new Particles(),
            new Wings(),
            // Hud
            new Arraylist(),
            new AtomicIsland(),
            new ArmorHUD(),
            new BASticker(),
            new BPSCounter(),
            new CPSCounter(),
            new CustomText(),
            new FPSCounter(),
            new HUD(),
            new Keystrokes(),
            new MusicInfo(),
            new MusicVisualizer(),
            new PingCounter(),
            new PlayerList(),
            new PlayerModel(),
            new PotionEffect(),
            new DglabOverlay(),
            new Scoreboard(),
            new SessionInfo(),
            new TargetHUD(),
            new TextGui(),
            new TestElement(),
            // Client
            new ClientSettings(),
            new PostProcessing(),
            new HurtCam(),
            new Hotbar(),
            new NameTag(),
            new Interface(),
            new IRC(),
            new Optimization(),
            new Debugger()
    };

    public static Color CLIENT_THEME_COLOR = new Color(159, 24, 242);
    public static Color CLIENT_THEME_COLOR_2 = new Color(159, 24, 242);
    public static Color CLIENT_THEME_COLOR_BRIGHT = new Color(185, 69, 255);
    public static Color CLIENT_THEME_COLOR_BRIGHT_2 = new Color(185, 69, 255);

}
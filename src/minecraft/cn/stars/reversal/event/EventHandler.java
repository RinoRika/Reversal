package cn.stars.reversal.event;

import cn.stars.reversal.RainyAPI;
import cn.stars.reversal.Reversal;
import cn.stars.reversal.event.impl.*;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.impl.hud.ClientSettings;
import cn.stars.reversal.ui.clickgui.modern.MMTClickGUI;
import cn.stars.reversal.ui.clickgui.modern.ModernClickGUI;
import cn.stars.reversal.ui.notification.NotificationManager;
import cn.stars.reversal.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

public final class EventHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static EntityPlayer target = null;
    public static boolean canUpdateDeaths;

    public static void handle(final Event e) {
        final Module[] modules = Reversal.moduleManager.getModuleList();

        if (e instanceof Render2DEvent) {
            final Render2DEvent event = ((Render2DEvent) e);

            RenderUtil.delta2DFrameTime = (System.currentTimeMillis() - RenderUtil.last2DFrame) / 10F;
            RenderUtil.last2DFrame = System.currentTimeMillis();

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    if (module.getModuleInfo().category().equals(Category.HUD) && !RainyAPI.canDrawHUD()) return;
                    module.onRender2D(event);
                }
            }
            NotificationManager.onRender2D();

            Reversal.CLIENT_THEME_COLOR = new Color(ClientSettings.red0, ClientSettings.green0, ClientSettings.blue0, 255).getRGB();
            Reversal.CLIENT_THEME_COLOR_BRIGHT = new Color(Math.min(ClientSettings.red0 + 26, 255), Math.min(ClientSettings.green0 + 45, 255), Math.min(ClientSettings.blue0 + 13, 255)).hashCode();
            Reversal.CLIENT_THEME_COLOR_2 = new Color(ClientSettings.red1, ClientSettings.green1, ClientSettings.blue1, 255).getRGB();
            Reversal.CLIENT_THEME_COLOR_BRIGHT_2 = new Color(Math.min(ClientSettings.red1 + 26, 255), Math.min(ClientSettings.green1 + 45, 255), Math.min(ClientSettings.blue1 + 13, 255)).hashCode();

        } else if (e instanceof Render3DEvent) {
            final Render3DEvent event = ((Render3DEvent) e);

            RenderUtil.delta3DFrameTime = (System.currentTimeMillis() - RenderUtil.last3DFrame) / 10F;
            RenderUtil.last3DFrame = System.currentTimeMillis();

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    if (module.getModuleInfo().category().equals(Category.HUD) && !RainyAPI.canDrawHUD()) return;
                    module.onRender3D(event);
                }
            }
        } else if (e instanceof BlurEvent) {
            final BlurEvent event = ((BlurEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    if (module.getModuleInfo().category().equals(Category.HUD) && !RainyAPI.canDrawHUD()) return;
                    module.onBlur(event);
                }
            }
        } else if (e instanceof Shader3DEvent) {
            final Shader3DEvent event = ((Shader3DEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    if (module.getModuleInfo().category().equals(Category.HUD) && !RainyAPI.canDrawHUD()) return;
                    module.onShader3D(event);
                }
            }
        } else if (e instanceof PreBlurEvent) {
            final PreBlurEvent event = ((PreBlurEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onPreBlur(event);
                }
            }
        } else if (e instanceof PacketReceiveEvent) {
            final PacketReceiveEvent event = ((PacketReceiveEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onPacketReceive(event);
                }
            }
        } else if (e instanceof PacketSendEvent) {
            final PacketSendEvent event = ((PacketSendEvent) e);


            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onPacketSend(event);
                }
            }

        } else if (e instanceof PostMotionEvent) {
            final PostMotionEvent event = ((PostMotionEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onPostMotion(event);
                }
            }
        } else if (e instanceof PreMotionEvent) {
            final PreMotionEvent event = ((PreMotionEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onPreMotion(event);
                }

                /* Calls events that are always used called whether the module is on or not*/
                if (mc.currentScreen instanceof ModernClickGUI || mc.currentScreen instanceof MMTClickGUI) {
                    module.onUpdateAlwaysInGui();
                }
                module.onUpdateAlways();
            }
        } else if (e instanceof TickEvent) {
            final TickEvent event = (TickEvent) e;
            for (final Module module : modules) {
                if (module.isEnabled()) {

                    module.onTick(event);
                }
            }
        } else if (e instanceof ClickEvent) {
            final ClickEvent event = (ClickEvent) e;
            for (final Module module : modules) {
                if (module.isEnabled()) {

                    module.onClick(event);
                }
            }
        } else if (e instanceof KeyEvent) {
            final KeyEvent event = ((KeyEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onKey(event);
                }

                if (module.getKeyBind() == event.getKey()) {
                    module.toggleModule();
                }
            }
        } else if (e instanceof StrafeEvent) {
            final StrafeEvent event = ((StrafeEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onStrafe(event);
                }
            }
        } else if (e instanceof CanPlaceBlockEvent) {
            final CanPlaceBlockEvent event = ((CanPlaceBlockEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onCanPlaceBlock(event);
                }
            }
        } else if (e instanceof BlockBreakEvent) {
            final BlockBreakEvent event = ((BlockBreakEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onBlockBreak(event);
                }
            }
        } else if (e instanceof AlphaEvent){
            final AlphaEvent event = (AlphaEvent) e;

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onAlpha(event);
                }
            }
        } else if (e instanceof AttackEvent) {
            final AttackEvent event = ((AttackEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onAttack(event);
                }
            }
        } else if (e instanceof MoveButtonEvent) {
            final MoveButtonEvent event = ((MoveButtonEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onMoveButton(event);
                }
            }
        } else if (e instanceof MoveEvent) {
            final MoveEvent event = ((MoveEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onMove(event);
                }
            }
        } else if (e instanceof WorldEvent) {
            final WorldEvent event = ((WorldEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onWorld(event);
                }
            }
        } else if (e instanceof UpdateEvent) {
            final UpdateEvent event = ((UpdateEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onUpdate(event);
                }
            }
        } else if (e instanceof BlockCollideEvent) {
            final BlockCollideEvent event = ((BlockCollideEvent) e);

            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onBlockCollide(event);
                }
            }
        } else if (e instanceof TeleportEvent) {
            final TeleportEvent event = ((TeleportEvent) e);


            for (final Module module : modules) {
                if (module.isEnabled()) {
                    module.onTeleport(event);
                }
            }
        }
    }
}
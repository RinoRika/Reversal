package net.minecraft.client.gui;

import cn.stars.reversal.GameInstance;
import cn.stars.reversal.event.impl.PreBlurEvent;
import cn.stars.reversal.event.impl.Render2DEvent;
import cn.stars.reversal.font.FontManager;
import cn.stars.reversal.module.impl.client.Debugger;
import cn.stars.reversal.module.impl.client.Hotbar;
import cn.stars.reversal.module.impl.render.AppleSkin;
import cn.stars.reversal.module.impl.render.Crosshair;
import cn.stars.reversal.util.ReversalLogger;
import cn.stars.reversal.util.animation.rise.Animation;
import cn.stars.reversal.util.animation.rise.Easing;
import cn.stars.reversal.util.misc.ModuleInstance;
import cn.stars.reversal.util.render.ColorUtils;
import cn.stars.reversal.util.render.RoundedUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.Config;
import net.minecraft.util.*;
import net.minecraft.world.border.WorldBorder;
import net.optifine.CustomColors;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class GuiIngame extends Gui {
    private static final ResourceLocation vignetteTexPath = new ResourceLocation("textures/misc/vignette.png");
    private static final ResourceLocation widgetsTexPath = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation pumpkinBlurTexPath = new ResourceLocation("textures/misc/pumpkinblur.png");
    private final Random rand = new Random();
    private final Minecraft mc;
    private final GuiNewChat persistantChatGUI;
    private final GuiOverlayDebug overlayDebug;
    private final GuiSpectator spectatorGui;
    private final GuiPlayerTabOverlay overlayPlayerList;
    private final Animation armorAnimation = new Animation(Easing.EASE_OUT_EXPO, 800);
    private final Animation healthAnimation = new Animation(Easing.EASE_OUT_EXPO, 800);
    private final Animation foodAnimation = new Animation(Easing.EASE_OUT_EXPO, 800);
    public float prevVignetteBrightness = 1.0F;
    private int updateCounter;
    private String recordPlaying = "";
    private int recordPlayingUpFor;
    private boolean recordIsPlaying;
    private int remainingHighlightTicks;
    private ItemStack highlightingItemStack;
    private int titlesTimer;
    private String displayedTitle = "";
    private String displayedSubTitle = "";
    private int titleFadeIn;
    private int titleDisplayTime;
    private int titleFadeOut;
    private int playerHealth = 0;
    private int lastPlayerHealth = 0;
    private long lastSystemTime = 0L;
    private long healthUpdateCounter = 0L;

    public GuiIngame(Minecraft mcIn) {
        this.mc = mcIn;
        this.overlayDebug = new GuiOverlayDebug(mcIn);
        this.spectatorGui = new GuiSpectator(mcIn);
        this.persistantChatGUI = new GuiNewChat(mcIn);
        this.overlayPlayerList = new GuiPlayerTabOverlay(mcIn, this);
        this.setDefaultTitlesTimes();
    }

    public void setDefaultTitlesTimes() {
        this.titleFadeIn = 10;
        this.titleDisplayTime = 70;
        this.titleFadeOut = 20;
    }

    public void renderGameOverlay(float partialTicks) {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        this.mc.entityRenderer.setupOverlayRendering();
        GlStateManager.enableBlend();

        if (Config.isVignetteEnabled()) {
            this.renderVignette(this.mc.thePlayer.getBrightness(partialTicks), scaledresolution);
        } else {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }

        ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(3);

        if (this.mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
            this.renderPumpkinOverlay(scaledresolution);
        }

        if (!this.mc.thePlayer.isPotionActive(Potion.confusion)) {
            float f = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;

            if (f > 0.0F) {
                this.renderPortal(f, scaledresolution);
            }
        }

        if (this.mc.playerController.isSpectator()) {
            this.spectatorGui.renderTooltip(scaledresolution, partialTicks);
        } else {
            ModuleInstance.getModule(Hotbar.class).renderHotbar(scaledresolution, partialTicks);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        GlStateManager.enableBlend();

        if (this.showCrosshair()) {
            GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
            GlStateManager.enableAlpha();
            this.drawTexturedModalRect(i / 2 - 7, j / 2 - 7, 0, 0, 16, 16);
        }

        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        this.mc.mcProfiler.startSection("bossHealth");
        this.renderBossHealth();
        this.mc.mcProfiler.endSection();

        if (this.mc.playerController.shouldDrawHUD()) {
            this.renderPlayerStats(scaledresolution);
        }

        GlStateManager.disableBlend();

        if (this.mc.thePlayer.getSleepTimer() > 0) {
            this.mc.mcProfiler.startSection("sleep");
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            int j1 = this.mc.thePlayer.getSleepTimer();
            float f1 = (float) j1 / 100.0F;

            if (f1 > 1.0F) {
                f1 = 1.0F - (float) (j1 - 100) / 10.0F;
            }

            int k = (int) (220.0F * f1) << 24 | 1052704;
            drawRect(0, 0, i, j, k);
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            this.mc.mcProfiler.endSection();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int k1 = i / 2 - 91;

        if (this.mc.thePlayer.isRidingHorse()) {
            this.renderHorseJumpBar(scaledresolution, k1);
        } else if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
            this.renderExpBar(scaledresolution, k1);
        }

        if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator()) {
            this.renderSelectedItem(scaledresolution);
        } else if (this.mc.thePlayer.isSpectator()) {
            this.spectatorGui.renderSelectedItem(scaledresolution);
        }

        if (this.mc.gameSettings.showDebugInfo) {
            this.overlayDebug.renderDebugInfo(scaledresolution);
        }

        if (this.recordPlayingUpFor > 0) {
            this.mc.mcProfiler.startSection("overlayMessage");
            float f2 = (float) this.recordPlayingUpFor - partialTicks;
            int l1 = (int) (f2 * 255.0F / 20.0F);

            if (l1 > 255) {
                l1 = 255;
            }

            if (l1 > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (i / 2), (float) (j - 68), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                int l = 16777215;

                if (this.recordIsPlaying) {
                    l = MathHelper.hsvToRGB(f2 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                this.getFontRenderer().drawString(this.recordPlaying, -this.getFontRenderer().getStringWidth(this.recordPlaying) / 2, -4, l + (l1 << 24 & -16777216));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            this.mc.mcProfiler.endSection();
        }

        if (this.titlesTimer > 0) {
            this.mc.mcProfiler.startSection("titleAndSubtitle");
            float f3 = (float) this.titlesTimer - partialTicks;
            int i2 = 255;

            if (this.titlesTimer > this.titleFadeOut + this.titleDisplayTime) {
                float f4 = (float) (this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut) - f3;
                i2 = (int) (f4 * 255.0F / (float) this.titleFadeIn);
            }

            if (this.titlesTimer <= this.titleFadeOut) {
                i2 = (int) (f3 * 255.0F / (float) this.titleFadeOut);
            }

            i2 = MathHelper.clamp_int(i2, 0, 255);

            if (i2 > 8) {
                int j2 = i2 << 24 & -16777216;
                if (ModuleInstance.getInterface().modernFont_T_S.enabled) {
                    GlStateManager.pushMatrix();
                    FontManager.getRegularBold(64).drawString(this.displayedTitle,  i / 2f - FontManager.getRegularBold(64).width(this.displayedTitle) / 2f, j / 2f - 30.0F + ModuleInstance.getInterface().yOffset.getFloat(), 16777215 | j2);
                    FontManager.getRegular(32).drawString(this.displayedSubTitle, i / 2f - FontManager.getRegular(32).width(this.displayedSubTitle) / 2f, j / 2f + 10.0F + ModuleInstance.getInterface().yOffset.getFloat(), 16777215 | j2);
                    GlStateManager.popMatrix();
                } else {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(4.0F, 4.0F, 4.0F);
                    this.getFontRenderer().drawString(this.displayedTitle, (float) (-this.getFontRenderer().getStringWidth(this.displayedTitle) / 2), -10.0F + ModuleInstance.getInterface().yOffset.getFloat() / 4, 16777215 | j2, true);
                    GlStateManager.popMatrix();
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(2.0F, 2.0F, 2.0F);
                    this.getFontRenderer().drawString(this.displayedSubTitle, (float) (-this.getFontRenderer().getStringWidth(this.displayedSubTitle) / 2), 5.0F + ModuleInstance.getInterface().yOffset.getFloat() / 2, 16777215 | j2, true);
                    GlStateManager.popMatrix();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }

            this.mc.mcProfiler.endSection();
        }

        Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.thePlayer.getName());

        if (scoreplayerteam != null) {
            int i1 = scoreplayerteam.getChatFormat().getColorIndex();

            if (i1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null) {
            if (ModuleInstance.getModule(cn.stars.reversal.module.impl.hud.Scoreboard.class).enabled) {
                ModuleInstance.getModule(cn.stars.reversal.module.impl.hud.Scoreboard.class).renderScoreboard(scoreobjective1);
            } else {
                renderScoreboard(scoreobjective1, scaledresolution);
            }
        } else {
            ModuleInstance.getModule(cn.stars.reversal.module.impl.hud.Scoreboard.class).setWidth(0);
            ModuleInstance.getModule(cn.stars.reversal.module.impl.hud.Scoreboard.class).setHeight(0);
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, (float) (j - 48), 0.0F);
        this.mc.mcProfiler.startSection("chat");
        this.persistantChatGUI.drawChat(this.updateCounter);
        this.mc.mcProfiler.endSection();
        GlStateManager.popMatrix();
        scoreobjective1 = scoreboard.getObjectiveInDisplaySlot(0);

        if (this.mc.gameSettings.keyBindPlayerList.isKeyDown() && (!this.mc.isIntegratedServerRunning() || this.mc.thePlayer.sendQueue.getPlayerInfoMap().size() > 1 || scoreobjective1 != null)) {
            this.overlayPlayerList.updatePlayerList(true);
            this.overlayPlayerList.renderPlayerlist(i, scoreboard, scoreobjective1);
        } else {
            this.overlayPlayerList.updatePlayerList(false);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();

        Debugger.postProcessingProfiler.start();
        ModuleInstance.getPostProcessing().blurScreen();
        Debugger.postProcessingProfiler.stop();

        Debugger.render2dProfiler.start();
        final Render2DEvent render2DEvent = new Render2DEvent(partialTicks, scaledresolution);
        render2DEvent.call();
        Debugger.render2dProfiler.stop();

        Debugger.postProcessingProfiler.start();
        ModuleInstance.getPostProcessing().blurScreenPost();
        Debugger.postProcessingProfiler.stop();

        Debugger.render2dProfiler.start();
        GameInstance.render2DRunnables(partialTicks, true);
        GameInstance.clearRunnables();
        Debugger.render2dProfiler.stop();

        new PreBlurEvent().call();
    }

    public void renderHorseJumpBar(ScaledResolution scaledRes, int x) {
        this.mc.mcProfiler.startSection("jumpBar");
        this.mc.getTextureManager().bindTexture(Gui.icons);
        float f = this.mc.thePlayer.getHorseJumpPower();
        int i = 182;
        int j = (int) (f * (float) (i + 1));
        int k = scaledRes.getScaledHeight() - 32 + 3;
        this.drawTexturedModalRect(x, k, 0, 84, i, 5);

        if (j > 0) {
            this.drawTexturedModalRect(x, k, 0, 89, j, 5);
        }

        this.mc.mcProfiler.endSection();
    }

    public void renderExpBar(ScaledResolution scaledRes, int x) {
        this.mc.mcProfiler.startSection("expBar");
        this.mc.getTextureManager().bindTexture(Gui.icons);
        int i = this.mc.thePlayer.xpBarCap();

        if (i > 0) {
            int j = 182;
            int k = (int) (this.mc.thePlayer.experience * (float) (j + 1));
            int l = scaledRes.getScaledHeight() - 32 + 3;
            this.drawTexturedModalRect(x, l, 0, 64, j, 5);

            if (k > 0) {
                this.drawTexturedModalRect(x, l, 0, 69, k, 5);
            }
        }

        this.mc.mcProfiler.endSection();

        if (this.mc.thePlayer.experienceLevel > 0) {
            this.mc.mcProfiler.startSection("expLevel");
            int k1 = 8453920;

            if (Config.isCustomColors()) {
                k1 = CustomColors.getExpBarTextColor(k1);
            }

            String s = "" + this.mc.thePlayer.experienceLevel;
            int l1 = (scaledRes.getScaledWidth() - this.getFontRenderer().getStringWidth(s)) / 2;
            int i1 = scaledRes.getScaledHeight() - 31 - 4;
            if (ModuleInstance.getModule(Hotbar.class).modernBars.isEnabled()) {
                GameInstance.psm18.drawStringWithShadow(s, l1 + 1, i1, Color.GREEN.getRGB());
            } else {
                this.getFontRenderer().drawString(s, l1 + 1, i1, 0);
                this.getFontRenderer().drawString(s, l1 - 1, i1, 0);
                this.getFontRenderer().drawString(s, l1, i1 + 1, 0);
                this.getFontRenderer().drawString(s, l1, i1 - 1, 0);
                this.getFontRenderer().drawString(s, l1, i1, k1);
            }
            this.mc.mcProfiler.endSection();
        }
    }

    public void renderSelectedItem(ScaledResolution scaledRes) {
        this.mc.mcProfiler.startSection("selectedItemName");

        if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null) {
            String s = this.highlightingItemStack.getDisplayName();

            if (this.highlightingItemStack.hasDisplayName()) {
                s = EnumChatFormatting.ITALIC + s;
            }

            int i = (scaledRes.getScaledWidth() - this.getFontRenderer().getStringWidth(s)) / 2;
            int j = scaledRes.getScaledHeight() - 59;

            if (!this.mc.playerController.shouldDrawHUD()) {
                j += 14;
            }

            int k = (int) ((float) this.remainingHighlightTicks * 256.0F / 10.0F);

            if (k > 255) {
                k = 255;
            }

            if (k > 0) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                this.getFontRenderer().drawStringWithShadow(s, (float) i, (float) j, 16777215 + (k << 24));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }

        this.mc.mcProfiler.endSection();
    }

    protected boolean showCrosshair() {
        if (ModuleInstance.getModule(Crosshair.class).isEnabled()) return false;
        if (this.mc.gameSettings.showDebugInfo && !this.mc.thePlayer.hasReducedDebug() && !this.mc.gameSettings.reducedDebugInfo) {
            return false;
        } else if (this.mc.playerController.isSpectator()) {
            if (this.mc.pointedEntity != null) {
                return true;
            } else {
                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();

                    return this.mc.theWorld.getTileEntity(blockpos) instanceof IInventory;
                }

                return false;
            }
        } else {
            return true;
        }
    }

    private void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes) {
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")));

        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        int i = this.getFontRenderer().getStringWidth(objective.getDisplayName());

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            i = Math.max(i, this.getFontRenderer().getStringWidth(s));
        }

        int i1 = collection.size() * this.getFontRenderer().FONT_HEIGHT;
        int j1 = scaledRes.getScaledHeight() / 2 + i1 / 3;
        int k1 = 3;
        int l1 = scaledRes.getScaledWidth() - i - k1;
        int j = 0;

        for (Score score1 : collection) {
            ++j;
            ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
            String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
            String s2 = EnumChatFormatting.RED + "" + score1.getScorePoints();
            int k = j1 - j * this.getFontRenderer().FONT_HEIGHT;
            int l = scaledRes.getScaledWidth() - k1 + 2;
            drawRect(l1 - 2, k, l, k + this.getFontRenderer().FONT_HEIGHT, 1342177280);
            this.getFontRenderer().drawString(s1, l1, k, 553648127);
            this.getFontRenderer().drawString(s2, l - this.getFontRenderer().getStringWidth(s2), k, 553648127);

            if (j == collection.size()) {
                String s3 = objective.getDisplayName();
                drawRect(l1 - 2, k - this.getFontRenderer().FONT_HEIGHT - 1, l, k - 1, 1610612736);
                drawRect(l1 - 2, k - 1, l, k, 1342177280);
                this.getFontRenderer().drawString(s3, l1 + i / 2 - this.getFontRenderer().getStringWidth(s3) / 2, k - this.getFontRenderer().FONT_HEIGHT, 553648127);
            }
        }
    }

    private void renderPlayerStats(ScaledResolution scaledRes) {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
            int i = MathHelper.ceiling_float_int(entityplayer.getHealth());
            boolean flag = this.healthUpdateCounter > (long) this.updateCounter && (this.healthUpdateCounter - (long) this.updateCounter) / 3L % 2L == 1L;

            if (i < this.playerHealth && entityplayer.hurtResistantTime > 0) {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = this.updateCounter + 20;
            } else if (i > this.playerHealth && entityplayer.hurtResistantTime > 0) {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = this.updateCounter + 10;
            }

            if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
                this.playerHealth = i;
                this.lastPlayerHealth = i;
                this.lastSystemTime = Minecraft.getSystemTime();
            }

            this.playerHealth = i;
            int j = this.lastPlayerHealth;
            this.rand.setSeed(this.updateCounter * 312871L);
            FoodStats foodstats = entityplayer.getFoodStats();
            int k = foodstats.getFoodLevel();
            IAttributeInstance iattributeinstance = entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            int i1 = scaledRes.getScaledWidth() / 2 - 91;
            int j1 = scaledRes.getScaledWidth() / 2 + 91;
            int k1 = scaledRes.getScaledHeight() - 39;
            float f = (float) iattributeinstance.getAttributeValue();
            float f1 = entityplayer.getAbsorptionAmount();
            int l1 = MathHelper.ceiling_float_int((f + f1) / 2.0F / 10.0F);
            int i2 = Math.max(10 - (l1 - 2), 3);
            int j2 = k1 - (l1 - 1) * i2 - 10;

            int l1_ = MathHelper.ceiling_float_int(20f / 2.0F / 10.0F);
            int i2_ = Math.max(10 - (l1_ - 2), 3);
            int j2_ = k1 - (l1_ - 1) * i2_ - 10;

            float f2 = f1;
            int k2 = entityplayer.getTotalArmorValue();
            int l2 = -1;

            if (entityplayer.isPotionActive(Potion.regeneration)) {
                l2 = this.updateCounter % MathHelper.ceiling_float_int(f + 5.0F);
            }

            this.mc.mcProfiler.startSection("armor");

            if (ModuleInstance.getModule(Hotbar.class).modernBars.isEnabled()) {
                armorAnimation.run(k2 * 4);
                if (k2 > 0) {
                    RoundedUtil.drawRound(i1, j2_, (float) armorAnimation.getValue(), 7, 2, new Color(150, 150, 150, Math.min(150 + k2 * 5, 255)));
                    GameInstance.regular16.drawString(k2 + "", i1 - GameInstance.regular16.width(k2 + "") + armorAnimation.getValue(), j2_ + 2, new Color(250, 250, 250, Math.min(150 + k2 * 5, 255)).getRGB());
                }
            } else {
                for (int i3 = 0; i3 < 10; ++i3) {
                    if (k2 > 0) {
                        int j3 = i1 + i3 * 8;

                        if (i3 * 2 + 1 < k2) {
                            this.drawTexturedModalRect(j3, j2, 34, 9, 9, 9);
                        }

                        if (i3 * 2 + 1 == k2) {
                            this.drawTexturedModalRect(j3, j2, 25, 9, 9, 9);
                        }

                        if (i3 * 2 + 1 > k2) {
                            this.drawTexturedModalRect(j3, j2, 16, 9, 9, 9);
                        }
                    }
                }
            }

            this.mc.mcProfiler.endStartSection("health");

            int i4 = MathHelper.ceiling_float_int(20.0f / 2.0F) - 1;
            int l3 = MathHelper.ceiling_float_int((float) (i4 + 1) / 10.0F) - 1;
            int j4 = k1 - l3 * i2;
            if (ModuleInstance.getModule(Hotbar.class).modernBars.isEnabled()) {
                RoundedUtil.drawRound(i1, j4, 80, 7, 2, new Color(0,0,0,130));
                healthAnimation.run(80 / f * mc.thePlayer.getHealth()); // max health may above 20
                if (entityplayer.isPotionActive(Potion.regeneration)) {
                    RoundedUtil.drawGradientRound(i1, j4, (float) healthAnimation.getValue(), 7, 2,
                            ColorUtils.INSTANCE.interpolateColorsBackAndForth(5, 1000, new Color(250, 20, 20, 250), new Color(50, 19, 19, 250), true),
                            ColorUtils.INSTANCE.interpolateColorsBackAndForth(5, 1000, new Color(250, 20, 20, 250), new Color(50, 19, 19, 250), true),
                            ColorUtils.INSTANCE.interpolateColorsBackAndForth(5, 2000, new Color(250, 20, 20, 250), new Color(50, 19, 19, 250), true),
                            ColorUtils.INSTANCE.interpolateColorsBackAndForth(5, 2000, new Color(250, 20, 20, 250), new Color(50, 19, 19, 250), true));
                } else if (entityplayer.isPotionActive(Potion.poison)) {
                    RoundedUtil.drawRound(i1, j4, (float) healthAnimation.getValue(), 7, 2, new Color(40, 100, 40, 250));
                } else if (entityplayer.isPotionActive(Potion.wither)) {
                    RoundedUtil.drawRound(i1, j4, (float) healthAnimation.getValue(), 7, 2, new Color(35, 20, 20, 250));
                } else {
                    RoundedUtil.drawRound(i1, j4, (float) healthAnimation.getValue(), 7, 2, new Color(Math.min(150 + (int) (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) * 5, 255), Math.min(20 + (int) mc.thePlayer.getAbsorptionAmount() * 15, 255), 20, 250));
                }

                GameInstance.regular16.drawString((int) (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) + "", i1 - GameInstance.regular16.width((int) (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) + "") + healthAnimation.getValue(), j4 + 2, new Color(250, 250, 250, Math.min(150 + (int) (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) * 5, 255)).getRGB());
            } else {
                for (int row = MathHelper.ceiling_float_int((f + f1) / 2.0F) - 1; row >= 0; row--) {
                    int texOffset = 16;
                    if (entityplayer.isPotionActive(Potion.poison)) texOffset += 36;
                    else if (entityplayer.isPotionActive(Potion.wither)) texOffset += 72;

                    int rowGroup = MathHelper.ceiling_float_int((row + 1) / 10.0F) - 1;
                    int x = i1 + row % 10 * 8;
                    int y = k1 - rowGroup * i2;

                    if (i <= 4) y += this.rand.nextInt(2);
                    if (row == l2) y -= 2;

                    int hardcoreY = entityplayer.worldObj.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0;

                    this.drawTexturedModalRect(x, y, 16 + (flag ? 9 : 0), 9 * hardcoreY, 9, 9);

                    if (flag) {
                        int state = row * 2 + 1;
                        if (state < j) this.drawTexturedModalRect(x, y, texOffset + 54, 9 * hardcoreY, 9, 9);
                        else if (state == j) this.drawTexturedModalRect(x, y, texOffset + 63, 9 * hardcoreY, 9, 9);
                    }

                    if (f2 <= 0) {
                        int state = row * 2 + 1;
                        if (state < i) this.drawTexturedModalRect(x, y, texOffset + 36, 9 * hardcoreY, 9, 9);
                        else if (state == i) this.drawTexturedModalRect(x, y, texOffset + 45, 9 * hardcoreY, 9, 9);
                    } else {
                        boolean isHalf = (f2 == f1 && f1 % 2 == 1);
                        this.drawTexturedModalRect(x, y, texOffset + (isHalf ? 153 : 144), 9 * hardcoreY, 9, 9);
                        f2 -= 2;
                    }
                }
            }

            Entity entity = entityplayer.ridingEntity;

            if (entity == null) {
                if (ModuleInstance.getModule(Hotbar.class).modernBars.isEnabled()) {
                    this.mc.mcProfiler.endStartSection("food");
                    int j9 = j1 - 80;
                    RoundedUtil.drawRound(j9, j4, 80, 7, 2, new Color(0,0,0,130));
                    foodAnimation.run(k * 4);
                    if (entityplayer.isPotionActive(Potion.hunger)) {
                        RoundedUtil.drawRound(j9, j4, (float) foodAnimation.getValue(), 7, 2, new Color(40, 100, 40, 250));
                    } else {
                        RoundedUtil.drawRound(j9, j4, (float) foodAnimation.getValue(), 7, 2, new Color(220, 100, 20, 255));
                    }
                    GameInstance.regular16.drawString(k + "", j9 - GameInstance.regular16.width(k + "") + foodAnimation.getValue(), j4 + 2, new Color(250, 250, 250, Math.min(150 + k * 5, 255)).getRGB());
                } else {
                    for (int k6 = 0; k6 < 10; ++k6) {
                        int j7 = k1;
                        int l7 = 16;
                        int k8 = 0;

                        if (entityplayer.isPotionActive(Potion.hunger)) {
                            l7 += 36;
                            k8 = 13;
                        }

                        if (entityplayer.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (k * 3 + 1) == 0) {
                            j7 = k1 + (this.rand.nextInt(3) - 1);
                        }

                        int j9 = j1 - k6 * 8 - 9;
                        this.drawTexturedModalRect(j9, j7, 16 + k8 * 9, 27, 9, 9);

                        if (k6 * 2 + 1 < k) {
                            this.drawTexturedModalRect(j9, j7, l7 + 36, 27, 9, 9);
                        }

                        if (k6 * 2 + 1 == k) {
                            this.drawTexturedModalRect(j9, j7, l7 + 45, 27, 9, 9);
                        }
                    }
                }
            } else if (entity instanceof EntityLivingBase) {
                this.mc.mcProfiler.endStartSection("mountHealth");
                EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
                int i7 = (int) Math.ceil(entitylivingbase.getHealth());
                float f3 = entitylivingbase.getMaxHealth();
                int j8 = (int) (f3 + 0.5F) / 2;

                if (j8 > 30) {
                    j8 = 30;
                }

                int i9 = k1;

                for (int k9 = 0; j8 > 0; k9 += 20) {
                    int l4 = Math.min(j8, 10);
                    j8 -= l4;

                    for (int i5 = 0; i5 < l4; ++i5) {
                        int j5 = 52;

                        int l5 = j1 - i5 * 8 - 9;
                        this.drawTexturedModalRect(l5, i9, j5, 9, 9, 9);

                        if (i5 * 2 + 1 + k9 < i7) {
                            this.drawTexturedModalRect(l5, i9, j5 + 36, 9, 9, 9);
                        }

                        if (i5 * 2 + 1 + k9 == i7) {
                            this.drawTexturedModalRect(l5, i9, j5 + 45, 9, 9, 9);
                        }
                    }

                    i9 -= 10;
                }
            }

            this.mc.mcProfiler.endStartSection("air");

            if (entityplayer.isInsideOfMaterial(Material.water)) {
                int l6 = this.mc.thePlayer.getAir();
                int k7 = MathHelper.ceiling_double_int((double) (l6 - 2) * 10.0D / 300.0D);
                int i8 = MathHelper.ceiling_double_int((double) l6 * 10.0D / 300.0D) - k7;

                for (int l8 = 0; l8 < k7 + i8; ++l8) {
                    if (l8 < k7) {
                        this.drawTexturedModalRect(j1 - l8 * 8 - 9, j2, 16, 18, 9, 9);
                    } else {
                        this.drawTexturedModalRect(j1 - l8 * 8 - 9, j2, 25, 18, 9, 9);
                    }
                }
            }

            this.mc.mcProfiler.endSection();


            if (!ModuleInstance.getModule(Hotbar.class).modernBars.isEnabled())
                try {
                    this.mc.getTextureManager().bindTexture(new ResourceLocation("reversal/images/texture/appleskin/icons.png"));
                    ModuleInstance.getModule(AppleSkin.class).renderOverlay();
                } catch (Exception e) {
                    ReversalLogger.error("Error occurred while rendering overlay.", e);
                }
        }
    }

    private void renderBossHealth() {
        if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
            --BossStatus.statusBarTime;
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaledWidth();
            int j = 182;
            int k = i / 2 - j / 2;
            int l = (int) (BossStatus.healthScale * (float) (j + 1));
            int i1 = 12;
            this.drawTexturedModalRect(k, i1, 0, 74, j, 5);
            this.drawTexturedModalRect(k, i1, 0, 74, j, 5);

            if (l > 0) {
                this.drawTexturedModalRect(k, i1, 0, 79, l, 5);
            }

            String s = BossStatus.bossName;
            this.getFontRenderer().drawStringWithShadow(s, (float) (i / 2 - this.getFontRenderer().getStringWidth(s) / 2), (float) (i1 - 10), 16777215);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(icons);
        }
    }

    private void renderPumpkinOverlay(ScaledResolution scaledRes) {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        this.mc.getTextureManager().bindTexture(pumpkinBlurTexPath);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderVignette(float lightLevel, ScaledResolution scaledRes) {
        if (!Config.isVignetteEnabled()) {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        } else {
            lightLevel = 1.0F - lightLevel;
            lightLevel = MathHelper.clamp_float(lightLevel, 0.0F, 1.0F);
            WorldBorder worldborder = this.mc.theWorld.getWorldBorder();
            float f = (float) worldborder.getClosestDistance(this.mc.thePlayer);
            double d0 = Math.min(worldborder.getResizeSpeed() * (double) worldborder.getWarningTime() * 1000.0D, Math.abs(worldborder.getTargetSize() - worldborder.getDiameter()));
            double d1 = Math.max(worldborder.getWarningDistance(), d0);

            if ((double) f < d1) {
                f = 1.0F - (float) ((double) f / d1);
            } else {
                f = 0.0F;
            }

            this.prevVignetteBrightness = (float) ((double) this.prevVignetteBrightness + (double) (lightLevel - this.prevVignetteBrightness) * 0.01D);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.tryBlendFuncSeparate(0, 769, 1, 0);

            if (f > 0.0F) {
                GlStateManager.color(0.0F, f, f, 1.0F);
            } else {
                GlStateManager.color(this.prevVignetteBrightness, this.prevVignetteBrightness, this.prevVignetteBrightness, 1.0F);
            }

            this.mc.getTextureManager().bindTexture(vignetteTexPath);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(0.0D, scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
            worldrenderer.pos(scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
            worldrenderer.pos(scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
            worldrenderer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }
    }

    private void renderPortal(float timeInPortal, ScaledResolution scaledRes) {
        if (timeInPortal < 1.0F) {
            timeInPortal = timeInPortal * timeInPortal;
            timeInPortal = timeInPortal * timeInPortal;
            timeInPortal = timeInPortal * 0.8F + 0.2F;
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, timeInPortal);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        TextureAtlasSprite textureatlassprite = this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.portal.getDefaultState());
        float f = textureatlassprite.getMinU();
        float f1 = textureatlassprite.getMinV();
        float f2 = textureatlassprite.getMaxU();
        float f3 = textureatlassprite.getMaxV();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, scaledRes.getScaledHeight(), -90.0D).tex(f, f3).endVertex();
        worldrenderer.pos(scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), -90.0D).tex(f2, f3).endVertex();
        worldrenderer.pos(scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(f2, f1).endVertex();
        worldrenderer.pos(0.0D, 0.0D, -90.0D).tex(f, f1).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void updateTick() {
        if (this.recordPlayingUpFor > 0) {
            --this.recordPlayingUpFor;
        }

        if (this.titlesTimer > 0) {
            --this.titlesTimer;

            if (this.titlesTimer <= 0) {
                this.displayedTitle = "";
                this.displayedSubTitle = "";
            }
        }

        ++this.updateCounter;

        if (this.mc.thePlayer != null) {
            ItemStack itemstack = this.mc.thePlayer.inventory.getCurrentItem();

            if (itemstack == null) {
                this.remainingHighlightTicks = 0;
            } else if (this.highlightingItemStack != null && itemstack.getItem() == this.highlightingItemStack.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.highlightingItemStack) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == this.highlightingItemStack.getMetadata())) {
                if (this.remainingHighlightTicks > 0) {
                    --this.remainingHighlightTicks;
                }
            } else {
                this.remainingHighlightTicks = 40;
            }

            this.highlightingItemStack = itemstack;
        }
    }

    public void setRecordPlayingMessage(String recordName) {
        this.setRecordPlaying(I18n.format("record.nowPlaying", recordName), true);
    }

    public void setRecordPlaying(String message, boolean isPlaying) {
        this.recordPlaying = message;
        this.recordPlayingUpFor = 60;
        this.recordIsPlaying = isPlaying;
    }

    public void displayTitle(String title, String subTitle, int timeFadeIn, int displayTime, int timeFadeOut) {
        if (title == null && subTitle == null && timeFadeIn < 0 && displayTime < 0 && timeFadeOut < 0) {
            this.displayedTitle = "";
            this.displayedSubTitle = "";
            this.titlesTimer = 0;
        } else if (title != null) {
            this.displayedTitle = title;
            this.titlesTimer = this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut;
        } else if (subTitle != null) {
            this.displayedSubTitle = subTitle;
        } else {
            if (timeFadeIn >= 0) {
                this.titleFadeIn = timeFadeIn;
            }

            if (displayTime >= 0) {
                this.titleDisplayTime = displayTime;
            }

            if (timeFadeOut >= 0) {
                this.titleFadeOut = timeFadeOut;
            }

            if (this.titlesTimer > 0) {
                this.titlesTimer = this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut;
            }
        }
    }

    public void setRecordPlaying(IChatComponent component, boolean isPlaying) {
        this.setRecordPlaying(component.getUnformattedText(), isPlaying);
    }

    public GuiNewChat getChatGUI() {
        return this.persistantChatGUI;
    }

    public int getUpdateCounter() {
        return this.updateCounter;
    }

    public FontRenderer getFontRenderer() {
        return this.mc.fontRendererObj;
    }

    public GuiSpectator getSpectatorGui() {
        return this.spectatorGui;
    }

    public GuiPlayerTabOverlay getTabList() {
        return this.overlayPlayerList;
    }

    public void resetPlayersOverlayFooterHeader() {
        this.overlayPlayerList.resetFooterHeader();
    }
}

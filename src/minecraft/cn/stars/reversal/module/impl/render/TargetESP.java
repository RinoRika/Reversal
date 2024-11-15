package cn.stars.reversal.module.impl.render;

import cn.stars.reversal.event.impl.AttackEvent;
import cn.stars.reversal.event.impl.Render3DEvent;
import cn.stars.reversal.event.impl.WorldEvent;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.setting.impl.ModeValue;
import cn.stars.reversal.util.animation.advanced.Animation;
import cn.stars.reversal.util.animation.advanced.Direction;
import cn.stars.reversal.util.animation.advanced.impl.SmoothStepAnimation;
import cn.stars.reversal.util.render.RenderUtil;
import cn.stars.reversal.util.render.ThemeType;
import cn.stars.reversal.util.render.ThemeUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import javax.vecmath.Vector2f;
import java.awt.*;

@ModuleInfo(name = "TargetESP", chineseName = "敌人标记", description = "Display a ESP when you hit targets", chineseDescription = "当你攻击目标时渲染ESP", category = Category.RENDER)
public class TargetESP extends Module {
    private final ModeValue mode = new ModeValue("Mode", this, "Rectangle", "Rectangle", "Round");
    EntityLivingBase attackedEntity;
    private final Animation auraESPAnim = new SmoothStepAnimation(650, 1);

    @Override
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            attackedEntity = (EntityLivingBase) event.getTarget();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (attackedEntity != null) {
            // No null pointer anymore
            auraESPAnim.setDirection(!(attackedEntity.isDead || mc.thePlayer.getDistanceToEntity(attackedEntity) > 10) ? Direction.FORWARDS : Direction.BACKWARDS);
            if (auraESPAnim.finished(Direction.BACKWARDS)) {
                attackedEntity = null;
                return;
            }
            Color color = ThemeUtil.getThemeColor(ThemeType.ARRAYLIST, 1);
            Color color2 = ThemeUtil.getThemeColor(ThemeType.ARRAYLIST, 2);
            float dst = mc.thePlayer.getSmoothDistanceToEntity(attackedEntity);
            Vector2f vector2f = RenderUtil.targetESPSPos(attackedEntity, event.getPartialTicks());
            if (vector2f == null) return;
            RenderUtil.drawTargetESP2D(vector2f.x, vector2f.y, color, color2, 1.0f - MathHelper.clamp_float(Math.abs(dst - 6.0f) / 60.0f, 0.0f, 0.75f), 1, auraESPAnim.getOutput().floatValue());
        }
    }

    @Override
    public void onWorld(WorldEvent event) {
        attackedEntity = null;
    }
}

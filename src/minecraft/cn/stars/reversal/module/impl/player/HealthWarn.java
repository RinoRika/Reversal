package cn.stars.reversal.module.impl.player;

import cn.stars.reversal.Reversal;
import cn.stars.reversal.event.impl.PreMotionEvent;
import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.value.impl.NumberValue;
import cn.stars.reversal.ui.notification.NotificationType;

@ModuleInfo(name = "HealthWarn", chineseName = "低血量警告", description = "Give a warning to you on low health",
        chineseDescription = "在低血量时向你发送警告", category = Category.PLAYER)
public class HealthWarn extends Module {
    private final NumberValue health = new NumberValue("Health", this, 10, 1, 20, 1);
    private boolean canWarn;
    @Override
    public void onEnable() {
        canWarn = true;
    }
    @Override
    public void onDisable() {
        canWarn = true;
    }
    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.getHealth() <= health.getValue()) {
            if (canWarn) {
                Reversal.notificationManager.registerNotification("You dont have enough health!","HP Warning", 3000, NotificationType.WARNING, 5);
                canWarn = false;
            }
        } else {
            canWarn = true;
        }
    }
}

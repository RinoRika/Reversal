/*
 * Reversal Client - A PVP Client with hack visual.
 * Copyright 2025 Aerolite Society, Some rights reserved.
 */
package cn.stars.reversal.module.impl.client;

import cn.stars.reversal.module.Category;
import cn.stars.reversal.module.Module;
import cn.stars.reversal.module.ModuleInfo;
import cn.stars.reversal.value.impl.BoolValue;

@ModuleInfo(name = "IRC", localizedName = "module.IRC.name", description = "Private Reversal chat channel", localizedDescription = "module.IRC.desc", category = Category.CLIENT)
public class IRC extends Module {
    public final BoolValue markOnlineUsers = new BoolValue("Mark Online Users", this, true);
    public final BoolValue allowDisable = new BoolValue("Allow Disable", this, false);

    @Override
    public void onUpdateAlways() {
        if (!allowDisable.isEnabled() && !this.isEnabled()) toggleModule();
    }
}

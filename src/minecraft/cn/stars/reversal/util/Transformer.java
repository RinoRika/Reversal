package cn.stars.reversal.util;

import cn.stars.reversal.GameInstance;
import cn.stars.reversal.module.impl.misc.CustomName;
import cn.stars.reversal.util.misc.ModuleInstance;

public class Transformer implements GameInstance {
    public static String constructString(String string) {
        String result = string;
        if (ModuleInstance.getModule(CustomName.class).isEnabled() && ModuleInstance.getModule(CustomName.class).textValue.getText() != null && !ModuleInstance.getModule(CustomName.class).textValue.getText().isEmpty()) {
            result = result.replace(mc.session.getUsername(), ModuleInstance.getModule(CustomName.class).textValue.getText().replace("&", "§"));
        }
        return result;
    }
}

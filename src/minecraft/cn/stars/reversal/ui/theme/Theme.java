package cn.stars.reversal.ui.theme;

import lombok.Getter;

@Getter
public enum Theme {
    DARKMODE("Dark Mode", 1),
    LIGHTMODE("Light Mode", 1);

    private final String name;
    public float nameOpacityInMainMenu, opacityInMainMenu;

    Theme(final String name, final float opacityInMainMenu) {
        this.name = name;
        this.nameOpacityInMainMenu = opacityInMainMenu;
    }
}
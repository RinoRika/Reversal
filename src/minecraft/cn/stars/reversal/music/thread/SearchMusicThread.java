package cn.stars.reversal.music.thread;

import cn.stars.reversal.RainyAPI;
import cn.stars.reversal.music.api.MusicAPI;
import cn.stars.reversal.music.ui.MusicPlayerScreen;
import cn.stars.reversal.music.ui.gui.MusicPlayerGUI;
import cn.stars.reversal.music.ui.gui.impl.PlayListGUI;
import lombok.Getter;

/**
 * @author ChengFeng
 * @since 2024/8/16
 **/
public class SearchMusicThread extends Thread {
    private final MusicPlayerScreen parent;
    @Getter
    private MusicPlayerGUI gui;

    public SearchMusicThread(MusicPlayerScreen parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        gui = new PlayListGUI(MusicAPI.search(parent.getSearchField().getText()), parent.getCurrentGUI());
    }
}

package com.sharry.picturepicker.widget.toolbar;

/**
 * StatusBar/NavigationBar 的样式
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.2
 * @since 2017/10/10 13:52
 */
public enum Style {

    TRANSPARENT(0),
    TRANSLUCENCE(1),
    HIDE(3),
    DEFAULT(4);

    int val;

    Style(int val) {
        this.val = val;
    }

    int getVal() {
        return val;
    }

}
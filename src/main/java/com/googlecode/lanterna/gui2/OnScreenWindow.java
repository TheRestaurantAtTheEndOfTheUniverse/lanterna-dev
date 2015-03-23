/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Window;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author kessinger
 */
public class OnScreenWindow extends BasicWindow {

    private static final Set<Hint> ON_SCREEN = new HashSet<Hint>() {{
            add(Window.Hint.FIT_TERMINAL_WINDOW);
        }};

    public OnScreenWindow() {
    }

    public OnScreenWindow(String title) {
        super(title);
    }

    @Override
    public Set<Hint> getHints() {
        return ON_SCREEN;
    }

}

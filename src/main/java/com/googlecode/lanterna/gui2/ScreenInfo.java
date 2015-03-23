/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalSize;

/**
 *
 * @author kessinger
 */
public class ScreenInfo {

    private final TerminalSize screenSize;

    public ScreenInfo(TerminalSize screenSize) {
        this.screenSize = screenSize;
    }

    public TerminalSize getScreenSize() {
        return screenSize;
    }
}

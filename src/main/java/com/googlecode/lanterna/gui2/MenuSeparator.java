/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.input.KeyStroke;

/**
 *
 * @author kessinger
 */
public class MenuSeparator implements MenuItem {

    @Override
    public Runnable getAction() {
        return null;
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public String getLabel(int menuWidth) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<menuWidth;i++)
            sb.append(Symbols.SINGLE_LINE_HORIZONTAL);
        
        return sb.toString();
    }

    @Override
    public KeyStroke getShortCut() {
        return null;
    }
    
}

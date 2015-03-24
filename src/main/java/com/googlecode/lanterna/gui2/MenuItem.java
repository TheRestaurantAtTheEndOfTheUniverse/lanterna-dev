/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.input.KeyStroke;

/**
 *
 * @author kessinger
 */
public interface MenuItem {

    Runnable getAction();

    String getLabel();

    String getLabel(int menuWidth);

    KeyStroke getShortCut();
  
}

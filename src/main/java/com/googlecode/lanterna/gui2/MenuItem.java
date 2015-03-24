/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

/**
 *
 * @author kessinger
 */
public interface MenuItem {

    Runnable getAction();

    String getLabel();

    String getLabel(int menuWidth);

    String getShortCut();
  
}

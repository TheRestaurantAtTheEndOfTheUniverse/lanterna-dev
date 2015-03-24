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
public class ActionableMenuItem implements MenuItem {
  private String label;
  private String shortCut;
  private Runnable action;

  public ActionableMenuItem(String label) {
    this.label = label;
  }

  public ActionableMenuItem(String label, String shortCut) {
    this.label = label;
    this.shortCut = shortCut;
  }

    @Override
  public String getLabel() {
    return label;
  }
  
    @Override
  public String getLabel(int menuWidth) {
    return label;
  }

    @Override
  public String getShortCut() {
    return shortCut;
  }  

    @Override
    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }
}

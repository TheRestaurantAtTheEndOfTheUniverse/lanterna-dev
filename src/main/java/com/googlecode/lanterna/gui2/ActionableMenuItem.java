/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

/**
 *
 * @author kessinger
 */
public class ActionableMenuItem implements MenuItem {

    private String label;
    private KeyStroke shortCut;
    private Runnable action;

    public ActionableMenuItem(String label) {
        this.label = label;
    }

    public ActionableMenuItem(String label, KeyStroke shortCut) {
        this.label = label;
        this.shortCut = shortCut;
    }

    @Override
    public String getLabel() {
        return label+"  "+getShortcutLabel();
    }

    @Override
    public String getLabel(int menuWidth) {
        final StringBuilder sb = new StringBuilder(label);
        final String shortcutLabel = getShortcutLabel();
        while(sb.length() + shortcutLabel.length() < menuWidth)
            sb.append(' ');
        
        sb.append(shortcutLabel);
        
        return sb.toString();
    }

    @Override
    public KeyStroke getShortCut() {
        return shortCut;
    }

    @Override
    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    private String getKeyTypeLabel() {
        switch (shortCut.getKeyType()) {
            case F1:
                return "F1";
            case F2:
                return "F2";
            case F3:
                return "F3";
            case F4:
                return "F4";
            case F5:
                return "F5";
            case F6:
                return "F6";
            case F7:
                return "F7";
            case F8:
                return "F8";
            case F9:
                return "F9";
            case F10:
                return "F10";
            case Delete:
                return "Delete";
            case End:
                return "End";
            case Enter:
                return "Enter";
            case PageUp:
                return "Page up";
            case PageDown:
                return "Page down";
            case Home:
                return "Home";
            case Insert:
                return "Insert";
            default:
                return "";
        }
    }

    private String getShortcutLabel() {
        if (shortCut == null) {
            return "";
        }

        if(shortCut.getKeyType() == KeyType.Character) {
            final StringBuilder sb = new StringBuilder();
            if (shortCut.isCtrlDown()) {
                sb.append("Ctrl-");
            }            
           if (Character.isUpperCase(shortCut.getCharacter())) {
                sb.append("Shift-");
            }
            if (shortCut.isAltDown()) {
                sb.append("Alt-");
            }
            
            sb.append(Character.toLowerCase(shortCut.getCharacter()));
            return sb.toString();
        }
        
        return getKeyTypeLabel();
    }
}

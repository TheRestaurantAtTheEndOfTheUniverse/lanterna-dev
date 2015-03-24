/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;

/**
 *
 * @author kessinger
 */
public class MenuWindow extends AbstractWindow {

    private Menu menu;
    private int selectedIndex = 0;

    public MenuWindow(Menu menu) {
        this.menu = menu;
        this.setPosition(TerminalPosition.TOP_LEFT_CORNER);
    }

    @Override
    public TerminalSize getPreferredSize() {
        int maxWidth = 0;
        for (MenuItem item : menu.getItems()) {
            maxWidth = Math.max(maxWidth, item.getLabel().length());
        }

        return new TerminalSize(maxWidth+2, menu.getItems().size());
    }

    public int findUp() {
        for (int i = selectedIndex - 1; i >= 0; i--) {
            if (menu.getItems().get(i).getAction() != null) {
                return i;
            }
        }

        return selectedIndex;
    }

    public int findDown() {
        for (int i = selectedIndex + 1; i < menu.getItems().size(); i++) {
            if (menu.getItems().get(i).getAction() != null) {
                return i;
            }
        }

        return selectedIndex;
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        switch (key.getKeyType()) {
            case ArrowDown:
                selectedIndex = findDown();
                break;
            case ArrowUp:
                selectedIndex = findUp();
                break;
            case Enter:
                final MenuItem item = menu.getItems().get(selectedIndex);
                item.getAction().run();
                close();
                break;
            case Escape:
                close();
                break;
        }

        return true;
    }

    @Override
    public void draw(TextGUIGraphics graphics) {
        graphics.applyThemeStyle(graphics.getThemeDefinition(Window.class).getNormal());
        graphics.fill(' ');
        int row = 0;
        
        for (MenuItem item : menu.getItems()) {
            if (selectedIndex == row) {
                graphics.applyThemeStyle(graphics.getThemeDefinition(Window.class).getSelected());
            }
            else {
                graphics.applyThemeStyle(graphics.getThemeDefinition(Window.class).getNormal());
            }

            graphics.putString(0, row++, item.getLabel(graphics.getSize().getColumns()));
        }
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void setTitle(String title) {
    }
}

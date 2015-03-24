/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.TextGUI.Listener;
import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kessinger
 */
public class MenuBar extends AbstractInteractableComponent<MenuBarRenderer> implements Listener {

    private List<Menu> menus = new ArrayList<>();
    private Menu activeMenu;
    private Map<Menu, TerminalPosition> menuBodyRenderPostitions = new HashMap<>();
    private WindowBasedTextGUI overlayTarget;

    public MenuBar(WindowBasedTextGUI overlayTarget) {
        this.overlayTarget = overlayTarget;
    }

    public void addMenu(Menu menu) {
        menus.add(menu);
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public Menu getActiveMenu() {
        return activeMenu;
    }

    public void setBodyPosition(Menu menu, TerminalPosition position) {
        menuBodyRenderPostitions.put(menu, position);
    }

    private void openBody() {
        if(activeMenu != null && activeMenu.getItems().size() > 0) {
            final TerminalPosition topLeft = menuBodyRenderPostitions.get(activeMenu);
            MenuWindow body = new MenuWindow(activeMenu);
            body.setPosition(menuBodyRenderPostitions.get(activeMenu));
            overlayTarget.addOverlay(body);
        }
    }

    @Override
    protected MenuBarRenderer createDefaultRenderer() {
        return new MenuBarRenderer();
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        activeMenu = menus.get(0);
    }

    @Override
    public void onRemoved(Container container) {
        overlayTarget.removeListener(this);
        super.onRemoved(container);
    }

    @Override
    public void onAdded(Container container) {
        overlayTarget.addListener(this);
        super.onAdded(container);
    }

    @Override
    public Result handleKeyStroke(KeyStroke keyStroke) {
        final int index = menus.indexOf(activeMenu);

        switch (keyStroke.getKeyType()) {
            case ArrowRight:
                if (index < menus.size() - 1) {
                    activeMenu = menus.get(index + 1);
                    return Result.HANDLED;
                }
                return Result.MOVE_FOCUS_RIGHT;
            case ArrowLeft:
                if (index > 0) {
                    activeMenu = menus.get(index - 1);
                    return Result.HANDLED;
                }
                return Result.MOVE_FOCUS_LEFT;
            case Enter:
                openBody();
                return Result.HANDLED;
        }

        return super.handleKeyStroke(keyStroke);
    }

    @Override
    public boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
        for (Menu m : menus) {
            for (MenuItem item : m.getItems()) {
                if (item.getShortCut() != null && item.getShortCut().equals(keyStroke) && item.getAction() != null) {
                    item.getAction().run();
                    return true;
                }
            }
        }
        
        return false;
    }
}

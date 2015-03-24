/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.AbstractBorder;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

/**
 *
 * @author kessinger
 */
public class EmptyBorder extends AbstractBorder {

    private class EmptyBorderRenderer implements Border.BorderRenderer {

        @Override
        public TerminalSize getPreferredSize(Component component) {
            EmptyBorder border = (EmptyBorder) component;
            Component wrappedComponent = border.getComponent();
            TerminalSize preferredSize = wrappedComponent == null ? TerminalSize.ZERO
                                         : wrappedComponent.getPreferredSize();
            
            preferredSize = preferredSize.withRelativeColumns(border.getLeft() + border.getRight()).withRelativeRows(border.getTop() + border.getBottom());

            return preferredSize.max(new TerminalSize(border.getLeft() + border.getRight(), border.getTop() + border.getBottom()));
        }

        @Override
        public TerminalPosition getWrappedComponentTopLeftOffset() {
            return new TerminalPosition(left, top);
        }

        @Override
        public TerminalSize getWrappedComponentSize(TerminalSize borderSize) {
            return borderSize.withRelativeColumns(-left - right).withRelativeRows(-top - bottom);
        }

        @Override
        public void drawComponent(TextGUIGraphics graphics, Component component) {
            Border border = (Border) component;
            Component wrappedComponent = border.getComponent();
            if (wrappedComponent == null) {
                return;
            }
            TerminalSize drawableArea = graphics.getSize();

            if (color != null) {
                graphics.setBackgroundColor(color);
                graphics.fill(' ');
            }

            wrappedComponent.draw(graphics.newTextGraphics(getWrappedComponentTopLeftOffset(), 
                                                           getWrappedComponentSize(drawableArea)));
        }
    }

    private int top;
    private int right;
    private int bottom;
    private int left;

    private TextColor color;

    public EmptyBorder(int width, TextColor color) {
        this(width, width, width, width, color);
    }

    public EmptyBorder(int top, int right, int bottom, int left, TextColor color) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        this.color = color;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    @Override
    protected BorderRenderer createDefaultRenderer() {
        return new EmptyBorderRenderer();
    }
}

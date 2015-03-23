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
    EmptyBorder border = (EmptyBorder)component;
    Component wrappedComponent = border.getComponent();
    TerminalSize preferredSize;
    if(wrappedComponent == null) {
      preferredSize = TerminalSize.ZERO;
    }
    else {
      preferredSize = wrappedComponent.getPreferredSize();
    }
    preferredSize = preferredSize.withRelativeColumns(width*2).withRelativeRows(width*2);

    return preferredSize.max(new TerminalSize(border.getWidth() * 2, width * 2));
  }

  @Override
  public TerminalPosition getWrappedComponentTopLeftOffset() {
    return new TerminalPosition(width, width);
  }

  @Override
  public TerminalSize getWrappedComponentSize(TerminalSize borderSize) {
    return borderSize.withRelativeColumns(-2*width).withRelativeRows(-2*width);
  }

  @Override
  public void drawComponent(TextGUIGraphics graphics, Component component) {
    Border border = (Border)component;
    Component wrappedComponent = border.getComponent();
    if(wrappedComponent == null) {
      return;
    }
    TerminalSize drawableArea = graphics.getSize();

    if(color != null) {
      graphics.setBackgroundColor(color);
      graphics.fill(' ');
    }

    wrappedComponent.draw(graphics.newTextGraphics(getWrappedComponentTopLeftOffset(), getWrappedComponentSize(drawableArea)));
  }
}

  
  private int width;
  private TextColor color;

  public EmptyBorder(int width, TextColor color) {
    this.width = width;
    this.color = color;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
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

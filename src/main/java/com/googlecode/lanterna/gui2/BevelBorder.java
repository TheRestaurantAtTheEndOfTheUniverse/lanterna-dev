/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.AbstractBorder;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

/**
 *
 * @author kessinger
 */
public class BevelBorder extends AbstractBorder {
  private String title;

  public BevelBorder(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
    
  private static class BevelBorderRenderer implements BorderRenderer {
    
    @Override
    public TerminalPosition getWrappedComponentTopLeftOffset() {
      return TerminalPosition.OFFSET_1x1;
    }

    @Override
    public TerminalSize getWrappedComponentSize(TerminalSize borderSize) {
      return borderSize.withRelativeColumns(-2).withRelativeRows(-2);
    }

    @Override
    public TerminalSize getPreferredSize(Component component) {
      BevelBorder border = (BevelBorder)component;
      Component wrappedComponent = border.getComponent();
      TerminalSize preferredSize;
      if(wrappedComponent == null) {
        preferredSize = TerminalSize.ZERO;
      }
      else {
        preferredSize = wrappedComponent.getPreferredSize();
      }
      preferredSize = preferredSize.withRelativeColumns(2).withRelativeRows(2);
      String borderTitle = border.getTitle();
      return preferredSize.max(new TerminalSize((borderTitle.isEmpty() ? 2 : borderTitle.length() + 4), 2));
    }

    @Override
    public void drawComponent(TextGUIGraphics graphics, Component component) {
      BevelBorder border = (BevelBorder)component;
      Component wrappedComponent = border.getComponent();
      if(wrappedComponent == null) {
        return;
      }
      
      ThemeDefinition themeDefinition = graphics.getThemeDefinition(DefaultWindowDecorationRenderer.class);
      char horizontalLine = themeDefinition.getCharacter("HORIZONTAL_LINE", Symbols.SINGLE_LINE_HORIZONTAL);
      char verticalLine = themeDefinition.getCharacter("VERTICAL_LINE", Symbols.SINGLE_LINE_VERTICAL);
      char bottomLeftCorner = themeDefinition.getCharacter("BOTTOM_LEFT_CORNER", Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
      char topLeftCorner = themeDefinition.getCharacter("TOP_LEFT_CORNER", Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
      char bottomRightCorner = themeDefinition.getCharacter("BOTTOM_RIGHT_CORNER", Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
      char topRightCorner = themeDefinition.getCharacter("TOP_RIGHT_CORNER", Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);

      TerminalSize drawableArea = graphics.getSize();
      final String renderTitle = border.getTitle().substring(0, Math.max(0, Math.min(border.getTitle().length(),
        drawableArea.getColumns() - 3)));

      graphics.applyThemeStyle(themeDefinition.getPreLight());
      graphics.drawLine(new TerminalPosition(0, drawableArea.getRows() - 2), new TerminalPosition(0, 1), verticalLine);
      graphics.drawLine(new TerminalPosition(1, 0), new TerminalPosition(drawableArea.getColumns() - 2, 0), horizontalLine);
      graphics.setCharacter(0, 0, topLeftCorner);
      graphics.setCharacter(0, drawableArea.getRows() - 1, bottomLeftCorner);

      graphics.applyThemeStyle(themeDefinition.getNormal());

      graphics.drawLine(
        new TerminalPosition(drawableArea.getColumns() - 1, 1),
        new TerminalPosition(drawableArea.getColumns() - 1, drawableArea.getRows() - 2),
        verticalLine);
      graphics.drawLine(
        new TerminalPosition(1, drawableArea.getRows() - 1),
        new TerminalPosition(drawableArea.getColumns() - 2, drawableArea.getRows() - 1),
        horizontalLine);

      graphics.setCharacter(drawableArea.getColumns() - 1, 0, topRightCorner);
      graphics.setCharacter(drawableArea.getColumns() - 1, drawableArea.getRows() - 1, bottomRightCorner);

      if(renderTitle.length() > 0) {
        graphics.putString(2, 0, renderTitle);
      }
      
      wrappedComponent.draw(graphics.newTextGraphics(getWrappedComponentTopLeftOffset(), 
        getWrappedComponentSize(drawableArea)));
    }    
  }
  
  @Override
  protected BorderRenderer createDefaultRenderer() {
    return new BevelBorderRenderer();
  }  
}

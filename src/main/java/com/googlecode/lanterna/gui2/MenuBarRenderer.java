/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

/**
 *
 * @author kessinger
 */
public class MenuBarRenderer implements InteractableRenderer<MenuBar> {

  @Override
  public TerminalPosition getCursorLocation(MenuBar component) {
    return null;
  }

  @Override
  public TerminalSize getPreferredSize(MenuBar component) {
    int width=0;
    for(Menu m : component.getMenus())
      width += m.getTitle().length();
    
     return new TerminalSize(width+(component.getMenus().size()-1)*2, 1);
  }

  /**
   *
   * @param graphics
   * @param bar
   */
  @Override
  public void drawComponent(TextGUIGraphics graphics, MenuBar bar) {    
    graphics.applyThemeStyle(graphics.getThemeDefinition(MenuBar.class).getNormal());  
    graphics.fill(' ');
    
    bar.getTextGUI();
    
    int x = 0;
    final TerminalPosition topLeft = graphics.getAbsolutePosition(new TerminalPosition(0,1));
    
    for(Menu m : bar.getMenus()) {
      final TerminalPosition position = topLeft.withRelativeColumn(x);
      bar.setBodyPosition(m, position);
      
      if(bar.isFocused() && bar.getActiveMenu() == m)
        graphics.applyThemeStyle(graphics.getThemeDefinition(MenuBar.class).getSelected());
      else
        graphics.applyThemeStyle(graphics.getThemeDefinition(MenuBar.class).getNormal());
      
      graphics.putString(x, 0, m.getTitle());
      x+=m.getTitle().length()+2;
    }
  }  
}

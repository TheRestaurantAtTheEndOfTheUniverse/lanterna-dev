/*
 * This file is part of lanterna (http://code.google.com/p/lanterna/).
 * 
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2010-2012 mabe02
 */

package org.lantern.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lantern.LanternException;
import org.lantern.gui.theme.Theme;
import org.lantern.input.Key;
import org.lantern.terminal.ACS;
import org.lantern.terminal.TerminalPosition;
import org.lantern.terminal.TerminalSize;

/**
 *
 * @author mberglun
 */
public class StaticTextArea  extends AbstractInteractableComponent
{
    private final List<String> lines;
    private final TerminalSize maxSize;
    private final int longestLine;
    private int scrollTopIndex;

    public StaticTextArea(TerminalSize maxSize, String text)
    {
        if(text == null)
            text = "";
        
        this.lines = new ArrayList<String>();
        this.maxSize = maxSize;
        this.scrollTopIndex = 0;
        lines.addAll(Arrays.asList(text.split("\n")));
        
        int longestLine = 0;
        for(String line: lines)
            if(line.length() > longestLine)
                longestLine = line.length();
        this.longestLine = longestLine;
    }

    public TerminalSize getPreferredSize()
    {
        return new TerminalSize(
                maxSize.getColumns() < longestLine ? maxSize.getColumns() : longestLine, 
                maxSize.getRows() < lines.size() ? maxSize.getRows() : lines.size());
    }

    public void repaint(TextGraphics graphics)
    {
        graphics.applyThemeItem(Theme.Category.Item);
        graphics.fillArea(' ');

        for(int i = scrollTopIndex; i < lines.size(); i++) {
            if(i - scrollTopIndex >= graphics.getHeight())
                break;

            graphics.applyThemeItem(Theme.Category.Item);
            printItem(graphics, 0, 0 + i - scrollTopIndex, lines.get(i));
        }

        if(lines.size() > graphics.getHeight()) {
            graphics.applyThemeItem(Theme.Category.DefaultDialog);
            graphics.drawString(graphics.getWidth() - 1, 0, ACS.ARROW_UP + "");

            graphics.applyThemeItem(Theme.Category.DefaultDialog);
            for(int i = 1; i < graphics.getHeight() - 1; i++)
                graphics.drawString(graphics.getWidth() - 1, i, ACS.BLOCK_MIDDLE + "");

            graphics.applyThemeItem(Theme.Category.DefaultDialog);
            graphics.drawString(graphics.getWidth() - 1, graphics.getHeight() - 1, ACS.ARROW_DOWN + "");
            
            //Finally print the 'tick'
            int scrollableSize = lines.size() - graphics.getHeight();
            double position = (double)scrollTopIndex / ((double)scrollableSize - 1.0);
            int tickPosition = (int)(((double)graphics.getHeight() - 3.0) * position);

            graphics.applyThemeItem(Theme.Category.Shadow);
            graphics.drawString(graphics.getWidth() - 1, 1 + tickPosition, " ");
            setHotspot(graphics.translateToGlobalCoordinates(new TerminalPosition(graphics.getWidth() - 1, 1 + tickPosition)));
        }
    }

    public void keyboardInteraction(Key key, InteractableResult result) throws LanternException
    {
        switch(key.getKind()) {
            case Tab:
            case ArrowRight:
            case Enter:
                result.type = Result.NEXT_INTERACTABLE;
                break;

            case ReverseTab:
            case ArrowLeft:
                result.type = Result.PREVIOUS_INTERACTABLE;
                break;

            case ArrowDown:
                if(scrollTopIndex < lines.size() - maxSize.getRows())
                    scrollTopIndex++;
                break;

            case ArrowUp:
                if(scrollTopIndex > 0)
                    scrollTopIndex--;
                break;
        }
        invalidate();
    }

    private void printItem(TextGraphics graphics, int x, int y, String text)
    {
        if(text.length() > graphics.getWidth())
            text = text.substring(0, graphics.getWidth());
        graphics.drawString(x, y, text);
    }

}
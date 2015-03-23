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
 * Copyright (C) 2010-2014 Martin
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.VirtualScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author kessinger
 */
public class VirtualMultiWindowTextGUI extends AbstractWindowTextGUI<VirtualScreen> {

    public VirtualMultiWindowTextGUI(Screen screen) {
        this(screen, TextColor.ANSI.BLUE);
    }

    public VirtualMultiWindowTextGUI(Screen screen, TextColor backgroundColor) {
        this(screen, new DefaultWindowManager(), new EmptySpace(backgroundColor));
    }

    public VirtualMultiWindowTextGUI(Screen screen, WindowManager windowManager, Component background) {
        this(screen, windowManager, new WindowShadowRenderer(), background);
    }

    public VirtualMultiWindowTextGUI(Screen screen, WindowManager windowManager, WindowPostRenderer postRenderer, Component background) {
        super(new VirtualScreen(screen), windowManager, postRenderer, background);
    }
    
    @Override
    public synchronized void updateScreen() throws IOException {
        TerminalSize preferredSize = TerminalSize.ONE;
        for(Window window: getWindows()) {
            preferredSize = preferredSize.max(window.getPreferredSize());
        }
        screen.setMinimumSize(preferredSize.withRelativeColumns(10).withRelativeRows(5));
        super.updateScreen();
    }
}

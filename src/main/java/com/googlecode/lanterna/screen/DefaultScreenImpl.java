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
package com.googlecode.lanterna.screen;

import com.googlecode.lanterna.CJKUtils;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.ResizeListener;
import com.googlecode.lanterna.terminal.TextColor;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalPosition;
import com.googlecode.lanterna.terminal.TerminalSize;
import java.io.IOException;
import java.util.*;

/**
 * A layer to put on top of a Terminal object, giving you a kind of screen buffer to use, which is a lot easier to work
 * with. Drawing text or graphics to the terminal is kind of like writing to a bitmap.
 *
 * @author Martin
 */
public class DefaultScreenImpl implements Screen {

    private final Object mutex;
    private final Terminal terminal;
    private final LinkedList<TerminalSize> resizeQueue;
    private TerminalPosition cursorPosition;
    private TerminalSize terminalSize;
    private ScreenCharacter[][] visibleScreen;
    private ScreenCharacter[][] backbuffer;
    private ScreenCharacter paddingCharacter;
    private boolean wholeScreenInvalid;
    private boolean hasBeenActivated;

    //How to deal with \t characters
    private TabBehaviour tabBehaviour;

    /**
     * Creates a new Screen on top of a supplied terminal, will query the terminal for its size. The screen is initially
     * blank.
     *
     * @param terminal
     * @throws LanternaException
     */
    public DefaultScreenImpl(Terminal terminal) throws IOException {
        this(terminal, terminal.getTerminalSize());
    }

    /**
     * Creates a new Screen on top of a supplied terminal and will set the size of the screen to a supplied value. The
     * screen is initially blank.
     *
     * @param terminal
     * @param terminalSize
     */
    public DefaultScreenImpl(Terminal terminal, TerminalSize terminalSize) {
        this(terminal, terminalSize.getColumns(), terminalSize.getRows());
    }

    /**
     * Creates a new Screen on top of a supplied terminal and will set the size of the screen to a supplied value. The
     * screen is initially blank.
     *
     * @param terminal
     * @param terminalWidth Width (number of columns) of the terminal
     * @param terminalHeight Height (number of rows) of the terminal
     */
    public DefaultScreenImpl(Terminal terminal, int terminalWidth, int terminalHeight) {
        this.mutex = new Object();
        this.terminal = terminal;
        this.terminalSize = new TerminalSize(terminalWidth, terminalHeight);
        this.visibleScreen = new ScreenCharacter[terminalHeight][terminalWidth];
        this.backbuffer = new ScreenCharacter[terminalHeight][terminalWidth];
        this.paddingCharacter = new ScreenCharacter('X', TextColor.ANSI.GREEN, TextColor.ANSI.BLACK);
        this.resizeQueue = new LinkedList<TerminalSize>();
        this.wholeScreenInvalid = false;
        this.hasBeenActivated = false;
        this.cursorPosition = new TerminalPosition(0, 0);
        this.tabBehaviour = TabBehaviour.ALIGN_TO_COLUMN_8;

        this.terminal.addResizeListener(new TerminalResizeListener());

        //Initialize the screen
        clear();
    }

    /**
     * @return The terminal which is the backend for this screen
     */
    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * @return Position where the cursor will be located after the screen has been refreshed or {@code null} if the
     * cursor is not visible
     */
    @Override
    public TerminalPosition getCursorPosition() {
        return cursorPosition;
    }

    /**
     * Moves the current cursor position or hides it. If the cursor is hidden and given a new position, it will be
     * visible after this method call.
     *
     * @param position 0-indexed column and row numbers of the new position, or if {@code null}, hides the cursor
     */
    @Override
    public void setCursorPosition(TerminalPosition position) {
        this.cursorPosition = position;
    }

    /**
     * Moves the current cursor position, and if the cursor was hidden it will be visible after this call
     *
     * @param column 0-indexed column number of the new position
     * @param row 0-indexed row number of the new position
     */
    @Override
    public void setCursorPosition(int column, int row) {
        synchronized(mutex) {
            if(column >= 0 && column < terminalSize.getColumns()
                    && row >= 0 && row < terminalSize.getRows()) {
                setCursorPosition(new TerminalPosition(column, row));
            }
        }
    }

    /**
     * Sets the behaviour for what to do about tab characters.
     *
     * @see TabBehaviour
     */
    @Override
    public void setTabBehaviour(TabBehaviour tabBehaviour) {
        if(tabBehaviour != null) {
            this.tabBehaviour = tabBehaviour;
        }
    }

    @Override
    public void setPaddingCharacter(
            char character,
            TextColor foregroundColor,
            TextColor backgroundColor,
            ScreenCharacterStyle... style) {

        this.paddingCharacter = new ScreenCharacter(character, foregroundColor, backgroundColor, new HashSet<ScreenCharacterStyle>(Arrays.asList(style)));
    }

    /**
     * Gets the behaviour for what to do about tab characters.
     *
     * @see TabBehaviour
     */
    @Override
    public TabBehaviour getTabBehaviour() {
        return tabBehaviour;
    }

    /**
     * Reads the next {@code Key} from the input queue, or returns null if there is nothing on the queue.
     */
    @Override
    public KeyStroke readInput() throws IOException {
        return terminal.readInput();
    }

    /**
     * @return Size of the screen
     */
    @Override
    public TerminalSize getTerminalSize() {
        synchronized(mutex) {
            return terminalSize;
        }
    }

    /**
     * Calling this method will put the underlying terminal in private mode, clear the screen, move the cursor and
     * refresh.
     *
     * @throws LanternaException
     */
    @Override
    public void startScreen() throws IOException {
        if(hasBeenActivated) {
            return;
        }

        hasBeenActivated = true;
        terminal.enterPrivateMode();
        terminal.getTerminalSize();
        synchronized(mutex) {
            resizeScreenIfNeeded();
        }
        terminal.clearScreen();
        clear();
        if(cursorPosition != null) {
            terminal.setCursorVisible(true);
            terminal.moveCursor(cursorPosition.getColumn(), cursorPosition.getRow());
        } else {
            terminal.setCursorVisible(false);
        }
        refresh();
    }

    /**
     * Calling this method will make the underlying terminal leave private mode, effectively going back to whatever
     * state the terminal was in before calling {@code startScreen()}
     *
     * @throws LanternaException
     */
    @Override
    public void stopScreen() throws IOException {
        if(!hasBeenActivated) {
            return;
        }

        while(readInput() != null) {
            //Drain the input queue before exiting private mode and closing the Screen.
        }
        terminal.exitPrivateMode();
        hasBeenActivated = false;
    }

    /**
     * Erases all the characters on the screen, effectively giving you a blank area. The default background color will
     * be used, if you want to fill the screen with a different color you will need to do this manually.
     */
    @Override
    public void clear() {
        //ScreenCharacter is immutable, so we can use it for every element
        ScreenCharacter background = new ScreenCharacter(' ');

        synchronized(mutex) {
            for(int y = 0; y < terminalSize.getRows(); y++) {
                for(int x = 0; x < terminalSize.getColumns(); x++) {
                    backbuffer[y][x] = background;
                }
            }
        }
    }

    /**
     * Draws a string on the screen at a particular position
     *
     * @param x 0-indexed column number of where to put the first character in the string
     * @param y 0-indexed row number of where to put the first character in the string
     * @param string Text to put on the screen
     * @param foregroundColor What color to use for the text
     * @param backgroundColor What color to use for the background
     * @param styles Additional styles to apply to the text
     */
    @Override
    public void putString(int x, int y, String string, TextColor foregroundColor,
            TextColor backgroundColor, ScreenCharacterStyle... styles) {
        Set<ScreenCharacterStyle> drawStyle = EnumSet.noneOf(ScreenCharacterStyle.class);
        drawStyle.addAll(Arrays.asList(styles));
        putString(x, y, string, foregroundColor, backgroundColor, drawStyle);
    }

    /**
     * Draws a string on the screen at a particular position
     *
     * @param x 0-indexed column number of where to put the first character in the string
     * @param y 0-indexed row number of where to put the first character in the string
     * @param string Text to put on the screen
     * @param foregroundColor What color to use for the text
     * @param backgroundColor What color to use for the background
     * @param styles Additional styles to apply to the text
     */
    @Override
    public void putString(int x, int y, String string, TextColor foregroundColor,
            TextColor backgroundColor, Set<ScreenCharacterStyle> styles) {
        string = tabBehaviour.replaceTabs(string, x);
        for(int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            putCharacter(x + i, y, new ScreenCharacter(character, foregroundColor, backgroundColor, styles));
            if(CJKUtils.isCharCJK(character)) {
                putCharacter(x + ++i, y, ScreenCharacter.CJK_PADDING_CHARACTER);
            }
        }
    }

    void putCharacter(int x, int y, ScreenCharacter character) {
        synchronized(mutex) {
            if(y < 0 || y >= backbuffer.length || x < 0 || x >= backbuffer[0].length) {
                return;
            }

            //Only create a new character if the
            if(!backbuffer[y][x].equals(character)) {
                backbuffer[y][x] = character;
            }
        }
    }

    /**
     * This method will check if there are any resize commands pending. If true, you need to call refresh() to perform
     * the screen resize
     *
     * @return true if the size is the same as before, false if the screen should be resized
     */
    @Override
    public boolean resizePending() {
        synchronized(resizeQueue) {
            return !resizeQueue.isEmpty();
        }
    }

    /**
     * Calling this method will check if the terminal has changed since and in that case update the dimensions of this
     * Screen to match.
     *
     * @return Will return true if dimensions were changed, otherwise false. You probably want to clear and redraw the
     * entire screen if this method returns true.
     */
    public boolean updateScreenSize() {
        if(!resizePending()) {
            return false;
        }
        synchronized(mutex) {
            resizeScreenIfNeeded();
        }
        return true;
    }

    /**
     * Clears the terminal and repaints with the whole content of the Screen. This is useful of something has written to
     * the terminal outside of the Screen (System.out or through direct calls to the underlying Terminal) and you want
     * to make sure that the content of Screen is completely pushed to the terminal.
     */
    @Override
    public void completeRefresh() {
        wholeScreenInvalid = true;
        refresh();
    }

    /**
     * Call this method to make changes done through {@code putCharacter(...)},
     * {@code putString(...)} visible on the terminal. The screen will calculate the changes that are required and send
     * the necessary characters and control sequences to make it so. If the terminal has been resized since the last
     * refresh, and no call to {@code doResize()} has been made, this method will resize the internal buffer and fill
     * the extra space with a padding character.
     */
    @Override
    public void refresh() {
        if(!hasBeenActivated) {
            return;
        }

        synchronized(mutex) {
            //If any resize operations are in the queue, execute them
            resizeScreenIfNeeded();

            Map<TerminalPosition, ScreenCharacter> updateMap = new TreeMap<TerminalPosition, ScreenCharacter>(new ScreenPointComparator());

            for(int y = 0; y < terminalSize.getRows(); y++) {
                for(int x = 0; x < terminalSize.getColumns(); x++) {
                    ScreenCharacter c = backbuffer[y][x];
                    if(!c.equals(visibleScreen[y][x]) || wholeScreenInvalid) {
                        visibleScreen[y][x] = c;    //Remember, ScreenCharacter is immutable, we don't need to worry about it being modified
                        updateMap.put(new TerminalPosition(x, y), c);
                    }
                }
            }

            Writer terminalWriter = new Writer();
            terminalWriter.reset();
            TerminalPosition previousPoint = null;
            for(TerminalPosition nextUpdate : updateMap.keySet()) {
                if(previousPoint == null || previousPoint.getRow() != nextUpdate.getRow()
                        || previousPoint.getColumn() + 1 != nextUpdate.getColumn()) {
                    terminalWriter.setCursorPosition(nextUpdate.getColumn(), nextUpdate.getRow());
                }
                if(updateMap.get(nextUpdate) != ScreenCharacter.CJK_PADDING_CHARACTER) {
                    terminalWriter.writeCharacter(updateMap.get(nextUpdate));
                }
                previousPoint = nextUpdate;
            }
            if(cursorPosition != null) {
                terminalWriter.setCursorVisible(true);
                terminalWriter.setCursorPosition(cursorPosition.getColumn(), cursorPosition.getRow());
            } else {
                terminalWriter.setCursorVisible(false);
            }
            wholeScreenInvalid = false;
        }
        terminal.flush();
    }

    //WARNING!!! Should only be called in a block synchronized on mutex! See refresh()
    private void resizeScreenIfNeeded() {
        TerminalSize newSize;
        synchronized(resizeQueue) {
            if(resizeQueue.isEmpty()) {
                return;
            }

            newSize = resizeQueue.getLast();
            resizeQueue.clear();
        }

        int height = newSize.getRows();
        int width = newSize.getColumns();
        ScreenCharacter[][] newBackBuffer = new ScreenCharacter[height][width];
        ScreenCharacter[][] newVisibleScreen = new ScreenCharacter[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(backbuffer.length > 0 && x < backbuffer[0].length && y < backbuffer.length) {
                    newBackBuffer[y][x] = backbuffer[y][x];
                } else {
                    newBackBuffer[y][x] = new ScreenCharacter(paddingCharacter);
                }

                if(visibleScreen.length > 0 && x < visibleScreen[0].length && y < visibleScreen.length) {
                    newVisibleScreen[y][x] = visibleScreen[y][x];
                } else {
                    newVisibleScreen[y][x] = new ScreenCharacter(paddingCharacter);
                }
            }
        }

        backbuffer = newBackBuffer;
        visibleScreen = newVisibleScreen;
        wholeScreenInvalid = true;
        terminalSize = newSize;
    }

    private static class ScreenPointComparator implements Comparator<TerminalPosition> {

        public int compare(TerminalPosition o1, TerminalPosition o2) {
            if(o1.getRow() == o2.getRow()) {
                if(o1.getColumn() == o2.getColumn()) {
                    return 0;
                } else {
                    return new Integer(o1.getColumn()).compareTo(o2.getColumn());
                }
            } else {
                return new Integer(o1.getRow()).compareTo(o2.getRow());
            }
        }
    }

    private class TerminalResizeListener implements ResizeListener {

        @Override
        public void onResized(Terminal terminal, TerminalSize newSize) {
            synchronized(resizeQueue) {
                if(!terminalSize.equals(newSize)) {
                    resizeQueue.add(newSize);
                }
            }
        }
    }

    private class Writer {

        private TextColor currentForegroundColor;
        private TextColor currentBackgroundColor;
        private boolean currentlyIsBold;
        private boolean currentlyIsUnderline;
        private boolean currentlyIsNegative;
        private boolean currentlyIsBlinking;
        private boolean currentlyIsBordered;

        public Writer() {
            currentForegroundColor = TextColor.ANSI.DEFAULT;
            currentBackgroundColor = TextColor.ANSI.DEFAULT;
            currentlyIsBold = false;
            currentlyIsUnderline = false;
            currentlyIsNegative = false;
            currentlyIsBlinking = false;
            currentlyIsBordered = false;
        }

        void setCursorPosition(int x, int y) {
            terminal.moveCursor(x, y);
        }

        private void setCursorVisible(boolean visible) {
            terminal.setCursorVisible(visible);
        }

        void writeCharacter(ScreenCharacter character) {
            if(currentlyIsBlinking != character.isBlinking()) {
                if(character.isBlinking()) {
                    terminal.applySGR(Terminal.SGR.ENTER_BLINK);
                    currentlyIsBlinking = true;
                } else {
                    terminal.applySGR(Terminal.SGR.RESET_ALL);
                    character.getBackgroundColor().applyAsBackground(terminal);
                    character.getForegroundColor().applyAsForeground(terminal);

                    // emulating "stop_blink_mode" so that previous formatting is preserved
                    currentlyIsBold = false;
                    currentlyIsUnderline = false;
                    currentlyIsNegative = false;
                    currentlyIsBlinking = false;
                    currentlyIsBordered = false;
                }
            }
            if(currentForegroundColor != character.getForegroundColor()
                    && !currentForegroundColor.equals(character.getForegroundColor())) {
                character.getForegroundColor().applyAsForeground(terminal);
                currentForegroundColor = character.getForegroundColor();
            }
            if(currentBackgroundColor != character.getBackgroundColor()
                    && !currentBackgroundColor.equals(character.getBackgroundColor())) {
                character.getBackgroundColor().applyAsBackground(terminal);
                currentBackgroundColor = character.getBackgroundColor();
            }
            if(currentlyIsBold != character.isBold()) {
                if(character.isBold()) {
                    terminal.applySGR(Terminal.SGR.ENTER_BOLD);
                    currentlyIsBold = true;
                } else {
                    terminal.applySGR(Terminal.SGR.EXIT_BOLD);
                    currentlyIsBold = false;
                }
            }
            if(currentlyIsUnderline != character.isUnderline()) {
                if(character.isUnderline()) {
                    terminal.applySGR(Terminal.SGR.ENTER_UNDERLINE);
                    currentlyIsUnderline = true;
                } else {
                    terminal.applySGR(Terminal.SGR.EXIT_UNDERLINE);
                    currentlyIsUnderline = false;
                }
            }
            if(currentlyIsNegative != character.isNegative()) {
                if(character.isNegative()) {
                    terminal.applySGR(Terminal.SGR.ENTER_REVERSE);
                    currentlyIsNegative = true;
                } else {
                    terminal.applySGR(Terminal.SGR.EXIT_REVERSE);
                    currentlyIsNegative = false;
                }
            }
            if(currentlyIsBordered != character.isBordered()) {
                if(character.isBordered()) {
                    terminal.applySGR(Terminal.SGR.ENTER_BORDERED);
                    currentlyIsBordered = true;
                } else {
                    terminal.applySGR(Terminal.SGR.EXIT_BORDERED);
                    currentlyIsBordered = false;
                }
            }
            terminal.putCharacter(character.getCharacter());
        }

        void reset() {
            terminal.applySGR(Terminal.SGR.RESET_ALL);
            terminal.moveCursor(0, 0);

            currentBackgroundColor = TextColor.ANSI.DEFAULT;
            currentForegroundColor = TextColor.ANSI.DEFAULT;
            currentlyIsBold = false;
            currentlyIsNegative = false;
            currentlyIsUnderline = false;
            currentlyIsBordered = false;
        }
    }
}

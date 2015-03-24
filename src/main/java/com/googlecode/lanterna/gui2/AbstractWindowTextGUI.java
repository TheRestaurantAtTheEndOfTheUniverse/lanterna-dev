/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public abstract class AbstractWindowTextGUI<T extends Screen>  extends AbstractTextGUI implements WindowBasedTextGUI {
  protected T screen;  
  private final WindowManager windowManager;
    private final BasePane backgroundPane;
    private final List<Window> windows;
    private final List<Window> overlays;
    private final WindowPostRenderer postRenderer;
    private boolean eofWhenNoWindows;

    protected AbstractWindowTextGUI(T screen, WindowManager windowManager, WindowPostRenderer postRenderer, Component background) {
        super(screen);
        if(windowManager == null) {
            throw new IllegalArgumentException("Creating a window-based TextGUI requires a WindowManager");
        }
        if(background == null) {
            //Use a sensible default instead of throwing
            background = new EmptySpace(TextColor.ANSI.BLUE);
        }
        this.screen = screen;
        this.windowManager = windowManager;
        this.backgroundPane = new AbstractBasePane() {
            @Override
            public TextGUI getTextGUI() {
                return AbstractWindowTextGUI.this;
            }

            @Override
            public TerminalPosition toGlobal(TerminalPosition localPosition) {
                return localPosition;
            }
        };
        this.backgroundPane.setComponent(background);
        this.windows = new ArrayList<Window>();
        this.overlays = new ArrayList<Window>();
        this.postRenderer = postRenderer;
        this.eofWhenNoWindows = false;
    }

    protected boolean isOverlay(Window window) {
        return overlays.contains(window);
    }
    
    @Override
    public synchronized boolean isPendingUpdate() {
        for(Window window: windows) {
            if(window.isInvalid()) {
                return true;
            }
        }
        return super.isPendingUpdate() || backgroundPane.isInvalid() || windowManager.isInvalid();
    }

    @Override
    protected synchronized KeyStroke readKeyStroke() throws IOException {
        KeyStroke keyStroke = super.pollInput();
        if(eofWhenNoWindows && keyStroke == null && windows.isEmpty()) {
            return new KeyStroke(KeyType.EOF);
        }
        else if(keyStroke != null) {
            return keyStroke;
        }
        else {
            return super.readKeyStroke();
        }
    }

    @Override
    protected synchronized void drawGUI(TextGUIGraphics graphics) {
        backgroundPane.draw(graphics);
        drawWindows(graphics, windows);
        drawWindows(graphics, overlays);
    }

  protected void drawWindows(TextGUIGraphics graphics, List<Window> windows) throws IllegalArgumentException {
    getWindowManager().prepareWindows(this, Collections.unmodifiableList(windows), graphics.getSize());
    for(Window window: windows) {
      TextGUIGraphics windowGraphics = graphics.newTextGraphics(window.getPosition(), window.getDecoratedSize());
      WindowDecorationRenderer decorationRenderer = getWindowManager().getWindowDecorationRenderer(window);
      windowGraphics = decorationRenderer.draw(this, windowGraphics, window);
      window.draw(windowGraphics);
      window.setContentOffset(decorationRenderer.getOffset(window));
      if(postRenderer != null && !window.getHints().contains(Window.Hint.NO_POST_RENDERING)) {
        postRenderer.postRender(graphics, this, window);
      }
    }
  }

    @Override
    public synchronized TerminalPosition getCursorPosition() {
        Window activeWindow = getActiveWindow();
        if(activeWindow != null) {
            return activeWindow.toGlobal(activeWindow.getCursorPosition());
        }
        else {
            return backgroundPane.getCursorPosition();
        }
    }

    /**
     * Sets whether the TextGUI should return EOF when you try to read input while there are no windows in the window
     * manager. Setting this to true (on by default) will make the GUI automatically exit when the last window has been
     * closed.
     * @param eofWhenNoWindows Should the GUI return EOF when there are no windows left
     */
    public void setEOFWhenNoWindows(boolean eofWhenNoWindows) {
        this.eofWhenNoWindows = eofWhenNoWindows;
    }

    /**
     * Returns whether the TextGUI should return EOF when you try to read input while there are no windows in the window
     * manager. When this is true (true by default) will make the GUI automatically exit when the last window has been
     * closed.
     * @return Should the GUI return EOF when there are no windows left
     */
    public boolean isEOFWhenNoWindows() {
        return eofWhenNoWindows;
    }

    @Override
    public synchronized Interactable getFocusedInteractable() {
        Window activeWindow = getActiveWindow();
        if(activeWindow != null) {
            return activeWindow.getFocusedInteractable();
        }
        else {
            return backgroundPane.getFocusedInteractable();
        }
    }

    @Override
    public synchronized boolean handleInput(KeyStroke keyStroke) {
        Window activeWindow = getActiveWindow();
        if(activeWindow != null) {
            return activeWindow.handleInput(keyStroke);
        }
        else {
            return backgroundPane.handleInput(keyStroke);
        }
    }

    @Override
    public WindowManager getWindowManager() {
        return windowManager;
    }

    @Override
    public synchronized WindowBasedTextGUI addWindow(Window window) {
        if(window.getTextGUI() != null) {
            window.getTextGUI().removeWindow(window);
        }
        window.setTextGUI(this);
        windowManager.onAdded(this, window, windows);
        if(!windows.contains(window)) {
            windows.add(window);
        }
        invalidate();
        return this;
    }

    @Override
    public synchronized WindowBasedTextGUI removeWindow(Window window) {
        if(isOverlay(window))
            removeWindow(window, overlays);
        else
            removeWindow(window, windows);

        invalidate();
        return this;
    }
    
    private void removeWindow(Window window, List<Window> layer) {
        layer.remove(window);
        window.setTextGUI(null);
        windowManager.onRemoved(this, window, layer);
        invalidate();
    }

    @Override
    public synchronized Collection<Window> getWindows() {
        return Collections.unmodifiableList(new ArrayList<Window>(windows));
    }

    @Override
    public synchronized Window getActiveWindow() {
        if (!overlays.isEmpty()) {
            return overlays.get(overlays.size() - 1);
        }

        return windows.isEmpty() ? null : windows.get(windows.size() - 1);
    }

    public BasePane getBackgroundPane() {
        return backgroundPane;
    }

    @Override
    public synchronized WindowBasedTextGUI moveToTop(Window window) {
        if (isOverlay(window)) {
            moveToTop(window, overlays);
        }
        else {
            moveToTop(window, windows);
        }

        invalidate();
        return this;
    }
    
    private void moveToTop(Window window, List<Window> windowList) {
        if(!windowList.contains(window)) {
            throw new IllegalArgumentException("Window " + window + " isn't in AbstractMultiWindowTextGUI " + this);
        }
        windowList.remove(window);
        windowList.add(window);
        invalidate();
    }

    
    @Override
    public synchronized WindowBasedTextGUI addOverlay(Window window) {
        if(window.getTextGUI() != null) {
            window.getTextGUI().removeWindow(window);
        }
        window.setTextGUI(this);

        if(!overlays.contains(window)) {
            overlays.add(window);
        }
        invalidate();
        return this;
    }
}

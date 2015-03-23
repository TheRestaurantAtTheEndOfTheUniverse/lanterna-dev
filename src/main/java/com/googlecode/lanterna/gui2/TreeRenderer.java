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

/**
 *
 * @author kessinger
 */
public class TreeRenderer implements InteractableRenderer<Tree> {

    private int calculateHeight(TreeNode node) {
        int count = 0;
        if (node.isExpanded()) {
            for (TreeNode child : node.getChildren()) {
                count += calculateHeight(child);
            }
        }

        return count;
    }

    private int calculateWidth(TreeNode node, int depth) {
        int width = 1 + depth + node.getTitle().length();
        for (TreeNode child : node.getChildren()) {
            if (child.isExpanded()) {
                width = Math.max(width, calculateWidth(child, depth + 1));
            }
        }

        return width;
    }

    @Override
    public TerminalPosition getCursorLocation(Tree component) {
        return null;
    }

    @Override
    public TerminalSize getPreferredSize(Tree tree) {
        if (tree.getRoot() == null) {
            return TerminalSize.ZERO;
        }

        final int nodeCount = calculateHeight(tree.getRoot());
        final int width = calculateWidth(tree.getRoot(), 0);

        return new TerminalSize(width, nodeCount);
    }

    private static ThemeDefinition getThemeDefinition(TextGUIGraphics graphics) {
        return graphics.getThemeDefinition(Tree.class);
    }

    private int drawNode(TextGUIGraphics graphics, Tree tree, TreeNode root, char left, int column, int row) {
        if (column > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < column - 1; i++) {
                sb.append(Symbols.SINGLE_LINE_VERTICAL);
            }

            sb.append(left);

            graphics.applyThemeStyle(getThemeDefinition(graphics).getNormal());
            graphics.putString(0, row, sb.toString());
        }

        char prefix;
        if (!root.hasChildren()) {
            prefix = Symbols.SINGLE_LINE_HORIZONTAL;
        }
        else {
            prefix = root.isExpanded() ? Symbols.BLACK_DOWN_POINTING_SMALL_TRIANGLE : Symbols.BLACK_RIGHT_POINTING_SMALL_TRIANGLE;
        }

        graphics.setCharacter(column, row, prefix);

        if (tree.isFocused() && root == tree.getSelectedNode()) {
            graphics.applyThemeStyle(getThemeDefinition(graphics).getSelected());
        }

        graphics.putString(column + 1, row, root.getTitle());

        int printedRows = 1;
        if (root.isExpanded()) {
            for (int i = 0; i < root.getChildren().size() - 1; i++) {
                printedRows += drawNode(graphics, tree, root.getChildren().get(i), Symbols.SINGLE_LINE_T_RIGHT, column + 1, row + printedRows);
            }

            if (!root.getChildren().isEmpty()) {
                printedRows += drawNode(graphics, tree, root.getChildren().get(root.getChildren().size() - 1), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER, column + 1, row + printedRows);
            }
        }

        return printedRows;
    }

    @Override
    public void drawComponent(TextGUIGraphics graphics, Tree tree) {
        ThemeDefinition themeDefinition = getThemeDefinition(graphics);
        if (tree.isFocused()) {
            graphics.applyThemeStyle(themeDefinition.getActive());
        }
        else {
            graphics.applyThemeStyle(themeDefinition.getNormal());
        }

        graphics.fill(' ');

        drawNode(graphics, tree, tree.getRoot(), (char) 0, 0, 0);
    }

}

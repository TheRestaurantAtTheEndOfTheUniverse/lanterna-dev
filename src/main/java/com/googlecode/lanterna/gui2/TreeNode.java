/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author kessinger
 */
public class TreeNode {

    private String title;
    private List<TreeNode> children = new ArrayList<>();
    private TreeNode parent;
    private boolean expanded = true;

    public TreeNode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public TreeNode getChild(int index) {
        return index < children.size() ? children.get(index) : null;
    }

    public void addChild(TreeNode node) {
        children.add(node);
        node.setParent(this);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    public int getChildCount() {
        return children.size();
    }

    public boolean isLast() {
        return getParent() == null || getIndex() == getParent().getChildCount() - 1;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void expand() {
        expanded = true;
    }

    public void collapse() {
        expanded = false;
    }

    public int getIndex() {
        if (getParent() == null) {
            return 0;
        }

        return getParent().getChildren().indexOf(this);
    }

    public TreeNode getNextSibling() {
        if (getParent() == null) {
            return null;
        }

        int index = getIndex();
        if (index < getParent().getChildren().size() - 1) {
            return getParent().getChildren().get(index + 1);
        }

        return null;
    }

    public TreeNode getPrevSibling() {
        if (getParent() == null) {
            return null;
        }

        int index = getIndex();
        if (index > 0) {
            return getParent().getChildren().get(index - 1);
        }

        return null;
    }
}

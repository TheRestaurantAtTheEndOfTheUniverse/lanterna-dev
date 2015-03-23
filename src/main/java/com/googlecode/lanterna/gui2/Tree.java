/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;

/**
 *
 * @author kessinger
 */
public class Tree extends AbstractInteractableComponent<TreeRenderer> implements Interactable {
  private TreeNode root;
  private TreeNode selectedNode;
  
  public Tree(TreeNode root) {
    this.root = root;
    this.selectedNode = root;
  }

  public TreeNode getRoot() {
    return root;
  }

  public TreeNode getSelectedNode() {
    return selectedNode;
  }

  public void setSelectedNode(TreeNode selectedNode) {
    this.selectedNode = selectedNode;
  }

  @Override
  protected TreeRenderer createDefaultRenderer() {
    return new TreeRenderer();
  }

  public Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
    final TreeNode parent = selectedNode.getParent();
          TreeNode next;
    switch(keyStroke.getKeyType()) {
      case ArrowUp:
        if(selectedNode.getIndex() == 0)
          next = selectedNode.getParent();
        else {
           final TreeNode prevSibling = selectedNode.getPrevSibling();
           boolean goDown = prevSibling.isExpanded() && prevSibling.hasChildren();
     
           next = goDown ? prevSibling.getChild(prevSibling.getChildCount()-1) : prevSibling;
        }

        if(next != null)
          setSelectedNode(next);

        return Result.HANDLED;
      case ArrowDown:
        boolean goDown = selectedNode.isExpanded() && selectedNode.hasChildren();

        if(goDown)
          next = selectedNode.getChild(0);
        else
          if(selectedNode.isLast())
            next = parent == null ? null : selectedNode.getParent().getNextSibling();
          else
            next = selectedNode.getNextSibling();

        if(next != null)
          setSelectedNode(next);

        return Result.HANDLED;
      case Insert:
        if(selectedNode.isExpanded())
          selectedNode.collapse();
        else
          selectedNode.expand();
        
        return Result.HANDLED;
      case ArrowRight:
      case Tab:
        return Result.MOVE_FOCUS_RIGHT;
      case ArrowLeft:
      case ReverseTab:
        return Result.MOVE_FOCUS_LEFT;
    }
      return Interactable.Result.UNHANDLED;
  }
}

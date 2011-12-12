package com.gemserk.tools.animationeditor.main.tree;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.gemserk.tools.animationeditor.core.Node;
import com.gemserk.tools.animationeditor.core.tree.TreeEditor;
import com.gemserk.tools.animationeditor.main.TreeNodeEditorImpl;

/**
 * Updates the TreeModel based on changes made over the Nodes of the current skeleton.
 */
public class TreeEditorWithJtreeInterceptorImpl implements TreeEditor {

	JTree tree;
	DefaultTreeModel model;

	Map<String, TreeNodeEditorImpl> treeNodes = new HashMap<String, TreeNodeEditorImpl>();
	TreeEditor treeEditor;

	public TreeEditorWithJtreeInterceptorImpl(TreeEditor treeEditor, JTree tree) {
		this.treeEditor = treeEditor;
		this.tree = tree;
		this.model = (DefaultTreeModel) tree.getModel();
	}

	private void createTreeNodeForChild(Node node, DefaultMutableTreeNode parentTreeNode) {
		TreeNodeEditorImpl childNode = new TreeNodeEditorImpl(node);
		for (int i = 0; i < node.getChildren().size(); i++) {
			Node child = node.getChildren().get(i);
			createTreeNodeForChild(child, childNode);
		}
		parentTreeNode.add(childNode);
		treeNodes.put(node.getId(), childNode);
	}

	@Override
	public void select(Node node) {
		treeEditor.select(node);

		TreeNodeEditorImpl treeNode = treeNodes.get(node.getId());
		// model.nodeChanged(treeNodeEditorImpl);
		if (treeNode == null) {
			Node parent = node.getParent();
			treeNode = treeNodes.get(parent.getId());
			// return;
		}
		focusOnTreeNode(treeNode);
	}

	@Override
	public void remove(Node node) {
		treeEditor.remove(node);

		Node parent = node.getParent();
		TreeNodeEditorImpl parentTreeNode = treeNodes.get(parent.getId());
		if (parentTreeNode == null)
			throw new IllegalArgumentException("Node should be on the JTree to call remove");
		TreeNodeEditorImpl treeNode = treeNodes.get(node.getId());
		parentTreeNode.remove(treeNode);
		model.reload();
	}

	private void focusOnTreeNode(TreeNodeEditorImpl parentTreeNode) {
		TreePath path = new TreePath(parentTreeNode.getPath());
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
	}

	@Override
	public void add(Node node) {
		treeEditor.add(node);

		Node parent = node.getParent();
		TreeNodeEditorImpl parentTreeNode = treeNodes.get(parent.getId());
		if (parentTreeNode == null) {
			DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) model.getRoot();
			if (treeRoot == null)
				throw new IllegalStateException("Expected to have a root DefaultMutableTreeNode in the TreeModel");
			createTreeNodeForChild(node, treeRoot);
			model.reload();
			return;
		}
		createTreeNodeForChild(node, parentTreeNode);
		model.reload();
		// focusOnTreeNode(parentTreeNode);
	}

	@Override
	public Node getNearestNode(float x, float y) {
		return treeEditor.getNearestNode(x, y);
	}

	@Override
	public Node getRoot() {
		return treeEditor.getRoot();
	}

	@Override
	public boolean isSelectedNode(Node node) {
		return treeEditor.isSelectedNode(node);
	}

	public void moveSelected(float dx, float dy) {
		treeEditor.moveSelected(dx, dy);
	}

	public void rotateSelected(float angle) {
		treeEditor.rotateSelected(angle);
	}

	@Override
	public Node getSelectedNode() {
		return treeEditor.getSelectedNode();
	}

}
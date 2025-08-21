package com.bihe0832.android.base.compose.debug.tree;

import java.util.Iterator;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/11/24.
 * Description: Description
 */
class TreeNodeIterator<T> implements Iterator<TreeNode<T>> {

    enum ProcessStages {
        ProcessParent, ProcessChildCurNode, ProcessChildSubNode
    }


    private ProcessStages doNext;

    private TreeNode<T> next;

    private Iterator<TreeNode<T>> childrenCurNodeIter;

    private Iterator<TreeNode<T>> childrenSubNodeIter;

    private TreeNode<T> treeNode;

    public TreeNodeIterator(TreeNode<T> treeNode) {
        this.treeNode = treeNode;
        this.doNext = ProcessStages.ProcessParent;
        this.childrenCurNodeIter = treeNode.children.iterator();
    }

    @Override
    public boolean hasNext() {

        if (this.doNext == ProcessStages.ProcessParent) {
            this.next = this.treeNode;
            this.doNext = ProcessStages.ProcessChildCurNode;
            return true;
        }

        if (this.doNext == ProcessStages.ProcessChildCurNode) {
            if (childrenCurNodeIter.hasNext()) {
                TreeNode<T> childDirect = childrenCurNodeIter.next();
                childrenSubNodeIter = childDirect.iterator();
                this.doNext = ProcessStages.ProcessChildSubNode;
                return hasNext();
            } else {
                this.doNext = null;
                return false;
            }
        }

        if (this.doNext == ProcessStages.ProcessChildSubNode) {
            if (childrenSubNodeIter.hasNext()) {
                this.next = childrenSubNodeIter.next();
                return true;
            } else {
                this.next = null;
                this.doNext = ProcessStages.ProcessChildCurNode;
                return hasNext();
            }
        }

        return false;
    }

    @Override
    public TreeNode<T> next() {
        return this.next;
    }

    /**
     * 目前不支持删除节点
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.util.Enumeration;
import java.util.Stack;

public class ElementIterator implements Cloneable{
    private Element root;
    private Stack<StackItem> elementStack=null;

    public ElementIterator(Document document){
        root=document.getDefaultRootElement();
    }

    public ElementIterator(Element root){
        this.root=root;
    }

    public int depth(){
        if(elementStack==null){
            return 0;
        }
        return elementStack.size();
    }

    public Element current(){
        if(elementStack==null){
            return first();
        }
        /**
         get a handle to the element on top of the stack.
         */
        if(!elementStack.empty()){
            StackItem item=elementStack.peek();
            Element elem=item.getElement();
            int index=item.getIndex();
            // self reference
            if(index==-1){
                return elem;
            }
            // return the child at location "index".
            return elem.getElement(index);
        }
        return null;
    }    public synchronized Object clone(){
        try{
            ElementIterator it=new ElementIterator(root);
            if(elementStack!=null){
                it.elementStack=new Stack<StackItem>();
                for(int i=0;i<elementStack.size();i++){
                    StackItem item=elementStack.elementAt(i);
                    StackItem clonee=(StackItem)item.clone();
                    it.elementStack.push(clonee);
                }
            }
            return it;
        }catch(CloneNotSupportedException e){
            throw new InternalError(e);
        }
    }

    public Element first(){
        // just in case...
        if(root==null){
            return null;
        }
        elementStack=new Stack<StackItem>();
        if(root.getElementCount()!=0){
            elementStack.push(new StackItem(root));
        }
        return root;
    }

    public Element next(){
        /** if current() has not been invoked
         and next is invoked, the very first
         element will be returned. */
        if(elementStack==null){
            return first();
        }
        // no more elements
        if(elementStack.isEmpty()){
            return null;
        }
        // get a handle to the element on top of the stack
        StackItem item=elementStack.peek();
        Element elem=item.getElement();
        int index=item.getIndex();
        if(index+1<elem.getElementCount()){
            Element child=elem.getElement(index+1);
            if(child.isLeaf()){
                /** In this case we merely want to increment
                 the child index of the item on top of the
                 stack.*/
                item.incrementIndex();
            }else{
                /** In this case we need to push the child(branch)
                 on the stack so that we can iterate over its
                 children. */
                elementStack.push(new StackItem(child));
            }
            return child;
        }else{
            /** No more children for the item on top of the
             stack therefore pop the stack. */
            elementStack.pop();
            if(!elementStack.isEmpty()){
                /** Increment the child index for the item that
                 is now on top of the stack. */
                StackItem top=elementStack.peek();
                top.incrementIndex();
                /** We now want to return its next child, therefore
                 call next() recursively. */
                return next();
            }
        }
        return null;
    }

    public Element previous(){
        int stackSize;
        if(elementStack==null||(stackSize=elementStack.size())==0){
            return null;
        }
        // get a handle to the element on top of the stack
        //
        StackItem item=elementStack.peek();
        Element elem=item.getElement();
        int index=item.getIndex();
        if(index>0){
            /** return child at previous index. */
            return getDeepestLeaf(elem.getElement(--index));
        }else if(index==0){
            /** this implies that current is the element's
             first child, therefore previous is the
             element itself. */
            return elem;
        }else if(index==-1){
            if(stackSize==1){
                // current is the root, nothing before it.
                return null;
            }
            /** We need to return either the item
             below the top item or one of the
             former's children. */
            StackItem top=elementStack.pop();
            item=elementStack.peek();
            // restore the top item.
            elementStack.push(top);
            elem=item.getElement();
            index=item.getIndex();
            return ((index==-1)?elem:getDeepestLeaf(elem.getElement
                    (index)));
        }
        // should never get here.
        return null;
    }

    private Element getDeepestLeaf(Element parent){
        if(parent.isLeaf()){
            return parent;
        }
        int childCount=parent.getElementCount();
        if(childCount==0){
            return parent;
        }
        return getDeepestLeaf(parent.getElement(childCount-1));
    }

    private void dumpTree(){
        Element elem;
        while(true){
            if((elem=next())!=null){
                System.out.println("elem: "+elem.getName());
                AttributeSet attr=elem.getAttributes();
                String s="";
                Enumeration names=attr.getAttributeNames();
                while(names.hasMoreElements()){
                    Object key=names.nextElement();
                    Object value=attr.getAttribute(key);
                    if(value instanceof AttributeSet){
                        // don't go recursive
                        s=s+key+"=**AttributeSet** ";
                    }else{
                        s=s+key+"="+value+" ";
                    }
                }
                System.out.println("attributes: "+s);
            }else{
                break;
            }
        }
    }

    private class StackItem implements Cloneable{
        Element item;
        int childIndex;

        private StackItem(Element elem){
            /**
             * -1 index implies a self reference,
             * as opposed to an index into its
             * list of children.
             */
            this.item=elem;
            this.childIndex=-1;
        }

        private void incrementIndex(){
            childIndex++;
        }

        private Element getElement(){
            return item;
        }

        private int getIndex(){
            return childIndex;
        }

        protected Object clone() throws CloneNotSupportedException{
            return super.clone();
        }
    }


}

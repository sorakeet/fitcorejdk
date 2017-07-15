/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xerces.internal.util;
//java imports

import java.util.Iterator;
import java.util.NoSuchElementException;
//xerces imports

public class XMLAttributesIteratorImpl extends XMLAttributesImpl implements Iterator{
    //pointer to current position.
    protected int fCurrent=0;
    protected Attribute fLastReturnedItem;

    public XMLAttributesIteratorImpl(){
    }

    public void removeAllAttributes(){
        super.removeAllAttributes();
        fCurrent=0;
    }    public boolean hasNext(){
        return fCurrent<getLength()?true:false;
    }//hasNext()

    public Object next(){
        if(hasNext()){
            // should this be of type javax.xml.stream.Attribute ?
            return fLastReturnedItem=fAttributes[fCurrent++];
        }else{
            throw new NoSuchElementException();
        }
    }//next

    public void remove(){
        //make sure that only last returned item can be removed.
        if(fLastReturnedItem==fAttributes[fCurrent-1]){
            //remove the attribute at current index and lower the current position by 1.
            removeAttributeAt(fCurrent--);
        }else{
            //either the next method has been called yet, or the remove method has already been called
            //after the last call to the next method.
            throw new IllegalStateException();
        }
    }//remove


    /** xxx: should we be doing this way ? Attribute event defines so many functions which doesn't make any sense
     *for Attribute.
     *
     */
    /**
     class AttributeImpl extends com.sun.org.apache.xerces.internal.util.XMLAttributesImpl.Attribute implements javax.xml.stream.events.Attribute{

     }
     */
} //XMLAttributesIteratorImpl

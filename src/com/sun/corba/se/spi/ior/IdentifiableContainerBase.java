/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import com.sun.corba.se.impl.ior.FreezableList;

import java.util.ArrayList;
import java.util.Iterator;

public class IdentifiableContainerBase extends FreezableList{
    public IdentifiableContainerBase(){
        super(new ArrayList());
    }

    public Iterator iteratorById(final int id){
        return new Iterator(){
            Iterator iter=IdentifiableContainerBase.this.iterator();
            Object current=advance();

            public boolean hasNext(){
                return current!=null;
            }

            public Object next(){
                Object result=current;
                current=advance();
                return result;
            }

            private Object advance(){
                while(iter.hasNext()){
                    Identifiable ide=(Identifiable)(iter.next());
                    if(ide.getId()==id)
                        return ide;
                }
                return null;
            }

            public void remove(){
                iter.remove();
            }
        };
    }
}

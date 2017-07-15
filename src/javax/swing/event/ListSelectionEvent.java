/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventObject;

public class ListSelectionEvent extends EventObject{
    private int firstIndex;
    private int lastIndex;
    private boolean isAdjusting;

    public ListSelectionEvent(Object source,int firstIndex,int lastIndex,
                              boolean isAdjusting){
        super(source);
        this.firstIndex=firstIndex;
        this.lastIndex=lastIndex;
        this.isAdjusting=isAdjusting;
    }

    public int getFirstIndex(){
        return firstIndex;
    }

    public int getLastIndex(){
        return lastIndex;
    }

    public boolean getValueIsAdjusting(){
        return isAdjusting;
    }

    public String toString(){
        String properties=
                " source="+getSource()+
                        " firstIndex= "+firstIndex+
                        " lastIndex= "+lastIndex+
                        " isAdjusting= "+isAdjusting+
                        " ";
        return getClass().getName()+"["+properties+"]";
    }
}

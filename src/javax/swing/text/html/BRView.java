/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.Element;

class BRView extends InlineView{
    public BRView(Element elem){
        super(elem);
    }

    public int getBreakWeight(int axis,float pos,float len){
        if(axis==X_AXIS){
            return ForcedBreakWeight;
        }else{
            return super.getBreakWeight(axis,pos,len);
        }
    }
}

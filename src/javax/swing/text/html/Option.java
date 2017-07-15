/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.text.AttributeSet;
import java.io.Serializable;

public class Option implements Serializable{
    private boolean selected;
    private String label;
    private AttributeSet attr;

    public Option(AttributeSet attr){
        this.attr=attr.copyAttributes();
        selected=(attr.getAttribute(HTML.Attribute.SELECTED)!=null);
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label){
        this.label=label;
    }

    public AttributeSet getAttributes(){
        return attr;
    }

    public String toString(){
        return label;
    }

    protected void setSelection(boolean state){
        selected=state;
    }

    public boolean isSelected(){
        return selected;
    }

    public String getValue(){
        String value=(String)attr.getAttribute(HTML.Attribute.VALUE);
        if(value==null){
            value=label;
        }
        return value;
    }
}

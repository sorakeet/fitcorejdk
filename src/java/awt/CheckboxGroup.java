/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class CheckboxGroup implements java.io.Serializable{
    private static final long serialVersionUID=3729780091441768983L;
    Checkbox selectedCheckbox=null;

    public CheckboxGroup(){
    }

    public Checkbox getSelectedCheckbox(){
        return getCurrent();
    }

    @Deprecated
    public Checkbox getCurrent(){
        return selectedCheckbox;
    }

    @Deprecated
    public synchronized void setCurrent(Checkbox box){
        if(box!=null&&box.group!=this){
            return;
        }
        Checkbox oldChoice=this.selectedCheckbox;
        this.selectedCheckbox=box;
        if(oldChoice!=null&&oldChoice!=box&&oldChoice.group==this){
            oldChoice.setState(false);
        }
        if(box!=null&&oldChoice!=box&&!box.getState()){
            box.setStateInternal(true);
        }
    }

    public void setSelectedCheckbox(Checkbox box){
        setCurrent(box);
    }

    public String toString(){
        return getClass().getName()+"[selectedCheckbox="+selectedCheckbox+"]";
    }
}

/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

@SuppressWarnings("serial")
public class ButtonGroup implements Serializable{
    // the list of buttons participating in this group
    protected Vector<AbstractButton> buttons=new Vector<AbstractButton>();
    ButtonModel selection=null;

    public ButtonGroup(){
    }

    public void add(AbstractButton b){
        if(b==null){
            return;
        }
        buttons.addElement(b);
        if(b.isSelected()){
            if(selection==null){
                selection=b.getModel();
            }else{
                b.setSelected(false);
            }
        }
        b.getModel().setGroup(this);
    }

    public void remove(AbstractButton b){
        if(b==null){
            return;
        }
        buttons.removeElement(b);
        if(b.getModel()==selection){
            selection=null;
        }
        b.getModel().setGroup(null);
    }

    public void clearSelection(){
        if(selection!=null){
            ButtonModel oldSelection=selection;
            selection=null;
            oldSelection.setSelected(false);
        }
    }

    public Enumeration<AbstractButton> getElements(){
        return buttons.elements();
    }

    public ButtonModel getSelection(){
        return selection;
    }

    public void setSelected(ButtonModel m,boolean b){
        if(b&&m!=null&&m!=selection){
            ButtonModel oldSelection=selection;
            selection=m;
            if(oldSelection!=null){
                oldSelection.setSelected(false);
            }
            m.setSelected(true);
        }
    }

    public boolean isSelected(ButtonModel m){
        return (m==selection);
    }

    public int getButtonCount(){
        if(buttons==null){
            return 0;
        }else{
            return buttons.size();
        }
    }
}

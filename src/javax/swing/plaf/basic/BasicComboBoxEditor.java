/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.reflect.misc.MethodUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;

public class BasicComboBoxEditor implements ComboBoxEditor, FocusListener{
    protected JTextField editor;
    private Object oldValue;

    public BasicComboBoxEditor(){
        editor=createEditorComponent();
    }

    protected JTextField createEditorComponent(){
        JTextField editor=new BorderlessTextField("",9);
        editor.setBorder(null);
        return editor;
    }

    public Component getEditorComponent(){
        return editor;
    }

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusGained(FocusEvent e){
    }    public void setItem(Object anObject){
        String text;
        if(anObject!=null){
            text=anObject.toString();
            if(text==null){
                text="";
            }
            oldValue=anObject;
        }else{
            text="";
        }
        // workaround for 4530952
        if(!text.equals(editor.getText())){
            editor.setText(text);
        }
    }

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusLost(FocusEvent e){
    }    public Object getItem(){
        Object newValue=editor.getText();
        if(oldValue!=null&&!(oldValue instanceof String)){
            // The original value is not a string. Should return the value in it's
            // original type.
            if(newValue.equals(oldValue.toString())){
                return oldValue;
            }else{
                // Must take the value from the editor and get the value and cast it to the new type.
                Class<?> cls=oldValue.getClass();
                try{
                    Method method=MethodUtil.getMethod(cls,"valueOf",new Class[]{String.class});
                    newValue=MethodUtil.invoke(method,oldValue,new Object[]{editor.getText()});
                }catch(Exception ex){
                    // Fail silently and return the newValue (a String object)
                }
            }
        }
        return newValue;
    }

    static class BorderlessTextField extends JTextField{
        public BorderlessTextField(String value,int n){
            super(value,n);
        }

        // workaround for 4530952
        public void setText(String s){
            if(getText().equals(s)){
                return;
            }
            super.setText(s);
        }

        public void setBorder(Border b){
            if(!(b instanceof UIResource)){
                super.setBorder(b);
            }
        }
    }    public void selectAll(){
        editor.selectAll();
        editor.requestFocus();
    }

    public static class UIResource extends BasicComboBoxEditor
            implements javax.swing.plaf.UIResource{
    }



    public void addActionListener(ActionListener l){
        editor.addActionListener(l);
    }

    public void removeActionListener(ActionListener l){
        editor.removeActionListener(l);
    }




}

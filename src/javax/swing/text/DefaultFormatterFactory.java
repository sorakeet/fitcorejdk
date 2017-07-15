/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.io.Serializable;

public class DefaultFormatterFactory extends JFormattedTextField.AbstractFormatterFactory implements Serializable{
    private JFormattedTextField.AbstractFormatter defaultFormat;
    private JFormattedTextField.AbstractFormatter displayFormat;
    private JFormattedTextField.AbstractFormatter editFormat;
    private JFormattedTextField.AbstractFormatter nullFormat;

    public DefaultFormatterFactory(){
    }

    public DefaultFormatterFactory(JFormattedTextField.
                                           AbstractFormatter defaultFormat){
        this(defaultFormat,null);
    }

    public DefaultFormatterFactory(
            JFormattedTextField.AbstractFormatter defaultFormat,
            JFormattedTextField.AbstractFormatter displayFormat){
        this(defaultFormat,displayFormat,null);
    }

    public DefaultFormatterFactory(
            JFormattedTextField.AbstractFormatter defaultFormat,
            JFormattedTextField.AbstractFormatter displayFormat,
            JFormattedTextField.AbstractFormatter editFormat){
        this(defaultFormat,displayFormat,editFormat,null);
    }

    public DefaultFormatterFactory(
            JFormattedTextField.AbstractFormatter defaultFormat,
            JFormattedTextField.AbstractFormatter displayFormat,
            JFormattedTextField.AbstractFormatter editFormat,
            JFormattedTextField.AbstractFormatter nullFormat){
        this.defaultFormat=defaultFormat;
        this.displayFormat=displayFormat;
        this.editFormat=editFormat;
        this.nullFormat=nullFormat;
    }

    public JFormattedTextField.AbstractFormatter getFormatter(
            JFormattedTextField source){
        JFormattedTextField.AbstractFormatter format=null;
        if(source==null){
            return null;
        }
        Object value=source.getValue();
        if(value==null){
            format=getNullFormatter();
        }
        if(format==null){
            if(source.hasFocus()){
                format=getEditFormatter();
            }else{
                format=getDisplayFormatter();
            }
            if(format==null){
                format=getDefaultFormatter();
            }
        }
        return format;
    }

    public JFormattedTextField.AbstractFormatter getDefaultFormatter(){
        return defaultFormat;
    }

    public void setDefaultFormatter(JFormattedTextField.AbstractFormatter atf){
        defaultFormat=atf;
    }

    public JFormattedTextField.AbstractFormatter getDisplayFormatter(){
        return displayFormat;
    }

    public void setDisplayFormatter(JFormattedTextField.AbstractFormatter atf){
        displayFormat=atf;
    }

    public JFormattedTextField.AbstractFormatter getEditFormatter(){
        return editFormat;
    }

    public void setEditFormatter(JFormattedTextField.AbstractFormatter atf){
        editFormat=atf;
    }

    public JFormattedTextField.AbstractFormatter getNullFormatter(){
        return nullFormat;
    }

    public void setNullFormatter(JFormattedTextField.AbstractFormatter atf){
        nullFormat=atf;
    }
}

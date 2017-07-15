/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.colorchooser;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class AbstractColorChooserPanel extends JPanel{
    private final PropertyChangeListener enabledListener=new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent event){
            Object value=event.getNewValue();
            if(value instanceof Boolean){
                setEnabled((Boolean)value);
            }
        }
    };
    private JColorChooser chooser;

    public abstract String getDisplayName();

    public int getMnemonic(){
        return 0;
    }

    public int getDisplayedMnemonicIndex(){
        return -1;
    }

    public abstract Icon getSmallDisplayIcon();

    public abstract Icon getLargeDisplayIcon();

    public void installChooserPanel(JColorChooser enclosingChooser){
        if(chooser!=null){
            throw new RuntimeException("This chooser panel is already installed");
        }
        chooser=enclosingChooser;
        chooser.addPropertyChangeListener("enabled",enabledListener);
        setEnabled(chooser.isEnabled());
        buildChooser();
        updateChooser();
    }

    public abstract void updateChooser();

    protected abstract void buildChooser();

    public void uninstallChooserPanel(JColorChooser enclosingChooser){
        chooser.removePropertyChangeListener("enabled",enabledListener);
        chooser=null;
    }

    protected Color getColorFromModel(){
        ColorSelectionModel model=getColorSelectionModel();
        return (model!=null)
                ?model.getSelectedColor()
                :null;
    }

    public ColorSelectionModel getColorSelectionModel(){
        return (this.chooser!=null)
                ?this.chooser.getSelectionModel()
                :null;
    }

    void setSelectedColor(Color color){
        ColorSelectionModel model=getColorSelectionModel();
        if(model!=null){
            model.setSelectedColor(color);
        }
    }

    public void paint(Graphics g){
        super.paint(g);
    }

    int getInt(Object key,int defaultValue){
        Object value=UIManager.get(key,getLocale());
        if(value instanceof Integer){
            return ((Integer)value).intValue();
        }
        if(value instanceof String){
            try{
                return Integer.parseInt((String)value);
            }catch(NumberFormatException nfe){
            }
        }
        return defaultValue;
    }
}

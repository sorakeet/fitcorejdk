/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serializable;

public class BasicComboBoxRenderer extends JLabel
        implements ListCellRenderer, Serializable{
    private final static Border SAFE_NO_FOCUS_BORDER=new EmptyBorder(1,1,1,1);
    protected static Border noFocusBorder=new EmptyBorder(1,1,1,1);

    public BasicComboBoxRenderer(){
        super();
        setOpaque(true);
        setBorder(getNoFocusBorder());
    }

    private static Border getNoFocusBorder(){
        if(System.getSecurityManager()!=null){
            return SAFE_NO_FOCUS_BORDER;
        }else{
            return noFocusBorder;
        }
    }

    public Dimension getPreferredSize(){
        Dimension size;
        if((this.getText()==null)||(this.getText().equals(""))){
            setText(" ");
            size=super.getPreferredSize();
            setText("");
        }else{
            size=super.getPreferredSize();
        }
        return size;
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus){
        /**if (isSelected) {
         setBackground(UIManager.getColor("ComboBox.selectionBackground"));
         setForeground(UIManager.getColor("ComboBox.selectionForeground"));
         } else {
         setBackground(UIManager.getColor("ComboBox.background"));
         setForeground(UIManager.getColor("ComboBox.foreground"));
         }**/
        if(isSelected){
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }else{
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setFont(list.getFont());
        if(value instanceof Icon){
            setIcon((Icon)value);
        }else{
            setText((value==null)?"":value.toString());
        }
        return this;
    }

    public static class UIResource extends BasicComboBoxRenderer implements javax.swing.plaf.UIResource{
    }
}

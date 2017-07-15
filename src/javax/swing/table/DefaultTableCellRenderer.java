/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serializable;

public class DefaultTableCellRenderer extends JLabel
        implements TableCellRenderer, Serializable{
    private static final Border SAFE_NO_FOCUS_BORDER=new EmptyBorder(1,1,1,1);
    private static final Border DEFAULT_NO_FOCUS_BORDER=new EmptyBorder(1,1,1,1);
    protected static Border noFocusBorder=DEFAULT_NO_FOCUS_BORDER;
    // We need a place to store the color the JLabel should be returned
    // to after its foreground and background colors have been set
    // to the selection background color.
    // These ivars will be made protected when their names are finalized.
    private Color unselectedForeground;
    private Color unselectedBackground;

    public DefaultTableCellRenderer(){
        super();
        setOpaque(true);
        setBorder(getNoFocusBorder());
        setName("Table.cellRenderer");
    }

    private Border getNoFocusBorder(){
        Border border=DefaultLookup.getBorder(this,ui,"Table.cellNoFocusBorder");
        if(System.getSecurityManager()!=null){
            if(border!=null) return border;
            return SAFE_NO_FOCUS_BORDER;
        }else if(border!=null){
            if(noFocusBorder==null||noFocusBorder==DEFAULT_NO_FOCUS_BORDER){
                return border;
            }
        }
        return noFocusBorder;
    }

    public void updateUI(){
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    public void setForeground(Color c){
        super.setForeground(c);
        unselectedForeground=c;
    }

    public void setBackground(Color c){
        super.setBackground(c);
        unselectedBackground=c;
    }

    public boolean isOpaque(){
        Color back=getBackground();
        Component p=getParent();
        if(p!=null){
            p=p.getParent();
        }
        // p should now be the JTable.
        boolean colorMatch=(back!=null)&&(p!=null)&&
                back.equals(p.getBackground())&&
                p.isOpaque();
        return !colorMatch&&super.isOpaque();
    }

    public void firePropertyChange(String propertyName,boolean oldValue,boolean newValue){
    }

    public void repaint(long tm,int x,int y,int width,int height){
    }

    public void repaint(Rectangle r){
    }

    public void revalidate(){
    }

    // implements javax.swing.table.TableCellRenderer
    public Component getTableCellRendererComponent(JTable table,Object value,
                                                   boolean isSelected,boolean hasFocus,int row,int column){
        if(table==null){
            return this;
        }
        Color fg=null;
        Color bg=null;
        JTable.DropLocation dropLocation=table.getDropLocation();
        if(dropLocation!=null
                &&!dropLocation.isInsertRow()
                &&!dropLocation.isInsertColumn()
                &&dropLocation.getRow()==row
                &&dropLocation.getColumn()==column){
            fg=DefaultLookup.getColor(this,ui,"Table.dropCellForeground");
            bg=DefaultLookup.getColor(this,ui,"Table.dropCellBackground");
            isSelected=true;
        }
        if(isSelected){
            super.setForeground(fg==null?table.getSelectionForeground()
                    :fg);
            super.setBackground(bg==null?table.getSelectionBackground()
                    :bg);
        }else{
            Color background=unselectedBackground!=null
                    ?unselectedBackground
                    :table.getBackground();
            if(background==null||background instanceof javax.swing.plaf.UIResource){
                Color alternateColor=DefaultLookup.getColor(this,ui,"Table.alternateRowColor");
                if(alternateColor!=null&&row%2!=0){
                    background=alternateColor;
                }
            }
            super.setForeground(unselectedForeground!=null
                    ?unselectedForeground
                    :table.getForeground());
            super.setBackground(background);
        }
        setFont(table.getFont());
        if(hasFocus){
            Border border=null;
            if(isSelected){
                border=DefaultLookup.getBorder(this,ui,"Table.focusSelectedCellHighlightBorder");
            }
            if(border==null){
                border=DefaultLookup.getBorder(this,ui,"Table.focusCellHighlightBorder");
            }
            setBorder(border);
            if(!isSelected&&table.isCellEditable(row,column)){
                Color col;
                col=DefaultLookup.getColor(this,ui,"Table.focusCellForeground");
                if(col!=null){
                    super.setForeground(col);
                }
                col=DefaultLookup.getColor(this,ui,"Table.focusCellBackground");
                if(col!=null){
                    super.setBackground(col);
                }
            }
        }else{
            setBorder(getNoFocusBorder());
        }
        setValue(value);
        return this;
    }

    protected void setValue(Object value){
        setText((value==null)?"":value.toString());
    }

    public void invalidate(){
    }

    public void validate(){
    }

    public void repaint(){
    }

    protected void firePropertyChange(String propertyName,Object oldValue,Object newValue){
        // Strings get interned...
        if(propertyName=="text"
                ||propertyName=="labelFor"
                ||propertyName=="displayedMnemonic"
                ||((propertyName=="font"||propertyName=="foreground")
                &&oldValue!=newValue
                &&getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey)!=null)){
            super.firePropertyChange(propertyName,oldValue,newValue);
        }
    }

    public static class UIResource extends DefaultTableCellRenderer
            implements javax.swing.plaf.UIResource{
    }
}

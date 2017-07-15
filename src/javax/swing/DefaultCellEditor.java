/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.EventObject;

public class DefaultCellEditor extends AbstractCellEditor
        implements TableCellEditor, TreeCellEditor{
//
//  Instance Variables
//
    protected JComponent editorComponent;
    protected EditorDelegate delegate;
    protected int clickCountToStart=1;
//
//  Constructors
//

    @ConstructorProperties({"component"})
    public DefaultCellEditor(final JTextField textField){
        editorComponent=textField;
        this.clickCountToStart=2;
        delegate=new EditorDelegate(){
            public Object getCellEditorValue(){
                return textField.getText();
            }            public void setValue(Object value){
                textField.setText((value!=null)?value.toString():"");
            }


        };
        textField.addActionListener(delegate);
    }

    public DefaultCellEditor(final JCheckBox checkBox){
        editorComponent=checkBox;
        delegate=new EditorDelegate(){
            public void setValue(Object value){
                boolean selected=false;
                if(value instanceof Boolean){
                    selected=((Boolean)value).booleanValue();
                }else if(value instanceof String){
                    selected=value.equals("true");
                }
                checkBox.setSelected(selected);
            }

            public Object getCellEditorValue(){
                return Boolean.valueOf(checkBox.isSelected());
            }
        };
        checkBox.addActionListener(delegate);
        checkBox.setRequestFocusEnabled(false);
    }

    public DefaultCellEditor(final JComboBox comboBox){
        editorComponent=comboBox;
        comboBox.putClientProperty("JComboBox.isTableCellEditor",Boolean.TRUE);
        delegate=new EditorDelegate(){
            public void setValue(Object value){
                comboBox.setSelectedItem(value);
            }

            public Object getCellEditorValue(){
                return comboBox.getSelectedItem();
            }

            public boolean shouldSelectCell(EventObject anEvent){
                if(anEvent instanceof MouseEvent){
                    MouseEvent e=(MouseEvent)anEvent;
                    return e.getID()!=MouseEvent.MOUSE_DRAGGED;
                }
                return true;
            }

            public boolean stopCellEditing(){
                if(comboBox.isEditable()){
                    // Commit edited value.
                    comboBox.actionPerformed(new ActionEvent(
                            DefaultCellEditor.this,0,""));
                }
                return super.stopCellEditing();
            }
        };
        comboBox.addActionListener(delegate);
    }

    public Component getComponent(){
        return editorComponent;
    }
//
//  Modifying
//

    public int getClickCountToStart(){
        return clickCountToStart;
    }

    public void setClickCountToStart(int count){
        clickCountToStart=count;
    }
//
//  Override the implementations of the superclass, forwarding all methods
//  from the CellEditor interface to our delegate.
//

    public Object getCellEditorValue(){
        return delegate.getCellEditorValue();
    }

    public boolean isCellEditable(EventObject anEvent){
        return delegate.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent){
        return delegate.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing(){
        return delegate.stopCellEditing();
    }

    public void cancelCellEditing(){
        delegate.cancelCellEditing();
    }
//
//  Implementing the TreeCellEditor Interface
//

    public Component getTreeCellEditorComponent(JTree tree,Object value,
                                                boolean isSelected,
                                                boolean expanded,
                                                boolean leaf,int row){
        String stringValue=tree.convertValueToText(value,isSelected,
                expanded,leaf,row,false);
        delegate.setValue(stringValue);
        return editorComponent;
    }

    //
//  Implementing the CellEditor Interface
//
    public Component getTableCellEditorComponent(JTable table,Object value,
                                                 boolean isSelected,
                                                 int row,int column){
        delegate.setValue(value);
        if(editorComponent instanceof JCheckBox){
            //in order to avoid a "flashing" effect when clicking a checkbox
            //in a table, it is important for the editor to have as a border
            //the same border that the renderer has, and have as the background
            //the same color as the renderer has. This is primarily only
            //needed for JCheckBox since this editor doesn't fill all the
            //visual space of the table cell, unlike a text field.
            TableCellRenderer renderer=table.getCellRenderer(row,column);
            Component c=renderer.getTableCellRendererComponent(table,value,
                    isSelected,true,row,column);
            if(c!=null){
                editorComponent.setOpaque(true);
                editorComponent.setBackground(c.getBackground());
                if(c instanceof JComponent){
                    editorComponent.setBorder(((JComponent)c).getBorder());
                }
            }else{
                editorComponent.setOpaque(false);
            }
        }
        return editorComponent;
    }
//
//  Protected EditorDelegate class
//

    protected class EditorDelegate implements ActionListener, ItemListener, Serializable{
        protected Object value;

        public Object getCellEditorValue(){
            return value;
        }

        public void setValue(Object value){
            this.value=value;
        }

        public boolean isCellEditable(EventObject anEvent){
            if(anEvent instanceof MouseEvent){
                return ((MouseEvent)anEvent).getClickCount()>=clickCountToStart;
            }
            return true;
        }

        public boolean shouldSelectCell(EventObject anEvent){
            return true;
        }

        public boolean startCellEditing(EventObject anEvent){
            return true;
        }

        public boolean stopCellEditing(){
            fireEditingStopped();
            return true;
        }

        public void cancelCellEditing(){
            fireEditingCanceled();
        }

        public void actionPerformed(ActionEvent e){
            DefaultCellEditor.this.stopCellEditing();
        }

        public void itemStateChanged(ItemEvent e){
            DefaultCellEditor.this.stopCellEditing();
        }
    }
} // End of class JCellEditor

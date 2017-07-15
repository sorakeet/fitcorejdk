/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

public class TableColumn extends Object implements Serializable{
    public final static String COLUMN_WIDTH_PROPERTY="columWidth";
    public final static String HEADER_VALUE_PROPERTY="headerValue";
    public final static String HEADER_RENDERER_PROPERTY="headerRenderer";
    public final static String CELL_RENDERER_PROPERTY="cellRenderer";
//
//  Instance Variables
//
    protected int modelIndex;
    protected Object identifier;
    protected int width;
    protected int minWidth;
    protected int maxWidth;
    protected TableCellRenderer headerRenderer;
    protected Object headerValue;
    protected TableCellRenderer cellRenderer;
    protected TableCellEditor cellEditor;
    protected boolean isResizable;
    @Deprecated
    transient protected int resizedPostingDisableCount;
    private int preferredWidth;
    private SwingPropertyChangeSupport changeSupport;
//
// Constructors
//

    public TableColumn(){
        this(0);
    }

    public TableColumn(int modelIndex){
        this(modelIndex,75,null,null);
    }

    public TableColumn(int modelIndex,int width,
                       TableCellRenderer cellRenderer,
                       TableCellEditor cellEditor){
        super();
        this.modelIndex=modelIndex;
        preferredWidth=this.width=Math.max(width,0);
        this.cellRenderer=cellRenderer;
        this.cellEditor=cellEditor;
        // Set other instance variables to default values.
        minWidth=Math.min(15,this.width);
        maxWidth=Integer.MAX_VALUE;
        isResizable=true;
        resizedPostingDisableCount=0;
        headerValue=null;
    }

    public TableColumn(int modelIndex,int width){
        this(modelIndex,width,null,null);
    }
//
// Modifying and Querying attributes
//

    public int getModelIndex(){
        return modelIndex;
    }

    public void setModelIndex(int modelIndex){
        int old=this.modelIndex;
        this.modelIndex=modelIndex;
        firePropertyChange("modelIndex",old,modelIndex);
    }

    private void firePropertyChange(String propertyName,int oldValue,int newValue){
        if(oldValue!=newValue){
            firePropertyChange(propertyName,Integer.valueOf(oldValue),Integer.valueOf(newValue));
        }
    }

    private void firePropertyChange(String propertyName,Object oldValue,Object newValue){
        if(changeSupport!=null){
            changeSupport.firePropertyChange(propertyName,oldValue,newValue);
        }
    }

    public Object getIdentifier(){
        return (identifier!=null)?identifier:getHeaderValue();
    }

    public void setIdentifier(Object identifier){
        Object old=this.identifier;
        this.identifier=identifier;
        firePropertyChange("identifier",old,identifier);
    }

    public Object getHeaderValue(){
        return headerValue;
    }

    public void setHeaderValue(Object headerValue){
        Object old=this.headerValue;
        this.headerValue=headerValue;
        firePropertyChange("headerValue",old,headerValue);
    }

    public TableCellRenderer getHeaderRenderer(){
        return headerRenderer;
    }
    //
    // Renderers and Editors
    //

    public void setHeaderRenderer(TableCellRenderer headerRenderer){
        TableCellRenderer old=this.headerRenderer;
        this.headerRenderer=headerRenderer;
        firePropertyChange("headerRenderer",old,headerRenderer);
    }

    public TableCellRenderer getCellRenderer(){
        return cellRenderer;
    }

    public void setCellRenderer(TableCellRenderer cellRenderer){
        TableCellRenderer old=this.cellRenderer;
        this.cellRenderer=cellRenderer;
        firePropertyChange("cellRenderer",old,cellRenderer);
    }

    public TableCellEditor getCellEditor(){
        return cellEditor;
    }

    public void setCellEditor(TableCellEditor cellEditor){
        TableCellEditor old=this.cellEditor;
        this.cellEditor=cellEditor;
        firePropertyChange("cellEditor",old,cellEditor);
    }

    public int getWidth(){
        return width;
    }

    public void setWidth(int width){
        int old=this.width;
        this.width=Math.min(Math.max(width,minWidth),maxWidth);
        firePropertyChange("width",old,this.width);
    }

    public int getPreferredWidth(){
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth){
        int old=this.preferredWidth;
        this.preferredWidth=Math.min(Math.max(preferredWidth,minWidth),maxWidth);
        firePropertyChange("preferredWidth",old,this.preferredWidth);
    }

    public int getMinWidth(){
        return minWidth;
    }

    public void setMinWidth(int minWidth){
        int old=this.minWidth;
        this.minWidth=Math.max(Math.min(minWidth,maxWidth),0);
        if(width<this.minWidth){
            setWidth(this.minWidth);
        }
        if(preferredWidth<this.minWidth){
            setPreferredWidth(this.minWidth);
        }
        firePropertyChange("minWidth",old,this.minWidth);
    }

    public int getMaxWidth(){
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth){
        int old=this.maxWidth;
        this.maxWidth=Math.max(minWidth,maxWidth);
        if(width>this.maxWidth){
            setWidth(this.maxWidth);
        }
        if(preferredWidth>this.maxWidth){
            setPreferredWidth(this.maxWidth);
        }
        firePropertyChange("maxWidth",old,this.maxWidth);
    }

    public boolean getResizable(){
        return isResizable;
    }

    public void setResizable(boolean isResizable){
        boolean old=this.isResizable;
        this.isResizable=isResizable;
        firePropertyChange("isResizable",old,this.isResizable);
    }

    private void firePropertyChange(String propertyName,boolean oldValue,boolean newValue){
        if(oldValue!=newValue){
            firePropertyChange(propertyName,Boolean.valueOf(oldValue),Boolean.valueOf(newValue));
        }
    }

    public void sizeWidthToFit(){
        if(headerRenderer==null){
            return;
        }
        Component c=headerRenderer.getTableCellRendererComponent(null,
                getHeaderValue(),false,false,0,0);
        setMinWidth(c.getMinimumSize().width);
        setMaxWidth(c.getMaximumSize().width);
        setPreferredWidth(c.getPreferredSize().width);
        setWidth(getPreferredWidth());
    }

    @Deprecated
    public void disableResizedPosting(){
        resizedPostingDisableCount++;
    }

    @Deprecated
    public void enableResizedPosting(){
        resizedPostingDisableCount--;
    }
//
// Property Change Support
//

    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener){
        if(changeSupport==null){
            changeSupport=new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener){
        if(changeSupport!=null){
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(){
        if(changeSupport==null){
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }
//
// Protected Methods
//

    protected TableCellRenderer createDefaultHeaderRenderer(){
        DefaultTableCellRenderer label=new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable table,Object value,
                                                           boolean isSelected,boolean hasFocus,int row,int column){
                if(table!=null){
                    JTableHeader header=table.getTableHeader();
                    if(header!=null){
                        setForeground(header.getForeground());
                        setBackground(header.getBackground());
                        setFont(header.getFont());
                    }
                }
                setText((value==null)?"":value.toString());
                setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                return this;
            }
        };
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
} // End of class TableColumn

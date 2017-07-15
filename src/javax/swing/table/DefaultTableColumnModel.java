/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Vector;

public class DefaultTableColumnModel implements TableColumnModel,
        PropertyChangeListener, ListSelectionListener, Serializable{
//
// Instance Variables
//
    protected Vector<TableColumn> tableColumns;
    protected ListSelectionModel selectionModel;
    protected int columnMargin;
    protected EventListenerList listenerList=new EventListenerList();
    transient protected ChangeEvent changeEvent=null;
    protected boolean columnSelectionAllowed;
    protected int totalColumnWidth;

    //
// Constructors
//
    public DefaultTableColumnModel(){
        super();
        // Initialize local ivars to default
        tableColumns=new Vector<TableColumn>();
        setSelectionModel(createSelectionModel());
        setColumnMargin(1);
        invalidateWidthCache();
        setColumnSelectionAllowed(false);
    }
//
// Modifying the model
//

    protected ListSelectionModel createSelectionModel(){
        return new DefaultListSelectionModel();
    }

    private void invalidateWidthCache(){
        totalColumnWidth=-1;
    }

    public void addColumn(TableColumn aColumn){
        if(aColumn==null){
            throw new IllegalArgumentException("Object is null");
        }
        tableColumns.addElement(aColumn);
        aColumn.addPropertyChangeListener(this);
        invalidateWidthCache();
        // Post columnAdded event notification
        fireColumnAdded(new TableColumnModelEvent(this,0,
                getColumnCount()-1));
    }

    public void removeColumn(TableColumn column){
        int columnIndex=tableColumns.indexOf(column);
        if(columnIndex!=-1){
            // Adjust for the selection
            if(selectionModel!=null){
                selectionModel.removeIndexInterval(columnIndex,columnIndex);
            }
            column.removePropertyChangeListener(this);
            tableColumns.removeElementAt(columnIndex);
            invalidateWidthCache();
            // Post columnAdded event notification.  (JTable and JTableHeader
            // listens so they can adjust size and redraw)
            fireColumnRemoved(new TableColumnModelEvent(this,
                    columnIndex,0));
        }
    }    public void setColumnMargin(int newMargin){
        if(newMargin!=columnMargin){
            columnMargin=newMargin;
            // Post columnMarginChanged event notification.
            fireColumnMarginChanged();
        }
    }
//
// Querying the model
//

    public void moveColumn(int columnIndex,int newIndex){
        if((columnIndex<0)||(columnIndex>=getColumnCount())||
                (newIndex<0)||(newIndex>=getColumnCount()))
            throw new IllegalArgumentException("moveColumn() - Index out of range");
        TableColumn aColumn;
        // If the column has not yet moved far enough to change positions
        // post the event anyway, the "draggedDistance" property of the
        // tableHeader will say how far the column has been dragged.
        // Here we are really trying to get the best out of an
        // API that could do with some rethinking. We preserve backward
        // compatibility by slightly bending the meaning of these methods.
        if(columnIndex==newIndex){
            fireColumnMoved(new TableColumnModelEvent(this,columnIndex,newIndex));
            return;
        }
        aColumn=tableColumns.elementAt(columnIndex);
        tableColumns.removeElementAt(columnIndex);
        boolean selected=selectionModel.isSelectedIndex(columnIndex);
        selectionModel.removeIndexInterval(columnIndex,columnIndex);
        tableColumns.insertElementAt(aColumn,newIndex);
        selectionModel.insertIndexInterval(newIndex,1,true);
        if(selected){
            selectionModel.addSelectionInterval(newIndex,newIndex);
        }else{
            selectionModel.removeSelectionInterval(newIndex,newIndex);
        }
        fireColumnMoved(new TableColumnModelEvent(this,columnIndex,
                newIndex));
    }    public int getColumnCount(){
        return tableColumns.size();
    }

    protected void fireColumnMoved(TableColumnModelEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TableColumnModelListener.class){
                // Lazily create the event:
                // if (e == null)
                //  e = new ChangeEvent(this);
                ((TableColumnModelListener)listeners[i+1]).
                        columnMoved(e);
            }
        }
    }    public Enumeration<TableColumn> getColumns(){
        return tableColumns.elements();
    }

    protected void fireColumnRemoved(TableColumnModelEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TableColumnModelListener.class){
                // Lazily create the event:
                // if (e == null)
                //  e = new ChangeEvent(this);
                ((TableColumnModelListener)listeners[i+1]).
                        columnRemoved(e);
            }
        }
    }    public int getColumnIndex(Object identifier){
        if(identifier==null){
            throw new IllegalArgumentException("Identifier is null");
        }
        Enumeration enumeration=getColumns();
        TableColumn aColumn;
        int index=0;
        while(enumeration.hasMoreElements()){
            aColumn=(TableColumn)enumeration.nextElement();
            // Compare them this way in case the column's identifier is null.
            if(identifier.equals(aColumn.getIdentifier()))
                return index;
            index++;
        }
        throw new IllegalArgumentException("Identifier not found");
    }

    protected void fireColumnAdded(TableColumnModelEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TableColumnModelListener.class){
                // Lazily create the event:
                // if (e == null)
                //  e = new ChangeEvent(this);
                ((TableColumnModelListener)listeners[i+1]).
                        columnAdded(e);
            }
        }
    }    public TableColumn getColumn(int columnIndex){
        return tableColumns.elementAt(columnIndex);
    }

    public TableColumnModelListener[] getColumnModelListeners(){
        return listenerList.getListeners(TableColumnModelListener.class);
    }    public int getColumnMargin(){
        return columnMargin;
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }    public int getColumnIndexAtX(int x){
        if(x<0){
            return -1;
        }
        int cc=getColumnCount();
        for(int column=0;column<cc;column++){
            x=x-getColumn(column).getWidth();
            if(x<0){
                return column;
            }
        }
        return -1;
    }

    // PENDING(alan)
    // implements java.beans.PropertyChangeListener
    public void propertyChange(PropertyChangeEvent evt){
        String name=evt.getPropertyName();
        if(name=="width"||name=="preferredWidth"){
            invalidateWidthCache();
            // This is a misnomer, we're using this method
            // simply to cause a relayout.
            fireColumnMarginChanged();
        }
    }    public int getTotalColumnWidth(){
        if(totalColumnWidth==-1){
            recalcWidthCache();
        }
        return totalColumnWidth;
    }
//
// Selection model
//

    // implements javax.swing.event.ListSelectionListener
    public void valueChanged(ListSelectionEvent e){
        fireColumnSelectionChanged(e);
    }    public void setSelectionModel(ListSelectionModel newModel){
        if(newModel==null){
            throw new IllegalArgumentException("Cannot set a null SelectionModel");
        }
        ListSelectionModel oldModel=selectionModel;
        if(newModel!=oldModel){
            if(oldModel!=null){
                oldModel.removeListSelectionListener(this);
            }
            selectionModel=newModel;
            newModel.addListSelectionListener(this);
        }
    }

    protected void fireColumnSelectionChanged(ListSelectionEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TableColumnModelListener.class){
                // Lazily create the event:
                // if (e == null)
                //  e = new ChangeEvent(this);
                ((TableColumnModelListener)listeners[i+1]).
                        columnSelectionChanged(e);
            }
        }
    }    public ListSelectionModel getSelectionModel(){
        return selectionModel;
    }

    // implements javax.swing.table.TableColumnModel
    public void setColumnSelectionAllowed(boolean flag){
        columnSelectionAllowed=flag;
    }

    // implements javax.swing.table.TableColumnModel
    public boolean getColumnSelectionAllowed(){
        return columnSelectionAllowed;
    }

    // implements javax.swing.table.TableColumnModel
    public int[] getSelectedColumns(){
        if(selectionModel!=null){
            int iMin=selectionModel.getMinSelectionIndex();
            int iMax=selectionModel.getMaxSelectionIndex();
            if((iMin==-1)||(iMax==-1)){
                return new int[0];
            }
            int[] rvTmp=new int[1+(iMax-iMin)];
            int n=0;
            for(int i=iMin;i<=iMax;i++){
                if(selectionModel.isSelectedIndex(i)){
                    rvTmp[n++]=i;
                }
            }
            int[] rv=new int[n];
            System.arraycopy(rvTmp,0,rv,0,n);
            return rv;
        }
        return new int[0];
    }

    // implements javax.swing.table.TableColumnModel
    public int getSelectedColumnCount(){
        if(selectionModel!=null){
            int iMin=selectionModel.getMinSelectionIndex();
            int iMax=selectionModel.getMaxSelectionIndex();
            int count=0;
            for(int i=iMin;i<=iMax;i++){
                if(selectionModel.isSelectedIndex(i)){
                    count++;
                }
            }
            return count;
        }
        return 0;
    }
//
// Listener Support Methods
//

    // implements javax.swing.table.TableColumnModel
    public void addColumnModelListener(TableColumnModelListener x){
        listenerList.add(TableColumnModelListener.class,x);
    }

    // implements javax.swing.table.TableColumnModel
    public void removeColumnModelListener(TableColumnModelListener x){
        listenerList.remove(TableColumnModelListener.class,x);
    }


//
//   Event firing methods
//









    protected void fireColumnMarginChanged(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TableColumnModelListener.class){
                // Lazily create the event:
                if(changeEvent==null)
                    changeEvent=new ChangeEvent(this);
                ((TableColumnModelListener)listeners[i+1]).
                        columnMarginChanged(changeEvent);
            }
        }
    }


//
// Implementing the PropertyChangeListener interface
//


//
// Implementing ListSelectionListener interface
//


//
// Protected Methods
//



    protected void recalcWidthCache(){
        Enumeration enumeration=getColumns();
        totalColumnWidth=0;
        while(enumeration.hasMoreElements()){
            totalColumnWidth+=((TableColumn)enumeration.nextElement()).getWidth();
        }
    }


} // End of class DefaultTableColumnModel

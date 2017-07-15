/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.io.Serializable;
import java.util.EventListener;

public abstract class AbstractTableModel implements TableModel, Serializable{
//
// Instance Variables
//
    protected EventListenerList listenerList=new EventListenerList();
//
// Default Implementation of the Interface
//

    public int findColumn(String columnName){
        for(int i=0;i<getColumnCount();i++){
            if(columnName.equals(getColumnName(i))){
                return i;
            }
        }
        return -1;
    }

    public String getColumnName(int column){
        String result="";
        for(;column>=0;column=column/26-1){
            result=(char)((char)(column%26)+'A')+result;
        }
        return result;
    }

    public Class<?> getColumnClass(int columnIndex){
        return Object.class;
    }

    public boolean isCellEditable(int rowIndex,int columnIndex){
        return false;
    }

    public void setValueAt(Object aValue,int rowIndex,int columnIndex){
    }
//
//  Managing Listeners
//

    public void addTableModelListener(TableModelListener l){
        listenerList.add(TableModelListener.class,l);
    }

    public void removeTableModelListener(TableModelListener l){
        listenerList.remove(TableModelListener.class,l);
    }

    public TableModelListener[] getTableModelListeners(){
        return listenerList.getListeners(TableModelListener.class);
    }
//
//  Fire methods
//

    public void fireTableDataChanged(){
        fireTableChanged(new TableModelEvent(this));
    }

    public void fireTableChanged(TableModelEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==TableModelListener.class){
                ((TableModelListener)listeners[i+1]).tableChanged(e);
            }
        }
    }

    public void fireTableStructureChanged(){
        fireTableChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
    }

    public void fireTableRowsInserted(int firstRow,int lastRow){
        fireTableChanged(new TableModelEvent(this,firstRow,lastRow,
                TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT));
    }

    public void fireTableRowsUpdated(int firstRow,int lastRow){
        fireTableChanged(new TableModelEvent(this,firstRow,lastRow,
                TableModelEvent.ALL_COLUMNS,TableModelEvent.UPDATE));
    }

    public void fireTableRowsDeleted(int firstRow,int lastRow){
        fireTableChanged(new TableModelEvent(this,firstRow,lastRow,
                TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));
    }

    public void fireTableCellUpdated(int row,int column){
        fireTableChanged(new TableModelEvent(this,row,row,column));
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }
} // End of class AbstractTableModel

/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.event.TableModelEvent;
import java.io.Serializable;
import java.util.Vector;

public class DefaultTableModel extends AbstractTableModel implements Serializable{
//
// Instance Variables
//
    protected Vector dataVector;
    protected Vector columnIdentifiers;
//
// Constructors
//

    public DefaultTableModel(){
        this(0,0);
    }

    public DefaultTableModel(int rowCount,int columnCount){
        this(newVector(columnCount),rowCount);
    }

    public DefaultTableModel(Vector columnNames,int rowCount){
        setDataVector(newVector(rowCount),columnNames);
    }

    private static Vector newVector(int size){
        Vector v=new Vector(size);
        v.setSize(size);
        return v;
    }

    public void setDataVector(Vector dataVector,Vector columnIdentifiers){
        this.dataVector=nonNullVector(dataVector);
        this.columnIdentifiers=nonNullVector(columnIdentifiers);
        justifyRows(0,getRowCount());
        fireTableStructureChanged();
    }

    private static Vector nonNullVector(Vector v){
        return (v!=null)?v:new Vector();
    }

    private void justifyRows(int from,int to){
        // Sometimes the DefaultTableModel is subclassed
        // instead of the AbstractTableModel by mistake.
        // Set the number of rows for the case when getRowCount
        // is overridden.
        dataVector.setSize(getRowCount());
        for(int i=from;i<to;i++){
            if(dataVector.elementAt(i)==null){
                dataVector.setElementAt(new Vector(),i);
            }
            ((Vector)dataVector.elementAt(i)).setSize(getColumnCount());
        }
    }

    public int getRowCount(){
        return dataVector.size();
    }

    public void setRowCount(int rowCount){
        setNumRows(rowCount);
    }

    public void setNumRows(int rowCount){
        int old=getRowCount();
        if(old==rowCount){
            return;
        }
        dataVector.setSize(rowCount);
        if(rowCount<=old){
            fireTableRowsDeleted(rowCount,old-1);
        }else{
            justifyRows(old,rowCount);
            fireTableRowsInserted(old,rowCount-1);
        }
    }

    public int getColumnCount(){
        return columnIdentifiers.size();
    }

    public void setColumnCount(int columnCount){
        columnIdentifiers.setSize(columnCount);
        justifyRows(0,getRowCount());
        fireTableStructureChanged();
    }
//
// Manipulating rows
//

    public Object getValueAt(int row,int column){
        Vector rowVector=(Vector)dataVector.elementAt(row);
        return rowVector.elementAt(column);
    }

    public DefaultTableModel(Object[] columnNames,int rowCount){
        this(convertToVector(columnNames),rowCount);
    }

    protected static Vector convertToVector(Object[] anArray){
        if(anArray==null){
            return null;
        }
        Vector<Object> v=new Vector<Object>(anArray.length);
        for(Object o : anArray){
            v.addElement(o);
        }
        return v;
    }

    public DefaultTableModel(Vector data,Vector columnNames){
        setDataVector(data,columnNames);
    }

    public DefaultTableModel(Object[][] data,Object[] columnNames){
        setDataVector(data,columnNames);
    }

    public void setDataVector(Object[][] dataVector,Object[] columnIdentifiers){
        setDataVector(convertToVector(dataVector),convertToVector(columnIdentifiers));
    }

    protected static Vector convertToVector(Object[][] anArray){
        if(anArray==null){
            return null;
        }
        Vector<Vector> v=new Vector<Vector>(anArray.length);
        for(Object[] o : anArray){
            v.addElement(convertToVector(o));
        }
        return v;
    }

    private static int gcd(int i,int j){
        return (j==0)?i:gcd(j,i%j);
    }

    private static void rotate(Vector v,int a,int b,int shift){
        int size=b-a;
        int r=size-shift;
        int g=gcd(size,r);
        for(int i=0;i<g;i++){
            int to=i;
            Object tmp=v.elementAt(a+to);
            for(int from=(to+r)%size;from!=i;from=(to+r)%size){
                v.setElementAt(v.elementAt(a+from),a+to);
                to=from;
            }
            v.setElementAt(tmp,a+to);
        }
    }

    public Vector getDataVector(){
        return dataVector;
    }

    public void newDataAvailable(TableModelEvent event){
        fireTableChanged(event);
    }

    public void newRowsAdded(TableModelEvent e){
        justifyRows(e.getFirstRow(),e.getLastRow()+1);
        fireTableChanged(e);
    }

    public void rowsRemoved(TableModelEvent event){
        fireTableChanged(event);
    }
//
// Manipulating columns
//

    public void addRow(Object[] rowData){
        addRow(convertToVector(rowData));
    }

    public void addRow(Vector rowData){
        insertRow(getRowCount(),rowData);
    }

    public void insertRow(int row,Vector rowData){
        dataVector.insertElementAt(rowData,row);
        justifyRows(row,row+1);
        fireTableRowsInserted(row,row);
    }

    public void insertRow(int row,Object[] rowData){
        insertRow(row,convertToVector(rowData));
    }

    public void moveRow(int start,int end,int to){
        int shift=to-start;
        int first, last;
        if(shift<0){
            first=to;
            last=end;
        }else{
            first=start;
            last=to+end-start;
        }
        rotate(dataVector,first,last+1,shift);
        fireTableRowsUpdated(first,last);
    }

    public void removeRow(int row){
        dataVector.removeElementAt(row);
        fireTableRowsDeleted(row,row);
    }
//
// Implementing the TableModel interface
//

    public void setColumnIdentifiers(Object[] newIdentifiers){
        setColumnIdentifiers(convertToVector(newIdentifiers));
    }

    public void setColumnIdentifiers(Vector columnIdentifiers){
        setDataVector(dataVector,columnIdentifiers);
    }

    public void addColumn(Object columnName){
        addColumn(columnName,(Vector)null);
    }

    public void addColumn(Object columnName,Vector columnData){
        columnIdentifiers.addElement(columnName);
        if(columnData!=null){
            int columnSize=columnData.size();
            if(columnSize>getRowCount()){
                dataVector.setSize(columnSize);
            }
            justifyRows(0,getRowCount());
            int newColumn=getColumnCount()-1;
            for(int i=0;i<columnSize;i++){
                Vector row=(Vector)dataVector.elementAt(i);
                row.setElementAt(columnData.elementAt(i),newColumn);
            }
        }else{
            justifyRows(0,getRowCount());
        }
        fireTableStructureChanged();
    }

    public void addColumn(Object columnName,Object[] columnData){
        addColumn(columnName,convertToVector(columnData));
    }

    public String getColumnName(int column){
        Object id=null;
        // This test is to cover the case when
        // getColumnCount has been subclassed by mistake ...
        if(column<columnIdentifiers.size()&&(column>=0)){
            id=columnIdentifiers.elementAt(column);
        }
        return (id==null)?super.getColumnName(column)
                :id.toString();
    }
//
// Protected Methods
//

    public boolean isCellEditable(int row,int column){
        return true;
    }

    public void setValueAt(Object aValue,int row,int column){
        Vector rowVector=(Vector)dataVector.elementAt(row);
        rowVector.setElementAt(aValue,column);
        fireTableCellUpdated(row,column);
    }
} // End of class DefaultTableModel

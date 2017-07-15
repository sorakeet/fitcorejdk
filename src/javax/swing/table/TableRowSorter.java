/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.*;
import java.text.Collator;
import java.util.Comparator;

public class TableRowSorter<M extends TableModel> extends DefaultRowSorter<M,Integer>{
    private static final Comparator COMPARABLE_COMPARATOR=
            new ComparableComparator();
    private M tableModel;
    private TableStringConverter stringConverter;

    public TableRowSorter(){
        this(null);
    }

    public TableRowSorter(M model){
        setModel(model);
    }

    public void setModel(M model){
        tableModel=model;
        setModelWrapper(new TableRowSorterModelWrapper());
    }

    public TableStringConverter getStringConverter(){
        return stringConverter;
    }

    public void setStringConverter(TableStringConverter stringConverter){
        this.stringConverter=stringConverter;
    }

    protected boolean useToString(int column){
        Comparator comparator=super.getComparator(column);
        if(comparator!=null){
            return false;
        }
        Class columnClass=getModel().getColumnClass(column);
        if(columnClass==String.class){
            return false;
        }
        if(Comparable.class.isAssignableFrom(columnClass)){
            return false;
        }
        return true;
    }

    public Comparator<?> getComparator(int column){
        Comparator comparator=super.getComparator(column);
        if(comparator!=null){
            return comparator;
        }
        Class columnClass=getModel().getColumnClass(column);
        if(columnClass==String.class){
            return Collator.getInstance();
        }
        if(Comparable.class.isAssignableFrom(columnClass)){
            return COMPARABLE_COMPARATOR;
        }
        return Collator.getInstance();
    }

    private static class ComparableComparator implements Comparator{
        @SuppressWarnings("unchecked")
        public int compare(Object o1,Object o2){
            return ((Comparable)o1).compareTo(o2);
        }
    }

    private class TableRowSorterModelWrapper extends ModelWrapper<M,Integer>{
        public M getModel(){
            return tableModel;
        }

        public int getColumnCount(){
            return (tableModel==null)?0:tableModel.getColumnCount();
        }

        public int getRowCount(){
            return (tableModel==null)?0:tableModel.getRowCount();
        }

        public Object getValueAt(int row,int column){
            return tableModel.getValueAt(row,column);
        }

        public String getStringValueAt(int row,int column){
            TableStringConverter converter=getStringConverter();
            if(converter!=null){
                // Use the converter
                String value=converter.toString(
                        tableModel,row,column);
                if(value!=null){
                    return value;
                }
                return "";
            }
            // No converter, use getValueAt followed by toString
            Object o=getValueAt(row,column);
            if(o==null){
                return "";
            }
            String string=o.toString();
            if(string==null){
                return "";
            }
            return string;
        }

        public Integer getIdentifier(int index){
            return index;
        }
    }
}

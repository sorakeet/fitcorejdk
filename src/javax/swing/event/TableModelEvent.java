/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.table.TableModel;
import java.util.EventObject;

public class TableModelEvent extends EventObject{
    public static final int INSERT=1;
    public static final int UPDATE=0;
    public static final int DELETE=-1;
    public static final int HEADER_ROW=-1;
    public static final int ALL_COLUMNS=-1;
//
//  Instance Variables
//
    protected int type;
    protected int firstRow;
    protected int lastRow;
    protected int column;
//
// Constructors
//

    public TableModelEvent(TableModel source){
        // Use Integer.MAX_VALUE instead of getRowCount() in case rows were deleted.
        this(source,0,Integer.MAX_VALUE,ALL_COLUMNS,UPDATE);
    }

    public TableModelEvent(TableModel source,int firstRow,int lastRow,int column,int type){
        super(source);
        this.firstRow=firstRow;
        this.lastRow=lastRow;
        this.column=column;
        this.type=type;
    }

    public TableModelEvent(TableModel source,int row){
        this(source,row,row,ALL_COLUMNS,UPDATE);
    }

    public TableModelEvent(TableModel source,int firstRow,int lastRow){
        this(source,firstRow,lastRow,ALL_COLUMNS,UPDATE);
    }

    public TableModelEvent(TableModel source,int firstRow,int lastRow,int column){
        this(source,firstRow,lastRow,column,UPDATE);
    }
//
// Querying Methods
//

    public int getFirstRow(){
        return firstRow;
    }

    ;

    public int getLastRow(){
        return lastRow;
    }

    ;

    public int getColumn(){
        return column;
    }

    ;

    public int getType(){
        return type;
    }
}

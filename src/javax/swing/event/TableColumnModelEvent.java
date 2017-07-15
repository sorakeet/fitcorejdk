/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.table.TableColumnModel;
import java.util.EventObject;

public class TableColumnModelEvent extends EventObject{
//
//  Instance Variables
//
    protected int fromIndex;
    protected int toIndex;
//
// Constructors
//

    public TableColumnModelEvent(TableColumnModel source,int from,int to){
        super(source);
        fromIndex=from;
        toIndex=to;
    }
//
// Querying Methods
//

    public int getFromIndex(){
        return fromIndex;
    }

    ;

    public int getToIndex(){
        return toIndex;
    }

    ;
}

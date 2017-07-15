/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventObject;

public abstract class CaretEvent extends EventObject{
    public CaretEvent(Object source){
        super(source);
    }

    public abstract int getDot();

    public abstract int getMark();
}

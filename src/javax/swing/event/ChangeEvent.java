/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ChangeEvent extends EventObject{
    public ChangeEvent(Object source){
        super(source);
    }
}

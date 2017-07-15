/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.undo;

import java.util.Hashtable;

public interface StateEditable{
    public static final String RCSID="$Id: StateEditable.java,v 1.2 1997/09/08 19:39:08 marklin Exp $";

    public void storeState(Hashtable<Object,Object> state);

    public void restoreState(Hashtable<?,?> state);
} // End of interface StateEditable

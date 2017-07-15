/**
 * Copyright (c) 1996, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public class PropertyVetoException extends Exception{
    private static final long serialVersionUID=129596057694162164L;
    private PropertyChangeEvent evt;

    public PropertyVetoException(String mess,PropertyChangeEvent evt){
        super(mess);
        this.evt=evt;
    }

    public PropertyChangeEvent getPropertyChangeEvent(){
        return evt;
    }
}

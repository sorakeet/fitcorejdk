/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.util.EventListenerProxy;

public class VetoableChangeListenerProxy
        extends EventListenerProxy<VetoableChangeListener>
        implements VetoableChangeListener{
    private final String propertyName;

    public VetoableChangeListenerProxy(String propertyName,VetoableChangeListener listener){
        super(listener);
        this.propertyName=propertyName;
    }

    public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException{
        getListener().vetoableChange(event);
    }

    public String getPropertyName(){
        return this.propertyName;
    }
}

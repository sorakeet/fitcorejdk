/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.util.EventListenerProxy;

public class PropertyChangeListenerProxy
        extends EventListenerProxy<PropertyChangeListener>
        implements PropertyChangeListener{
    private final String propertyName;

    public PropertyChangeListenerProxy(String propertyName,PropertyChangeListener listener){
        super(listener);
        this.propertyName=propertyName;
    }

    public void propertyChange(PropertyChangeEvent event){
        getListener().propertyChange(event);
    }

    public String getPropertyName(){
        return this.propertyName;
    }
}

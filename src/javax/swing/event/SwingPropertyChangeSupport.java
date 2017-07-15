/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;

public final class SwingPropertyChangeSupport extends PropertyChangeSupport{
    // Serialization version ID
    static final long serialVersionUID=7162625831330845068L;
    private final boolean notifyOnEDT;

    public SwingPropertyChangeSupport(Object sourceBean){
        this(sourceBean,false);
    }

    public SwingPropertyChangeSupport(Object sourceBean,boolean notifyOnEDT){
        super(sourceBean);
        this.notifyOnEDT=notifyOnEDT;
    }

    public void firePropertyChange(final PropertyChangeEvent evt){
        if(evt==null){
            throw new NullPointerException();
        }
        if(!isNotifyOnEDT()
                ||SwingUtilities.isEventDispatchThread()){
            super.firePropertyChange(evt);
        }else{
            SwingUtilities.invokeLater(
                    new Runnable(){
                        public void run(){
                            firePropertyChange(evt);
                        }
                    });
        }
    }

    public final boolean isNotifyOnEDT(){
        return notifyOnEDT;
    }
}

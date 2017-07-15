/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

import java.io.NotSerializableException;

public class NodeChangeEvent extends java.util.EventObject{
    // Defined so that this class isn't flagged as a potential problem when
    // searches for missing serialVersionUID fields are done.
    private static final long serialVersionUID=8068949086596572957L;
    private Preferences child;

    public NodeChangeEvent(Preferences parent,Preferences child){
        super(parent);
        this.child=child;
    }

    public Preferences getParent(){
        return (Preferences)getSource();
    }

    public Preferences getChild(){
        return child;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws NotSerializableException{
        throw new NotSerializableException("Not serializable.");
    }

    private void readObject(java.io.ObjectInputStream in)
            throws NotSerializableException{
        throw new NotSerializableException("Not serializable.");
    }
}

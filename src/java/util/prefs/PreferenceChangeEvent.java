/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

import java.io.NotSerializableException;

public class PreferenceChangeEvent extends java.util.EventObject{
    // Defined so that this class isn't flagged as a potential problem when
    // searches for missing serialVersionUID fields are done.
    private static final long serialVersionUID=793724513368024975L;
    private String key;
    private String newValue;

    public PreferenceChangeEvent(Preferences node,String key,
                                 String newValue){
        super(node);
        this.key=key;
        this.newValue=newValue;
    }

    public Preferences getNode(){
        return (Preferences)getSource();
    }

    public String getKey(){
        return key;
    }

    public String getNewValue(){
        return newValue;
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

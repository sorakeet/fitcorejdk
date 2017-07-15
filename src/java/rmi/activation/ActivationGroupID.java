/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import java.rmi.server.UID;

public class ActivationGroupID implements java.io.Serializable{
    private static final long serialVersionUID=-1648432278909740833L;
    private ActivationSystem system;
    private UID uid=new UID();

    public ActivationGroupID(ActivationSystem system){
        this.system=system;
    }

    public ActivationSystem getSystem(){
        return system;
    }

    public int hashCode(){
        return uid.hashCode();
    }

    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }else if(obj instanceof ActivationGroupID){
            ActivationGroupID id=(ActivationGroupID)obj;
            return (uid.equals(id.uid)&&system.equals(id.system));
        }else{
            return false;
        }
    }
}

/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import java.io.Serializable;
import java.rmi.MarshalledObject;

public final class ActivationDesc implements Serializable{
    private static final long serialVersionUID=7455834104417690957L;
    private ActivationGroupID groupID;
    private String className;
    private String location;
    private MarshalledObject<?> data;
    private boolean restart;

    public ActivationDesc(String className,
                          String location,
                          MarshalledObject<?> data)
            throws ActivationException{
        this(ActivationGroup.internalCurrentGroupID(),
                className,location,data,false);
    }

    public ActivationDesc(ActivationGroupID groupID,
                          String className,
                          String location,
                          MarshalledObject<?> data,
                          boolean restart){
        if(groupID==null)
            throw new IllegalArgumentException("groupID can't be null");
        this.groupID=groupID;
        this.className=className;
        this.location=location;
        this.data=data;
        this.restart=restart;
    }

    public ActivationDesc(String className,
                          String location,
                          MarshalledObject<?> data,
                          boolean restart)
            throws ActivationException{
        this(ActivationGroup.internalCurrentGroupID(),
                className,location,data,restart);
    }

    public ActivationDesc(ActivationGroupID groupID,
                          String className,
                          String location,
                          MarshalledObject<?> data){
        this(groupID,className,location,data,false);
    }

    public ActivationGroupID getGroupID(){
        return groupID;
    }

    public String getClassName(){
        return className;
    }

    public String getLocation(){
        return location;
    }

    public MarshalledObject<?> getData(){
        return data;
    }

    public boolean getRestartMode(){
        return restart;
    }

    public int hashCode(){
        return ((location==null
                ?0
                :location.hashCode()<<24)^
                (groupID==null
                        ?0
                        :groupID.hashCode()<<16)^
                (className==null
                        ?0
                        :className.hashCode()<<9)^
                (data==null
                        ?0
                        :data.hashCode()<<1)^
                (restart
                        ?1
                        :0));
    }

    public boolean equals(Object obj){
        if(obj instanceof ActivationDesc){
            ActivationDesc desc=(ActivationDesc)obj;
            return
                    ((groupID==null?desc.groupID==null:
                            groupID.equals(desc.groupID))&&
                            (className==null?desc.className==null:
                                    className.equals(desc.className))&&
                            (location==null?desc.location==null:
                                    location.equals(desc.location))&&
                            (data==null?desc.data==null:
                                    data.equals(desc.data))&&
                            (restart==desc.restart));
        }else{
            return false;
        }
    }
}

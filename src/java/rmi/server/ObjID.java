/**
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import sun.security.action.GetPropertyAction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.AccessController;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class ObjID implements Serializable{
    public static final int REGISTRY_ID=0;
    public static final int ACTIVATOR_ID=1;
    public static final int DGC_ID=2;
    private static final long serialVersionUID=-6386392263968365220L;
    private static final AtomicLong nextObjNum=new AtomicLong(0);
    private static final UID mySpace=new UID();
    private static final SecureRandom secureRandom=new SecureRandom();
    private final long objNum;
    private final UID space;

    public ObjID(){
        /**
         * If generating random object numbers, create a new UID to
         * ensure uniqueness; otherwise, use a shared UID because
         * sequential object numbers already ensure uniqueness.
         */
        if(useRandomIDs()){
            space=new UID();
            objNum=secureRandom.nextLong();
        }else{
            space=mySpace;
            objNum=nextObjNum.getAndIncrement();
        }
    }

    private static boolean useRandomIDs(){
        String value=AccessController.doPrivileged(
                new GetPropertyAction("java.rmi.server.randomIDs"));
        return value==null?true:Boolean.parseBoolean(value);
    }

    public ObjID(int objNum){
        space=new UID((short)0);
        this.objNum=objNum;
    }

    private ObjID(long objNum,UID space){
        this.objNum=objNum;
        this.space=space;
    }

    public static ObjID read(ObjectInput in) throws IOException{
        long num=in.readLong();
        UID space=UID.read(in);
        return new ObjID(num,space);
    }

    public void write(ObjectOutput out) throws IOException{
        out.writeLong(objNum);
        space.write(out);
    }

    public int hashCode(){
        return (int)objNum;
    }

    public boolean equals(Object obj){
        if(obj instanceof ObjID){
            ObjID id=(ObjID)obj;
            return objNum==id.objNum&&space.equals(id.space);
        }else{
            return false;
        }
    }

    public String toString(){
        return "["+(space.equals(mySpace)?"":space+", ")+
                objNum+"]";
    }
}

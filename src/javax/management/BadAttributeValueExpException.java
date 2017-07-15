/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.io.IOException;
import java.io.ObjectInputStream;

public class BadAttributeValueExpException extends Exception{
    private static final long serialVersionUID=-3105272988410493376L;
    private Object val;

    public BadAttributeValueExpException(Object val){
        this.val=val==null?null:val.toString();
    }

    public String toString(){
        return "BadAttributeValueException: "+val;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField gf=ois.readFields();
        Object valObj=gf.get("val",null);
        if(valObj==null){
            val=null;
        }else if(valObj instanceof String){
            val=valObj;
        }else if(System.getSecurityManager()==null
                ||valObj instanceof Long
                ||valObj instanceof Integer
                ||valObj instanceof Float
                ||valObj instanceof Double
                ||valObj instanceof Byte
                ||valObj instanceof Short
                ||valObj instanceof Boolean){
            val=valObj.toString();
        }else{ // the serialized object is from a version without JDK-8019292 fix
            val=System.identityHashCode(valObj)+"@"+valObj.getClass().getName();
        }
    }
}

/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;
// java import

import java.io.Serializable;
// RI import

public class ObjectInstance implements Serializable{
    private static final long serialVersionUID=-4099952623687795850L;
    private ObjectName name;
    private String className;

    public ObjectInstance(String objectName,String className)
            throws MalformedObjectNameException{
        this(new ObjectName(objectName),className);
    }

    public ObjectInstance(ObjectName objectName,String className){
        if(objectName.isPattern()){
            final IllegalArgumentException iae=
                    new IllegalArgumentException("Invalid name->"+
                            objectName.toString());
            throw new RuntimeOperationsException(iae);
        }
        this.name=objectName;
        this.className=className;
    }

    public int hashCode(){
        final int classHash=((className==null)?0:className.hashCode());
        return name.hashCode()^classHash;
    }

    public boolean equals(Object object){
        if(!(object instanceof ObjectInstance)){
            return false;
        }
        ObjectInstance val=(ObjectInstance)object;
        if(!name.equals(val.getObjectName())) return false;
        if(className==null)
            return (val.getClassName()==null);
        return className.equals(val.getClassName());
    }

    public String toString(){
        return getClassName()+"["+getObjectName()+"]";
    }

    public ObjectName getObjectName(){
        return name;
    }

    public String getClassName(){
        return className;
    }
}

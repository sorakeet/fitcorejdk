/**
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;
// java import

import java.io.Serializable;

public class Attribute implements Serializable{
    private static final long serialVersionUID=2484220110589082382L;
    private String name;
    private Object value=null;

    public Attribute(String name,Object value){
        if(name==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null "));
        }
        this.name=name;
        this.value=value;
    }

    public int hashCode(){
        return name.hashCode()^(value==null?0:value.hashCode());
    }

    public boolean equals(Object object){
        if(!(object instanceof Attribute)){
            return false;
        }
        Attribute val=(Attribute)object;
        if(value==null){
            if(val.getValue()==null){
                return name.equals(val.getName());
            }else{
                return false;
            }
        }
        return ((name.equals(val.getName()))&&
                (value.equals(val.getValue())));
    }

    public String toString(){
        return getName()+" = "+getValue();
    }

    public String getName(){
        return name;
    }

    public Object getValue(){
        return value;
    }
}

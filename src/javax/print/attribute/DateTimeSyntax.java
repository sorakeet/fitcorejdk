/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

import java.io.Serializable;
import java.util.Date;

public abstract class DateTimeSyntax implements Serializable, Cloneable{
    private static final long serialVersionUID=-1400819079791208582L;
    // Hidden data members.
    private Date value;
    // Hidden constructors.

    protected DateTimeSyntax(Date value){
        if(value==null){
            throw new NullPointerException("value is null");
        }
        this.value=value;
    }
    // Exported operations.

    public Date getValue(){
        return new Date(value.getTime());
    }
    // Exported operations inherited and overridden from class Object.

    public int hashCode(){
        return value.hashCode();
    }

    public boolean equals(Object object){
        return (object!=null&&
                object instanceof DateTimeSyntax&&
                value.equals(((DateTimeSyntax)object).value));
    }

    public String toString(){
        return ""+value;
    }
}

/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class BadBinaryOpValueExpException extends Exception{
    private static final long serialVersionUID=5068475589449021227L;
    private ValueExp exp;

    public BadBinaryOpValueExpException(ValueExp exp){
        this.exp=exp;
    }

    public ValueExp getExp(){
        return exp;
    }

    public String toString(){
        return "BadBinaryOpValueExpException: "+exp;
    }
}

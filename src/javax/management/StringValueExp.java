/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class StringValueExp implements ValueExp{
    private static final long serialVersionUID=-3256390509806284044L;
    private String val;

    public StringValueExp(){
    }

    public StringValueExp(String val){
        this.val=val;
    }

    public String getValue(){
        return val;
    }

    public String toString(){
        return "'"+val.replace("'","''")+"'";
    }

    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException{
        return this;
    }

    @Deprecated
    public void setMBeanServer(MBeanServer s){
    }
}

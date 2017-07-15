/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

class BooleanValueExp extends QueryEval implements ValueExp{
    private static final long serialVersionUID=7754922052666594581L;
    private boolean val=false;

    BooleanValueExp(boolean val){
        this.val=val;
    }

    BooleanValueExp(Boolean val){
        this.val=val.booleanValue();
    }

    public Boolean getValue(){
        return Boolean.valueOf(val);
    }

    public String toString(){
        return String.valueOf(val);
    }

    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException{
        return this;
    }

    @Deprecated
    public void setMBeanServer(MBeanServer s){
        super.setMBeanServer(s);
    }
}

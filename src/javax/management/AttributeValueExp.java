/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class AttributeValueExp implements ValueExp{
    private static final long serialVersionUID=-7768025046539163385L;
    private String attr;

    @Deprecated
    public AttributeValueExp(){
    }

    public AttributeValueExp(String attr){
        this.attr=attr;
    }

    public String getAttributeName(){
        return attr;
    }

    @Override
    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException{
        Object result=getAttribute(name);
        if(result instanceof Number){
            return new NumericValueExp((Number)result);
        }else if(result instanceof String){
            return new StringValueExp((String)result);
        }else if(result instanceof Boolean){
            return new BooleanValueExp((Boolean)result);
        }else{
            throw new BadAttributeValueExpException(result);
        }
    }

    @Deprecated
    @Override
    public void setMBeanServer(MBeanServer s){
    }

    protected Object getAttribute(ObjectName name){
        try{
            // Get the value from the MBeanServer
            MBeanServer server=QueryEval.getMBeanServer();
            return server.getAttribute(name,attr);
        }catch(Exception re){
            return null;
        }
    }

    @Override
    public String toString(){
        return attr;
    }
}

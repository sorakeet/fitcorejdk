/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public class Expression extends Statement{
    private static Object unbound=new Object();
    private Object value=unbound;

    public Expression(Object value,Object target,String methodName,Object[] arguments){
        this(target,methodName,arguments);
        setValue(value);
    }

    @ConstructorProperties({"target","methodName","arguments"})
    public Expression(Object target,String methodName,Object[] arguments){
        super(target,methodName,arguments);
    }

    @Override
    public void execute() throws Exception{
        setValue(invoke());
    }

    String instanceName(Object instance){
        return instance==unbound?"<unbound>":super.instanceName(instance);
    }

    public String toString(){
        return instanceName(value)+"="+super.toString();
    }

    public Object getValue() throws Exception{
        if(value==unbound){
            setValue(invoke());
        }
        return value;
    }

    public void setValue(Object value){
        this.value=value;
    }
}

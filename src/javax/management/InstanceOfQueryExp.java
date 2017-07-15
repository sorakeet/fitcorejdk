/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

class InstanceOfQueryExp extends QueryEval implements QueryExp{
    private static final long serialVersionUID=-1081892073854801359L;
    private StringValueExp classNameValue;

    // We are using StringValueExp here to be consistent with other queries,
    // although we should actually either use a simple string (the classname)
    // or a ValueExp - which would allow more complex queries - like for
    // instance evaluating the class name from an AttributeValueExp.
    // As it stands - using StringValueExp instead of a simple constant string
    // doesn't serve any useful purpose besides offering a consistent
    // look & feel.
    public InstanceOfQueryExp(StringValueExp classNameValue){
        if(classNameValue==null){
            throw new IllegalArgumentException("Null class name.");
        }
        this.classNameValue=classNameValue;
    }

    public StringValueExp getClassNameValue(){
        return classNameValue;
    }

    public boolean apply(ObjectName name)
            throws BadStringOperationException,
            BadBinaryOpValueExpException,
            BadAttributeValueExpException,
            InvalidApplicationException{
        // Get the class name value
        final StringValueExp val;
        try{
            val=(StringValueExp)classNameValue.apply(name);
        }catch(ClassCastException x){
            // Should not happen - unless someone wrongly implemented
            // StringValueExp.apply().
            final BadStringOperationException y=
                    new BadStringOperationException(x.toString());
            y.initCause(x);
            throw y;
        }
        // Test whether the MBean is an instance of that class.
        try{
            return getMBeanServer().isInstanceOf(name,val.getValue());
        }catch(InstanceNotFoundException infe){
            return false;
        }
    }

    public String toString(){
        return "InstanceOf "+classNameValue.toString();
    }
}

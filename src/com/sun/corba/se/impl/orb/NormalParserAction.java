/**
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orb;

import com.sun.corba.se.spi.orb.Operation;

import java.util.Properties;

public class NormalParserAction extends ParserActionBase{
    public NormalParserAction(String propertyName,
                              Operation operation,String fieldName){
        super(propertyName,false,operation,fieldName);
    }

    public Object apply(Properties props){
        Object value=props.getProperty(getPropertyName());
        if(value!=null)
            return getOperation().operate(value);
        else
            return null;
    }
}

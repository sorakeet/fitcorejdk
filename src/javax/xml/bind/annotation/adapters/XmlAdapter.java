/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation.adapters;

public abstract class XmlAdapter<ValueType,BoundType>{
    protected XmlAdapter(){
    }

    public abstract BoundType unmarshal(ValueType v) throws Exception;

    public abstract ValueType marshal(BoundType v) throws Exception;
}

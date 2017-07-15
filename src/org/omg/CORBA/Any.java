/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;

abstract public class Any implements IDLEntity{
    abstract public boolean equal(Any a);

    abstract public TypeCode type();

    abstract public void type(TypeCode t);
    ///////////////////////////////////////////////////////////////////////////
    // marshalling/unmarshalling routines

    abstract public void read_value(InputStream is,TypeCode t)
            throws MARSHAL;

    abstract public void write_value(OutputStream os);

    abstract public OutputStream create_output_stream();

    abstract public InputStream create_input_stream();
    ///////////////////////////////////////////////////////////////////////////
    // basic insertion/extraction methods

    abstract public short extract_short() throws BAD_OPERATION;

    abstract public void insert_short(short s);

    abstract public int extract_long() throws BAD_OPERATION;

    abstract public void insert_long(int l);

    abstract public long extract_longlong() throws BAD_OPERATION;

    abstract public void insert_longlong(long l);

    abstract public short extract_ushort() throws BAD_OPERATION;

    abstract public void insert_ushort(short s);

    abstract public int extract_ulong() throws BAD_OPERATION;

    abstract public void insert_ulong(int l);

    abstract public long extract_ulonglong() throws BAD_OPERATION;

    abstract public void insert_ulonglong(long l);

    abstract public float extract_float() throws BAD_OPERATION;

    abstract public void insert_float(float f);

    abstract public double extract_double() throws BAD_OPERATION;

    abstract public void insert_double(double d);

    abstract public boolean extract_boolean() throws BAD_OPERATION;

    abstract public void insert_boolean(boolean b);

    abstract public char extract_char() throws BAD_OPERATION;

    abstract public void insert_char(char c) throws DATA_CONVERSION;

    abstract public char extract_wchar() throws BAD_OPERATION;

    abstract public void insert_wchar(char c);

    abstract public byte extract_octet() throws BAD_OPERATION;

    abstract public void insert_octet(byte b);

    abstract public Any extract_any() throws BAD_OPERATION;

    abstract public void insert_any(Any a);

    abstract public Object extract_Object() throws BAD_OPERATION;

    abstract public void insert_Object(Object o);

    abstract public java.io.Serializable extract_Value() throws BAD_OPERATION;

    abstract public void insert_Value(java.io.Serializable v);

    abstract public void insert_Value(java.io.Serializable v,TypeCode t)
            throws MARSHAL;

    abstract public void insert_Object(Object o,TypeCode t)
            throws BAD_PARAM;

    abstract public String extract_string() throws BAD_OPERATION;

    abstract public void insert_string(String s) throws DATA_CONVERSION, MARSHAL;

    abstract public String extract_wstring() throws BAD_OPERATION;

    abstract public void insert_wstring(String s) throws MARSHAL;

    abstract public TypeCode extract_TypeCode() throws BAD_OPERATION;

    abstract public void insert_TypeCode(TypeCode t);

    @Deprecated
    public Principal extract_Principal() throws BAD_OPERATION{
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public void insert_Principal(Principal p){
        throw new NO_IMPLEMENT();
    }
    ///////////////////////////////////////////////////////////////////////////
    // insertion/extraction of streamables

    public Streamable extract_Streamable()
            throws BAD_INV_ORDER{
        throw new NO_IMPLEMENT();
    }

    public void insert_Streamable(Streamable s){
        throw new NO_IMPLEMENT();
    }

    public java.math.BigDecimal extract_fixed(){
        throw new NO_IMPLEMENT();
    }

    public void insert_fixed(java.math.BigDecimal value){
        throw new NO_IMPLEMENT();
    }

    public void insert_fixed(java.math.BigDecimal value,TypeCode type)
            throws BAD_INV_ORDER{
        throw new NO_IMPLEMENT();
    }
}

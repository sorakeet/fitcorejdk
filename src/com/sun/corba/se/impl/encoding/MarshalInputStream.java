/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import org.omg.CORBA.Any;
import org.omg.CORBA.Principal;
import org.omg.CORBA.TypeCode;

public interface MarshalInputStream{
    public boolean read_boolean();

    public char read_char();

    public char read_wchar();

    public byte read_octet();

    public short read_short();

    public short read_ushort();

    public int read_long();

    public int read_ulong();

    public long read_longlong();

    public long read_ulonglong();

    public float read_float();

    public double read_double();

    public String read_string();

    public String read_wstring();

    public void read_boolean_array(boolean[] value,int offset,int length);

    public void read_char_array(char[] value,int offset,int length);

    public void read_wchar_array(char[] value,int offset,int length);

    public void read_octet_array(byte[] value,int offset,int length);

    public void read_short_array(short[] value,int offset,int length);

    public void read_ushort_array(short[] value,int offset,int length);

    public void read_long_array(int[] value,int offset,int length);

    public void read_ulong_array(int[] value,int offset,int length);

    public void read_longlong_array(long[] value,int offset,int length);

    public void read_ulonglong_array(long[] value,int offset,int length);

    public void read_float_array(float[] value,int offset,int length);

    public void read_double_array(double[] value,int offset,int length);

    public org.omg.CORBA.Object read_Object();

    public TypeCode read_TypeCode();

    public Any read_any();

    public Principal read_Principal();

    public org.omg.CORBA.Object read_Object(Class stubClass);

    public java.io.Serializable read_value() throws Exception;

    public void consumeEndian();

    // Determines the current byte stream position
    // (also handles fragmented streams)
    public int getPosition();

    // mark/reset from java.io.InputStream
    public void mark(int readAheadLimit);

    public void reset();

    public void performORBVersionSpecificInit();

    public void resetCodeSetConverters();
}

/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public interface DataOutputStream extends org.omg.CORBA.portable.ValueBase{
    void write_any(Any value);

    void write_boolean(boolean value);

    void write_char(char value);

    void write_wchar(char value);

    void write_octet(byte value);

    void write_short(short value);

    void write_ushort(short value);

    void write_long(int value);

    void write_ulong(int value);

    void write_longlong(long value);

    void write_ulonglong(long value);

    void write_float(float value);

    void write_double(double value);
    // write_longdouble not supported by IDL/Java mapping

    void write_string(String value);

    void write_wstring(String value);

    void write_Object(Object value);

    void write_Abstract(java.lang.Object value);

    void write_Value(java.io.Serializable value);

    void write_TypeCode(TypeCode value);

    void write_any_array(Any[] seq,int offset,int length);

    void write_boolean_array(boolean[] seq,int offset,int length);

    void write_char_array(char[] seq,int offset,int length);

    void write_wchar_array(char[] seq,int offset,int length);

    void write_octet_array(byte[] seq,int offset,int length);

    void write_short_array(short[] seq,int offset,int length);

    void write_ushort_array(short[] seq,int offset,int length);

    void write_long_array(int[] seq,int offset,int length);

    void write_ulong_array(int[] seq,int offset,int length);

    void write_ulonglong_array(long[] seq,int offset,int length);

    void write_longlong_array(long[] seq,int offset,int length);

    void write_float_array(float[] seq,int offset,int length);

    void write_double_array(double[] seq,int offset,int length);
} // interface DataOutputStream

/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public interface DataInputStream extends org.omg.CORBA.portable.ValueBase{
    Any read_any();

    boolean read_boolean();

    char read_char();

    char read_wchar();

    byte read_octet();

    short read_short();

    short read_ushort();

    int read_long();

    int read_ulong();

    long read_longlong();

    long read_ulonglong();

    float read_float();

    double read_double();
    // read_longdouble not supported by IDL/Java mapping

    String read_string();

    String read_wstring();

    Object read_Object();

    java.lang.Object read_Abstract();

    java.io.Serializable read_Value();

    TypeCode read_TypeCode();

    void read_any_array(AnySeqHolder seq,int offset,int length);

    void read_boolean_array(BooleanSeqHolder seq,int offset,int length);

    void read_char_array(CharSeqHolder seq,int offset,int length);

    void read_wchar_array(WCharSeqHolder seq,int offset,int length);

    void read_octet_array(OctetSeqHolder seq,int offset,int length);

    void read_short_array(ShortSeqHolder seq,int offset,int length);

    void read_ushort_array(UShortSeqHolder seq,int offset,int length);

    void read_long_array(LongSeqHolder seq,int offset,int length);

    void read_ulong_array(ULongSeqHolder seq,int offset,int length);

    void read_ulonglong_array(ULongLongSeqHolder seq,int offset,int length);

    void read_longlong_array(LongLongSeqHolder seq,int offset,int length);

    void read_float_array(FloatSeqHolder seq,int offset,int length);

    void read_double_array(DoubleSeqHolder seq,int offset,int length);
} // interface DataInputStream

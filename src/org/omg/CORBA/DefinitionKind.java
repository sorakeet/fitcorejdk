/**
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * File: ./org/omg/CORBA/DefinitionKind.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 * By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */
/**
 * File: ./org/omg/CORBA/DefinitionKind.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */
package org.omg.CORBA;

public class DefinitionKind implements org.omg.CORBA.portable.IDLEntity{
    public static final int _dk_none=0,
            _dk_all=1,
            _dk_Attribute=2,
            _dk_Constant=3,
            _dk_Exception=4,
            _dk_Interface=5,
            _dk_Module=6,
            _dk_Operation=7,
            _dk_Typedef=8,
            _dk_Alias=9,
            _dk_Struct=10,
            _dk_Union=11,
            _dk_Enum=12,
            _dk_Primitive=13,
            _dk_String=14,
            _dk_Sequence=15,
            _dk_Array=16,
            _dk_Repository=17,
            _dk_Wstring=18,
            _dk_Fixed=19,
            _dk_Value=20,
            _dk_ValueBox=21,
            _dk_ValueMember=22,
            _dk_Native=23,
            _dk_AbstractInterface=24;
    public static final DefinitionKind dk_none=new DefinitionKind(_dk_none);
    public static final DefinitionKind dk_all=new DefinitionKind(_dk_all);
    public static final DefinitionKind dk_Attribute=new DefinitionKind(_dk_Attribute);
    public static final DefinitionKind dk_Constant=new DefinitionKind(_dk_Constant);
    public static final DefinitionKind dk_Exception=new DefinitionKind(_dk_Exception);
    public static final DefinitionKind dk_Interface=new DefinitionKind(_dk_Interface);
    public static final DefinitionKind dk_Module=new DefinitionKind(_dk_Module);
    public static final DefinitionKind dk_Operation=new DefinitionKind(_dk_Operation);
    public static final DefinitionKind dk_Typedef=new DefinitionKind(_dk_Typedef);
    public static final DefinitionKind dk_Alias=new DefinitionKind(_dk_Alias);
    public static final DefinitionKind dk_Struct=new DefinitionKind(_dk_Struct);
    public static final DefinitionKind dk_Union=new DefinitionKind(_dk_Union);
    public static final DefinitionKind dk_Enum=new DefinitionKind(_dk_Enum);
    public static final DefinitionKind dk_Primitive=new DefinitionKind(_dk_Primitive);
    public static final DefinitionKind dk_String=new DefinitionKind(_dk_String);
    public static final DefinitionKind dk_Sequence=new DefinitionKind(_dk_Sequence);
    public static final DefinitionKind dk_Array=new DefinitionKind(_dk_Array);
    public static final DefinitionKind dk_Repository=new DefinitionKind(_dk_Repository);
    public static final DefinitionKind dk_Wstring=new DefinitionKind(_dk_Wstring);
    public static final DefinitionKind dk_Fixed=new DefinitionKind(_dk_Fixed);
    public static final DefinitionKind dk_Value=new DefinitionKind(_dk_Value);
    public static final DefinitionKind dk_ValueBox=new DefinitionKind(_dk_ValueBox);
    public static final DefinitionKind dk_ValueMember=new DefinitionKind(_dk_ValueMember);
    public static final DefinitionKind dk_Native=new DefinitionKind(_dk_Native);
    public static final DefinitionKind dk_AbstractInterface=new DefinitionKind(_dk_AbstractInterface);
    private int _value;

    protected DefinitionKind(int _value){
        this._value=_value;
    }

    public static DefinitionKind from_int(int i){
        switch(i){
            case _dk_none:
                return dk_none;
            case _dk_all:
                return dk_all;
            case _dk_Attribute:
                return dk_Attribute;
            case _dk_Constant:
                return dk_Constant;
            case _dk_Exception:
                return dk_Exception;
            case _dk_Interface:
                return dk_Interface;
            case _dk_Module:
                return dk_Module;
            case _dk_Operation:
                return dk_Operation;
            case _dk_Typedef:
                return dk_Typedef;
            case _dk_Alias:
                return dk_Alias;
            case _dk_Struct:
                return dk_Struct;
            case _dk_Union:
                return dk_Union;
            case _dk_Enum:
                return dk_Enum;
            case _dk_Primitive:
                return dk_Primitive;
            case _dk_String:
                return dk_String;
            case _dk_Sequence:
                return dk_Sequence;
            case _dk_Array:
                return dk_Array;
            case _dk_Repository:
                return dk_Repository;
            case _dk_Wstring:
                return dk_Wstring;
            case _dk_Fixed:
                return dk_Fixed;
            case _dk_Value:
                return dk_Value;
            case _dk_ValueBox:
                return dk_ValueBox;
            case _dk_ValueMember:
                return dk_ValueMember;
            case _dk_Native:
                return dk_Native;
            default:
                throw new BAD_PARAM();
        }
    }

    public int value(){
        return _value;
    }
}

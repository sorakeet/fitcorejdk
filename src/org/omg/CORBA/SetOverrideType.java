/**
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public class SetOverrideType implements org.omg.CORBA.portable.IDLEntity{
    public static final int _SET_OVERRIDE=0;
    public static final int _ADD_OVERRIDE=1;
    public static final SetOverrideType SET_OVERRIDE=new SetOverrideType(_SET_OVERRIDE);
    public static final SetOverrideType ADD_OVERRIDE=new SetOverrideType(_ADD_OVERRIDE);
    private int _value;

    protected SetOverrideType(int _value){
        this._value=_value;
    }

    public static SetOverrideType from_int(int i){
        switch(i){
            case _SET_OVERRIDE:
                return SET_OVERRIDE;
            case _ADD_OVERRIDE:
                return ADD_OVERRIDE;
            default:
                throw new BAD_PARAM();
        }
    }

    public int value(){
        return _value;
    }
}

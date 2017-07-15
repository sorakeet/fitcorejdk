/**
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class CompletionStatus implements org.omg.CORBA.portable.IDLEntity{
    public static final int _COMPLETED_YES=0,
            _COMPLETED_NO=1,
            _COMPLETED_MAYBE=2;
    public static final CompletionStatus COMPLETED_YES=new CompletionStatus(_COMPLETED_YES);
    public static final CompletionStatus COMPLETED_NO=new CompletionStatus(_COMPLETED_NO);
    public static final CompletionStatus COMPLETED_MAYBE=new CompletionStatus(_COMPLETED_MAYBE);
    private int _value;

    private CompletionStatus(int _value){
        this._value=_value;
    }

    public static CompletionStatus from_int(int i){
        switch(i){
            case _COMPLETED_YES:
                return COMPLETED_YES;
            case _COMPLETED_NO:
                return COMPLETED_NO;
            case _COMPLETED_MAYBE:
                return COMPLETED_MAYBE;
            default:
                throw new BAD_PARAM();
        }
    }

    public int value(){
        return _value;
    }
}

/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import com.sun.corba.se.impl.util.SUNVMCID;

public abstract class SystemException extends RuntimeException{
    public int minor;
    public CompletionStatus completed;

    protected SystemException(String reason,int minor,CompletionStatus completed){
        super(reason);
        this.minor=minor;
        this.completed=completed;
    }

    public String toString(){
        // The fully qualified exception class name
        String result=super.toString();
        // The vmcid part
        int vmcid=minor&0xFFFFF000;
        switch(vmcid){
            case OMGVMCID.value:
                result+="  vmcid: OMG";
                break;
            case SUNVMCID.value:
                result+="  vmcid: SUN";
                break;
            default:
                result+="  vmcid: 0x"+Integer.toHexString(vmcid);
                break;
        }
        // The minor code part
        int mc=minor&0x00000FFF;
        result+="  minor code: "+mc;
        // The completion status part
        switch(completed.value()){
            case CompletionStatus._COMPLETED_YES:
                result+="  completed: Yes";
                break;
            case CompletionStatus._COMPLETED_NO:
                result+="  completed: No";
                break;
            case CompletionStatus._COMPLETED_MAYBE:
            default:
                result+=" completed: Maybe";
                break;
        }
        return result;
    }
}

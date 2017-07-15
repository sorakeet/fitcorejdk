/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class INVALID_ACTIVITY extends SystemException{
    public INVALID_ACTIVITY(){
        this("");
    }

    public INVALID_ACTIVITY(String detailMessage){
        this(detailMessage,0,CompletionStatus.COMPLETED_NO);
    }

    public INVALID_ACTIVITY(String detailMessage,
                            int minorCode,
                            CompletionStatus completionStatus){
        super(detailMessage,minorCode,completionStatus);
    }

    public INVALID_ACTIVITY(int minorCode,
                            CompletionStatus completionStatus){
        this("",minorCode,completionStatus);
    }
}

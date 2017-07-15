/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class REBIND extends SystemException{
    public REBIND(){
        this("");
    }

    public REBIND(String detailMessage){
        this(detailMessage,0,CompletionStatus.COMPLETED_NO);
    }

    public REBIND(String detailMessage,
                  int minorCode,
                  CompletionStatus completionStatus){
        super(detailMessage,minorCode,completionStatus);
    }

    public REBIND(int minorCode,
                  CompletionStatus completionStatus){
        this("",minorCode,completionStatus);
    }
}

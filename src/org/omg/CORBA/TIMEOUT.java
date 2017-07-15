/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class TIMEOUT extends SystemException{
    public TIMEOUT(){
        this("");
    }

    public TIMEOUT(String detailMessage){
        this(detailMessage,0,CompletionStatus.COMPLETED_NO);
    }

    public TIMEOUT(String detailMessage,
                   int minorCode,
                   CompletionStatus completionStatus){
        super(detailMessage,minorCode,completionStatus);
    }

    public TIMEOUT(int minorCode,
                   CompletionStatus completionStatus){
        this("",minorCode,completionStatus);
    }
}

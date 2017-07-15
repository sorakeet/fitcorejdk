/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class BAD_QOS extends SystemException{
    public BAD_QOS(){
        this("");
    }

    public BAD_QOS(String detailMessage){
        this(detailMessage,0,CompletionStatus.COMPLETED_NO);
    }

    public BAD_QOS(String detailMessage,
                   int minorCode,
                   CompletionStatus completionStatus){
        super(detailMessage,minorCode,completionStatus);
    }

    public BAD_QOS(int minorCode,
                   CompletionStatus completionStatus){
        this("",minorCode,completionStatus);
    }
}

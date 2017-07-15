/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class CODESET_INCOMPATIBLE extends SystemException{
    public CODESET_INCOMPATIBLE(){
        this("");
    }

    public CODESET_INCOMPATIBLE(String detailMessage){
        this(detailMessage,0,CompletionStatus.COMPLETED_NO);
    }

    public CODESET_INCOMPATIBLE(String detailMessage,
                                int minorCode,
                                CompletionStatus completionStatus){
        super(detailMessage,minorCode,completionStatus);
    }

    public CODESET_INCOMPATIBLE(int minorCode,
                                CompletionStatus completionStatus){
        this("",minorCode,completionStatus);
    }
}

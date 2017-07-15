/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class SizeLimitExceededException extends LimitExceededException{
    private static final long serialVersionUID=7129289564879168579L;

    public SizeLimitExceededException(){
        super();
    }

    public SizeLimitExceededException(String explanation){
        super(explanation);
    }
}

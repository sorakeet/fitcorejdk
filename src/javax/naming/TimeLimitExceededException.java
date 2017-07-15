/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class TimeLimitExceededException extends LimitExceededException{
    private static final long serialVersionUID=-3597009011385034696L;

    public TimeLimitExceededException(){
        super();
    }

    public TimeLimitExceededException(String explanation){
        super(explanation);
    }
}

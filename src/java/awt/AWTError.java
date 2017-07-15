/**
 * Copyright (c) 1995, 1997, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class AWTError extends Error{
    private static final long serialVersionUID=-1819846354050686206L;

    public AWTError(String msg){
        super(msg);
    }
}

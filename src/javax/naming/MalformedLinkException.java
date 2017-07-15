/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class MalformedLinkException extends LinkException{
    private static final long serialVersionUID=-3066740437737830242L;

    public MalformedLinkException(String explanation){
        super(explanation);
    }

    public MalformedLinkException(){
        super();
    }
}

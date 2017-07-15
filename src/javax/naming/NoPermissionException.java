/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class NoPermissionException extends NamingSecurityException{
    private static final long serialVersionUID=8395332708699751775L;

    public NoPermissionException(String explanation){
        super(explanation);
    }

    public NoPermissionException(){
        super();
    }
}

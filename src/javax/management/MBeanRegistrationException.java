/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class MBeanRegistrationException extends MBeanException{
    private static final long serialVersionUID=4482382455277067805L;

    public MBeanRegistrationException(Exception e){
        super(e);
    }

    public MBeanRegistrationException(Exception e,String message){
        super(e,message);
    }
}

/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public class OperationsException extends JMException{
    private static final long serialVersionUID=-4967597595580536216L;

    public OperationsException(){
        super();
    }

    public OperationsException(String message){
        super(message);
    }
}

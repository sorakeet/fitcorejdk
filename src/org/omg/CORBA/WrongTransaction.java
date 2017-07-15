/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class WrongTransaction extends UserException{
    public WrongTransaction(){
        super(WrongTransactionHelper.id());
    }

    public WrongTransaction(String reason){
        super(WrongTransactionHelper.id()+"  "+reason);
    }
}

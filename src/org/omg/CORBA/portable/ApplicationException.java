/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.portable;

public class ApplicationException extends Exception{
    private String id;
    private InputStream ins;

    public ApplicationException(String id,
                                InputStream ins){
        this.id=id;
        this.ins=ins;
    }

    public String getId(){
        return id;
    }

    public InputStream getInputStream(){
        return ins;
    }
}

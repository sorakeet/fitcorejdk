/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import java.io.Serializable;

public final class Holder<T> implements Serializable{
    private static final long serialVersionUID=2623699057546497185L;
    public T value;

    public Holder(){
    }

    public Holder(T value){
        this.value=value;
    }
}

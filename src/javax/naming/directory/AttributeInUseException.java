/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class AttributeInUseException extends NamingException{
    private static final long serialVersionUID=4437710305529322564L;

    public AttributeInUseException(String explanation){
        super(explanation);
    }

    public AttributeInUseException(){
        super();
    }
}

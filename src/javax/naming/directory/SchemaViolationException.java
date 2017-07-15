/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class SchemaViolationException extends NamingException{
    private static final long serialVersionUID=-3041762429525049663L;

    public SchemaViolationException(){
        super();
    }

    public SchemaViolationException(String explanation){
        super(explanation);
    }
}

/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class ConfigurationException extends NamingException{
    private static final long serialVersionUID=-2535156726228855704L;

    public ConfigurationException(String explanation){
        super(explanation);
    }

    public ConfigurationException(){
        super();
    }
}

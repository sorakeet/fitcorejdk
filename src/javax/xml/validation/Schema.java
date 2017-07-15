/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.validation;

public abstract class Schema{
    protected Schema(){
    }

    public abstract Validator newValidator();

    public abstract ValidatorHandler newValidatorHandler();
}

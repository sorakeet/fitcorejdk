/**
 * Copyright (c) 2004, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.validation;

public abstract class SchemaFactoryLoader{
    protected SchemaFactoryLoader(){
    }

    public abstract SchemaFactory newFactory(String schemaLanguage);
}

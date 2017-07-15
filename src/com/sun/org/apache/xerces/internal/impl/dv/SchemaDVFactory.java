/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.dv;

import com.sun.org.apache.xerces.internal.util.SymbolHash;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;

public abstract class SchemaDVFactory{
    private static final String DEFAULT_FACTORY_CLASS="com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl";

    // can't create a new object of this class
    protected SchemaDVFactory(){
    }

    public static synchronized final SchemaDVFactory getInstance() throws DVFactoryException{
        return getInstance(DEFAULT_FACTORY_CLASS);
    } //getInstance():  SchemaDVFactory

    public static synchronized final SchemaDVFactory getInstance(String factoryClass) throws DVFactoryException{
        try{
            // if the class name is not specified, use the default one
            return (SchemaDVFactory)(ObjectFactory.newInstance(factoryClass,true));
        }catch(ClassCastException e4){
            throw new DVFactoryException("Schema factory class "+factoryClass+" does not extend from SchemaDVFactory.");
        }
    }

    public abstract XSSimpleType getBuiltInType(String name);

    public abstract SymbolHash getBuiltInTypes();

    public abstract XSSimpleType createTypeRestriction(String name,String targetNamespace,
                                                       short finalSet,XSSimpleType base,
                                                       XSObjectList annotations);

    public abstract XSSimpleType createTypeList(String name,String targetNamespace,
                                                short finalSet,XSSimpleType itemType,
                                                XSObjectList annotations);

    public abstract XSSimpleType createTypeUnion(String name,String targetNamespace,
                                                 short finalSet,XSSimpleType[] memberTypes,
                                                 XSObjectList annotations);
}

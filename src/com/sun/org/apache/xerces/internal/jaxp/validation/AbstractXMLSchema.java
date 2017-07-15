/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.jaxp.validation;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;
import java.util.HashMap;

abstract class AbstractXMLSchema extends Schema implements
        XSGrammarPoolContainer{
    private final HashMap fFeatures;
    private final HashMap fProperties;

    public AbstractXMLSchema(){
        fFeatures=new HashMap();
        fProperties=new HashMap();
    }

    /**
     * Schema methods
     */
    public final Validator newValidator(){
        return new ValidatorImpl(this);
    }

    public final ValidatorHandler newValidatorHandler(){
        return new ValidatorHandlerImpl(this);
    }

    public final Boolean getFeature(String featureId){
        return (Boolean)fFeatures.get(featureId);
    }

    public final void setFeature(String featureId,boolean state){
        fFeatures.put(featureId,state?Boolean.TRUE:Boolean.FALSE);
    }

    public final Object getProperty(String propertyId){
        return fProperties.get(propertyId);
    }

    public final void setProperty(String propertyId,Object state){
        fProperties.put(propertyId,state);
    }
} // AbstractXMLSchema

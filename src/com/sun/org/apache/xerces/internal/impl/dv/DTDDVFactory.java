/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import com.sun.org.apache.xerces.internal.impl.dv.dtd.DTDDVFactoryImpl;
import com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl;
import com.sun.org.apache.xerces.internal.utils.ObjectFactory;

import java.util.Map;

public abstract class DTDDVFactory{
    private static final String DEFAULT_FACTORY_CLASS=
            "com.sun.org.apache.xerces.internal.impl.dv.dtd.DTDDVFactoryImpl";
    private static final String XML11_DATATYPE_VALIDATOR_FACTORY=
            "com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl";

    // can't create a new object of this class
    protected DTDDVFactory(){
    }

    public static final DTDDVFactory getInstance() throws DVFactoryException{
        return getInstance(DEFAULT_FACTORY_CLASS);
    }

    public static final DTDDVFactory getInstance(String factoryClass) throws DVFactoryException{
        try{
            if(DEFAULT_FACTORY_CLASS.equals(factoryClass)){
                return new DTDDVFactoryImpl();
            }else if(XML11_DATATYPE_VALIDATOR_FACTORY.equals(factoryClass)){
                return new XML11DTDDVFactoryImpl();
            }else{
                //fall back for compatibility
                return (DTDDVFactory)
                        (ObjectFactory.newInstance(factoryClass,true));
            }
        }catch(ClassCastException e){
            throw new DVFactoryException("DTD factory class "+factoryClass+" does not extend from DTDDVFactory.");
        }
    }

    public abstract DatatypeValidator getBuiltInDV(String name);

    public abstract Map<String,DatatypeValidator> getBuiltInTypes();
}

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
package com.sun.org.apache.xerces.internal.impl.dv.dtd;

import com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XML11DTDDVFactoryImpl extends DTDDVFactoryImpl{
    static Map<String,DatatypeValidator> XML11BUILTINTYPES;

    static{
        Map<String,DatatypeValidator> xml11BuiltInTypes=new HashMap<>();
        xml11BuiltInTypes.put("XML11ID",new XML11IDDatatypeValidator());
        DatatypeValidator dvTemp=new XML11IDREFDatatypeValidator();
        xml11BuiltInTypes.put("XML11IDREF",dvTemp);
        xml11BuiltInTypes.put("XML11IDREFS",new ListDatatypeValidator(dvTemp));
        dvTemp=new XML11NMTOKENDatatypeValidator();
        xml11BuiltInTypes.put("XML11NMTOKEN",dvTemp);
        xml11BuiltInTypes.put("XML11NMTOKENS",new ListDatatypeValidator(dvTemp));
        XML11BUILTINTYPES=Collections.unmodifiableMap(xml11BuiltInTypes);
    } // <clinit>

    @Override
    public DatatypeValidator getBuiltInDV(String name){
        if(XML11BUILTINTYPES.get(name)!=null){
            return XML11BUILTINTYPES.get(name);
        }
        return fBuiltInTypes.get(name);
    }

    @Override
    public Map<String,DatatypeValidator> getBuiltInTypes(){
        final HashMap<String,DatatypeValidator> toReturn=new HashMap<>(fBuiltInTypes);
        toReturn.putAll(XML11BUILTINTYPES);
        return toReturn;
    }
}//XML11DTDDVFactoryImpl

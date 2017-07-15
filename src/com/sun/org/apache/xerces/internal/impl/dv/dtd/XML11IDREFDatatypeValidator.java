/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.dv.dtd;

import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import com.sun.org.apache.xerces.internal.util.XML11Char;

public class XML11IDREFDatatypeValidator extends IDREFDatatypeValidator{
    // construct an IDREF datatype validator
    public XML11IDREFDatatypeValidator(){
        super();
    }

    public void validate(String content,ValidationContext context) throws InvalidDatatypeValueException{
        //Check if is valid key-[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
        if(context.useNamespaces()){
            if(!XML11Char.isXML11ValidNCName(content)){
                throw new InvalidDatatypeValueException("IDREFInvalidWithNamespaces",new Object[]{content});
            }
        }else{
            if(!XML11Char.isXML11ValidName(content)){
                throw new InvalidDatatypeValueException("IDREFInvalid",new Object[]{content});
            }
        }
        context.addIdRef(content);
    }
}

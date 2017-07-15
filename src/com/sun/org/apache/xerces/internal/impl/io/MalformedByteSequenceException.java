/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
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
 * Copyright 2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.io;

import com.sun.org.apache.xerces.internal.util.MessageFormatter;

import java.io.CharConversionException;
import java.util.Locale;

public class MalformedByteSequenceException extends CharConversionException{
    static final long serialVersionUID=8436382245048328739L;
    //
    // Data
    //
    private MessageFormatter fFormatter;
    private Locale fLocale;
    private String fDomain;
    private String fKey;
    private Object[] fArguments;
    private String fMessage;
    //
    // Constructors
    //

    public MalformedByteSequenceException(MessageFormatter formatter,
                                          Locale locale,String domain,String key,Object[] arguments){
        fFormatter=formatter;
        fLocale=locale;
        fDomain=domain;
        fKey=key;
        fArguments=arguments;
    } // <init>(MessageFormatter, Locale, String, String, Object[])
    //
    // Public methods
    //

    public String getDomain(){
        return fDomain;
    } // getDomain

    public String getKey(){
        return fKey;
    } // getKey()

    public Object[] getArguments(){
        return fArguments;
    } // getArguments();

    public String getMessage(){
        if(fMessage==null){
            fMessage=fFormatter.formatMessage(fLocale,fKey,fArguments);
            // The references to the message formatter and locale
            // aren't needed anymore so null them.
            fFormatter=null;
            fLocale=null;
        }
        return fMessage;
    } // getMessage()
} // MalformedByteSequenceException

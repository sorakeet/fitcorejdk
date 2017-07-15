/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni;

public class XNIException
        extends RuntimeException{
    static final long serialVersionUID=9019819772686063775L;
    //
    // Data
    //
    private Exception fException;
    //
    // Constructors
    //

    public XNIException(String message){
        super(message);
    } // <init>(String)

    public XNIException(Exception exception){
        super(exception.getMessage());
        fException=exception;
    } // <init>(Exception)

    public XNIException(String message,Exception exception){
        super(message);
        fException=exception;
    } // <init>(Exception,String)
    //
    // Public methods
    //

    public Exception getException(){
        return fException;
    } // getException():Exception

    public Throwable getCause(){
        return fException;
    }
} // class QName

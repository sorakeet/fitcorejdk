/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
// CatalogException.java - Catalog exception
/**
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
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
package com.sun.org.apache.xml.internal.resolver;

public class CatalogException extends Exception{
    public static final int WRAPPER=1;
    public static final int INVALID_ENTRY=2;
    public static final int INVALID_ENTRY_TYPE=3;
    public static final int NO_XML_PARSER=4;
    public static final int UNKNOWN_FORMAT=5;
    public static final int UNPARSEABLE=6;
    public static final int PARSE_FAILED=7;
    public static final int UNENDED_COMMENT=8;
    private Exception exception=null;
    private int exceptionType=0;

    public CatalogException(int type,String message){
        super(message);
        this.exceptionType=type;
        this.exception=null;
    }

    public CatalogException(int type){
        super("Catalog Exception "+type);
        this.exceptionType=type;
        this.exception=null;
    }

    public CatalogException(Exception e){
        super();
        this.exceptionType=WRAPPER;
        this.exception=e;
    }

    public CatalogException(String message,Exception e){
        super(message);
        this.exceptionType=WRAPPER;
        this.exception=e;
    }

    public String getMessage(){
        String message=super.getMessage();
        if(message==null&&exception!=null){
            return exception.getMessage();
        }else{
            return message;
        }
    }

    public String toString(){
        if(exception!=null){
            return exception.toString();
        }else{
            return super.toString();
        }
    }

    public Exception getException(){
        return exception;
    }

    public int getExceptionType(){
        return exceptionType;
    }
}

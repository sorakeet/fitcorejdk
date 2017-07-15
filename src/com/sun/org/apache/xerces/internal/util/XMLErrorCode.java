/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004,2005 The Apache Software Foundation.
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
 * Copyright 2004,2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

final class XMLErrorCode{
    //
    // Data
    //
    private String fDomain;
    private String fKey;

    public XMLErrorCode(String domain,String key){
        fDomain=domain;
        fKey=key;
    }

    public void setValues(String domain,String key){
        fDomain=domain;
        fKey=key;
    }

    public int hashCode(){
        return fDomain.hashCode()+fKey.hashCode();
    }

    public boolean equals(Object obj){
        if(!(obj instanceof XMLErrorCode))
            return false;
        XMLErrorCode err=(XMLErrorCode)obj;
        return (fDomain.equals(err.fDomain)&&fKey.equals(err.fKey));
    }
}

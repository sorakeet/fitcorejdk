/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.utils.resolver;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import org.w3c.dom.Attr;

public class ResourceResolverException extends XMLSecurityException{
    private static final long serialVersionUID=1L;
    private Attr uri=null;
    private String baseURI=null;

    public ResourceResolverException(String msgID,Attr uri,String baseURI){
        super(msgID);
        this.uri=uri;
        this.baseURI=baseURI;
    }

    public ResourceResolverException(String msgID,Object exArgs[],Attr uri,
                                     String baseURI){
        super(msgID,exArgs);
        this.uri=uri;
        this.baseURI=baseURI;
    }

    public ResourceResolverException(String msgID,Exception originalException,
                                     Attr uri,String baseURI){
        super(msgID,originalException);
        this.uri=uri;
        this.baseURI=baseURI;
    }

    public ResourceResolverException(String msgID,Object exArgs[],
                                     Exception originalException,Attr uri,
                                     String baseURI){
        super(msgID,exArgs,originalException);
        this.uri=uri;
        this.baseURI=baseURI;
    }

    public Attr getURI(){
        return this.uri;
    }

    public void setURI(Attr uri){
        this.uri=uri;
    }

    public void setbaseURI(String baseURI){
        this.baseURI=baseURI;
    }

    public String getbaseURI(){
        return this.baseURI;
    }
}

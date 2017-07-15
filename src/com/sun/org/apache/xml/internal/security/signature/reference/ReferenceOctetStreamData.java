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
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * $Id$
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
/** Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 */
/**
 * $Id$
 */
package com.sun.org.apache.xml.internal.security.signature.reference;

import java.io.InputStream;

public class ReferenceOctetStreamData implements ReferenceData{
    private InputStream octetStream;
    private String uri;
    private String mimeType;

    public ReferenceOctetStreamData(InputStream octetStream){
        if(octetStream==null){
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream=octetStream;
    }

    public ReferenceOctetStreamData(InputStream octetStream,String uri,
                                    String mimeType){
        if(octetStream==null){
            throw new NullPointerException("octetStream is null");
        }
        this.octetStream=octetStream;
        this.uri=uri;
        this.mimeType=mimeType;
    }

    public InputStream getOctetStream(){
        return octetStream;
    }

    public String getURI(){
        return uri;
    }

    public String getMimeType(){
        return mimeType;
    }
}

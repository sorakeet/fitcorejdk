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
package com.sun.org.apache.xml.internal.security.signature;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ObjectContainer extends SignatureElementProxy{
    public ObjectContainer(Document doc){
        super(doc);
    }

    public ObjectContainer(Element element,String baseURI) throws XMLSecurityException{
        super(element,baseURI);
    }

    public String getId(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_ID);
    }

    public void setId(String Id){
        if(Id!=null){
            this.constructionElement.setAttributeNS(null,Constants._ATT_ID,Id);
            this.constructionElement.setIdAttributeNS(null,Constants._ATT_ID,true);
        }
    }

    public String getMimeType(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_MIMETYPE);
    }

    public void setMimeType(String MimeType){
        if(MimeType!=null){
            this.constructionElement.setAttributeNS(null,Constants._ATT_MIMETYPE,MimeType);
        }
    }

    public String getEncoding(){
        return this.constructionElement.getAttributeNS(null,Constants._ATT_ENCODING);
    }

    public void setEncoding(String Encoding){
        if(Encoding!=null){
            this.constructionElement.setAttributeNS(null,Constants._ATT_ENCODING,Encoding);
        }
    }

    public Node appendChild(Node node){
        return this.constructionElement.appendChild(node);
    }

    public String getBaseLocalName(){
        return Constants._TAG_OBJECT;
    }
}

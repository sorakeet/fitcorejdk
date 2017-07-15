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
package com.sun.org.apache.xml.internal.security.keys.content;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.content.keyvalues.DSAKeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.keyvalues.RSAKeyValue;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.PublicKey;

public class KeyValue extends SignatureElementProxy implements KeyInfoContent{
    public KeyValue(Document doc,DSAKeyValue dsaKeyValue){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.constructionElement.appendChild(dsaKeyValue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyValue(Document doc,RSAKeyValue rsaKeyValue){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.constructionElement.appendChild(rsaKeyValue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyValue(Document doc,Element unknownKeyValue){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        this.constructionElement.appendChild(unknownKeyValue);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyValue(Document doc,PublicKey pk){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        if(pk instanceof java.security.interfaces.DSAPublicKey){
            DSAKeyValue dsa=new DSAKeyValue(this.doc,pk);
            this.constructionElement.appendChild(dsa.getElement());
            XMLUtils.addReturnToElement(this.constructionElement);
        }else if(pk instanceof java.security.interfaces.RSAPublicKey){
            RSAKeyValue rsa=new RSAKeyValue(this.doc,pk);
            this.constructionElement.appendChild(rsa.getElement());
            XMLUtils.addReturnToElement(this.constructionElement);
        }
    }

    public KeyValue(Element element,String BaseURI) throws XMLSecurityException{
        super(element,BaseURI);
    }

    public PublicKey getPublicKey() throws XMLSecurityException{
        Element rsa=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_RSAKEYVALUE,0);
        if(rsa!=null){
            RSAKeyValue kv=new RSAKeyValue(rsa,this.baseURI);
            return kv.getPublicKey();
        }
        Element dsa=
                XMLUtils.selectDsNode(
                        this.constructionElement.getFirstChild(),Constants._TAG_DSAKEYVALUE,0);
        if(dsa!=null){
            DSAKeyValue kv=new DSAKeyValue(dsa,this.baseURI);
            return kv.getPublicKey();
        }
        return null;
    }

    public String getBaseLocalName(){
        return Constants._TAG_KEYVALUE;
    }
}

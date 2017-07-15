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
package com.sun.org.apache.xml.internal.security.keys.content.x509;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

public class XMLX509IssuerSerial extends SignatureElementProxy implements XMLX509DataContent{
    private static java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(XMLX509IssuerSerial.class.getName());

    public XMLX509IssuerSerial(Element element,String baseURI) throws XMLSecurityException{
        super(element,baseURI);
    }

    public XMLX509IssuerSerial(Document doc,String x509IssuerName,String x509SerialNumber){
        this(doc,x509IssuerName,new BigInteger(x509SerialNumber));
    }

    public XMLX509IssuerSerial(Document doc,String x509IssuerName,BigInteger x509SerialNumber){
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
        addTextElement(x509IssuerName,Constants._TAG_X509ISSUERNAME);
        addTextElement(x509SerialNumber.toString(),Constants._TAG_X509SERIALNUMBER);
    }

    public XMLX509IssuerSerial(Document doc,String x509IssuerName,int x509SerialNumber){
        this(doc,x509IssuerName,new BigInteger(Integer.toString(x509SerialNumber)));
    }

    public XMLX509IssuerSerial(Document doc,X509Certificate x509certificate){
        this(
                doc,
                x509certificate.getIssuerX500Principal().getName(),
                x509certificate.getSerialNumber()
        );
    }

    public int getSerialNumberInteger(){
        return this.getSerialNumber().intValue();
    }

    public BigInteger getSerialNumber(){
        String text=
                this.getTextFromChildElement(Constants._TAG_X509SERIALNUMBER,Constants.SignatureSpecNS);
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"X509SerialNumber text: "+text);
        }
        return new BigInteger(text);
    }

    public int hashCode(){
        int result=17;
        result=31*result+getSerialNumber().hashCode();
        result=31*result+getIssuerName().hashCode();
        return result;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof XMLX509IssuerSerial)){
            return false;
        }
        XMLX509IssuerSerial other=(XMLX509IssuerSerial)obj;
        return this.getSerialNumber().equals(other.getSerialNumber())
                &&this.getIssuerName().equals(other.getIssuerName());
    }

    public String getIssuerName(){
        return RFC2253Parser.normalize(
                this.getTextFromChildElement(Constants._TAG_X509ISSUERNAME,Constants.SignatureSpecNS)
        );
    }

    public String getBaseLocalName(){
        return Constants._TAG_X509ISSUERSERIAL;
    }
}

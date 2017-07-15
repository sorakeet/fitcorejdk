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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.cert.X509Certificate;

public class XMLX509SubjectName extends SignatureElementProxy implements XMLX509DataContent{
    public XMLX509SubjectName(Element element,String BaseURI)
            throws XMLSecurityException{
        super(element,BaseURI);
    }

    public XMLX509SubjectName(Document doc,X509Certificate x509certificate){
        this(doc,x509certificate.getSubjectX500Principal().getName());
    }

    public XMLX509SubjectName(Document doc,String X509SubjectNameString){
        super(doc);
        this.addText(X509SubjectNameString);
    }

    public int hashCode(){
        int result=17;
        result=31*result+this.getSubjectName().hashCode();
        return result;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof XMLX509SubjectName)){
            return false;
        }
        XMLX509SubjectName other=(XMLX509SubjectName)obj;
        String otherSubject=other.getSubjectName();
        String thisSubject=this.getSubjectName();
        return thisSubject.equals(otherSubject);
    }

    public String getSubjectName(){
        return RFC2253Parser.normalize(this.getTextFromTextChild());
    }

    public String getBaseLocalName(){
        return Constants._TAG_X509SUBJECTNAME;
    }
}
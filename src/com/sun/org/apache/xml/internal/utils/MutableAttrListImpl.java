/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: MutableAttrListImpl.java,v 1.2.4.1 2005/09/15 08:15:47 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: MutableAttrListImpl.java,v 1.2.4.1 2005/09/15 08:15:47 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.io.Serializable;

public class MutableAttrListImpl extends AttributesImpl
        implements Serializable{
    static final long serialVersionUID=6289452013442934470L;

    public MutableAttrListImpl(){
        super();
    }

    public MutableAttrListImpl(Attributes atts){
        super(atts);
    }

    public void addAttributes(Attributes atts){
        int nAtts=atts.getLength();
        for(int i=0;i<nAtts;i++){
            String uri=atts.getURI(i);
            if(null==uri)
                uri="";
            String localName=atts.getLocalName(i);
            String qname=atts.getQName(i);
            int index=this.getIndex(uri,localName);
            // System.out.println("MutableAttrListImpl#addAttributes: "+uri+":"+localName+", "+index+", "+atts.getQName(i)+", "+this);
            if(index>=0)
                this.setAttribute(index,uri,localName,qname,atts.getType(i),
                        atts.getValue(i));
            else
                addAttribute(uri,localName,qname,atts.getType(i),
                        atts.getValue(i));
        }
    }

    public void addAttribute(String uri,String localName,String qName,
                             String type,String value){
        if(null==uri)
            uri="";
        // getIndex(qName) seems to be more reliable than getIndex(uri, localName),
        // in the case of the xmlns attribute anyway.
        int index=this.getIndex(qName);
        // int index = this.getIndex(uri, localName);
        // System.out.println("MutableAttrListImpl#addAttribute: "+uri+":"+localName+", "+index+", "+qName+", "+this);
        if(index>=0)
            this.setAttribute(index,uri,localName,qName,type,value);
        else
            super.addAttribute(uri,localName,qName,type,value);
    }

    public boolean contains(String name){
        return getValue(name)!=null;
    }
}
// end of MutableAttrListImpl.java

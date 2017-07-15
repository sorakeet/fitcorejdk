/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription;

public class XSDDescription extends XMLResourceIdentifierImpl
        implements XMLSchemaDescription{
    // used to indicate what triggered the call
    public final static short CONTEXT_INITIALIZE=-1;
    public final static short CONTEXT_INCLUDE=0;
    public final static short CONTEXT_REDEFINE=1;
    public final static short CONTEXT_IMPORT=2;
    public final static short CONTEXT_PREPARSE=3;
    public final static short CONTEXT_INSTANCE=4;
    public final static short CONTEXT_ELEMENT=5;
    public final static short CONTEXT_ATTRIBUTE=6;
    public final static short CONTEXT_XSITYPE=7;
    // REVISIT: write description of these fields
    protected short fContextType;
    protected String[] fLocationHints;
    protected QName fTriggeringComponent;
    protected QName fEnclosedElementName;
    protected XMLAttributes fAttributes;

    public String getGrammarType(){
        return XMLGrammarDescription.XML_SCHEMA;
    }

    public short getContextType(){
        return fContextType;
    }

    public String getTargetNamespace(){
        return fNamespace;
    }

    public String[] getLocationHints(){
        return fLocationHints;
    }

    public QName getTriggeringComponent(){
        return fTriggeringComponent;
    }

    public QName getEnclosingElementName(){
        return fEnclosedElementName;
    }

    public XMLAttributes getAttributes(){
        return fAttributes;
    }

    public void setAttributes(XMLAttributes attributes){
        fAttributes=attributes;
    }

    public void setEnclosingElementName(QName enclosedElementName){
        fEnclosedElementName=enclosedElementName;
    }

    public void setTriggeringComponent(QName triggeringComponent){
        fTriggeringComponent=triggeringComponent;
    }

    public void setLocationHints(String[] locationHints){
        int length=locationHints.length;
        fLocationHints=new String[length];
        System.arraycopy(locationHints,0,fLocationHints,0,length);
        //fLocationHints = locationHints ;
    }

    public void setTargetNamespace(String targetNamespace){
        fNamespace=targetNamespace;
    }

    public void setContextType(short contextType){
        fContextType=contextType;
    }

    public boolean fromInstance(){
        return fContextType==CONTEXT_ATTRIBUTE||
                fContextType==CONTEXT_ELEMENT||
                fContextType==CONTEXT_INSTANCE||
                fContextType==CONTEXT_XSITYPE;
    }

    public boolean isExternal(){
        return fContextType==CONTEXT_INCLUDE||
                fContextType==CONTEXT_REDEFINE||
                fContextType==CONTEXT_IMPORT||
                fContextType==CONTEXT_ELEMENT||
                fContextType==CONTEXT_ATTRIBUTE||
                fContextType==CONTEXT_XSITYPE;
    }

    public boolean equals(Object descObj){
        if(!(descObj instanceof XMLSchemaDescription)) return false;
        XMLSchemaDescription desc=(XMLSchemaDescription)descObj;
        if(fNamespace!=null)
            return fNamespace.equals(desc.getTargetNamespace());
        else // fNamespace == null
            return desc.getTargetNamespace()==null;
    }

    public int hashCode(){
        return (fNamespace==null)?0:fNamespace.hashCode();
    }

    public void reset(){
        super.clear();
        fContextType=CONTEXT_INITIALIZE;
        fLocationHints=null;
        fTriggeringComponent=null;
        fEnclosedElementName=null;
        fAttributes=null;
    }

    public XSDDescription makeClone(){
        XSDDescription desc=new XSDDescription();
        desc.fAttributes=this.fAttributes;
        desc.fBaseSystemId=this.fBaseSystemId;
        desc.fContextType=this.fContextType;
        desc.fEnclosedElementName=this.fEnclosedElementName;
        desc.fExpandedSystemId=this.fExpandedSystemId;
        desc.fLiteralSystemId=this.fLiteralSystemId;
        desc.fLocationHints=this.fLocationHints;
        desc.fPublicId=this.fPublicId;
        desc.fNamespace=this.fNamespace;
        desc.fTriggeringComponent=this.fTriggeringComponent;
        return desc;
    }
} // XSDDescription

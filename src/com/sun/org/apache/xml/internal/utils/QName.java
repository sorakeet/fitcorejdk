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
 * $Id: QName.java,v 1.2.4.1 2005/09/15 08:15:52 suresh_emailid Exp $
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
 * $Id: QName.java,v 1.2.4.1 2005/09/15 08:15:52 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.w3c.dom.Element;

import java.util.Stack;
import java.util.StringTokenizer;

public class QName implements java.io.Serializable{
    public static final String S_XMLNAMESPACEURI=
            "http://www.w3.org/XML/1998/namespace";
    static final long serialVersionUID=467434581652829920L;
    protected String _localName;
    protected String _namespaceURI;
    protected String _prefix;
    private int m_hashCode;

    public QName(){
    }

    public QName(String namespaceURI,String localName){
        this(namespaceURI,localName,false);
    }

    public QName(String namespaceURI,String localName,boolean validate){
        // This check was already here.  So, for now, I will not add it to the validation
        // that is done when the validate parameter is true.
        if(localName==null)
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                    XMLErrorResources.ER_ARG_LOCALNAME_NULL,null)); //"Argument 'localName' is null");
        if(validate){
            if(!XML11Char.isXML11ValidNCName(localName)){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_LOCALNAME_INVALID,null)); //"Argument 'localName' not a valid NCName");
            }
        }
        _namespaceURI=namespaceURI;
        _localName=localName;
        m_hashCode=toString().hashCode();
    }

    public QName(String namespaceURI,String prefix,String localName){
        this(namespaceURI,prefix,localName,false);
    }

    public QName(String namespaceURI,String prefix,String localName,boolean validate){
        // This check was already here.  So, for now, I will not add it to the validation
        // that is done when the validate parameter is true.
        if(localName==null)
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                    XMLErrorResources.ER_ARG_LOCALNAME_NULL,null)); //"Argument 'localName' is null");
        if(validate){
            if(!XML11Char.isXML11ValidNCName(localName)){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_LOCALNAME_INVALID,null)); //"Argument 'localName' not a valid NCName");
            }
            if((null!=prefix)&&(!XML11Char.isXML11ValidNCName(prefix))){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_PREFIX_INVALID,null)); //"Argument 'prefix' not a valid NCName");
            }
        }
        _namespaceURI=namespaceURI;
        _prefix=prefix;
        _localName=localName;
        m_hashCode=toString().hashCode();
    }

    public QName(String localName){
        this(localName,false);
    }

    public QName(String localName,boolean validate){
        // This check was already here.  So, for now, I will not add it to the validation
        // that is done when the validate parameter is true.
        if(localName==null)
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                    XMLErrorResources.ER_ARG_LOCALNAME_NULL,null)); //"Argument 'localName' is null");
        if(validate){
            if(!XML11Char.isXML11ValidNCName(localName)){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_LOCALNAME_INVALID,null)); //"Argument 'localName' not a valid NCName");
            }
        }
        _namespaceURI=null;
        _localName=localName;
        m_hashCode=toString().hashCode();
    }

    public QName(String qname,Stack namespaces){
        this(qname,namespaces,false);
    }

    public QName(String qname,Stack namespaces,boolean validate){
        String namespace=null;
        String prefix=null;
        int indexOfNSSep=qname.indexOf(':');
        if(indexOfNSSep>0){
            prefix=qname.substring(0,indexOfNSSep);
            if(prefix.equals("xml")){
                namespace=S_XMLNAMESPACEURI;
            }
            // Do we want this?
            else if(prefix.equals("xmlns")){
                return;
            }else{
                int depth=namespaces.size();
                for(int i=depth-1;i>=0;i--){
                    NameSpace ns=(NameSpace)namespaces.elementAt(i);
                    while(null!=ns){
                        if((null!=ns.m_prefix)&&prefix.equals(ns.m_prefix)){
                            namespace=ns.m_uri;
                            i=-1;
                            break;
                        }
                        ns=ns.m_next;
                    }
                }
            }
            if(null==namespace){
                throw new RuntimeException(
                        XMLMessages.createXMLMessage(
                                XMLErrorResources.ER_PREFIX_MUST_RESOLVE,
                                new Object[]{prefix}));  //"Prefix must resolve to a namespace: "+prefix);
            }
        }
        _localName=(indexOfNSSep<0)
                ?qname:qname.substring(indexOfNSSep+1);
        if(validate){
            if((_localName==null)||(!XML11Char.isXML11ValidNCName(_localName))){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_LOCALNAME_INVALID,null)); //"Argument 'localName' not a valid NCName");
            }
        }
        _namespaceURI=namespace;
        _prefix=prefix;
        m_hashCode=toString().hashCode();
    }

    public QName(String qname,Element namespaceContext,
                 PrefixResolver resolver){
        this(qname,namespaceContext,resolver,false);
    }

    public QName(String qname,Element namespaceContext,
                 PrefixResolver resolver,boolean validate){
        _namespaceURI=null;
        int indexOfNSSep=qname.indexOf(':');
        if(indexOfNSSep>0){
            if(null!=namespaceContext){
                String prefix=qname.substring(0,indexOfNSSep);
                _prefix=prefix;
                if(prefix.equals("xml")){
                    _namespaceURI=S_XMLNAMESPACEURI;
                }
                // Do we want this?
                else if(prefix.equals("xmlns")){
                    return;
                }else{
                    _namespaceURI=resolver.getNamespaceForPrefix(prefix,
                            namespaceContext);
                }
                if(null==_namespaceURI){
                    throw new RuntimeException(
                            XMLMessages.createXMLMessage(
                                    XMLErrorResources.ER_PREFIX_MUST_RESOLVE,
                                    new Object[]{prefix}));  //"Prefix must resolve to a namespace: "+prefix);
                }
            }else{
                // TODO: error or warning...
            }
        }
        _localName=(indexOfNSSep<0)
                ?qname:qname.substring(indexOfNSSep+1);
        if(validate){
            if((_localName==null)||(!XML11Char.isXML11ValidNCName(_localName))){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_LOCALNAME_INVALID,null)); //"Argument 'localName' not a valid NCName");
            }
        }
        m_hashCode=toString().hashCode();
    }

    public QName(String qname,PrefixResolver resolver){
        this(qname,resolver,false);
    }

    public QName(String qname,PrefixResolver resolver,boolean validate){
        String prefix=null;
        _namespaceURI=null;
        int indexOfNSSep=qname.indexOf(':');
        if(indexOfNSSep>0){
            prefix=qname.substring(0,indexOfNSSep);
            if(prefix.equals("xml")){
                _namespaceURI=S_XMLNAMESPACEURI;
            }else{
                _namespaceURI=resolver.getNamespaceForPrefix(prefix);
            }
            if(null==_namespaceURI){
                throw new RuntimeException(
                        XMLMessages.createXMLMessage(
                                XMLErrorResources.ER_PREFIX_MUST_RESOLVE,
                                new Object[]{prefix}));  //"Prefix must resolve to a namespace: "+prefix);
            }
            _localName=qname.substring(indexOfNSSep+1);
        }else if(indexOfNSSep==0){
            throw new RuntimeException(
                    XMLMessages.createXMLMessage(
                            XMLErrorResources.ER_NAME_CANT_START_WITH_COLON,
                            null));
        }else{
            _localName=qname;
        }
        if(validate){
            if((_localName==null)||(!XML11Char.isXML11ValidNCName(_localName))){
                throw new IllegalArgumentException(XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_ARG_LOCALNAME_INVALID,null)); //"Argument 'localName' not a valid NCName");
            }
        }
        m_hashCode=toString().hashCode();
        _prefix=prefix;
    }

    public static QName getQNameFromString(String name){
        StringTokenizer tokenizer=new StringTokenizer(name,"{}",false);
        QName qname;
        String s1=tokenizer.nextToken();
        String s2=tokenizer.hasMoreTokens()?tokenizer.nextToken():null;
        if(null==s2)
            qname=new QName(null,s1);
        else
            qname=new QName(s1,s2);
        return qname;
    }

    public static boolean isXMLNSDecl(String attRawName){
        return (attRawName.startsWith("xmlns")
                &&(attRawName.equals("xmlns")
                ||attRawName.startsWith("xmlns:")));
    }

    public static String getPrefixFromXMLNSDecl(String attRawName){
        int index=attRawName.indexOf(':');
        return (index>=0)?attRawName.substring(index+1):"";
    }

    public static String getLocalPart(String qname){
        int index=qname.indexOf(':');
        return (index<0)?qname:qname.substring(index+1);
    }

    public static String getPrefixPart(String qname){
        int index=qname.indexOf(':');
        return (index>=0)?qname.substring(0,index):"";
    }

    public String getPrefix(){
        return _prefix;
    }

    public String toNamespacedString(){
        return (_namespaceURI!=null
                ?("{"+_namespaceURI+"}"+_localName):_localName);
    }

    public String getNamespace(){
        return getNamespaceURI();
    }

    public String getNamespaceURI(){
        return _namespaceURI;
    }

    public String getLocalPart(){
        return getLocalName();
    }

    public String getLocalName(){
        return _localName;
    }

    public int hashCode(){
        return m_hashCode;
    }

    public boolean equals(Object object){
        if(object==this)
            return true;
        if(object instanceof QName){
            QName qname=(QName)object;
            String thisnamespace=getNamespaceURI();
            String thatnamespace=qname.getNamespaceURI();
            return getLocalName().equals(qname.getLocalName())
                    &&(((null!=thisnamespace)&&(null!=thatnamespace))
                    ?thisnamespace.equals(thatnamespace)
                    :((null==thisnamespace)&&(null==thatnamespace)));
        }else
            return false;
    }

    public String toString(){
        return _prefix!=null
                ?(_prefix+":"+_localName)
                :(_namespaceURI!=null
                ?("{"+_namespaceURI+"}"+_localName):_localName);
    }

    public boolean equals(String ns,String localPart){
        String thisnamespace=getNamespaceURI();
        return getLocalName().equals(localPart)
                &&(((null!=thisnamespace)&&(null!=ns))
                ?thisnamespace.equals(ns)
                :((null==thisnamespace)&&(null==ns)));
    }
}

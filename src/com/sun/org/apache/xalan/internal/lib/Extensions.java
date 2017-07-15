/**
 * Copyright (c) 2005, 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: Extensions.java,v 1.2.4.1 2005/09/10 18:53:32 jeffsuttor Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: Extensions.java,v 1.2.4.1 2005/09/10 18:53:32 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xalan.internal.xslt.EnvironmentCheck;
import com.sun.org.apache.xpath.internal.NodeSet;
import com.sun.org.apache.xpath.internal.objects.XBoolean;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

public class Extensions{
    static final String JDK_DEFAULT_DOM="com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";

    private Extensions(){
    }  // Make sure class cannot be instantiated

    public static NodeSet nodeset(ExpressionContext myProcessor,Object rtf){
        String textNodeValue;
        if(rtf instanceof NodeIterator){
            return new NodeSet((NodeIterator)rtf);
        }else{
            if(rtf instanceof String){
                textNodeValue=(String)rtf;
            }else if(rtf instanceof Boolean){
                textNodeValue=new XBoolean(((Boolean)rtf).booleanValue()).str();
            }else if(rtf instanceof Double){
                textNodeValue=new XNumber(((Double)rtf).doubleValue()).str();
            }else{
                textNodeValue=rtf.toString();
            }
            // This no longer will work right since the DTM.
            // Document myDoc = myProcessor.getContextNode().getOwnerDocument();
            Document myDoc=getDocument();
            Text textNode=myDoc.createTextNode(textNodeValue);
            DocumentFragment docFrag=myDoc.createDocumentFragment();
            docFrag.appendChild(textNode);
            return new NodeSet(docFrag);
        }
    }

    private static Document getDocument(){
        try{
            if(System.getSecurityManager()==null){
                return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            }else{
                return DocumentBuilderFactory.newInstance(JDK_DEFAULT_DOM,null).newDocumentBuilder().newDocument();
            }
        }catch(ParserConfigurationException pce){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(pce);
        }
    }

    public static NodeList intersection(NodeList nl1,NodeList nl2){
        return ExsltSets.intersection(nl1,nl2);
    }

    public static NodeList difference(NodeList nl1,NodeList nl2){
        return ExsltSets.difference(nl1,nl2);
    }

    public static NodeList distinct(NodeList nl){
        return ExsltSets.distinct(nl);
    }

    public static boolean hasSameNodes(NodeList nl1,NodeList nl2){
        NodeSet ns1=new NodeSet(nl1);
        NodeSet ns2=new NodeSet(nl2);
        if(ns1.getLength()!=ns2.getLength())
            return false;
        for(int i=0;i<ns1.getLength();i++){
            Node n=ns1.elementAt(i);
            if(!ns2.contains(n))
                return false;
        }
        return true;
    }

    public static XObject evaluate(ExpressionContext myContext,String xpathExpr)
            throws SAXNotSupportedException{
        return ExsltDynamic.evaluate(myContext,xpathExpr);
    }

    public static NodeList tokenize(String toTokenize){
        return tokenize(toTokenize," \t\n\r");
    }

    public static NodeList tokenize(String toTokenize,String delims){
        Document doc=getDocument();
        StringTokenizer lTokenizer=new StringTokenizer(toTokenize,delims);
        NodeSet resultSet=new NodeSet();
        synchronized(doc){
            while(lTokenizer.hasMoreTokens()){
                resultSet.addNode(doc.createTextNode(lTokenizer.nextToken()));
            }
        }
        return resultSet;
    }

    public static Node checkEnvironment(ExpressionContext myContext){
        Document factoryDocument=getDocument();
        Node resultNode=null;
        try{
            // First use reflection to try to load Which, which is a
            //  better version of EnvironmentCheck
            resultNode=checkEnvironmentUsingWhich(myContext,factoryDocument);
            if(null!=resultNode)
                return resultNode;
            // If reflection failed, fallback to our internal EnvironmentCheck
            EnvironmentCheck envChecker=new EnvironmentCheck();
            Map<String,Object> h=envChecker.getEnvironmentHash();
            resultNode=factoryDocument.createElement("checkEnvironmentExtension");
            envChecker.appendEnvironmentReport(resultNode,factoryDocument,h);
            envChecker=null;
        }catch(Exception e){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
        }
        return resultNode;
    }

    private static Node checkEnvironmentUsingWhich(ExpressionContext myContext,
                                                   Document factoryDocument){
        final String WHICH_CLASSNAME="org.apache.env.Which";
        final String WHICH_METHODNAME="which";
        final Class WHICH_METHOD_ARGS[]={Hashtable.class,
                String.class,
                String.class};
        try{
            // Use reflection to try to find xml-commons utility 'Which'
            Class clazz=ObjectFactory.findProviderClass(WHICH_CLASSNAME,true);
            if(null==clazz)
                return null;
            // Fully qualify names since this is the only method they're used in
            java.lang.reflect.Method method=clazz.getMethod(WHICH_METHODNAME,WHICH_METHOD_ARGS);
            Hashtable report=new Hashtable();
            // Call the method with our Hashtable, common options, and ignore return value
            Object[] methodArgs={report,"XmlCommons;Xalan;Xerces;Crimson;Ant",""};
            Object returnValue=method.invoke(null,methodArgs);
            // Create a parent to hold the report and append hash to it
            Node resultNode=factoryDocument.createElement("checkEnvironmentExtension");
            com.sun.org.apache.xml.internal.utils.Hashtree2Node.appendHashToNode(report,"whichReport",
                    resultNode,factoryDocument);
            return resultNode;
        }catch(Throwable t){
            // Simply return null; no need to report error
            return null;
        }
    }
}

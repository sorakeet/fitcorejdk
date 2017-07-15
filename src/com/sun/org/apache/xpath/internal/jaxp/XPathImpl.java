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
// $Id: XPathImpl.java,v 1.2 2005/08/16 22:41:08 jeffsuttor Exp $
package com.sun.org.apache.xpath.internal.jaxp;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xalan.internal.utils.FactoryImpl;
import com.sun.org.apache.xalan.internal.utils.FeatureManager;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xpath.internal.XPath;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

public class XPathImpl implements javax.xml.xpath.XPath{
    private static Document d=null;
    private final FeatureManager featureManager;
    // Private variables
    private XPathVariableResolver variableResolver;
    private XPathFunctionResolver functionResolver;
    private XPathVariableResolver origVariableResolver;
    private XPathFunctionResolver origFunctionResolver;
    private NamespaceContext namespaceContext=null;
    private JAXPPrefixResolver prefixResolver;
    // By default Extension Functions are allowed in XPath Expressions. If
    // Secure Processing Feature is set on XPathFactory then the invocation of
    // extensions function need to throw XPathFunctionException
    private boolean featureSecureProcessing=false;
    private boolean useServiceMechanism=true;

    XPathImpl(XPathVariableResolver vr,XPathFunctionResolver fr){
        this(vr,fr,false,true,new FeatureManager());
    }

    XPathImpl(XPathVariableResolver vr,XPathFunctionResolver fr,
              boolean featureSecureProcessing,boolean useServiceMechanism,
              FeatureManager featureManager){
        this.origVariableResolver=this.variableResolver=vr;
        this.origFunctionResolver=this.functionResolver=fr;
        this.featureSecureProcessing=featureSecureProcessing;
        this.useServiceMechanism=useServiceMechanism;
        this.featureManager=featureManager;
    }    public void setXPathVariableResolver(XPathVariableResolver resolver){
        if(resolver==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"XPathVariableResolver"});
            throw new NullPointerException(fmsg);
        }
        this.variableResolver=resolver;
    }

    public void reset(){
        this.variableResolver=this.origVariableResolver;
        this.functionResolver=this.origFunctionResolver;
        this.namespaceContext=null;
    }    public XPathVariableResolver getXPathVariableResolver(){
        return variableResolver;
    }

    public void setXPathFunctionResolver(XPathFunctionResolver resolver){
        if(resolver==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"XPathFunctionResolver"});
            throw new NullPointerException(fmsg);
        }
        this.functionResolver=resolver;
    }

    public XPathFunctionResolver getXPathFunctionResolver(){
        return functionResolver;
    }

    public void setNamespaceContext(NamespaceContext nsContext){
        if(nsContext==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"NamespaceContext"});
            throw new NullPointerException(fmsg);
        }
        this.namespaceContext=nsContext;
        this.prefixResolver=new JAXPPrefixResolver(nsContext);
    }

    public NamespaceContext getNamespaceContext(){
        return namespaceContext;
    }



    private DocumentBuilder getParser(){
        try{
            // we'd really like to cache those DocumentBuilders, but we can't because:
            // 1. thread safety. parsers are not thread-safe, so at least
            //    we need one instance per a thread.
            // 2. parsers are non-reentrant, so now we are looking at having a
            // pool of parsers.
            // 3. then the class loading issue. The look-up procedure of
            //    DocumentBuilderFactory.newInstance() depends on context class loader
            //    and system properties, which may change during the execution of JVM.
            //
            // so we really have to create a fresh DocumentBuilder every time we need one
            // - KK
            DocumentBuilderFactory dbf=FactoryImpl.getDOMFactory(useServiceMechanism);
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            return dbf.newDocumentBuilder();
        }catch(ParserConfigurationException e){
            // this should never happen with a well-behaving JAXP implementation.
            throw new Error(e);
        }
    }

    private XObject eval(String expression,Object contextItem)
            throws javax.xml.transform.TransformerException{
        XPath xpath=new XPath(expression,
                null,prefixResolver,XPath.SELECT);
        XPathContext xpathSupport=null;
        if(functionResolver!=null){
            JAXPExtensionsProvider jep=new JAXPExtensionsProvider(
                    functionResolver,featureSecureProcessing,featureManager);
            xpathSupport=new XPathContext(jep);
        }else{
            xpathSupport=new XPathContext();
        }
        XObject xobj=null;
        xpathSupport.setVarStack(new JAXPVariableStack(variableResolver));
        // If item is null, then we will create a a Dummy contextNode
        if(contextItem instanceof Node){
            xobj=xpath.execute(xpathSupport,(Node)contextItem,
                    prefixResolver);
        }else{
            xobj=xpath.execute(xpathSupport,DTM.NULL,prefixResolver);
        }
        return xobj;
    }

    public Object evaluate(String expression,Object item,QName returnType)
            throws XPathExpressionException{
        if(expression==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"XPath expression"});
            throw new NullPointerException(fmsg);
        }
        if(returnType==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"returnType"});
            throw new NullPointerException(fmsg);
        }
        // Checking if requested returnType is supported. returnType need to
        // be defined in XPathConstants
        if(!isSupported(returnType)){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE,
                    new Object[]{returnType.toString()});
            throw new IllegalArgumentException(fmsg);
        }
        try{
            XObject resultObject=eval(expression,item);
            return getResultAsType(resultObject,returnType);
        }catch(NullPointerException npe){
            // If VariableResolver returns null Or if we get
            // NullPointerException at this stage for some other reason
            // then we have to reurn XPathException
            throw new XPathExpressionException(npe);
        }catch(javax.xml.transform.TransformerException te){
            Throwable nestedException=te.getException();
            if(nestedException instanceof javax.xml.xpath.XPathFunctionException){
                throw (javax.xml.xpath.XPathFunctionException)nestedException;
            }else{
                // For any other exceptions we need to throw
                // XPathExpressionException ( as per spec )
                throw new XPathExpressionException(te);
            }
        }
    }

    private boolean isSupported(QName returnType){
        if((returnType.equals(XPathConstants.STRING))||
                (returnType.equals(XPathConstants.NUMBER))||
                (returnType.equals(XPathConstants.BOOLEAN))||
                (returnType.equals(XPathConstants.NODE))||
                (returnType.equals(XPathConstants.NODESET))){
            return true;
        }
        return false;
    }

    private Object getResultAsType(XObject resultObject,QName returnType)
            throws javax.xml.transform.TransformerException{
        // XPathConstants.STRING
        if(returnType.equals(XPathConstants.STRING)){
            return resultObject.str();
        }
        // XPathConstants.NUMBER
        if(returnType.equals(XPathConstants.NUMBER)){
            return new Double(resultObject.num());
        }
        // XPathConstants.BOOLEAN
        if(returnType.equals(XPathConstants.BOOLEAN)){
            return new Boolean(resultObject.bool());
        }
        // XPathConstants.NODESET ---ORdered, UNOrdered???
        if(returnType.equals(XPathConstants.NODESET)){
            return resultObject.nodelist();
        }
        // XPathConstants.NODE
        if(returnType.equals(XPathConstants.NODE)){
            NodeIterator ni=resultObject.nodeset();
            //Return the first node, or null
            return ni.nextNode();
        }
        String fmsg=XSLMessages.createXPATHMessage(
                XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE,
                new Object[]{returnType.toString()});
        throw new IllegalArgumentException(fmsg);
    }

    public String evaluate(String expression,Object item)
            throws XPathExpressionException{
        return (String)this.evaluate(expression,item,XPathConstants.STRING);
    }

    public XPathExpression compile(String expression)
            throws XPathExpressionException{
        if(expression==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"XPath expression"});
            throw new NullPointerException(fmsg);
        }
        try{
            XPath xpath=new XPath(expression,null,
                    prefixResolver,XPath.SELECT);
            // Can have errorListener
            XPathExpressionImpl ximpl=new XPathExpressionImpl(xpath,
                    prefixResolver,functionResolver,variableResolver,
                    featureSecureProcessing,useServiceMechanism,featureManager);
            return ximpl;
        }catch(javax.xml.transform.TransformerException te){
            throw new XPathExpressionException(te);
        }
    }

    public Object evaluate(String expression,InputSource source,
                           QName returnType) throws XPathExpressionException{
        // Checking validity of different parameters
        if(source==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"source"});
            throw new NullPointerException(fmsg);
        }
        if(expression==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"XPath expression"});
            throw new NullPointerException(fmsg);
        }
        if(returnType==null){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                    new Object[]{"returnType"});
            throw new NullPointerException(fmsg);
        }
        //Checking if requested returnType is supported.
        //returnType need to be defined in XPathConstants
        if(!isSupported(returnType)){
            String fmsg=XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_UNSUPPORTED_RETURN_TYPE,
                    new Object[]{returnType.toString()});
            throw new IllegalArgumentException(fmsg);
        }
        try{
            Document document=getParser().parse(source);
            XObject resultObject=eval(expression,document);
            return getResultAsType(resultObject,returnType);
        }catch(SAXException e){
            throw new XPathExpressionException(e);
        }catch(IOException e){
            throw new XPathExpressionException(e);
        }catch(javax.xml.transform.TransformerException te){
            Throwable nestedException=te.getException();
            if(nestedException instanceof javax.xml.xpath.XPathFunctionException){
                throw (javax.xml.xpath.XPathFunctionException)nestedException;
            }else{
                throw new XPathExpressionException(te);
            }
        }
    }

    public String evaluate(String expression,InputSource source)
            throws XPathExpressionException{
        return (String)this.evaluate(expression,source,XPathConstants.STRING);
    }


}

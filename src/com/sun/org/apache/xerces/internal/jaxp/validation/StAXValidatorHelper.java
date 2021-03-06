/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xerces.internal.jaxp.validation;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXResult;
import java.io.IOException;

public final class StAXValidatorHelper implements ValidatorHelper{
    private static final String DEFAULT_TRANSFORMER_IMPL="com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
    private XMLSchemaValidatorComponentManager fComponentManager;
    private Transformer identityTransformer1=null;
    private TransformerHandler identityTransformer2=null;
    private ValidatorHandlerImpl handler=null;

    public StAXValidatorHelper(XMLSchemaValidatorComponentManager componentManager){
        fComponentManager=componentManager;
    }

    public void validate(Source source,Result result)
            throws SAXException, IOException{
        if(result==null||result instanceof StAXResult){
            if(identityTransformer1==null){
                try{
                    SAXTransformerFactory tf=fComponentManager.getFeature(Constants.ORACLE_FEATURE_SERVICE_MECHANISM)?
                            (SAXTransformerFactory)SAXTransformerFactory.newInstance()
                            :(SAXTransformerFactory)TransformerFactory.newInstance(DEFAULT_TRANSFORMER_IMPL,StAXValidatorHelper.class.getClassLoader());
                    XMLSecurityManager securityManager=(XMLSecurityManager)fComponentManager.getProperty(Constants.SECURITY_MANAGER);
                    if(securityManager!=null){
                        for(XMLSecurityManager.Limit limit : XMLSecurityManager.Limit.values()){
                            if(securityManager.isSet(limit.ordinal())){
                                tf.setAttribute(limit.apiProperty(),
                                        securityManager.getLimitValueAsString(limit));
                            }
                        }
                        if(securityManager.printEntityCountInfo()){
                            tf.setAttribute(Constants.JDK_ENTITY_COUNT_INFO,"yes");
                        }
                    }
                    identityTransformer1=tf.newTransformer();
                    identityTransformer2=tf.newTransformerHandler();
                }catch(TransformerConfigurationException e){
                    // this is impossible, but again better safe than sorry
                    throw new TransformerFactoryConfigurationError(e);
                }
            }
            handler=new ValidatorHandlerImpl(fComponentManager);
            if(result!=null){
                handler.setContentHandler(identityTransformer2);
                identityTransformer2.setResult(result);
            }
            try{
                identityTransformer1.transform(source,new SAXResult(handler));
            }catch(TransformerException e){
                if(e.getException() instanceof SAXException)
                    throw (SAXException)e.getException();
                throw new SAXException(e);
            }finally{
                handler.setContentHandler(null);
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(),
                "SourceResultMismatch",
                new Object[]{source.getClass().getName(),result.getClass().getName()}));
    }
}

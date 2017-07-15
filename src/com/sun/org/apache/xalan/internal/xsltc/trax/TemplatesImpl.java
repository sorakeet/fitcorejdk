/**
 * Copyright (c) 2007, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: TemplatesImpl.java,v 1.8 2007/03/26 20:12:27 spericas Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: TemplatesImpl.java,v 1.8 2007/03/26 20:12:27 spericas Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.trax;

import com.sun.org.apache.xalan.internal.XalanConstants;
import com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.Translet;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;

import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class TemplatesImpl implements Templates, Serializable{
    public final static String DESERIALIZE_TRANSLET="jdk.xml.enableTemplatesImplDeserialization";
    static final long serialVersionUID=673094361519270707L;
    private static final ObjectStreamField[] serialPersistentFields=
            new ObjectStreamField[]{
                    new ObjectStreamField("_name",String.class),
                    new ObjectStreamField("_bytecodes",byte[][].class),
                    new ObjectStreamField("_class",Class[].class),
                    new ObjectStreamField("_transletIndex",int.class),
                    new ObjectStreamField("_outputProperties",Properties.class),
                    new ObjectStreamField("_indentNumber",int.class),
            };
    private static String ABSTRACT_TRANSLET
            ="com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet";
    private String _name=null;
    private byte[][] _bytecodes=null;
    private Class[] _class=null;
    private int _transletIndex=-1;
    private transient Map<String,Class<?>> _auxClasses=null;
    private Properties _outputProperties;
    private int _indentNumber;
    private transient URIResolver _uriResolver=null;
    private transient ThreadLocal _sdom=new ThreadLocal();
    private transient TransformerFactoryImpl _tfactory=null;
    private transient boolean _useServicesMechanism;
    private transient String _accessExternalStylesheet=XalanConstants.EXTERNAL_ACCESS_DEFAULT;

    protected TemplatesImpl(byte[][] bytecodes,String transletName,
                            Properties outputProperties,int indentNumber,
                            TransformerFactoryImpl tfactory){
        _bytecodes=bytecodes;
        init(transletName,outputProperties,indentNumber,tfactory);
    }

    private void init(String transletName,
                      Properties outputProperties,int indentNumber,
                      TransformerFactoryImpl tfactory){
        _name=transletName;
        _outputProperties=outputProperties;
        _indentNumber=indentNumber;
        _tfactory=tfactory;
        _useServicesMechanism=tfactory.useServicesMechnism();
        _accessExternalStylesheet=(String)tfactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
    }

    protected TemplatesImpl(Class[] transletClasses,String transletName,
                            Properties outputProperties,int indentNumber,
                            TransformerFactoryImpl tfactory){
        _class=transletClasses;
        _transletIndex=0;
        init(transletName,outputProperties,indentNumber,tfactory);
    }

    public TemplatesImpl(){
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream is)
            throws IOException, ClassNotFoundException{
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            String temp=SecuritySupport.getSystemProperty(DESERIALIZE_TRANSLET);
            if(temp==null||!(temp.length()==0||temp.equalsIgnoreCase("true"))){
                ErrorMsg err=new ErrorMsg(ErrorMsg.DESERIALIZE_TRANSLET_ERR);
                throw new UnsupportedOperationException(err.toString());
            }
        }
        // We have to read serialized fields first.
        ObjectInputStream.GetField gf=is.readFields();
        _name=(String)gf.get("_name",null);
        _bytecodes=(byte[][])gf.get("_bytecodes",null);
        _class=(Class[])gf.get("_class",null);
        _transletIndex=gf.get("_transletIndex",-1);
        _outputProperties=(Properties)gf.get("_outputProperties",null);
        _indentNumber=gf.get("_indentNumber",0);
        if(is.readBoolean()){
            _uriResolver=(URIResolver)is.readObject();
        }
        _tfactory=new TransformerFactoryImpl();
    }

    private void writeObject(ObjectOutputStream os)
            throws IOException, ClassNotFoundException{
        if(_auxClasses!=null){
            //throw with the same message as when Hashtable was used for compatibility.
            throw new NotSerializableException(
                    "com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable");
        }
        // Write serialized fields
        ObjectOutputStream.PutField pf=os.putFields();
        pf.put("_name",_name);
        pf.put("_bytecodes",_bytecodes);
        pf.put("_class",_class);
        pf.put("_transletIndex",_transletIndex);
        pf.put("_outputProperties",_outputProperties);
        pf.put("_indentNumber",_indentNumber);
        os.writeFields();
        if(_uriResolver instanceof Serializable){
            os.writeBoolean(true);
            os.writeObject((Serializable)_uriResolver);
        }else{
            os.writeBoolean(false);
        }
    }

    public boolean useServicesMechnism(){
        return _useServicesMechanism;
    }

    public synchronized void setURIResolver(URIResolver resolver){
        _uriResolver=resolver;
    }

    private synchronized byte[][] getTransletBytecodes(){
        return _bytecodes;
    }

    private synchronized void setTransletBytecodes(byte[][] bytecodes){
        _bytecodes=bytecodes;
    }

    private synchronized Class[] getTransletClasses(){
        try{
            if(_class==null) defineTransletClasses();
        }catch(TransformerConfigurationException e){
            // Falls through
        }
        return _class;
    }

    private void defineTransletClasses()
            throws TransformerConfigurationException{
        if(_bytecodes==null){
            ErrorMsg err=new ErrorMsg(ErrorMsg.NO_TRANSLET_CLASS_ERR);
            throw new TransformerConfigurationException(err.toString());
        }
        TransletClassLoader loader=(TransletClassLoader)
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        return new TransletClassLoader(ObjectFactory.findClassLoader(),_tfactory.getExternalExtensionsMap());
                    }
                });
        try{
            final int classCount=_bytecodes.length;
            _class=new Class[classCount];
            if(classCount>1){
                _auxClasses=new HashMap<>();
            }
            for(int i=0;i<classCount;i++){
                _class[i]=loader.defineClass(_bytecodes[i]);
                final Class superClass=_class[i].getSuperclass();
                // Check if this is the main class
                if(superClass.getName().equals(ABSTRACT_TRANSLET)){
                    _transletIndex=i;
                }else{
                    _auxClasses.put(_class[i].getName(),_class[i]);
                }
            }
            if(_transletIndex<0){
                ErrorMsg err=new ErrorMsg(ErrorMsg.NO_MAIN_TRANSLET_ERR,_name);
                throw new TransformerConfigurationException(err.toString());
            }
        }catch(ClassFormatError e){
            ErrorMsg err=new ErrorMsg(ErrorMsg.TRANSLET_CLASS_ERR,_name);
            throw new TransformerConfigurationException(err.toString());
        }catch(LinkageError e){
            ErrorMsg err=new ErrorMsg(ErrorMsg.TRANSLET_OBJECT_ERR,_name);
            throw new TransformerConfigurationException(err.toString());
        }
    }

    public synchronized int getTransletIndex(){
        try{
            if(_class==null) defineTransletClasses();
        }catch(TransformerConfigurationException e){
            // Falls through
        }
        return _transletIndex;
    }

    protected synchronized String getTransletName(){
        return _name;
    }

    protected synchronized void setTransletName(String name){
        _name=name;
    }

    public DOM getStylesheetDOM(){
        return (DOM)_sdom.get();
    }

    public void setStylesheetDOM(DOM sdom){
        _sdom.set(sdom);
    }    private Translet getTransletInstance()
            throws TransformerConfigurationException{
        try{
            if(_name==null) return null;
            if(_class==null) defineTransletClasses();
            // The translet needs to keep a reference to all its auxiliary
            // class to prevent the GC from collecting them
            AbstractTranslet translet=(AbstractTranslet)_class[_transletIndex].newInstance();
            translet.postInitialization();
            translet.setTemplates(this);
            translet.setServicesMechnism(_useServicesMechanism);
            translet.setAllowedProtocols(_accessExternalStylesheet);
            if(_auxClasses!=null){
                translet.setAuxiliaryClasses(_auxClasses);
            }
            return translet;
        }catch(InstantiationException e){
            ErrorMsg err=new ErrorMsg(ErrorMsg.TRANSLET_OBJECT_ERR,_name);
            throw new TransformerConfigurationException(err.toString());
        }catch(IllegalAccessException e){
            ErrorMsg err=new ErrorMsg(ErrorMsg.TRANSLET_OBJECT_ERR,_name);
            throw new TransformerConfigurationException(err.toString());
        }
    }

    static final class TransletClassLoader extends ClassLoader{
        private final Map<String,Class> _loadedExternalExtensionFunctions;

        TransletClassLoader(ClassLoader parent){
            super(parent);
            _loadedExternalExtensionFunctions=null;
        }

        TransletClassLoader(ClassLoader parent,Map<String,Class> mapEF){
            super(parent);
            _loadedExternalExtensionFunctions=mapEF;
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException{
            Class<?> ret=null;
            // The _loadedExternalExtensionFunctions will be empty when the
            // SecurityManager is not set and the FSP is turned off
            if(_loadedExternalExtensionFunctions!=null){
                ret=_loadedExternalExtensionFunctions.get(name);
            }
            if(ret==null){
                ret=super.loadClass(name);
            }
            return ret;
        }

        Class defineClass(final byte[] b){
            return defineClass(null,b,0,b.length);
        }
    }    public synchronized Transformer newTransformer()
            throws TransformerConfigurationException{
        TransformerImpl transformer;
        transformer=new TransformerImpl(getTransletInstance(),_outputProperties,
                _indentNumber,_tfactory);
        if(_uriResolver!=null){
            transformer.setURIResolver(_uriResolver);
        }
        if(_tfactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)){
            transformer.setSecureProcessing(true);
        }
        return transformer;
    }

    public synchronized Properties getOutputProperties(){
        try{
            return newTransformer().getOutputProperties();
        }catch(TransformerConfigurationException e){
            return null;
        }
    }




}

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
package com.sun.org.apache.xml.internal.security.encryption;

import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSerializer implements Serializer{
    protected Canonicalizer canon;

    protected static byte[] createContext(byte[] source,Node ctx) throws XMLEncryptionException{
        // Create the context to parse the document against
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        try{
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(byteArrayOutputStream,"UTF-8");
            outputStreamWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummy");
            // Run through each node up to the document node and find any xmlns: nodes
            Map<String,String> storedNamespaces=new HashMap<String,String>();
            Node wk=ctx;
            while(wk!=null){
                NamedNodeMap atts=wk.getAttributes();
                if(atts!=null){
                    for(int i=0;i<atts.getLength();++i){
                        Node att=atts.item(i);
                        String nodeName=att.getNodeName();
                        if((nodeName.equals("xmlns")||nodeName.startsWith("xmlns:"))
                                &&!storedNamespaces.containsKey(att.getNodeName())){
                            outputStreamWriter.write(" ");
                            outputStreamWriter.write(nodeName);
                            outputStreamWriter.write("=\"");
                            outputStreamWriter.write(att.getNodeValue());
                            outputStreamWriter.write("\"");
                            storedNamespaces.put(nodeName,att.getNodeValue());
                        }
                    }
                }
                wk=wk.getParentNode();
            }
            outputStreamWriter.write(">");
            outputStreamWriter.flush();
            byteArrayOutputStream.write(source);
            outputStreamWriter.write("</dummy>");
            outputStreamWriter.close();
            return byteArrayOutputStream.toByteArray();
        }catch(UnsupportedEncodingException e){
            throw new XMLEncryptionException("empty",e);
        }catch(IOException e){
            throw new XMLEncryptionException("empty",e);
        }
    }

    protected static String createContext(String source,Node ctx){
        // Create the context to parse the document against
        StringBuilder sb=new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummy");
        // Run through each node up to the document node and find any xmlns: nodes
        Map<String,String> storedNamespaces=new HashMap<String,String>();
        Node wk=ctx;
        while(wk!=null){
            NamedNodeMap atts=wk.getAttributes();
            if(atts!=null){
                for(int i=0;i<atts.getLength();++i){
                    Node att=atts.item(i);
                    String nodeName=att.getNodeName();
                    if((nodeName.equals("xmlns")||nodeName.startsWith("xmlns:"))
                            &&!storedNamespaces.containsKey(att.getNodeName())){
                        sb.append(" "+nodeName+"=\""+att.getNodeValue()+"\"");
                        storedNamespaces.put(nodeName,att.getNodeValue());
                    }
                }
            }
            wk=wk.getParentNode();
        }
        sb.append(">"+source+"</dummy>");
        return sb.toString();
    }

    public void setCanonicalizer(Canonicalizer canon){
        this.canon=canon;
    }

    public byte[] serializeToByteArray(Element element) throws Exception{
        return canonSerializeToByteArray(element);
    }

    public byte[] serializeToByteArray(NodeList content) throws Exception{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        canon.setWriter(baos);
        canon.notReset();
        for(int i=0;i<content.getLength();i++){
            canon.canonicalizeSubtree(content.item(i));
        }
        return baos.toByteArray();
    }

    public byte[] canonSerializeToByteArray(Node node) throws Exception{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        canon.setWriter(baos);
        canon.notReset();
        canon.canonicalizeSubtree(node);
        return baos.toByteArray();
    }

    public abstract Node deserialize(byte[] source,Node ctx) throws XMLEncryptionException;

    public String serialize(Element element) throws Exception{
        return canonSerialize(element);
    }

    public String canonSerialize(Node node) throws Exception{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        canon.setWriter(baos);
        canon.notReset();
        canon.canonicalizeSubtree(node);
        String ret=baos.toString("UTF-8");
        baos.reset();
        return ret;
    }

    public String serialize(NodeList content) throws Exception{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        canon.setWriter(baos);
        canon.notReset();
        for(int i=0;i<content.getLength();i++){
            canon.canonicalizeSubtree(content.item(i));
        }
        String ret=baos.toString("UTF-8");
        baos.reset();
        return ret;
    }

    public abstract Node deserialize(String source,Node ctx) throws XMLEncryptionException;
}

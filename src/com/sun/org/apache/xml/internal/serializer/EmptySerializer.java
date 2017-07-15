/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: EmptySerializer.java,v 1.2.4.1 2005/09/15 08:15:16 suresh_emailid Exp $
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
 * $Id: EmptySerializer.java,v 1.2.4.1 2005/09/15 08:15:16 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import java.util.Vector;

public class EmptySerializer implements SerializationHandler{
    protected static final String ERR="EmptySerializer method not over-ridden";

    public void setContentHandler(ContentHandler ch){
        aMethodIsCalled();
    }    protected void couldThrowIOException() throws IOException{
        return; // don't do anything.
    }

    void aMethodIsCalled(){
        // throw new RuntimeException(err);
        return;
    }

    public void close(){
        aMethodIsCalled();
    }

    public void serialize(Node node) throws IOException{
        couldThrowIOException();
    }

    public boolean setEscaping(boolean escape) throws SAXException{
        couldThrowSAXException();
        return false;
    }

    protected void couldThrowSAXException() throws SAXException{
        return; // don't do anything.
    }    public ContentHandler asContentHandler() throws IOException{
        couldThrowIOException();
        return null;
    }

    public void addAttribute(
            String uri,
            String localName,
            String rawName,
            String type,
            String value,
            boolean XSLAttribute)
            throws SAXException{
        couldThrowSAXException();
    }

    public void addAttributes(Attributes atts) throws SAXException{
        couldThrowSAXException();
    }

    public void addAttribute(String name,String value){
        aMethodIsCalled();
    }    public Properties getOutputFormat(){
        aMethodIsCalled();
        return null;
    }

    public void characters(String chars) throws SAXException{
        couldThrowSAXException();
    }    public OutputStream getOutputStream(){
        aMethodIsCalled();
        return null;
    }

    public void characters(Node node) throws SAXException{
        couldThrowSAXException();
    }    public Writer getWriter(){
        aMethodIsCalled();
        return null;
    }

    public void endElement(String elemName) throws SAXException{
        couldThrowSAXException();
    }    public boolean reset(){
        aMethodIsCalled();
        return false;
    }

    public void startElement(String uri,String localName,String qName)
            throws SAXException{
        couldThrowSAXException(qName);
    }

    protected void couldThrowSAXException(String elemQName) throws SAXException{
        return; // don't do anything.
    }

    public void startElement(String qName) throws SAXException{
        couldThrowSAXException(qName);
    }

    public void namespaceAfterStartElement(String uri,String prefix)
            throws SAXException{
        couldThrowSAXException();
    }

    public boolean startPrefixMapping(
            String prefix,
            String uri,
            boolean shouldFlush)
            throws SAXException{
        couldThrowSAXException();
        return false;
    }

    public void entityReference(String entityName) throws SAXException{
        couldThrowSAXException();
    }

    public NamespaceMappings getNamespaceMappings(){
        aMethodIsCalled();
        return null;
    }    public void setOutputFormat(Properties format){
        aMethodIsCalled();
    }

    public String getPrefix(String uri){
        aMethodIsCalled();
        return null;
    }    public void setOutputStream(OutputStream output){
        aMethodIsCalled();
    }

    public String getNamespaceURI(String name,boolean isElement){
        aMethodIsCalled();
        return null;
    }

    public String getNamespaceURIFromPrefix(String prefix){
        aMethodIsCalled();
        return null;
    }    public void setWriter(Writer writer){
        aMethodIsCalled();
    }

    public void setSourceLocator(SourceLocator locator){
        aMethodIsCalled();
    }    public void setTransformer(Transformer transformer){
        aMethodIsCalled();
    }

    public void addUniqueAttribute(String name,String value,int flags)
            throws SAXException{
        couldThrowSAXException();
    }    public Transformer getTransformer(){
        aMethodIsCalled();
        return null;
    }

    public void addXSLAttribute(String qName,String value,String uri){
        aMethodIsCalled();
    }

    public void addAttribute(String uri,String localName,String rawName,String type,String value) throws SAXException{
        couldThrowSAXException();
    }

    public void setNamespaceMappings(NamespaceMappings mappings){
        aMethodIsCalled();
    }

    public void flushPending() throws SAXException{
        couldThrowSAXException();
    }

    public void setDTDEntityExpansion(boolean expand){
        aMethodIsCalled();
    }

    public void setIsStandalone(boolean isStandalone){
        aMethodIsCalled();
    }

    public void setDocumentLocator(Locator arg0){
        aMethodIsCalled();
    }

    public void startDocument() throws SAXException{
        couldThrowSAXException();
    }

    public void endDocument() throws SAXException{
        couldThrowSAXException();
    }

    public void startPrefixMapping(String arg0,String arg1)
            throws SAXException{
        couldThrowSAXException();
    }

    public void endPrefixMapping(String arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void startElement(
            String arg0,
            String arg1,
            String arg2,
            Attributes arg3)
            throws SAXException{
        couldThrowSAXException();
    }

    public void endElement(String arg0,String arg1,String arg2)
            throws SAXException{
        couldThrowSAXException();
    }

    public void characters(char[] arg0,int arg1,int arg2) throws SAXException{
        couldThrowSAXException(arg0,arg1,arg2);
    }

    protected void couldThrowSAXException(char[] chars,int off,int len) throws SAXException{
        return; // don't do anything.
    }

    public void ignorableWhitespace(char[] arg0,int arg1,int arg2)
            throws SAXException{
        couldThrowSAXException();
    }

    public void processingInstruction(String arg0,String arg1)
            throws SAXException{
        couldThrowSAXException();
    }

    public void skippedEntity(String arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void comment(String comment) throws SAXException{
        couldThrowSAXException();
    }

    public void startDTD(String arg0,String arg1,String arg2)
            throws SAXException{
        couldThrowSAXException();
    }

    public void endDTD() throws SAXException{
        couldThrowSAXException();
    }

    public void startEntity(String arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void endEntity(String arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void startCDATA() throws SAXException{
        couldThrowSAXException();
    }

    public void endCDATA() throws SAXException{
        couldThrowSAXException();
    }

    public void comment(char[] arg0,int arg1,int arg2) throws SAXException{
        couldThrowSAXException();
    }

    public String getDoctypePublic(){
        aMethodIsCalled();
        return null;
    }

    public String getDoctypeSystem(){
        aMethodIsCalled();
        return null;
    }

    public String getEncoding(){
        aMethodIsCalled();
        return null;
    }

    public boolean getIndent(){
        aMethodIsCalled();
        return false;
    }

    public void setIndent(boolean indent){
        aMethodIsCalled();
    }

    public int getIndentAmount(){
        aMethodIsCalled();
        return 0;
    }

    public void setIndentAmount(int spaces){
        aMethodIsCalled();
    }

    public String getMediaType(){
        aMethodIsCalled();
        return null;
    }

    public boolean getOmitXMLDeclaration(){
        aMethodIsCalled();
        return false;
    }

    public String getStandalone(){
        aMethodIsCalled();
        return null;
    }

    public String getVersion(){
        aMethodIsCalled();
        return null;
    }

    public void setCdataSectionElements(Vector URI_and_localNames){
        aMethodIsCalled();
    }

    public void setDoctype(String system,String pub){
        aMethodIsCalled();
    }

    public void setVersion(String version){
        aMethodIsCalled();
    }

    public void setStandalone(String standalone){
        aMethodIsCalled();
    }

    public void setOmitXMLDeclaration(boolean b){
        aMethodIsCalled();
    }

    public void setMediaType(String mediatype){
        aMethodIsCalled();
    }

    public void setEncoding(String encoding){
        aMethodIsCalled();
    }

    public void setDoctypeSystem(String doctype){
        aMethodIsCalled();
    }

    public void setDoctypePublic(String doctype){
        aMethodIsCalled();
    }

    public void elementDecl(String arg0,String arg1) throws SAXException{
        couldThrowSAXException();
    }

    public void attributeDecl(
            String arg0,
            String arg1,
            String arg2,
            String arg3,
            String arg4)
            throws SAXException{
        couldThrowSAXException();
    }

    public void internalEntityDecl(String arg0,String arg1)
            throws SAXException{
        couldThrowSAXException();
    }

    public void externalEntityDecl(String arg0,String arg1,String arg2)
            throws SAXException{
        couldThrowSAXException();
    }

    public void warning(SAXParseException arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void error(SAXParseException arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void fatalError(SAXParseException arg0) throws SAXException{
        couldThrowSAXException();
    }

    public void notationDecl(String arg0,String arg1,String arg2) throws SAXException{
        couldThrowSAXException();
    }

    public void unparsedEntityDecl(
            String arg0,
            String arg1,
            String arg2,
            String arg3)
            throws SAXException{
        couldThrowSAXException();
    }





    public DOMSerializer asDOMSerializer() throws IOException{
        couldThrowIOException();
        return null;
    }


















}

/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xerces.internal.impl;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class XMLStreamFilterImpl implements XMLStreamReader{
    private StreamFilter fStreamFilter=null;
    private XMLStreamReader fStreamReader=null;
    private int fCurrentEvent;
    private boolean fEventAccepted=false;
    private boolean fStreamAdvancedByHasNext=false;

    public XMLStreamFilterImpl(XMLStreamReader reader,StreamFilter filter){
        fStreamReader=reader;
        this.fStreamFilter=filter;
        //this is debatable to initiate at an acceptable event,
        //but it's neccessary in order to pass the TCK and yet avoid skipping element
        try{
            if(fStreamFilter.accept(fStreamReader)){
                fEventAccepted=true;
            }else{
                findNextEvent();
            }
        }catch(XMLStreamException xs){
            System.err.println("Error while creating a stream Filter"+xs);
        }
    }

    private int findNextEvent() throws XMLStreamException{
        fStreamAdvancedByHasNext=false;
        while(fStreamReader.hasNext()){
            fCurrentEvent=fStreamReader.next();
            if(fStreamFilter.accept(fStreamReader)){
                fEventAccepted=true;
                return fCurrentEvent;
            }
        }
        //although it seems that IllegalStateException should be thrown when next() is called
        //on a stream that has no more items, we have to assume END_DOCUMENT is always accepted
        //in order to pass the TCK
        if(fCurrentEvent==XMLEvent.END_DOCUMENT)
            return fCurrentEvent;
        else
            return -1;
    }

    protected void setStreamFilter(StreamFilter sf){
        this.fStreamFilter=sf;
    }

    public Object getProperty(String name) throws IllegalArgumentException{
        return fStreamReader.getProperty(name);
    }

    public int next() throws XMLStreamException{
        if(fStreamAdvancedByHasNext&&fEventAccepted){
            fStreamAdvancedByHasNext=false;
            return fCurrentEvent;
        }
        int event=findNextEvent();
        if(event!=-1){
            return event;
        }
        throw new IllegalStateException("The stream reader has reached the end of the document, or there are no more "+
                " items to return");
    }

    public void require(int type,String namespaceURI,String localName) throws XMLStreamException{
        fStreamReader.require(type,namespaceURI,localName);
    }

    public String getElementText() throws XMLStreamException{
        return fStreamReader.getElementText();
    }

    public int nextTag() throws XMLStreamException{
        if(fStreamAdvancedByHasNext&&fEventAccepted&&
                (fCurrentEvent==XMLEvent.START_ELEMENT||fCurrentEvent==XMLEvent.START_ELEMENT)){
            fStreamAdvancedByHasNext=false;
            return fCurrentEvent;
        }
        int event=findNextTag();
        if(event!=-1){
            return event;
        }
        throw new IllegalStateException("The stream reader has reached the end of the document, or there are no more "+
                " items to return");
    }

    public boolean hasNext() throws XMLStreamException{
        if(fStreamReader.hasNext()){
            if(!fEventAccepted){
                if((fCurrentEvent=findNextEvent())==-1){
                    return false;
                }else{
                    fStreamAdvancedByHasNext=true;
                }
            }
            return true;
        }
        return false;
    }

    public void close() throws XMLStreamException{
        fStreamReader.close();
    }

    public String getNamespaceURI(String prefix){
        return fStreamReader.getNamespaceURI(prefix);
    }

    public boolean isStartElement(){
        return fStreamReader.isStartElement();
    }

    public boolean isEndElement(){
        return fStreamReader.isEndElement();
    }

    public boolean isCharacters(){
        return fStreamReader.isCharacters();
    }

    public boolean isWhiteSpace(){
        return fStreamReader.isWhiteSpace();
    }

    public String getAttributeValue(String namespaceURI,String localName){
        return fStreamReader.getAttributeValue(namespaceURI,localName);
    }

    public int getAttributeCount(){
        return fStreamReader.getAttributeCount();
    }

    public QName getAttributeName(int index){
        return fStreamReader.getAttributeName(index);
    }

    public String getAttributeNamespace(int index){
        return fStreamReader.getAttributeNamespace(index);
    }

    public String getAttributeLocalName(int index){
        return fStreamReader.getAttributeLocalName(index);
    }

    public String getAttributePrefix(int index){
        return fStreamReader.getAttributePrefix(index);
    }

    public String getAttributeType(int index){
        return fStreamReader.getAttributeType(index);
    }

    public String getAttributeValue(int index){
        return fStreamReader.getAttributeValue(index);
    }

    public boolean isAttributeSpecified(int index){
        return fStreamReader.isAttributeSpecified(index);
    }

    public int getNamespaceCount(){
        return fStreamReader.getNamespaceCount();
    }

    public String getNamespacePrefix(int index){
        return fStreamReader.getNamespacePrefix(index);
    }

    public String getNamespaceURI(int index){
        return fStreamReader.getNamespaceURI(index);
    }

    public javax.xml.namespace.NamespaceContext getNamespaceContext(){
        return fStreamReader.getNamespaceContext();
    }

    public int getEventType(){
        return fStreamReader.getEventType();
    }

    public String getText(){
        return fStreamReader.getText();
    }

    public char[] getTextCharacters(){
        return fStreamReader.getTextCharacters();
    }

    public int getTextCharacters(int sourceStart,char[] target,int targetStart,int length) throws XMLStreamException{
        return fStreamReader.getTextCharacters(sourceStart,target,targetStart,length);
    }

    public int getTextStart(){
        return fStreamReader.getTextStart();
    }

    public int getTextLength(){
        return fStreamReader.getTextLength();
    }

    public String getEncoding(){
        return fStreamReader.getEncoding();
    }

    public boolean hasText(){
        return fStreamReader.hasText();
    }

    public Location getLocation(){
        return fStreamReader.getLocation();
    }

    public QName getName(){
        return fStreamReader.getName();
    }

    public String getLocalName(){
        return fStreamReader.getLocalName();
    }

    public boolean hasName(){
        return fStreamReader.hasName();
    }

    public String getNamespaceURI(){
        return fStreamReader.getNamespaceURI();
    }

    public String getPrefix(){
        return fStreamReader.getPrefix();
    }

    public String getVersion(){
        return fStreamReader.getVersion();
    }

    public boolean isStandalone(){
        return fStreamReader.isStandalone();
    }

    public boolean standaloneSet(){
        return fStreamReader.standaloneSet();
    }

    public String getCharacterEncodingScheme(){
        return fStreamReader.getCharacterEncodingScheme();
    }

    public String getPITarget(){
        return fStreamReader.getPITarget();
    }

    public String getPIData(){
        return fStreamReader.getPIData();
    }

    private int findNextTag() throws XMLStreamException{
        fStreamAdvancedByHasNext=false;
        while(fStreamReader.hasNext()){
            fCurrentEvent=fStreamReader.nextTag();
            if(fStreamFilter.accept(fStreamReader)){
                fEventAccepted=true;
                return fCurrentEvent;
            }
        }
        if(fCurrentEvent==XMLEvent.END_DOCUMENT)
            return fCurrentEvent;
        else
            return -1;
    }
}

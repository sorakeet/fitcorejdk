/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import java.util.*;

class MimeTypeParameterList implements Cloneable{
    private static final String TSPECIALS="()<>@,;:\\\"/[]?=";
    private Hashtable<String,String> parameters;

    public MimeTypeParameterList(){
        parameters=new Hashtable<>();
    }

    public MimeTypeParameterList(String rawdata)
            throws MimeTypeParseException{
        parameters=new Hashtable<>();
        //    now parse rawdata
        parse(rawdata);
    }

    protected void parse(String rawdata) throws MimeTypeParseException{
        int length=rawdata.length();
        if(length>0){
            int currentIndex=skipWhiteSpace(rawdata,0);
            int lastIndex=0;
            if(currentIndex<length){
                char currentChar=rawdata.charAt(currentIndex);
                while((currentIndex<length)&&(currentChar==';')){
                    String name;
                    String value;
                    boolean foundit;
                    //    eat the ';'
                    ++currentIndex;
                    //    now parse the parameter name
                    //    skip whitespace
                    currentIndex=skipWhiteSpace(rawdata,currentIndex);
                    if(currentIndex<length){
                        //    find the end of the token char run
                        lastIndex=currentIndex;
                        currentChar=rawdata.charAt(currentIndex);
                        while((currentIndex<length)&&isTokenChar(currentChar)){
                            ++currentIndex;
                            currentChar=rawdata.charAt(currentIndex);
                        }
                        name=rawdata.substring(lastIndex,currentIndex).toLowerCase();
                        //    now parse the '=' that separates the name from the value
                        //    skip whitespace
                        currentIndex=skipWhiteSpace(rawdata,currentIndex);
                        if((currentIndex<length)&&(rawdata.charAt(currentIndex)=='=')){
                            //    eat it and parse the parameter value
                            ++currentIndex;
                            //    skip whitespace
                            currentIndex=skipWhiteSpace(rawdata,currentIndex);
                            if(currentIndex<length){
                                //    now find out whether or not we have a quoted value
                                currentChar=rawdata.charAt(currentIndex);
                                if(currentChar=='"'){
                                    //    yup it's quoted so eat it and capture the quoted string
                                    ++currentIndex;
                                    lastIndex=currentIndex;
                                    if(currentIndex<length){
                                        //    find the next unescqped quote
                                        foundit=false;
                                        while((currentIndex<length)&&!foundit){
                                            currentChar=rawdata.charAt(currentIndex);
                                            if(currentChar=='\\'){
                                                //    found an escape sequence so pass this and the next character
                                                currentIndex+=2;
                                            }else if(currentChar=='"'){
                                                //    foundit!
                                                foundit=true;
                                            }else{
                                                ++currentIndex;
                                            }
                                        }
                                        if(currentChar=='"'){
                                            value=unquote(rawdata.substring(lastIndex,currentIndex));
                                            //    eat the quote
                                            ++currentIndex;
                                        }else{
                                            throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                                        }
                                    }else{
                                        throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                                    }
                                }else if(isTokenChar(currentChar)){
                                    //    nope it's an ordinary token so it ends with a non-token char
                                    lastIndex=currentIndex;
                                    foundit=false;
                                    while((currentIndex<length)&&!foundit){
                                        currentChar=rawdata.charAt(currentIndex);
                                        if(isTokenChar(currentChar)){
                                            ++currentIndex;
                                        }else{
                                            foundit=true;
                                        }
                                    }
                                    value=rawdata.substring(lastIndex,currentIndex);
                                }else{
                                    //    it ain't a value
                                    throw new MimeTypeParseException("Unexpected character encountered at index "+currentIndex);
                                }
                                //    now put the data into the hashtable
                                parameters.put(name,value);
                            }else{
                                throw new MimeTypeParseException("Couldn't find a value for parameter named "+name);
                            }
                        }else{
                            throw new MimeTypeParseException("Couldn't find the '=' that separates a parameter name from its value.");
                        }
                    }else{
                        throw new MimeTypeParseException("Couldn't find parameter name");
                    }
                    //    setup the next iteration
                    currentIndex=skipWhiteSpace(rawdata,currentIndex);
                    if(currentIndex<length){
                        currentChar=rawdata.charAt(currentIndex);
                    }
                }
                if(currentIndex<length){
                    throw new MimeTypeParseException("More characters encountered in input than expected.");
                }
            }
        }
    }

    private static boolean isTokenChar(char c){
        return ((c>040)&&(c<0177))&&(TSPECIALS.indexOf(c)<0);
    }

    private static int skipWhiteSpace(String rawdata,int i){
        int length=rawdata.length();
        if(i<length){
            char c=rawdata.charAt(i);
            while((i<length)&&Character.isWhitespace(c)){
                ++i;
                c=rawdata.charAt(i);
            }
        }
        return i;
    }

    private static String unquote(String value){
        int valueLength=value.length();
        StringBuilder buffer=new StringBuilder(valueLength);
        boolean escaped=false;
        for(int i=0;i<valueLength;++i){
            char currentChar=value.charAt(i);
            if(!escaped&&(currentChar!='\\')){
                buffer.append(currentChar);
            }else if(escaped){
                buffer.append(currentChar);
                escaped=false;
            }else{
                escaped=true;
            }
        }
        return buffer.toString();
    }

    public int hashCode(){
        int code=Integer.MAX_VALUE/45; // "random" value for empty lists
        String paramName=null;
        Enumeration<String> enum_=this.getNames();
        while(enum_.hasMoreElements()){
            paramName=enum_.nextElement();
            code+=paramName.hashCode();
            code+=this.get(paramName).hashCode();
        }
        return code;
    } // hashCode()

    public boolean equals(Object thatObject){
        //System.out.println("MimeTypeParameterList.equals("+this+","+thatObject+")");
        if(!(thatObject instanceof MimeTypeParameterList)){
            return false;
        }
        MimeTypeParameterList that=(MimeTypeParameterList)thatObject;
        if(this.size()!=that.size()){
            return false;
        }
        String name=null;
        String thisValue=null;
        String thatValue=null;
        Set<Map.Entry<String,String>> entries=parameters.entrySet();
        Iterator<Map.Entry<String,String>> iterator=entries.iterator();
        Map.Entry<String,String> entry=null;
        while(iterator.hasNext()){
            entry=iterator.next();
            name=entry.getKey();
            thisValue=entry.getValue();
            thatValue=that.parameters.get(name);
            if((thisValue==null)||(thatValue==null)){
                // both null -> equal, only one null -> not equal
                if(thisValue!=thatValue){
                    return false;
                }
            }else if(!thisValue.equals(thatValue)){
                return false;
            }
        } // while iterator
        return true;
    } // equals()

    public int size(){
        return parameters.size();
    }

    public Object clone(){
        MimeTypeParameterList newObj=null;
        try{
            newObj=(MimeTypeParameterList)super.clone();
        }catch(CloneNotSupportedException cannotHappen){
        }
        newObj.parameters=(Hashtable)parameters.clone();
        return newObj;
    }

    public String toString(){
        // Heuristic: 8 characters per field
        StringBuilder buffer=new StringBuilder(parameters.size()*16);
        Enumeration<String> keys=parameters.keys();
        while(keys.hasMoreElements()){
            buffer.append("; ");
            String key=keys.nextElement();
            buffer.append(key);
            buffer.append('=');
            buffer.append(quote(parameters.get(key)));
        }
        return buffer.toString();
    }

    private static String quote(String value){
        boolean needsQuotes=false;
        //    check to see if we actually have to quote this thing
        int length=value.length();
        for(int i=0;(i<length)&&!needsQuotes;++i){
            needsQuotes=!isTokenChar(value.charAt(i));
        }
        if(needsQuotes){
            StringBuilder buffer=new StringBuilder((int)(length*1.5));
            //    add the initial quote
            buffer.append('"');
            //    add the properly escaped text
            for(int i=0;i<length;++i){
                char c=value.charAt(i);
                if((c=='\\')||(c=='"')){
                    buffer.append('\\');
                }
                buffer.append(c);
            }
            //    add the closing quote
            buffer.append('"');
            return buffer.toString();
        }else{
            return value;
        }
    }
    //    below here be scary parsing related things

    public String get(String name){
        return parameters.get(name.trim().toLowerCase());
    }

    public Enumeration<String> getNames(){
        return parameters.keys();
    }

    public boolean isEmpty(){
        return parameters.isEmpty();
    }

    public void set(String name,String value){
        parameters.put(name.trim().toLowerCase(),value);
    }

    public void remove(String name){
        parameters.remove(name.trim().toLowerCase());
    }
}

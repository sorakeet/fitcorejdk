/**
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xalan.internal.utils;

import com.sun.org.apache.xalan.internal.XalanConstants;
import org.xml.sax.SAXException;

import java.util.concurrent.CopyOnWriteArrayList;

public final class XMLSecurityManager{
    // Array list to store printed warnings for each SAX parser used
    private static final CopyOnWriteArrayList<String> printedWarnings=new CopyOnWriteArrayList<>();
    private final int[] values;
    private final int indexEntityCountInfo=10000;
    private State[] states;
    private boolean[] isSet;
    private String printEntityCountInfo="";
    public XMLSecurityManager(){
        this(false);
    }
    public XMLSecurityManager(boolean secureProcessing){
        values=new int[Limit.values().length];
        states=new State[Limit.values().length];
        isSet=new boolean[Limit.values().length];
        for(Limit limit : Limit.values()){
            if(secureProcessing){
                values[limit.ordinal()]=limit.secureValue();
                states[limit.ordinal()]=State.FSP;
            }else{
                values[limit.ordinal()]=limit.defaultValue();
                states[limit.ordinal()]=State.DEFAULT;
            }
        }
        //read system properties or jaxp.properties
        readSystemProperties();
    }

    private void readSystemProperties(){
        for(Limit limit : Limit.values()){
            if(!getSystemProperty(limit,limit.systemProperty())){
                //if system property is not found, try the older form if any
                for(NameMap nameMap : NameMap.values()){
                    String oldName=nameMap.getOldName(limit.systemProperty());
                    if(oldName!=null){
                        getSystemProperty(limit,oldName);
                    }
                }
            }
        }
    }

    private boolean getSystemProperty(Limit limit,String sysPropertyName){
        try{
            String value=SecuritySupport.getSystemProperty(sysPropertyName);
            if(value!=null&&!value.equals("")){
                values[limit.ordinal()]=Integer.parseInt(value);
                states[limit.ordinal()]=State.SYSTEMPROPERTY;
                return true;
            }
            value=SecuritySupport.readJAXPProperty(sysPropertyName);
            if(value!=null&&!value.equals("")){
                values[limit.ordinal()]=Integer.parseInt(value);
                states[limit.ordinal()]=State.JAXPDOTPROPERTIES;
                return true;
            }
        }catch(NumberFormatException e){
            //invalid setting
            throw new NumberFormatException("Invalid setting for system property: "+limit.systemProperty());
        }
        return false;
    }

    public static void printWarning(String parserClassName,String propertyName,SAXException exception){
        String key=parserClassName+":"+propertyName;
        if(printedWarnings.addIfAbsent(key)){
            System.err.println("Warning: "+parserClassName+": "+exception.getMessage());
        }
    }

    public void setSecureProcessing(boolean secure){
        for(Limit limit : Limit.values()){
            if(secure){
                setLimit(limit.ordinal(),State.FSP,limit.secureValue());
            }else{
                setLimit(limit.ordinal(),State.FSP,limit.defaultValue());
            }
        }
    }

    public void setLimit(int index,State state,int value){
        if(index==indexEntityCountInfo){
            //if it's explicitly set, it's treated as yes no matter the value
            printEntityCountInfo=XalanConstants.JDK_YES;
        }else{
            //only update if it shall override
            if(state.compareTo(states[index])>=0){
                values[index]=value;
                states[index]=state;
                isSet[index]=true;
            }
        }
    }

    public boolean setLimit(String propertyName,State state,Object value){
        int index=getIndex(propertyName);
        if(index>-1){
            setLimit(index,state,value);
            return true;
        }
        return false;
    }

    public void setLimit(int index,State state,Object value){
        if(index==indexEntityCountInfo){
            //if it's explicitly set, it's treated as yes no matter the value
            printEntityCountInfo=(String)value;
        }else{
            int temp=0;
            try{
                temp=Integer.parseInt((String)value);
                if(temp<0){
                    temp=0;
                }
            }catch(NumberFormatException e){
            }
            setLimit(index,state,temp);
        }
    }

    public int getIndex(String propertyName){
        for(Limit limit : Limit.values()){
            if(limit.equalsAPIPropertyName(propertyName)){
                //internally, ordinal is used as index
                return limit.ordinal();
            }
        }
        //special property to return entity count info
        if(propertyName.equals(XalanConstants.JDK_ENTITY_COUNT_INFO)){
            return indexEntityCountInfo;
        }
        return -1;
    }

    public void setLimit(Limit limit,State state,int value){
        setLimit(limit.ordinal(),state,value);
    }

    public String getLimitAsString(String propertyName){
        int index=getIndex(propertyName);
        if(index>-1){
            return getLimitValueByIndex(index);
        }
        return null;
    }

    public String getLimitValueByIndex(int index){
        if(index==indexEntityCountInfo){
            return printEntityCountInfo;
        }
        return Integer.toString(values[index]);
    }

    public String getLimitValueAsString(Limit limit){
        return Integer.toString(values[limit.ordinal()]);
    }

    public int getLimit(Limit limit){
        return values[limit.ordinal()];
    }

    public int getLimitByIndex(int index){
        return values[index];
    }

    public State getState(Limit limit){
        return states[limit.ordinal()];
    }

    public String getStateLiteral(Limit limit){
        return states[limit.ordinal()].literal();
    }

    public boolean isSet(int index){
        return isSet[index];
    }

    public boolean printEntityCountInfo(){
        return printEntityCountInfo.equals(XalanConstants.JDK_YES);
    }

    public static enum State{
        //this order reflects the overriding order
        DEFAULT("default"),FSP("FEATURE_SECURE_PROCESSING"),
        JAXPDOTPROPERTIES("jaxp.properties"),SYSTEMPROPERTY("system property"),
        APIPROPERTY("property");
        final String literal;

        State(String literal){
            this.literal=literal;
        }

        String literal(){
            return literal;
        }
    }

    public static enum Limit{
        ENTITY_EXPANSION_LIMIT("EntityExpansionLimit",XalanConstants.JDK_ENTITY_EXPANSION_LIMIT,
                XalanConstants.SP_ENTITY_EXPANSION_LIMIT,0,64000),
        MAX_OCCUR_NODE_LIMIT("MaxOccurLimit",XalanConstants.JDK_MAX_OCCUR_LIMIT,
                XalanConstants.SP_MAX_OCCUR_LIMIT,0,5000),
        ELEMENT_ATTRIBUTE_LIMIT("ElementAttributeLimit",XalanConstants.JDK_ELEMENT_ATTRIBUTE_LIMIT,
                XalanConstants.SP_ELEMENT_ATTRIBUTE_LIMIT,0,10000),
        TOTAL_ENTITY_SIZE_LIMIT("TotalEntitySizeLimit",XalanConstants.JDK_TOTAL_ENTITY_SIZE_LIMIT,
                XalanConstants.SP_TOTAL_ENTITY_SIZE_LIMIT,0,50000000),
        GENERAL_ENTITY_SIZE_LIMIT("MaxEntitySizeLimit",XalanConstants.JDK_GENERAL_ENTITY_SIZE_LIMIT,
                XalanConstants.SP_GENERAL_ENTITY_SIZE_LIMIT,0,0),
        PARAMETER_ENTITY_SIZE_LIMIT("MaxEntitySizeLimit",XalanConstants.JDK_PARAMETER_ENTITY_SIZE_LIMIT,
                XalanConstants.SP_PARAMETER_ENTITY_SIZE_LIMIT,0,1000000),
        MAX_ELEMENT_DEPTH_LIMIT("MaxElementDepthLimit",XalanConstants.JDK_MAX_ELEMENT_DEPTH,
                XalanConstants.SP_MAX_ELEMENT_DEPTH,0,0),
        MAX_NAME_LIMIT("MaxXMLNameLimit",XalanConstants.JDK_XML_NAME_LIMIT,
                XalanConstants.SP_XML_NAME_LIMIT,1000,1000),
        ENTITY_REPLACEMENT_LIMIT("EntityReplacementLimit",XalanConstants.JDK_ENTITY_REPLACEMENT_LIMIT,
                XalanConstants.SP_ENTITY_REPLACEMENT_LIMIT,0,3000000);
        final String key;
        final String apiProperty;
        final String systemProperty;
        final int defaultValue;
        final int secureValue;

        Limit(String key,String apiProperty,String systemProperty,int value,int secureValue){
            this.key=key;
            this.apiProperty=apiProperty;
            this.systemProperty=systemProperty;
            this.defaultValue=value;
            this.secureValue=secureValue;
        }

        public boolean equalsAPIPropertyName(String propertyName){
            return (propertyName==null)?false:apiProperty.equals(propertyName);
        }

        public boolean equalsSystemPropertyName(String propertyName){
            return (propertyName==null)?false:systemProperty.equals(propertyName);
        }

        public String key(){
            return key;
        }

        public String apiProperty(){
            return apiProperty;
        }

        String systemProperty(){
            return systemProperty;
        }

        public int defaultValue(){
            return defaultValue;
        }

        int secureValue(){
            return secureValue;
        }
    }

    public static enum NameMap{
        ENTITY_EXPANSION_LIMIT(XalanConstants.SP_ENTITY_EXPANSION_LIMIT,
                XalanConstants.ENTITY_EXPANSION_LIMIT),
        MAX_OCCUR_NODE_LIMIT(XalanConstants.SP_MAX_OCCUR_LIMIT,
                XalanConstants.MAX_OCCUR_LIMIT),
        ELEMENT_ATTRIBUTE_LIMIT(XalanConstants.SP_ELEMENT_ATTRIBUTE_LIMIT,
                XalanConstants.ELEMENT_ATTRIBUTE_LIMIT);
        final String newName;
        final String oldName;

        NameMap(String newName,String oldName){
            this.newName=newName;
            this.oldName=oldName;
        }

        String getOldName(String newName){
            if(newName.equals(this.newName)){
                return oldName;
            }
            return null;
        }
    }
}

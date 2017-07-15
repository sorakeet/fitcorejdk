/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xalan.internal.utils;

import com.sun.org.apache.xalan.internal.XalanConstants;

public final class FeatureManager extends FeaturePropertyBase{
    public FeatureManager(){
        values=new String[Feature.values().length];
        for(Feature feature : Feature.values()){
            values[feature.ordinal()]=feature.defaultValue();
        }
        //read system properties or jaxp.properties
        readSystemProperties();
    }

    private void readSystemProperties(){
        getSystemProperty(Feature.ORACLE_ENABLE_EXTENSION_FUNCTION,
                XalanConstants.SP_ORACLE_ENABLE_EXTENSION_FUNCTION);
    }

    public boolean isFeatureEnabled(Feature feature){
        return Boolean.parseBoolean(values[feature.ordinal()]);
    }

    public boolean isFeatureEnabled(String propertyName){
        return Boolean.parseBoolean(values[getIndex(propertyName)]);
    }

    public int getIndex(String propertyName){
        for(Feature feature : Feature.values()){
            if(feature.equalsName(propertyName)){
                return feature.ordinal();
            }
        }
        return -1;
    }

    public static enum State{
        //this order reflects the overriding order
        DEFAULT,FSP,JAXPDOTPROPERTIES,SYSTEMPROPERTY,APIPROPERTY
    }

    public static enum Feature{
        ORACLE_ENABLE_EXTENSION_FUNCTION(XalanConstants.ORACLE_ENABLE_EXTENSION_FUNCTION,
                "true");
        final String name;
        final String defaultValue;

        Feature(String name,String value){
            this.name=name;
            this.defaultValue=value;
        }

        public boolean equalsName(String propertyName){
            return (propertyName==null)?false:name.equals(propertyName);
        }

        String defaultValue(){
            return defaultValue;
        }
    }
}

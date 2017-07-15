/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

import java.util.*;

public class ParserConfigurationSettings
        implements XMLComponentManager{
    protected static final String PARSER_SETTINGS=
            Constants.XERCES_FEATURE_PREFIX+Constants.PARSER_SETTINGS;
    //
    // Data
    //
    // data
    protected Set<String> fRecognizedProperties;
    protected Map<String,Object> fProperties;
    protected Set<String> fRecognizedFeatures;
    protected Map<String,Boolean> fFeatures;
    protected XMLComponentManager fParentSettings;
    //
    // Constructors
    //

    public ParserConfigurationSettings(){
        this(null);
    } // <init>()

    public ParserConfigurationSettings(XMLComponentManager parent){
        // create storage for recognized features and properties
        fRecognizedFeatures=new HashSet<String>();
        fRecognizedProperties=new HashSet<String>();
        // create table for features and properties
        fFeatures=new HashMap<String,Boolean>();
        fProperties=new HashMap<String,Object>();
        // save parent
        fParentSettings=parent;
    } // <init>(XMLComponentManager)
    //
    // XMLParserConfiguration methods
    //

    public void addRecognizedFeatures(String[] featureIds){
        // add recognized features
        int featureIdsCount=featureIds!=null?featureIds.length:0;
        for(int i=0;i<featureIdsCount;i++){
            String featureId=featureIds[i];
            if(!fRecognizedFeatures.contains(featureId)){
                fRecognizedFeatures.add(featureId);
            }
        }
    } // addRecognizedFeatures(String[])

    public void setFeature(String featureId,boolean state)
            throws XMLConfigurationException{
        // check and store
        FeatureState checkState=checkFeature(featureId);
        if(checkState.isExceptional()){
            throw new XMLConfigurationException(checkState.status,featureId);
        }
        fFeatures.put(featureId,state);
    } // setFeature(String,boolean)

    protected FeatureState checkFeature(String featureId)
            throws XMLConfigurationException{
        // check feature
        if(!fRecognizedFeatures.contains(featureId)){
            if(fParentSettings!=null){
                return fParentSettings.getFeatureState(featureId);
            }else{
                return FeatureState.NOT_RECOGNIZED;
            }
        }
        // TODO: reasonable default?
        return FeatureState.RECOGNIZED;
    } // checkFeature(String)

    public void addRecognizedProperties(String[] propertyIds){
        fRecognizedProperties.addAll(Arrays.asList(propertyIds));
    } // addRecognizedProperties(String[])
    //
    // XMLComponentManager methods
    //

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        // check and store
        PropertyState checkState=checkProperty(propertyId);
        if(checkState.isExceptional()){
            throw new XMLConfigurationException(checkState.status,propertyId);
        }
        fProperties.put(propertyId,value);
    } // setProperty(String,Object)

    protected PropertyState checkProperty(String propertyId)
            throws XMLConfigurationException{
        // check property
        if(!fRecognizedProperties.contains(propertyId)){
            if(fParentSettings!=null){
                PropertyState state=fParentSettings.getPropertyState(propertyId);
                if(state.isExceptional()){
                    return state;
                }
            }else{
                return PropertyState.NOT_RECOGNIZED;
            }
        }
        return PropertyState.RECOGNIZED;
    } // checkProperty(String)

    public final boolean getFeature(String featureId)
            throws XMLConfigurationException{
        FeatureState state=getFeatureState(featureId);
        if(state.isExceptional()){
            throw new XMLConfigurationException(state.status,featureId);
        }
        return state.state;
    } // getFeature(String):boolean

    public final boolean getFeature(String featureId,boolean defaultValue){
        FeatureState state=getFeatureState(featureId);
        if(state.isExceptional()){
            return defaultValue;
        }
        return state.state;
    }

    public final Object getProperty(String propertyId)
            throws XMLConfigurationException{
        PropertyState state=getPropertyState(propertyId);
        if(state.isExceptional()){
            throw new XMLConfigurationException(state.status,propertyId);
        }
        return state.state;
    } // getProperty(String):Object

    public final Object getProperty(String propertyId,Object defaultValue){
        PropertyState state=getPropertyState(propertyId);
        if(state.isExceptional()){
            return defaultValue;
        }
        return state.state;
    }
    //
    // Protected methods
    //

    public FeatureState getFeatureState(String featureId){
        Boolean state=(Boolean)fFeatures.get(featureId);
        if(state==null){
            FeatureState checkState=checkFeature(featureId);
            if(checkState.isExceptional()){
                return checkState;
            }
            return FeatureState.is(false);
        }
        return FeatureState.is(state);
    }

    public PropertyState getPropertyState(String propertyId){
        Object propertyValue=fProperties.get(propertyId);
        if(propertyValue==null){
            PropertyState state=checkProperty(propertyId);
            if(state.isExceptional()){
                return state;
            }
        }
        return PropertyState.is(propertyValue);
    }
} // class ParserConfigurationSettings

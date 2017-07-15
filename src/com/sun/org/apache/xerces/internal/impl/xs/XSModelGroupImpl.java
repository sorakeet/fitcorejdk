/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002,2003-2004 The Apache Software Foundation.
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
 * Copyright 2002,2003-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSModelGroupImpl implements XSModelGroup{
    // types of model groups
    // REVISIT: can't use same constants as those for particles, because
    // there are place where the constants are used together. For example,
    // to check whether the content is an element or a sequence.
    public static final short MODELGROUP_CHOICE=101;
    public static final short MODELGROUP_SEQUENCE=102;
    public static final short MODELGROUP_ALL=103;
    // compositor of the model group
    public short fCompositor;
    // particles
    public XSParticleDecl[] fParticles=null;
    public int fParticleCount=0;
    // this particle's optional annotations
    public XSObjectList fAnnotations=null;
    private String fDescription=null;

    // whether this model group contains nothing
    public boolean isEmpty(){
        for(int i=0;i<fParticleCount;i++){
            if(!fParticles[i].isEmpty())
                return false;
        }
        return true;
    }

    public int minEffectiveTotalRange(){
        if(fCompositor==MODELGROUP_CHOICE)
            return minEffectiveTotalRangeChoice();
        else
            return minEffectiveTotalRangeAllSeq();
    }

    // return the sum of all min values of the particles
    private int minEffectiveTotalRangeAllSeq(){
        int total=0;
        for(int i=0;i<fParticleCount;i++)
            total+=fParticles[i].minEffectiveTotalRange();
        return total;
    }

    // return the min of all min values of the particles
    private int minEffectiveTotalRangeChoice(){
        int min=0, one;
        if(fParticleCount>0)
            min=fParticles[0].minEffectiveTotalRange();
        for(int i=1;i<fParticleCount;i++){
            one=fParticles[i].minEffectiveTotalRange();
            if(one<min)
                min=one;
        }
        return min;
    }

    public int maxEffectiveTotalRange(){
        if(fCompositor==MODELGROUP_CHOICE)
            return maxEffectiveTotalRangeChoice();
        else
            return maxEffectiveTotalRangeAllSeq();
    }

    // if one of the max value of the particles is unbounded, return unbounded;
    // otherwise return the sum of all max values
    private int maxEffectiveTotalRangeAllSeq(){
        int total=0, one;
        for(int i=0;i<fParticleCount;i++){
            one=fParticles[i].maxEffectiveTotalRange();
            if(one==SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            total+=one;
        }
        return total;
    }

    // if one of the max value of the particles is unbounded, return unbounded;
    // otherwise return the max of all max values
    private int maxEffectiveTotalRangeChoice(){
        int max=0, one;
        if(fParticleCount>0){
            max=fParticles[0].maxEffectiveTotalRange();
            if(max==SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
        }
        for(int i=1;i<fParticleCount;i++){
            one=fParticles[i].maxEffectiveTotalRange();
            if(one==SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            if(one>max)
                max=one;
        }
        return max;
    }

    public String toString(){
        // REVISIT: Commented code may help to eliminate redundant parentheses (test first before committing)
        if(fDescription==null){
            StringBuffer buffer=new StringBuffer();
            if(fCompositor==MODELGROUP_ALL)
                buffer.append("all(");
            else  //if (fMinOccurs != 1 || fMaxOccurs != 1)
                buffer.append('(');
            if(fParticleCount>0)
                buffer.append(fParticles[0].toString());
            for(int i=1;i<fParticleCount;i++){
                if(fCompositor==MODELGROUP_CHOICE)
                    buffer.append('|');
                else
                    buffer.append(',');
                buffer.append(fParticles[i].toString());
            }
            //if (fCompositor == MODELGROUP_ALL || fMinOccurs != 1 || fMaxOccurs != 1)
            buffer.append(')');
            fDescription=buffer.toString();
        }
        return fDescription;
    }

    public void reset(){
        fCompositor=MODELGROUP_SEQUENCE;
        fParticles=null;
        fParticleCount=0;
        fDescription=null;
        fAnnotations=null;
    }

    public short getType(){
        return XSConstants.MODEL_GROUP;
    }

    public String getName(){
        return null;
    }

    public String getNamespace(){
        return null;
    }

    public XSNamespaceItem getNamespaceItem(){
        return null;
    }

    public short getCompositor(){
        if(fCompositor==MODELGROUP_CHOICE)
            return XSModelGroup.COMPOSITOR_CHOICE;
        else if(fCompositor==MODELGROUP_SEQUENCE)
            return XSModelGroup.COMPOSITOR_SEQUENCE;
        else
            return XSModelGroup.COMPOSITOR_ALL;
    }

    public XSObjectList getParticles(){
        return new XSObjectListImpl(fParticles,fParticleCount);
    }

    public XSAnnotation getAnnotation(){
        return (fAnnotations!=null)?(XSAnnotation)fAnnotations.item(0):null;
    }

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }
} // class XSModelGroupImpl

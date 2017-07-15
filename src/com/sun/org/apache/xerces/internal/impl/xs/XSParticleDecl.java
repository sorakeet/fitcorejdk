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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.*;

public class XSParticleDecl implements XSParticle{
    // types of particles
    public static final short PARTICLE_EMPTY=0;
    public static final short PARTICLE_ELEMENT=1;
    public static final short PARTICLE_WILDCARD=2;
    public static final short PARTICLE_MODELGROUP=3;
    public static final short PARTICLE_ZERO_OR_MORE=4;
    public static final short PARTICLE_ZERO_OR_ONE=5;
    public static final short PARTICLE_ONE_OR_MORE=6;
    // type of the particle
    public short fType=PARTICLE_EMPTY;
    // term of the particle
    // for PARTICLE_ELEMENT : the element decl
    // for PARTICLE_WILDCARD: the wildcard decl
    // for PARTICLE_MODELGROUP: the model group
    public XSTerm fValue=null;
    // minimum occurrence of this particle
    public int fMinOccurs=1;
    // maximum occurrence of this particle
    public int fMaxOccurs=1;
    // optional annotation
    public XSObjectList fAnnotations=null;
    private String fDescription=null;

    // clone this decl
    public XSParticleDecl makeClone(){
        XSParticleDecl particle=new XSParticleDecl();
        particle.fType=fType;
        particle.fMinOccurs=fMinOccurs;
        particle.fMaxOccurs=fMaxOccurs;
        particle.fDescription=fDescription;
        particle.fValue=fValue;
        particle.fAnnotations=fAnnotations;
        return particle;
    }

    public boolean emptiable(){
        return minEffectiveTotalRange()==0;
    }

    public int minEffectiveTotalRange(){
        if(fType==XSParticleDecl.PARTICLE_EMPTY){
            return 0;
        }
        if(fType==PARTICLE_MODELGROUP){
            return ((XSModelGroupImpl)fValue).minEffectiveTotalRange()*fMinOccurs;
        }
        return fMinOccurs;
    }

    // whether this particle contains nothing
    public boolean isEmpty(){
        if(fType==PARTICLE_EMPTY)
            return true;
        if(fType==PARTICLE_ELEMENT||fType==PARTICLE_WILDCARD)
            return false;
        return ((XSModelGroupImpl)fValue).isEmpty();
    }

    public int maxEffectiveTotalRange(){
        if(fType==XSParticleDecl.PARTICLE_EMPTY){
            return 0;
        }
        if(fType==PARTICLE_MODELGROUP){
            int max=((XSModelGroupImpl)fValue).maxEffectiveTotalRange();
            if(max==SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            if(max!=0&&fMaxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            return max*fMaxOccurs;
        }
        return fMaxOccurs;
    }

    public String toString(){
        if(fDescription==null){
            StringBuffer buffer=new StringBuffer();
            appendParticle(buffer);
            if(!(fMinOccurs==0&&fMaxOccurs==0||
                    fMinOccurs==1&&fMaxOccurs==1)){
                buffer.append('{').append(fMinOccurs);
                if(fMaxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED)
                    buffer.append("-UNBOUNDED");
                else if(fMinOccurs!=fMaxOccurs)
                    buffer.append('-').append(fMaxOccurs);
                buffer.append('}');
            }
            fDescription=buffer.toString();
        }
        return fDescription;
    }

    void appendParticle(StringBuffer buffer){
        switch(fType){
            case PARTICLE_EMPTY:
                buffer.append("EMPTY");
                break;
            case PARTICLE_ELEMENT:
                buffer.append(fValue.toString());
                break;
            case PARTICLE_WILDCARD:
                buffer.append('(');
                buffer.append(fValue.toString());
                buffer.append(')');
                break;
            case PARTICLE_MODELGROUP:
                buffer.append(fValue.toString());
                break;
        }
    }

    public void reset(){
        fType=PARTICLE_EMPTY;
        fValue=null;
        fMinOccurs=1;
        fMaxOccurs=1;
        fDescription=null;
        fAnnotations=null;
    }

    public short getType(){
        return XSConstants.PARTICLE;
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

    public int getMinOccurs(){
        return fMinOccurs;
    }

    public int getMaxOccurs(){
        return fMaxOccurs;
    }

    public boolean getMaxOccursUnbounded(){
        return fMaxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED;
    }

    public XSTerm getTerm(){
        return fValue;
    }

    public XSObjectList getAnnotations(){
        return (fAnnotations!=null)?fAnnotations:XSObjectListImpl.EMPTY_LIST;
    }
} // class XSParticleDecl

/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.text;

public class FieldPosition{
    int field=0;
    int endIndex=0;
    int beginIndex=0;
    private Format.Field attribute;

    public FieldPosition(int field){
        this.field=field;
    }

    public FieldPosition(Format.Field attribute){
        this(attribute,-1);
    }

    public FieldPosition(Format.Field attribute,int fieldID){
        this.attribute=attribute;
        this.field=fieldID;
    }

    public Format.Field getFieldAttribute(){
        return attribute;
    }

    public int getField(){
        return field;
    }

    public int getBeginIndex(){
        return beginIndex;
    }

    public void setBeginIndex(int bi){
        beginIndex=bi;
    }

    public int getEndIndex(){
        return endIndex;
    }

    public void setEndIndex(int ei){
        endIndex=ei;
    }

    Format.FieldDelegate getFieldDelegate(){
        return new Delegate();
    }

    public int hashCode(){
        return (field<<24)|(beginIndex<<16)|endIndex;
    }

    public boolean equals(Object obj){
        if(obj==null) return false;
        if(!(obj instanceof FieldPosition))
            return false;
        FieldPosition other=(FieldPosition)obj;
        if(attribute==null){
            if(other.attribute!=null){
                return false;
            }
        }else if(!attribute.equals(other.attribute)){
            return false;
        }
        return (beginIndex==other.beginIndex
                &&endIndex==other.endIndex
                &&field==other.field);
    }

    public String toString(){
        return getClass().getName()+
                "[field="+field+",attribute="+attribute+
                ",beginIndex="+beginIndex+
                ",endIndex="+endIndex+']';
    }

    private boolean matchesField(Format.Field attribute){
        if(this.attribute!=null){
            return this.attribute.equals(attribute);
        }
        return false;
    }

    private boolean matchesField(Format.Field attribute,int field){
        if(this.attribute!=null){
            return this.attribute.equals(attribute);
        }
        return (field==this.field);
    }

    private class Delegate implements Format.FieldDelegate{
        private boolean encounteredField;

        public void formatted(Format.Field attr,Object value,int start,
                              int end,StringBuffer buffer){
            if(!encounteredField&&matchesField(attr)){
                setBeginIndex(start);
                setEndIndex(end);
                encounteredField=(start!=end);
            }
        }

        public void formatted(int fieldID,Format.Field attr,Object value,
                              int start,int end,StringBuffer buffer){
            if(!encounteredField&&matchesField(attr,fieldID)){
                setBeginIndex(start);
                setEndIndex(end);
                encounteredField=(start!=end);
            }
        }
    }
}

/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: ExpandedNameTable.java,v 1.2.4.1 2005/09/15 08:15:06 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * $Id: ExpandedNameTable.java,v 1.2.4.1 2005/09/15 08:15:06 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTM;

public class ExpandedNameTable{
    // These are all the types prerotated, for caller convenience.
    public static final int ELEMENT=((int)DTM.ELEMENT_NODE);
    public static final int ATTRIBUTE=((int)DTM.ATTRIBUTE_NODE);
    public static final int TEXT=((int)DTM.TEXT_NODE);
    public static final int CDATA_SECTION=((int)DTM.CDATA_SECTION_NODE);
    public static final int ENTITY_REFERENCE=((int)DTM.ENTITY_REFERENCE_NODE);
    public static final int ENTITY=((int)DTM.ENTITY_NODE);
    public static final int PROCESSING_INSTRUCTION=((int)DTM.PROCESSING_INSTRUCTION_NODE);
    public static final int COMMENT=((int)DTM.COMMENT_NODE);
    public static final int DOCUMENT=((int)DTM.DOCUMENT_NODE);
    public static final int DOCUMENT_TYPE=((int)DTM.DOCUMENT_TYPE_NODE);
    public static final int DOCUMENT_FRAGMENT=((int)DTM.DOCUMENT_FRAGMENT_NODE);
    public static final int NOTATION=((int)DTM.NOTATION_NODE);
    public static final int NAMESPACE=((int)DTM.NAMESPACE_NODE);
    private static int m_initialSize=128;
    private static ExtendedType[] m_defaultExtendedTypes;
    private static float m_loadFactor=0.75f;
    private static int m_initialCapacity=203;

    /**
     * Init default values
     */
    static{
        m_defaultExtendedTypes=new ExtendedType[DTM.NTYPES];
        for(int i=0;i<DTM.NTYPES;i++){
            m_defaultExtendedTypes[i]=new ExtendedType(i,"","");
        }
    }

    ExtendedType hashET=new ExtendedType(-1,"","");
    private ExtendedType[] m_extendedTypes;
    // %REVIEW% Since this is (should be) always equal
    // to the length of m_extendedTypes, do we need this?
    private int m_nextType;
    private int m_capacity;
    private int m_threshold;
    private HashEntry[] m_table;

    public ExpandedNameTable(){
        m_capacity=m_initialCapacity;
        m_threshold=(int)(m_capacity*m_loadFactor);
        m_table=new HashEntry[m_capacity];
        initExtendedTypes();
    }

    private void initExtendedTypes(){
        m_extendedTypes=new ExtendedType[m_initialSize];
        for(int i=0;i<DTM.NTYPES;i++){
            m_extendedTypes[i]=m_defaultExtendedTypes[i];
            m_table[i]=new HashEntry(m_defaultExtendedTypes[i],i,i,null);
        }
        m_nextType=DTM.NTYPES;
    }

    public int getExpandedTypeID(String namespace,String localName,int type){
        return getExpandedTypeID(namespace,localName,type,false);
    }

    public int getExpandedTypeID(String namespace,String localName,int type,boolean searchOnly){
        if(null==namespace)
            namespace="";
        if(null==localName)
            localName="";
        // Calculate the hash code
        int hash=type+namespace.hashCode()+localName.hashCode();
        // Redefine the hashET object to represent the new expanded name.
        hashET.redefine(type,namespace,localName,hash);
        // Calculate the index into the HashEntry table.
        int index=hash%m_capacity;
        if(index<0)
            index=-index;
        // Look up the expanded name in the hash table. Return the id if
        // the expanded name is already in the hash table.
        for(HashEntry e=m_table[index];e!=null;e=e.next){
            if(e.hash==hash&&e.key.equals(hashET))
                return e.value;
        }
        if(searchOnly){
            return DTM.NULL;
        }
        // Expand the internal HashEntry array if necessary.
        if(m_nextType>m_threshold){
            rehash();
            index=hash%m_capacity;
            if(index<0)
                index=-index;
        }
        // Create a new ExtendedType object
        ExtendedType newET=new ExtendedType(type,namespace,localName,hash);
        // Expand the m_extendedTypes array if necessary.
        if(m_extendedTypes.length==m_nextType){
            ExtendedType[] newArray=new ExtendedType[m_extendedTypes.length*2];
            System.arraycopy(m_extendedTypes,0,newArray,0,
                    m_extendedTypes.length);
            m_extendedTypes=newArray;
        }
        m_extendedTypes[m_nextType]=newET;
        // Create a new hash entry for the new ExtendedType and put it into
        // the table.
        HashEntry entry=new HashEntry(newET,m_nextType,hash,m_table[index]);
        m_table[index]=entry;
        return m_nextType++;
    }

    private void rehash(){
        int oldCapacity=m_capacity;
        HashEntry[] oldTable=m_table;
        int newCapacity=2*oldCapacity+1;
        m_capacity=newCapacity;
        m_threshold=(int)(newCapacity*m_loadFactor);
        m_table=new HashEntry[newCapacity];
        for(int i=oldCapacity-1;i>=0;i--){
            for(HashEntry old=oldTable[i];old!=null;){
                HashEntry e=old;
                old=old.next;
                int newIndex=e.hash%newCapacity;
                if(newIndex<0)
                    newIndex=-newIndex;
                e.next=m_table[newIndex];
                m_table[newIndex]=e;
            }
        }
    }

    public int getExpandedTypeID(int type){
        return type;
    }

    public String getLocalName(int ExpandedNameID){
        return m_extendedTypes[ExpandedNameID].getLocalName();
    }

    public final int getLocalNameID(int ExpandedNameID){
        // ExtendedType etype = m_extendedTypes[ExpandedNameID];
        if(m_extendedTypes[ExpandedNameID].getLocalName().equals(""))
            return 0;
        else
            return ExpandedNameID;
    }

    public String getNamespace(int ExpandedNameID){
        String namespace=m_extendedTypes[ExpandedNameID].getNamespace();
        return (namespace.equals("")?null:namespace);
    }

    public final int getNamespaceID(int ExpandedNameID){
        //ExtendedType etype = m_extendedTypes[ExpandedNameID];
        if(m_extendedTypes[ExpandedNameID].getNamespace().equals(""))
            return 0;
        else
            return ExpandedNameID;
    }

    public final short getType(int ExpandedNameID){
        //ExtendedType etype = m_extendedTypes[ExpandedNameID];
        return (short)m_extendedTypes[ExpandedNameID].getNodeType();
    }

    public int getSize(){
        return m_nextType;
    }

    public ExtendedType[] getExtendedTypes(){
        return m_extendedTypes;
    }

    private static final class HashEntry{
        ExtendedType key;
        int value;
        int hash;
        HashEntry next;

        protected HashEntry(ExtendedType key,int value,int hash,HashEntry next){
            this.key=key;
            this.value=value;
            this.hash=hash;
            this.next=next;
        }
    }
}

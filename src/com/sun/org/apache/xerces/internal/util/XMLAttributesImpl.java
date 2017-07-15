/**
 * Copyright (c) 2005, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.xml.internal.stream.XMLBufferListener;

public class XMLAttributesImpl
        implements XMLAttributes, XMLBufferListener{
    //
    // Constants
    //
    protected static final int TABLE_SIZE=101;
    protected static final int MAX_HASH_COLLISIONS=40;
    protected static final int MULTIPLIERS_SIZE=1<<5;
    protected static final int MULTIPLIERS_MASK=MULTIPLIERS_SIZE-1;
    protected static final int SIZE_LIMIT=20;
    //
    // Data
    //
    // features
    protected boolean fNamespaces=true;
    // data
    protected int fLargeCount=1;
    protected int fLength;
    protected Attribute[] fAttributes=new Attribute[4];
    protected Attribute[] fAttributeTableView;
    protected int[] fAttributeTableViewChainState;
    protected int fTableViewBuckets;
    protected boolean fIsTableViewConsistent;
    protected int[] fHashMultipliers;
    //
    // Constructors
    //

    public XMLAttributesImpl(){
        this(TABLE_SIZE);
    }

    public XMLAttributesImpl(int tableSize){
        fTableViewBuckets=tableSize;
        for(int i=0;i<fAttributes.length;i++){
            fAttributes[i]=new Attribute();
        }
    } // <init>()
    //
    // Public methods
    //

    public void setNamespaces(boolean namespaces){
        fNamespaces=namespaces;
    } // setNamespaces(boolean)
    //
    // XMLAttributes methods
    //

    public int addAttribute(QName name,String type,String value){
        return addAttribute(name,type,value,null);
    }

    public void removeAllAttributes(){
        fLength=0;
    } // removeAllAttributes()

    public void removeAttributeAt(int attrIndex){
        fIsTableViewConsistent=false;
        if(attrIndex<fLength-1){
            Attribute removedAttr=fAttributes[attrIndex];
            System.arraycopy(fAttributes,attrIndex+1,
                    fAttributes,attrIndex,fLength-attrIndex-1);
            // Make the discarded Attribute object available for re-use
            // by tucking it after the Attributes that are still in use
            fAttributes[fLength-1]=removedAttr;
        }
        fLength--;
    } // removeAttributeAt(int)

    public int getLength(){
        return fLength;
    } // getLength():int

    public int getIndex(String qName){
        for(int i=0;i<fLength;i++){
            Attribute attribute=fAttributes[i];
            if(attribute.name.rawname!=null&&
                    attribute.name.rawname.equals(qName)){
                return i;
            }
        }
        return -1;
    } // getIndex(String):int

    public int getIndex(String uri,String localPart){
        for(int i=0;i<fLength;i++){
            Attribute attribute=fAttributes[i];
            if(attribute.name.localpart!=null&&
                    attribute.name.localpart.equals(localPart)&&
                    ((uri==attribute.name.uri)||
                            (uri!=null&&attribute.name.uri!=null&&attribute.name.uri.equals(uri)))){
                return i;
            }
        }
        return -1;
    } // getIndex(String,String):int

    public void setName(int attrIndex,QName attrName){
        fAttributes[attrIndex].name.setValues(attrName);
    } // setName(int,QName)

    public void getName(int attrIndex,QName attrName){
        attrName.setValues(fAttributes[attrIndex].name);
    } // getName(int,QName)

    public String getPrefix(int index){
        if(index<0||index>=fLength){
            return null;
        }
        String prefix=fAttributes[index].name.prefix;
        // REVISIT: The empty string is not entered in the symbol table!
        return prefix!=null?prefix:"";
    } // getPrefix(int):String

    public String getURI(int index){
        if(index<0||index>=fLength){
            return null;
        }
        String uri=fAttributes[index].name.uri;
        return uri;
    } // getURI(int):String

    public String getLocalName(int index){
        if(!fNamespaces){
            return "";
        }
        if(index<0||index>=fLength){
            return null;
        }
        return fAttributes[index].name.localpart;
    } // getLocalName(int):String

    public String getQName(int index){
        if(index<0||index>=fLength){
            return null;
        }
        String rawname=fAttributes[index].name.rawname;
        return rawname!=null?rawname:"";
    } // getQName(int):String

    public QName getQualifiedName(int index){
        if(index<0||index>=fLength){
            return null;
        }
        return fAttributes[index].name;
    }
    //
    // AttributeList and Attributes methods
    //

    public void setType(int attrIndex,String attrType){
        fAttributes[attrIndex].type=attrType;
    } // setType(int,String)

    public String getType(int index){
        if(index<0||index>=fLength){
            return null;
        }
        return getReportableType(fAttributes[index].type);
    } // getType(int):String

    public String getType(String qname){
        int index=getIndex(qname);
        return index!=-1?getReportableType(fAttributes[index].type):null;
    } // getType(String):String

    public String getType(String uri,String localName){
        if(!fNamespaces){
            return null;
        }
        int index=getIndex(uri,localName);
        return index!=-1?getType(index):null;
    } // getType(String,String):String

    public void setValue(int attrIndex,String attrValue){
        setValue(attrIndex,attrValue,null);
    }
    //
    // AttributeList methods
    //

    public void setValue(int attrIndex,String attrValue,XMLString value){
        Attribute attribute=fAttributes[attrIndex];
        attribute.value=attrValue;
        attribute.nonNormalizedValue=attrValue;
        attribute.xmlValue=value;
    } // setValue(int,String)
    //
    // Attributes methods
    //

    public String getValue(int index){
        if(index<0||index>=fLength){
            return null;
        }
        if(fAttributes[index].value==null&&fAttributes[index].xmlValue!=null)
            fAttributes[index].value=fAttributes[index].xmlValue.toString();
        return fAttributes[index].value;
    } // getValue(int):String

    public String getValue(String qname){
        int index=getIndex(qname);
        if(index==-1)
            return null;
        if(fAttributes[index].value==null)
            fAttributes[index].value=fAttributes[index].xmlValue.toString();
        return fAttributes[index].value;
    } // getValue(String):String

    public String getValue(String uri,String localName){
        int index=getIndex(uri,localName);
        return index!=-1?getValue(index):null;
    } // getValue(String,String):String

    public void setNonNormalizedValue(int attrIndex,String attrValue){
        if(attrValue==null){
            attrValue=fAttributes[attrIndex].value;
        }
        fAttributes[attrIndex].nonNormalizedValue=attrValue;
    } // setNonNormalizedValue(int,String)

    public String getNonNormalizedValue(int attrIndex){
        String value=fAttributes[attrIndex].nonNormalizedValue;
        return value;
    } // getNonNormalizedValue(int):String

    public void setSpecified(int attrIndex,boolean specified){
        fAttributes[attrIndex].specified=specified;
    } // setSpecified(int,boolean)

    public boolean isSpecified(int attrIndex){
        return fAttributes[attrIndex].specified;
    } // isSpecified(int):boolean

    public Augmentations getAugmentations(int attributeIndex){
        if(attributeIndex<0||attributeIndex>=fLength){
            return null;
        }
        return fAttributes[attributeIndex].augs;
    }

    public Augmentations getAugmentations(String uri,String localName){
        int index=getIndex(uri,localName);
        return index!=-1?fAttributes[index].augs:null;
    }

    public Augmentations getAugmentations(String qName){
        int index=getIndex(qName);
        return index!=-1?fAttributes[index].augs:null;
    }

    public void setAugmentations(int attrIndex,Augmentations augs){
        fAttributes[attrIndex].augs=augs;
    }

    private String getReportableType(String type){
        if(type.charAt(0)=='('){
            return "NMTOKEN";
        }
        return type;
    }

    public int addAttribute(QName name,String type,String value,XMLString valueCache){
        int index;
        if(fLength<SIZE_LIMIT){
            index=name.uri!=null&&!name.uri.equals("")
                    ?getIndexFast(name.uri,name.localpart)
                    :getIndexFast(name.rawname);
            if(index==-1){
                index=fLength;
                if(fLength++==fAttributes.length){
                    Attribute[] attributes=new Attribute[fAttributes.length+4];
                    System.arraycopy(fAttributes,0,attributes,0,fAttributes.length);
                    for(int i=fAttributes.length;i<attributes.length;i++){
                        attributes[i]=new Attribute();
                    }
                    fAttributes=attributes;
                }
            }
        }else if(name.uri==null||
                name.uri.length()==0||
                (index=getIndexFast(name.uri,name.localpart))==-1){
            /**
             * If attributes were removed from the list after the table
             * becomes in use this isn't reflected in the table view. It's
             * assumed that once a user starts removing attributes they're
             * not likely to add more. We only make the view consistent if
             * the user of this class adds attributes, removes them, and
             * then adds more.
             */
            if(!fIsTableViewConsistent||fLength==SIZE_LIMIT||
                    (fLength>SIZE_LIMIT&&fLength>fTableViewBuckets)){
                prepareAndPopulateTableView();
                fIsTableViewConsistent=true;
            }
            int bucket=getTableViewBucket(name.rawname);
            // The chain is stale.
            // This must be a unique attribute.
            if(fAttributeTableViewChainState[bucket]!=fLargeCount){
                index=fLength;
                if(fLength++==fAttributes.length){
                    Attribute[] attributes=new Attribute[fAttributes.length<<1];
                    System.arraycopy(fAttributes,0,attributes,0,fAttributes.length);
                    for(int i=fAttributes.length;i<attributes.length;i++){
                        attributes[i]=new Attribute();
                    }
                    fAttributes=attributes;
                }
                // Update table view.
                fAttributeTableViewChainState[bucket]=fLargeCount;
                fAttributes[index].next=null;
                fAttributeTableView[bucket]=fAttributes[index];
            }
            // This chain is active.
            // We need to check if any of the attributes has the same rawname.
            else{
                // Search the table.
                int collisionCount=0;
                Attribute found=fAttributeTableView[bucket];
                while(found!=null){
                    if(found.name.rawname==name.rawname){
                        break;
                    }
                    found=found.next;
                    ++collisionCount;
                }
                // This attribute is unique.
                if(found==null){
                    index=fLength;
                    if(fLength++==fAttributes.length){
                        Attribute[] attributes=new Attribute[fAttributes.length<<1];
                        System.arraycopy(fAttributes,0,attributes,0,fAttributes.length);
                        for(int i=fAttributes.length;i<attributes.length;i++){
                            attributes[i]=new Attribute();
                        }
                        fAttributes=attributes;
                    }
                    // Select a new hash function and rehash the table view
                    // if the collision threshold is exceeded.
                    if(collisionCount>=MAX_HASH_COLLISIONS){
                        // The current attribute will be processed in the rehash.
                        // Need to set its name first.
                        fAttributes[index].name.setValues(name);
                        rebalanceTableView(fLength);
                    }else{
                        // Update table view
                        fAttributes[index].next=fAttributeTableView[bucket];
                        fAttributeTableView[bucket]=fAttributes[index];
                    }
                }
                // Duplicate. We still need to find the index.
                else{
                    index=getIndexFast(name.rawname);
                }
            }
        }
        // set values
        Attribute attribute=fAttributes[index];
        attribute.name.setValues(name);
        attribute.type=type;
        attribute.value=value;
        attribute.xmlValue=valueCache;
        attribute.nonNormalizedValue=value;
        attribute.specified=false;
        // clear augmentations
        if(attribute.augs!=null)
            attribute.augs.removeAllItems();
        return index;
    } // addAttribute(QName,String,XMLString)

    public String getName(int index){
        if(index<0||index>=fLength){
            return null;
        }
        return fAttributes[index].name.rawname;
    } // getName(int):String

    public int getIndexByLocalName(String localPart){
        for(int i=0;i<fLength;i++){
            Attribute attribute=fAttributes[i];
            if(attribute.name.localpart!=null&&
                    attribute.name.localpart.equals(localPart)){
                return i;
            }
        }
        return -1;
    } // getIndex(String):int

    public int getIndexFast(String qName){
        for(int i=0;i<fLength;++i){
            Attribute attribute=fAttributes[i];
            if(attribute.name.rawname==qName){
                return i;
            }
        }
        return -1;
    } // getIndexFast(String):int

    public void addAttributeNS(QName name,String type,String value){
        int index=fLength;
        if(fLength++==fAttributes.length){
            Attribute[] attributes;
            if(fLength<SIZE_LIMIT){
                attributes=new Attribute[fAttributes.length+4];
            }else{
                attributes=new Attribute[fAttributes.length<<1];
            }
            System.arraycopy(fAttributes,0,attributes,0,fAttributes.length);
            for(int i=fAttributes.length;i<attributes.length;i++){
                attributes[i]=new Attribute();
            }
            fAttributes=attributes;
        }
        // set values
        Attribute attribute=fAttributes[index];
        attribute.name.setValues(name);
        attribute.type=type;
        attribute.value=value;
        attribute.nonNormalizedValue=value;
        attribute.specified=false;
        // clear augmentations
        attribute.augs.removeAllItems();
    }

    public QName checkDuplicatesNS(){
        // If the list is small check for duplicates using pairwise comparison.
        final int length=fLength;
        if(length<=SIZE_LIMIT){
            final Attribute[] attributes=fAttributes;
            for(int i=0;i<length-1;++i){
                Attribute att1=attributes[i];
                for(int j=i+1;j<length;++j){
                    Attribute att2=attributes[j];
                    if(att1.name.localpart==att2.name.localpart&&
                            att1.name.uri==att2.name.uri){
                        return att2.name;
                    }
                }
            }
            return null;
        }
        // If the list is large check duplicates using a hash table.
        else{
            return checkManyDuplicatesNS();
        }
    }

    private QName checkManyDuplicatesNS(){
        // We don't want this table view to be read if someone calls
        // addAttribute so we invalidate it up front.
        fIsTableViewConsistent=false;
        prepareTableView();
        Attribute attr;
        int bucket;
        final int length=fLength;
        final Attribute[] attributes=fAttributes;
        final Attribute[] attributeTableView=fAttributeTableView;
        final int[] attributeTableViewChainState=fAttributeTableViewChainState;
        int largeCount=fLargeCount;
        for(int i=0;i<length;++i){
            attr=attributes[i];
            bucket=getTableViewBucket(attr.name.localpart,attr.name.uri);
            // The chain is stale.
            // This must be a unique attribute.
            if(attributeTableViewChainState[bucket]!=largeCount){
                attributeTableViewChainState[bucket]=largeCount;
                attr.next=null;
                attributeTableView[bucket]=attr;
            }
            // This chain is active.
            // We need to check if any of the attributes has the same name.
            else{
                // Search the table.
                int collisionCount=0;
                Attribute found=attributeTableView[bucket];
                while(found!=null){
                    if(found.name.localpart==attr.name.localpart&&
                            found.name.uri==attr.name.uri){
                        return attr.name;
                    }
                    found=found.next;
                    ++collisionCount;
                }
                // Select a new hash function and rehash the table view
                // if the collision threshold is exceeded.
                if(collisionCount>=MAX_HASH_COLLISIONS){
                    // The current attribute will be processed in the rehash.
                    rebalanceTableViewNS(i+1);
                    largeCount=fLargeCount;
                }else{
                    // Update table view
                    attr.next=attributeTableView[bucket];
                    attributeTableView[bucket]=attr;
                }
            }
        }
        return null;
    }

    public int getIndexFast(String uri,String localPart){
        for(int i=0;i<fLength;++i){
            Attribute attribute=fAttributes[i];
            if(attribute.name.localpart==localPart&&
                    attribute.name.uri==uri){
                return i;
            }
        }
        return -1;
    } // getIndexFast(String,String):int

    protected int getTableViewBucket(String qname){
        return (hash(qname)&0x7FFFFFFF)%fTableViewBuckets;
    }

    protected int getTableViewBucket(String localpart,String uri){
        if(uri==null){
            return (hash(localpart)&0x7FFFFFFF)%fTableViewBuckets;
        }else{
            return (hash(localpart,uri)&0x7FFFFFFF)%fTableViewBuckets;
        }
    }

    private int hash(String localpart){
        if(fHashMultipliers==null){
            return localpart.hashCode();
        }
        return hash0(localpart);
    } // hash(String):int

    private int hash(String localpart,String uri){
        if(fHashMultipliers==null){
            return localpart.hashCode()+uri.hashCode()*31;
        }
        return hash0(localpart)+hash0(uri)*fHashMultipliers[MULTIPLIERS_SIZE];
    } // hash(String,String):int

    private int hash0(String symbol){
        int code=0;
        final int length=symbol.length();
        final int[] multipliers=fHashMultipliers;
        for(int i=0;i<length;++i){
            code=code*multipliers[i&MULTIPLIERS_MASK]+symbol.charAt(i);
        }
        return code;
    } // hash0(String):int

    protected void cleanTableView(){
        if(++fLargeCount<0){
            // Overflow. We actually need to visit the chain state array.
            if(fAttributeTableViewChainState!=null){
                for(int i=fTableViewBuckets-1;i>=0;--i){
                    fAttributeTableViewChainState[i]=0;
                }
            }
            fLargeCount=1;
        }
    }

    private void growTableView(){
        final int length=fLength;
        int tableViewBuckets=fTableViewBuckets;
        do{
            tableViewBuckets=(tableViewBuckets<<1)+1;
            if(tableViewBuckets<0){
                tableViewBuckets=Integer.MAX_VALUE;
                break;
            }
        }
        while(length>tableViewBuckets);
        fTableViewBuckets=tableViewBuckets;
        fAttributeTableView=null;
        fLargeCount=1;
    }

    protected void prepareTableView(){
        if(fLength>fTableViewBuckets){
            growTableView();
        }
        if(fAttributeTableView==null){
            fAttributeTableView=new Attribute[fTableViewBuckets];
            fAttributeTableViewChainState=new int[fTableViewBuckets];
        }else{
            cleanTableView();
        }
    }

    protected void prepareAndPopulateTableView(){
        prepareAndPopulateTableView(fLength);
    }

    private void prepareAndPopulateTableView(final int count){
        prepareTableView();
        // Need to populate the hash table with the attributes we've processed so far.
        Attribute attr;
        int bucket;
        for(int i=0;i<count;++i){
            attr=fAttributes[i];
            bucket=getTableViewBucket(attr.name.rawname);
            if(fAttributeTableViewChainState[bucket]!=fLargeCount){
                fAttributeTableViewChainState[bucket]=fLargeCount;
                attr.next=null;
                fAttributeTableView[bucket]=attr;
            }else{
                // Update table view
                attr.next=fAttributeTableView[bucket];
                fAttributeTableView[bucket]=attr;
            }
        }
    }

    public void setURI(int attrIndex,String uri){
        fAttributes[attrIndex].name.uri=uri;
    } // getURI(int,QName)

    // Implementation methods
    public void setSchemaId(int attrIndex,boolean schemaId){
        fAttributes[attrIndex].schemaId=schemaId;
    }

    public boolean getSchemaId(int index){
        if(index<0||index>=fLength){
            return false;
        }
        return fAttributes[index].schemaId;
    }

    public boolean getSchemaId(String qname){
        int index=getIndex(qname);
        return index!=-1?fAttributes[index].schemaId:false;
    } // getType(String):String

    public boolean getSchemaId(String uri,String localName){
        if(!fNamespaces){
            return false;
        }
        int index=getIndex(uri,localName);
        return index!=-1?fAttributes[index].schemaId:false;
    } // getType(String,String):String

    //XMLBufferListener methods
    public void refresh(){
        if(fLength>0){
            for(int i=0;i<fLength;i++){
                getValue(i);
            }
        }
    }

    public void refresh(int pos){
    }

    private void prepareAndPopulateTableViewNS(final int count){
        prepareTableView();
        // Need to populate the hash table with the attributes we've processed so far.
        Attribute attr;
        int bucket;
        for(int i=0;i<count;++i){
            attr=fAttributes[i];
            bucket=getTableViewBucket(attr.name.localpart,attr.name.uri);
            if(fAttributeTableViewChainState[bucket]!=fLargeCount){
                fAttributeTableViewChainState[bucket]=fLargeCount;
                attr.next=null;
                fAttributeTableView[bucket]=attr;
            }else{
                // Update table view
                attr.next=fAttributeTableView[bucket];
                fAttributeTableView[bucket]=attr;
            }
        }
    }

    private void rebalanceTableView(final int count){
        if(fHashMultipliers==null){
            fHashMultipliers=new int[MULTIPLIERS_SIZE+1];
        }
        PrimeNumberSequenceGenerator.generateSequence(fHashMultipliers);
        prepareAndPopulateTableView(count);
    }

    private void rebalanceTableViewNS(final int count){
        if(fHashMultipliers==null){
            fHashMultipliers=new int[MULTIPLIERS_SIZE+1];
        }
        PrimeNumberSequenceGenerator.generateSequence(fHashMultipliers);
        prepareAndPopulateTableViewNS(count);
    }
    //
    // Classes
    //

    static class Attribute{
        //
        // Data
        //
        // basic info
        public QName name=new QName();
        public String type;
        public String value;
        public XMLString xmlValue;
        public String nonNormalizedValue;
        public boolean specified;
        public boolean schemaId;
        public Augmentations augs=new AugmentationsImpl();
        // Additional data for attribute table view
        public Attribute next;
    } // class Attribute
} // class XMLAttributesImpl

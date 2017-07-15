/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class CharacterDataImpl
        extends ChildNode{
    //
    // Constants
    //
    static final long serialVersionUID=7931170150428474230L;
    private static transient NodeList singletonNodeList=new NodeList(){
        public Node item(int index){
            return null;
        }

        public int getLength(){
            return 0;
        }
    };
    //
    // Data
    //
    protected String data;
    //
    // Constructors
    //

    public CharacterDataImpl(){
    }

    protected CharacterDataImpl(CoreDocumentImpl ownerDocument,String data){
        super(ownerDocument);
        this.data=data;
    }
    //
    // Node methods
    //

    public String getNodeValue(){
        if(needsSyncData()){
            synchronizeData();
        }
        return data;
    }

    public void setNodeValue(String value){
        setNodeValueInternal(value);
        // notify document
        ownerDocument().replacedText(this);
    }

    public NodeList getChildNodes(){
        return singletonNodeList;
    }

    public int getLength(){
        if(needsSyncData()){
            synchronizeData();
        }
        return data.length();
    }

    protected void setNodeValueInternal(String value){
        setNodeValueInternal(value,false);
    }
    //
    // CharacterData methods
    //

    protected void setNodeValueInternal(String value,boolean replace){
        CoreDocumentImpl ownerDocument=ownerDocument();
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        // revisit: may want to set the value in ownerDocument.
        // Default behavior, overridden in some subclasses
        if(needsSyncData()){
            synchronizeData();
        }
        // keep old value for document notification
        String oldvalue=this.data;
        // notify document
        ownerDocument.modifyingCharacterData(this,replace);
        this.data=value;
        // notify document
        ownerDocument.modifiedCharacterData(this,oldvalue,value,replace);
    }

    public String getData(){
        if(needsSyncData()){
            synchronizeData();
        }
        return data;
    }

    public void setData(String value)
            throws DOMException{
        setNodeValue(value);
    }

    public void appendData(String data){
        if(isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(data==null){
            return;
        }
        if(needsSyncData()){
            synchronizeData();
        }
        setNodeValue(this.data+data);
    } // appendData(String)

    public void deleteData(int offset,int count)
            throws DOMException{
        internalDeleteData(offset,count,false);
    } // deleteData(int,int)

    void internalDeleteData(int offset,int count,boolean replace)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerDocument();
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(count<0){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INDEX_SIZE_ERR",null);
                throw new DOMException(DOMException.INDEX_SIZE_ERR,msg);
            }
        }
        if(needsSyncData()){
            synchronizeData();
        }
        int tailLength=Math.max(data.length()-count-offset,0);
        try{
            String value=data.substring(0,offset)+
                    (tailLength>0?data.substring(offset+count,offset+count+tailLength):"");
            setNodeValueInternal(value,replace);
            // notify document
            ownerDocument.deletedText(this,offset,count);
        }catch(StringIndexOutOfBoundsException e){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INDEX_SIZE_ERR",null);
            throw new DOMException(DOMException.INDEX_SIZE_ERR,msg);
        }
    } // internalDeleteData(int,int,boolean)

    public void insertData(int offset,String data)
            throws DOMException{
        internalInsertData(offset,data,false);
    } // insertData(int,int)

    void internalInsertData(int offset,String data,boolean replace)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerDocument();
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        try{
            String value=
                    new StringBuffer(this.data).insert(offset,data).toString();
            setNodeValueInternal(value,replace);
            // notify document
            ownerDocument.insertedText(this,offset,data.length());
        }catch(StringIndexOutOfBoundsException e){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INDEX_SIZE_ERR",null);
            throw new DOMException(DOMException.INDEX_SIZE_ERR,msg);
        }
    } // internalInsertData(int,String,boolean)

    public void replaceData(int offset,int count,String data)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerDocument();
        // The read-only check is done by deleteData()
        // ***** This could be more efficient w/r/t Mutation Events,
        // specifically by aggregating DOMAttrModified and
        // DOMSubtreeModified. But mutation events are
        // underspecified; I don't feel compelled
        // to deal with it right now.
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
        if(needsSyncData()){
            synchronizeData();
        }
        //notify document
        ownerDocument.replacingData(this);
        // keep old value for document notification
        String oldvalue=this.data;
        internalDeleteData(offset,count,true);
        internalInsertData(offset,data,true);
        ownerDocument.replacedCharacterData(this,oldvalue,this.data);
    } // replaceData(int,int,String)

    public String substringData(int offset,int count)
            throws DOMException{
        if(needsSyncData()){
            synchronizeData();
        }
        int length=data.length();
        if(count<0||offset<0||offset>length-1){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INDEX_SIZE_ERR",null);
            throw new DOMException(DOMException.INDEX_SIZE_ERR,msg);
        }
        int tailIndex=Math.min(offset+count,length);
        return data.substring(offset,tailIndex);
    } // substringData(int,int):String
} // class CharacterDataImpl

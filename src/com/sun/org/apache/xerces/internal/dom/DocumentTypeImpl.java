/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.dom;

import org.w3c.dom.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DocumentTypeImpl
        extends ParentNode
        implements DocumentType{
    //
    // Constants
    //
    static final long serialVersionUID=7751299192316526485L;
    private static final ObjectStreamField[] serialPersistentFields=
            new ObjectStreamField[]{
                    new ObjectStreamField("name",String.class),
                    new ObjectStreamField("entities",NamedNodeMapImpl.class),
                    new ObjectStreamField("notations",NamedNodeMapImpl.class),
                    new ObjectStreamField("elements",NamedNodeMapImpl.class),
                    new ObjectStreamField("publicID",String.class),
                    new ObjectStreamField("systemID",String.class),
                    new ObjectStreamField("internalSubset",String.class),
                    new ObjectStreamField("doctypeNumber",int.class),
                    new ObjectStreamField("userData",Hashtable.class),
            };
    //
    // Data
    //
    protected String name;
    protected NamedNodeMapImpl entities;
    protected NamedNodeMapImpl notations;
    // NON-DOM
    protected NamedNodeMapImpl elements;
    // DOM2: support public ID.
    protected String publicID;
    // DOM2: support system ID.
    protected String systemID;
    // DOM2: support internal subset.
    protected String internalSubset;
    // Doctype number.   Doc types which have no owner may be assigned
    // a number, on demand, for ordering purposes for compareDocumentPosition
    private int doctypeNumber=0;
    private Map<String,UserDataRecord> userData=null;
    //
    // Constructors
    //

    public DocumentTypeImpl(CoreDocumentImpl ownerDocument,
                            String qualifiedName,
                            String publicID,String systemID){
        this(ownerDocument,qualifiedName);
        this.publicID=publicID;
        this.systemID=systemID;
    } // <init>(CoreDocumentImpl,String)

    public DocumentTypeImpl(CoreDocumentImpl ownerDocument,String name){
        super(ownerDocument);
        this.name=name;
        // DOM
        entities=new NamedNodeMapImpl(this);
        notations=new NamedNodeMapImpl(this);
        // NON-DOM
        elements=new NamedNodeMapImpl(this);
    } // <init>(CoreDocumentImpl,String)
    //
    // DOM2: methods.
    //

    public short getNodeType(){
        return Node.DOCUMENT_TYPE_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }

    protected int getNodeNumber(){
        // If the doctype has a document owner, get the node number
        // relative to the owner doc
        if(getOwnerDocument()!=null)
            return super.getNodeNumber();
        // The doctype is disconnected and not associated with any document.
        // Assign the doctype a number relative to the implementation.
        if(doctypeNumber==0){
            CoreDOMImplementationImpl cd=(CoreDOMImplementationImpl)CoreDOMImplementationImpl.getDOMImplementation();
            doctypeNumber=cd.assignDocTypeNumber();
        }
        return doctypeNumber;
    }

    public Object setUserData(String key,
                              Object data,UserDataHandler handler){
        if(userData==null)
            userData=new HashMap<>();
        if(data==null){
            if(userData!=null){
                UserDataRecord udr=userData.remove(key);
                if(udr!=null){
                    return udr.fData;
                }
            }
            return null;
        }else{
            UserDataRecord udr=userData.put(key,new UserDataRecord(data,handler));
            if(udr!=null){
                return udr.fData;
            }
        }
        return null;
    }
    //
    // Node methods
    //

    public Object getUserData(String key){
        if(userData==null){
            return null;
        }
        UserDataRecord udr=userData.get(key);
        if(udr!=null){
            return udr.fData;
        }
        return null;
    }

    @Override
    protected Map<String,UserDataRecord> getUserDataRecord(){
        return userData;
    }

    public Node cloneNode(boolean deep){
        DocumentTypeImpl newnode=(DocumentTypeImpl)super.cloneNode(deep);
        // NamedNodeMaps must be cloned explicitly, to avoid sharing them.
        newnode.entities=entities.cloneMap(newnode);
        newnode.notations=notations.cloneMap(newnode);
        newnode.elements=elements.cloneMap(newnode);
        return newnode;
    } // cloneNode(boolean):Node

    void setOwnerDocument(CoreDocumentImpl doc){
        super.setOwnerDocument(doc);
        entities.setOwnerDocument(doc);
        notations.setOwnerDocument(doc);
        elements.setOwnerDocument(doc);
    }

    public String getTextContent() throws DOMException{
        return null;
    }

    public void setTextContent(String textContent)
            throws DOMException{
        // no-op
    }

    public boolean isEqualNode(Node arg){
        if(!super.isEqualNode(arg)){
            return false;
        }
        if(needsSyncData()){
            synchronizeData();
        }
        DocumentTypeImpl argDocType=(DocumentTypeImpl)arg;
        //test if the following string attributes are equal: publicId,
        //systemId, internalSubset.
        if((getPublicId()==null&&argDocType.getPublicId()!=null)
                ||(getPublicId()!=null&&argDocType.getPublicId()==null)
                ||(getSystemId()==null&&argDocType.getSystemId()!=null)
                ||(getSystemId()!=null&&argDocType.getSystemId()==null)
                ||(getInternalSubset()==null
                &&argDocType.getInternalSubset()!=null)
                ||(getInternalSubset()!=null
                &&argDocType.getInternalSubset()==null)){
            return false;
        }
        if(getPublicId()!=null){
            if(!getPublicId().equals(argDocType.getPublicId())){
                return false;
            }
        }
        if(getSystemId()!=null){
            if(!getSystemId().equals(argDocType.getSystemId())){
                return false;
            }
        }
        if(getInternalSubset()!=null){
            if(!getInternalSubset().equals(argDocType.getInternalSubset())){
                return false;
            }
        }
        //test if NamedNodeMaps entities and notations are equal
        NamedNodeMapImpl argEntities=argDocType.entities;
        if((entities==null&&argEntities!=null)
                ||(entities!=null&&argEntities==null))
            return false;
        if(entities!=null&&argEntities!=null){
            if(entities.getLength()!=argEntities.getLength())
                return false;
            for(int index=0;entities.item(index)!=null;index++){
                Node entNode1=entities.item(index);
                Node entNode2=
                        argEntities.getNamedItem(entNode1.getNodeName());
                if(!((NodeImpl)entNode1).isEqualNode((NodeImpl)entNode2))
                    return false;
            }
        }
        NamedNodeMapImpl argNotations=argDocType.notations;
        if((notations==null&&argNotations!=null)
                ||(notations!=null&&argNotations==null))
            return false;
        if(notations!=null&&argNotations!=null){
            if(notations.getLength()!=argNotations.getLength())
                return false;
            for(int index=0;notations.item(index)!=null;index++){
                Node noteNode1=notations.item(index);
                Node noteNode2=
                        argNotations.getNamedItem(noteNode1.getNodeName());
                if(!((NodeImpl)noteNode1).isEqualNode((NodeImpl)noteNode2))
                    return false;
            }
        }
        return true;
    } //end isEqualNode

    public void setReadOnly(boolean readOnly,boolean deep){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        super.setReadOnly(readOnly,deep);
        // set read-only property
        elements.setReadOnly(readOnly,true);
        entities.setReadOnly(readOnly,true);
        notations.setReadOnly(readOnly,true);
    } // setReadOnly(boolean,boolean)
    //
    // DocumentType methods
    //

    public String getName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    } // getName():String

    public NamedNodeMap getEntities(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return entities;
    }

    public NamedNodeMap getNotations(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return notations;
    }
    //
    // Public methods
    //

    public String getPublicId(){
        if(needsSyncData()){
            synchronizeData();
        }
        return publicID;
    }

    public String getSystemId(){
        if(needsSyncData()){
            synchronizeData();
        }
        return systemID;
    }

    public String getInternalSubset(){
        if(needsSyncData()){
            synchronizeData();
        }
        return internalSubset;
    }

    public void setInternalSubset(String internalSubset){
        if(needsSyncData()){
            synchronizeData();
        }
        this.internalSubset=internalSubset;
    }

    public NamedNodeMap getElements(){
        if(needsSyncChildren()){
            synchronizeChildren();
        }
        return elements;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        // Convert the HashMap to Hashtable
        Hashtable<String,UserDataRecord> ud=(userData==null)?null:new Hashtable<>(userData);
        // Write serialized fields
        ObjectOutputStream.PutField pf=out.putFields();
        pf.put("name",name);
        pf.put("entities",entities);
        pf.put("notations",notations);
        pf.put("elements",elements);
        pf.put("publicID",publicID);
        pf.put("systemID",systemID);
        pf.put("internalSubset",internalSubset);
        pf.put("doctypeNumber",doctypeNumber);
        pf.put("userData",ud);
        out.writeFields();
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // We have to read serialized fields first.
        ObjectInputStream.GetField gf=in.readFields();
        name=(String)gf.get("name",null);
        entities=(NamedNodeMapImpl)gf.get("entities",null);
        notations=(NamedNodeMapImpl)gf.get("notations",null);
        elements=(NamedNodeMapImpl)gf.get("elements",null);
        publicID=(String)gf.get("publicID",null);
        systemID=(String)gf.get("systemID",null);
        internalSubset=(String)gf.get("internalSubset",null);
        doctypeNumber=gf.get("doctypeNumber",0);
        Hashtable<String,UserDataRecord> ud=
                (Hashtable<String,UserDataRecord>)gf.get("userData",null);
        //convert the Hashtable back to HashMap
        if(ud!=null) userData=new HashMap<>(ud);
    }
} // class DocumentTypeImpl

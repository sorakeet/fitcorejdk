/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2005 The Apache Software Foundation.
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
 * Copyright 2002-2005 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMap4Types;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.util.SymbolHash;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xs.*;

import java.lang.reflect.Array;
import java.util.*;

public final class XSModelImpl extends AbstractList implements XSModel, XSNamespaceItemList{
    // the max index / the max value of XSObject type
    private static final short MAX_COMP_IDX=XSTypeDefinition.SIMPLE_TYPE;
    private static final boolean[] GLOBAL_COMP={false,    // null
            true,     // attribute
            true,     // element
            true,     // type
            false,    // attribute use
            true,     // attribute group
            true,     // group
            false,    // model group
            false,    // particle
            false,    // wildcard
            false,    // idc
            true,     // notation
            false,    // annotation
            false,    // facet
            false,    // multi value facet
            true,     // complex type
            true      // simple type
    };
    // number of grammars/namespaces stored here
    private final int fGrammarCount;
    // all target namespaces
    private final String[] fNamespaces;
    // all schema grammar objects (for each namespace)
    private final SchemaGrammar[] fGrammarList;
    // a map from namespace to schema grammar
    private final SymbolHash fGrammarMap;
    // a map from element declaration to its substitution group
    private final SymbolHash fSubGroupMap;
    // store a certain kind of components from all namespaces
    private final XSNamedMap[] fGlobalComponents;
    // store a certain kind of components from one namespace
    private final XSNamedMap[][] fNSComponents;
    // a string list of all the target namespaces.
    private final StringList fNamespacesList;
    // whether there is any IDC in this XSModel
    private final boolean fHasIDC;
    // store all annotations
    private XSObjectList fAnnotations=null;

    public XSModelImpl(SchemaGrammar[] grammars){
        this(grammars,Constants.SCHEMA_VERSION_1_0);
    }

    public XSModelImpl(SchemaGrammar[] grammars,short s4sVersion){
        // copy namespaces/grammars from the array to our arrays
        int len=grammars.length;
        final int initialSize=Math.max(len+1,5);
        String[] namespaces=new String[initialSize];
        SchemaGrammar[] grammarList=new SchemaGrammar[initialSize];
        boolean hasS4S=false;
        for(int i=0;i<len;i++){
            final SchemaGrammar sg=grammars[i];
            final String tns=sg.getTargetNamespace();
            namespaces[i]=tns;
            grammarList[i]=sg;
            if(tns==SchemaSymbols.URI_SCHEMAFORSCHEMA){
                hasS4S=true;
            }
        }
        // If a schema for the schema namespace isn't included, include it here.
        if(!hasS4S){
            namespaces[len]=SchemaSymbols.URI_SCHEMAFORSCHEMA;
            grammarList[len++]=SchemaGrammar.getS4SGrammar(s4sVersion);
        }
        SchemaGrammar sg1, sg2;
        Vector gs;
        int i, j, k;
        // and recursively get all imported grammars, add them to our arrays
        for(i=0;i<len;i++){
            // get the grammar
            sg1=grammarList[i];
            gs=sg1.getImportedGrammars();
            // for each imported grammar
            for(j=gs==null?-1:gs.size()-1;j>=0;j--){
                sg2=(SchemaGrammar)gs.elementAt(j);
                // check whether this grammar is already in the list
                for(k=0;k<len;k++){
                    if(sg2==grammarList[k]){
                        break;
                    }
                }
                // if it's not, add it to the list
                if(k==len){
                    // ensure the capacity of the arrays
                    if(len==grammarList.length){
                        String[] newSA=new String[len*2];
                        System.arraycopy(namespaces,0,newSA,0,len);
                        namespaces=newSA;
                        SchemaGrammar[] newGA=new SchemaGrammar[len*2];
                        System.arraycopy(grammarList,0,newGA,0,len);
                        grammarList=newGA;
                    }
                    namespaces[len]=sg2.getTargetNamespace();
                    grammarList[len]=sg2;
                    len++;
                }
            }
        }
        fNamespaces=namespaces;
        fGrammarList=grammarList;
        boolean hasIDC=false;
        // establish the mapping from namespace to grammars
        fGrammarMap=new SymbolHash(len*2);
        for(i=0;i<len;i++){
            fGrammarMap.put(null2EmptyString(fNamespaces[i]),fGrammarList[i]);
            // update the idc field
            if(fGrammarList[i].hasIDConstraints()){
                hasIDC=true;
            }
        }
        fHasIDC=hasIDC;
        fGrammarCount=len;
        fGlobalComponents=new XSNamedMap[MAX_COMP_IDX+1];
        fNSComponents=new XSNamedMap[len][MAX_COMP_IDX+1];
        fNamespacesList=new StringListImpl(fNamespaces,fGrammarCount);
        // build substitution groups
        fSubGroupMap=buildSubGroups();
    }

    private SymbolHash buildSubGroups(){
        SubstitutionGroupHandler sgHandler=new SubstitutionGroupHandler(null);
        for(int i=0;i<fGrammarCount;i++){
            sgHandler.addSubstitutionGroup(fGrammarList[i].getSubstitutionGroups());
        }
        final XSObjectListImpl elements=getGlobalElements();
        final int len=elements.getLength();
        final SymbolHash subGroupMap=new SymbolHash(len*2);
        XSElementDecl head;
        XSElementDeclaration[] subGroup;
        for(int i=0;i<len;i++){
            head=(XSElementDecl)elements.item(i);
            subGroup=sgHandler.getSubstitutionGroup(head);
            subGroupMap.put(head,subGroup.length>0?
                    new XSObjectListImpl(subGroup,subGroup.length):XSObjectListImpl.EMPTY_LIST);
        }
        return subGroupMap;
    }

    private XSObjectListImpl getGlobalElements(){
        final SymbolHash[] tables=new SymbolHash[fGrammarCount];
        int length=0;
        for(int i=0;i<fGrammarCount;i++){
            tables[i]=fGrammarList[i].fAllGlobalElemDecls;
            length+=tables[i].getLength();
        }
        if(length==0){
            return XSObjectListImpl.EMPTY_LIST;
        }
        final XSObject[] components=new XSObject[length];
        int start=0;
        for(int i=0;i<fGrammarCount;i++){
            tables[i].getValues(components,start);
            start+=tables[i].getLength();
        }
        return new XSObjectListImpl(components,length);
    }

    private static final String null2EmptyString(String str){
        return str==null?XMLSymbols.EMPTY_STRING:str;
    }

    private SymbolHash buildSubGroups_Org(){
        SubstitutionGroupHandler sgHandler=new SubstitutionGroupHandler(null);
        for(int i=0;i<fGrammarCount;i++){
            sgHandler.addSubstitutionGroup(fGrammarList[i].getSubstitutionGroups());
        }
        final XSNamedMap elements=getComponents(XSConstants.ELEMENT_DECLARATION);
        final int len=elements.getLength();
        final SymbolHash subGroupMap=new SymbolHash(len*2);
        XSElementDecl head;
        XSElementDeclaration[] subGroup;
        for(int i=0;i<len;i++){
            head=(XSElementDecl)elements.item(i);
            subGroup=sgHandler.getSubstitutionGroup(head);
            subGroupMap.put(head,subGroup.length>0?
                    new XSObjectListImpl(subGroup,subGroup.length):XSObjectListImpl.EMPTY_LIST);
        }
        return subGroupMap;
    }

    public StringList getNamespaces(){
        return fNamespacesList;
    }

    public XSNamespaceItemList getNamespaceItems(){
        return this;
    }

    public synchronized XSNamedMap getComponents(short objectType){
        if(objectType<=0||objectType>MAX_COMP_IDX||
                !GLOBAL_COMP[objectType]){
            return XSNamedMapImpl.EMPTY_MAP;
        }
        SymbolHash[] tables=new SymbolHash[fGrammarCount];
        // get all hashtables from all namespaces for this type of components
        if(fGlobalComponents[objectType]==null){
            for(int i=0;i<fGrammarCount;i++){
                switch(objectType){
                    case XSConstants.TYPE_DEFINITION:
                    case XSTypeDefinition.COMPLEX_TYPE:
                    case XSTypeDefinition.SIMPLE_TYPE:
                        tables[i]=fGrammarList[i].fGlobalTypeDecls;
                        break;
                    case XSConstants.ATTRIBUTE_DECLARATION:
                        tables[i]=fGrammarList[i].fGlobalAttrDecls;
                        break;
                    case XSConstants.ELEMENT_DECLARATION:
                        tables[i]=fGrammarList[i].fGlobalElemDecls;
                        break;
                    case XSConstants.ATTRIBUTE_GROUP:
                        tables[i]=fGrammarList[i].fGlobalAttrGrpDecls;
                        break;
                    case XSConstants.MODEL_GROUP_DEFINITION:
                        tables[i]=fGrammarList[i].fGlobalGroupDecls;
                        break;
                    case XSConstants.NOTATION_DECLARATION:
                        tables[i]=fGrammarList[i].fGlobalNotationDecls;
                        break;
                }
            }
            // for complex/simple types, create a special implementation,
            // which take specific types out of the hash table
            if(objectType==XSTypeDefinition.COMPLEX_TYPE||
                    objectType==XSTypeDefinition.SIMPLE_TYPE){
                fGlobalComponents[objectType]=new XSNamedMap4Types(fNamespaces,tables,fGrammarCount,objectType);
            }else{
                fGlobalComponents[objectType]=new XSNamedMapImpl(fNamespaces,tables,fGrammarCount);
            }
        }
        return fGlobalComponents[objectType];
    }

    public synchronized XSNamedMap getComponentsByNamespace(short objectType,
                                                            String namespace){
        if(objectType<=0||objectType>MAX_COMP_IDX||
                !GLOBAL_COMP[objectType]){
            return XSNamedMapImpl.EMPTY_MAP;
        }
        // try to find the grammar
        int i=0;
        if(namespace!=null){
            for(;i<fGrammarCount;++i){
                if(namespace.equals(fNamespaces[i])){
                    break;
                }
            }
        }else{
            for(;i<fGrammarCount;++i){
                if(fNamespaces[i]==null){
                    break;
                }
            }
        }
        if(i==fGrammarCount){
            return XSNamedMapImpl.EMPTY_MAP;
        }
        // get the hashtable for this type of components
        if(fNSComponents[i][objectType]==null){
            SymbolHash table=null;
            switch(objectType){
                case XSConstants.TYPE_DEFINITION:
                case XSTypeDefinition.COMPLEX_TYPE:
                case XSTypeDefinition.SIMPLE_TYPE:
                    table=fGrammarList[i].fGlobalTypeDecls;
                    break;
                case XSConstants.ATTRIBUTE_DECLARATION:
                    table=fGrammarList[i].fGlobalAttrDecls;
                    break;
                case XSConstants.ELEMENT_DECLARATION:
                    table=fGrammarList[i].fGlobalElemDecls;
                    break;
                case XSConstants.ATTRIBUTE_GROUP:
                    table=fGrammarList[i].fGlobalAttrGrpDecls;
                    break;
                case XSConstants.MODEL_GROUP_DEFINITION:
                    table=fGrammarList[i].fGlobalGroupDecls;
                    break;
                case XSConstants.NOTATION_DECLARATION:
                    table=fGrammarList[i].fGlobalNotationDecls;
                    break;
            }
            // for complex/simple types, create a special implementation,
            // which take specific types out of the hash table
            if(objectType==XSTypeDefinition.COMPLEX_TYPE||
                    objectType==XSTypeDefinition.SIMPLE_TYPE){
                fNSComponents[i][objectType]=new XSNamedMap4Types(namespace,table,objectType);
            }else{
                fNSComponents[i][objectType]=new XSNamedMapImpl(namespace,table);
            }
        }
        return fNSComponents[i][objectType];
    }

    public synchronized XSObjectList getAnnotations(){
        if(fAnnotations!=null){
            return fAnnotations;
        }
        // do this in two passes to avoid inaccurate array size
        int totalAnnotations=0;
        for(int i=0;i<fGrammarCount;i++){
            totalAnnotations+=fGrammarList[i].fNumAnnotations;
        }
        if(totalAnnotations==0){
            fAnnotations=XSObjectListImpl.EMPTY_LIST;
            return fAnnotations;
        }
        XSAnnotationImpl[] annotations=new XSAnnotationImpl[totalAnnotations];
        int currPos=0;
        for(int i=0;i<fGrammarCount;i++){
            SchemaGrammar currGrammar=fGrammarList[i];
            if(currGrammar.fNumAnnotations>0){
                System.arraycopy(currGrammar.fAnnotations,0,annotations,currPos,currGrammar.fNumAnnotations);
                currPos+=currGrammar.fNumAnnotations;
            }
        }
        fAnnotations=new XSObjectListImpl(annotations,annotations.length);
        return fAnnotations;
    }

    public XSElementDeclaration getElementDeclaration(String name,
                                                      String namespace){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return (XSElementDeclaration)sg.fGlobalElemDecls.get(name);
    }

    public XSAttributeDeclaration getAttributeDeclaration(String name,
                                                          String namespace){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return (XSAttributeDeclaration)sg.fGlobalAttrDecls.get(name);
    }

    public XSTypeDefinition getTypeDefinition(String name,
                                              String namespace){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return (XSTypeDefinition)sg.fGlobalTypeDecls.get(name);
    }

    public XSAttributeGroupDefinition getAttributeGroup(String name,
                                                        String namespace){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return (XSAttributeGroupDefinition)sg.fGlobalAttrGrpDecls.get(name);
    }

    public XSModelGroupDefinition getModelGroupDefinition(String name,
                                                          String namespace){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return (XSModelGroupDefinition)sg.fGlobalGroupDecls.get(name);
    }

    public XSNotationDeclaration getNotationDeclaration(String name,
                                                        String namespace){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return (XSNotationDeclaration)sg.fGlobalNotationDecls.get(name);
    }

    public XSObjectList getSubstitutionGroup(XSElementDeclaration head){
        return (XSObjectList)fSubGroupMap.get(head);
    }

    public XSTypeDefinition getTypeDefinition(String name,
                                              String namespace,
                                              String loc){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return sg.getGlobalTypeDecl(name,loc);
    }

    public XSAttributeDeclaration getAttributeDeclaration(String name,
                                                          String namespace,
                                                          String loc){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return sg.getGlobalAttributeDecl(name,loc);
    }

    public XSElementDeclaration getElementDeclaration(String name,
                                                      String namespace,
                                                      String loc){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return sg.getGlobalElementDecl(name,loc);
    }

    public XSAttributeGroupDefinition getAttributeGroup(String name,
                                                        String namespace,
                                                        String loc){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return sg.getGlobalAttributeGroupDecl(name,loc);
    }

    public XSModelGroupDefinition getModelGroupDefinition(String name,
                                                          String namespace,
                                                          String loc){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return sg.getGlobalGroupDecl(name,loc);
    }

    public XSNotationDeclaration getNotationDeclaration(String name,
                                                        String namespace,
                                                        String loc){
        SchemaGrammar sg=(SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if(sg==null){
            return null;
        }
        return sg.getGlobalNotationDecl(name,loc);
    }

    public boolean hasIDConstraints(){
        return fHasIDC;
    }
    //
    // XSNamespaceItemList methods
    //

    public Object get(int index){
        if(index>=0&&index<fGrammarCount){
            return fGrammarList[index];
        }
        throw new IndexOutOfBoundsException("Index: "+index);
    }

    public Iterator iterator(){
        return listIterator0(0);
    }
    //
    // java.util.List methods
    //

    public ListIterator listIterator(){
        return listIterator0(0);
    }

    public ListIterator listIterator(int index){
        if(index>=0&&index<fGrammarCount){
            return listIterator0(index);
        }
        throw new IndexOutOfBoundsException("Index: "+index);
    }

    private ListIterator listIterator0(int index){
        return new XSNamespaceItemListIterator(index);
    }

    public int size(){
        return getLength();
    }

    public int getLength(){
        return fGrammarCount;
    }

    public XSNamespaceItem item(int index){
        if(index<0||index>=fGrammarCount){
            return null;
        }
        return fGrammarList[index];
    }

    public Object[] toArray(){
        Object[] a=new Object[fGrammarCount];
        toArray0(a);
        return a;
    }

    public Object[] toArray(Object[] a){
        if(a.length<fGrammarCount){
            Class arrayClass=a.getClass();
            Class componentType=arrayClass.getComponentType();
            a=(Object[])Array.newInstance(componentType,fGrammarCount);
        }
        toArray0(a);
        if(a.length>fGrammarCount){
            a[fGrammarCount]=null;
        }
        return a;
    }

    private void toArray0(Object[] a){
        if(fGrammarCount>0){
            System.arraycopy(fGrammarList,0,a,0,fGrammarCount);
        }
    }

    private final class XSNamespaceItemListIterator implements ListIterator{
        private int index;

        public XSNamespaceItemListIterator(int index){
            this.index=index;
        }

        public boolean hasNext(){
            return (index<fGrammarCount);
        }

        public Object next(){
            if(index<fGrammarCount){
                return fGrammarList[index++];
            }
            throw new NoSuchElementException();
        }

        public boolean hasPrevious(){
            return (index>0);
        }

        public Object previous(){
            if(index>0){
                return fGrammarList[--index];
            }
            throw new NoSuchElementException();
        }

        public int nextIndex(){
            return index;
        }

        public int previousIndex(){
            return index-1;
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }

        public void set(Object o){
            throw new UnsupportedOperationException();
        }

        public void add(Object o){
            throw new UnsupportedOperationException();
        }
    }
} // class XSModelImpl

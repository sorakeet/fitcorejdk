/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002,2004 The Apache Software Foundation.
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
 * Copyright 2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.util;

import com.sun.org.apache.xerces.internal.util.SymbolHash;
import com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import com.sun.org.apache.xerces.internal.xs.XSObject;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.*;

public class XSNamedMapImpl extends AbstractMap implements XSNamedMap{
    public static final XSNamedMapImpl EMPTY_MAP=new XSNamedMapImpl(new XSObject[0],0);
    // components of these namespaces are stored in this map
    final String[] fNamespaces;
    // number of namespaces
    final int fNSNum;
    // each entry contains components in one namespace
    final SymbolHash[] fMaps;
    // store all components from all namespace.
    // used when this map is accessed as a list.
    XSObject[] fArray=null;
    // store the number of components.
    // used when this map is accessed as a list.
    int fLength=-1;
    // Set of Map.Entry<QName,XSObject> for the java.util.Map methods
    private Set fEntrySet=null;

    public XSNamedMapImpl(String namespace,SymbolHash map){
        fNamespaces=new String[]{namespace};
        fMaps=new SymbolHash[]{map};
        fNSNum=1;
    }

    public XSNamedMapImpl(String[] namespaces,SymbolHash[] maps,int num){
        fNamespaces=namespaces;
        fMaps=maps;
        fNSNum=num;
    }

    public XSNamedMapImpl(XSObject[] array,int length){
        if(length==0){
            fNamespaces=null;
            fMaps=null;
            fNSNum=0;
            fArray=array;
            fLength=0;
            return;
        }
        // because all components are from the same target namesapce,
        // get the namespace from the first one.
        fNamespaces=new String[]{array[0].getNamespace()};
        fMaps=null;
        fNSNum=1;
        // copy elements to the Vector
        fArray=array;
        fLength=length;
    }

    public int size(){
        return getLength();
    }

    public synchronized int getLength(){
        if(fLength==-1){
            fLength=0;
            for(int i=0;i<fNSNum;i++){
                fLength+=fMaps[i].getLength();
            }
        }
        return fLength;
    }

    public synchronized XSObject item(int index){
        if(fArray==null){
            // calculate the total number of elements
            getLength();
            fArray=new XSObject[fLength];
            int pos=0;
            // get components from all SymbolHashes
            for(int i=0;i<fNSNum;i++){
                pos+=fMaps[i].getValues(fArray,pos);
            }
        }
        if(index<0||index>=fLength){
            return null;
        }
        return fArray[index];
    }

    public XSObject itemByName(String namespace,String localName){
        for(int i=0;i<fNSNum;i++){
            if(isEqual(namespace,fNamespaces[i])){
                // when this map is created from SymbolHash's
                // get the component from SymbolHash
                if(fMaps!=null){
                    return (XSObject)fMaps[i].get(localName);
                }
                // Otherwise (it's created from an array)
                // go through the array to find a matching name
                XSObject ret;
                for(int j=0;j<fLength;j++){
                    ret=fArray[j];
                    if(ret.getName().equals(localName)){
                        return ret;
                    }
                }
                return null;
            }
        }
        return null;
    }

    static boolean isEqual(String one,String two){
        return (one!=null)?one.equals(two):(two==null);
    }

    public boolean containsKey(Object key){
        return (get(key)!=null);
    }

    public Object get(Object key){
        if(key instanceof QName){
            final QName name=(QName)key;
            String namespaceURI=name.getNamespaceURI();
            if(XMLConstants.NULL_NS_URI.equals(namespaceURI)){
                namespaceURI=null;
            }
            String localPart=name.getLocalPart();
            return itemByName(namespaceURI,localPart);
        }
        return null;
    }

    public synchronized Set entrySet(){
        // Defer creation of the entry set until it is actually needed.
        if(fEntrySet==null){
            final int length=getLength();
            final XSNamedMapEntry[] entries=new XSNamedMapEntry[length];
            for(int i=0;i<length;++i){
                XSObject xso=item(i);
                entries[i]=new XSNamedMapEntry(new QName(xso.getNamespace(),xso.getName()),xso);
            }
            // Create a view of this immutable map.
            fEntrySet=new AbstractSet(){
                public Iterator iterator(){
                    return new Iterator(){
                        private int index=0;

                        public boolean hasNext(){
                            return (index<length);
                        }

                        public Object next(){
                            if(index<length){
                                return entries[index++];
                            }
                            throw new NoSuchElementException();
                        }

                        public void remove(){
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                public int size(){
                    return length;
                }
            };
        }
        return fEntrySet;
    }

    private static final class XSNamedMapEntry implements Entry{
        private final QName key;
        private final XSObject value;

        public XSNamedMapEntry(QName key,XSObject value){
            this.key=key;
            this.value=value;
        }

        public Object getKey(){
            return key;
        }

        public Object getValue(){
            return value;
        }

        public Object setValue(Object value){
            throw new UnsupportedOperationException();
        }

        public int hashCode(){
            return (key==null?0:key.hashCode())
                    ^(value==null?0:value.hashCode());
        }

        public boolean equals(Object o){
            if(o instanceof Map.Entry){
                Entry e=(Entry)o;
                Object otherKey=e.getKey();
                Object otherValue=e.getValue();
                return (key==null?otherKey==null:key.equals(otherKey))&&
                        (value==null?otherValue==null:value.equals(otherValue));
            }
            return false;
        }

        public String toString(){
            StringBuffer buffer=new StringBuffer();
            buffer.append(String.valueOf(key));
            buffer.append('=');
            buffer.append(String.valueOf(value));
            return buffer.toString();
        }
    }
} // class XSNamedMapImpl

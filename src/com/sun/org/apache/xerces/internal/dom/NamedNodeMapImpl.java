/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class NamedNodeMapImpl
        implements NamedNodeMap, Serializable{
    protected final static short READONLY=0x1<<0;
    protected final static short CHANGED=0x1<<1;
    protected final static short HASDEFAULTS=0x1<<2;
    //
    // Constants
    //
    static final long serialVersionUID=-7039242451046758020L;
    //
    // Data
    //
    protected short flags;
    protected List nodes;
    protected NodeImpl ownerNode; // the node this map belongs to
    //
    // Constructors
    //

    protected NamedNodeMapImpl(NodeImpl ownerNode){
        this.ownerNode=ownerNode;
    }
    //
    // NamedNodeMap methods
    //

    public Node getNamedItem(String name){
        int i=findNamePoint(name,0);
        return (i<0)?null:(Node)(nodes.get(i));
    } // getNamedItem(String):Node

    public Node setNamedItem(Node arg)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerNode.ownerDocument();
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(arg.getOwnerDocument()!=ownerDocument){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
        }
        int i=findNamePoint(arg.getNodeName(),0);
        NodeImpl previous=null;
        if(i>=0){
            previous=(NodeImpl)nodes.get(i);
            nodes.set(i,arg);
        }else{
            i=-1-i; // Insert point (may be end of list)
            if(null==nodes){
                nodes=new ArrayList(5);
            }
            nodes.add(i,arg);
        }
        return previous;
    } // setNamedItem(Node):Node

    public Node removeNamedItem(String name)
            throws DOMException{
        if(isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw
                    new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                            msg);
        }
        int i=findNamePoint(name,0);
        if(i<0){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        NodeImpl n=(NodeImpl)nodes.get(i);
        nodes.remove(i);
        return n;
    } // removeNamedItem(String):Node

    public Node item(int index){
        return (nodes!=null&&index<nodes.size())?
                (Node)(nodes.get(index)):null;
    }

    public int getLength(){
        return (nodes!=null)?nodes.size():0;
    }

    public Node getNamedItemNS(String namespaceURI,String localName){
        int i=findNamePoint(namespaceURI,localName);
        return (i<0)?null:(Node)(nodes.get(i));
    } // getNamedItemNS(String,String):Node

    public Node setNamedItemNS(Node arg)
            throws DOMException{
        CoreDocumentImpl ownerDocument=ownerNode.ownerDocument();
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(arg.getOwnerDocument()!=ownerDocument){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"WRONG_DOCUMENT_ERR",null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,msg);
            }
        }
        int i=findNamePoint(arg.getNamespaceURI(),arg.getLocalName());
        NodeImpl previous=null;
        if(i>=0){
            previous=(NodeImpl)nodes.get(i);
            nodes.set(i,arg);
        }else{
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i=findNamePoint(arg.getNodeName(),0);
            if(i>=0){
                previous=(NodeImpl)nodes.get(i);
                nodes.add(i,arg);
            }else{
                i=-1-i; // Insert point (may be end of list)
                if(null==nodes){
                    nodes=new ArrayList(5);
                }
                nodes.add(i,arg);
            }
        }
        return previous;
    } // setNamedItemNS(Node):Node

    public Node removeNamedItemNS(String namespaceURI,String name)
            throws DOMException{
        if(isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw
                    new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                            msg);
        }
        int i=findNamePoint(namespaceURI,name);
        if(i<0){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_FOUND_ERR",null);
            throw new DOMException(DOMException.NOT_FOUND_ERR,msg);
        }
        NodeImpl n=(NodeImpl)nodes.get(i);
        nodes.remove(i);
        return n;
    } // removeNamedItem(String):Node
    //
    // Public methods
    //

    protected int findNamePoint(String namespaceURI,String name){
        if(nodes==null) return -1;
        if(name==null) return -1;
        // This is a linear search through the same nodes ArrayList.
        // The ArrayList is sorted on the DOM Level 1 nodename.
        // The DOM Level 2 NS keys are namespaceURI and Localname,
        // so we must linear search thru it.
        // In addition, to get this to work with nodes without any namespace
        // (namespaceURI and localNames are both null) we then use the nodeName
        // as a secondary key.
        final int size=nodes.size();
        for(int i=0;i<size;++i){
            NodeImpl a=(NodeImpl)nodes.get(i);
            String aNamespaceURI=a.getNamespaceURI();
            String aLocalName=a.getLocalName();
            if(namespaceURI==null){
                if(aNamespaceURI==null
                        &&
                        (name.equals(aLocalName)
                                ||
                                (aLocalName==null&&name.equals(a.getNodeName()))))
                    return i;
            }else{
                if(namespaceURI.equals(aNamespaceURI)
                        &&
                        name.equals(aLocalName))
                    return i;
            }
        }
        return -1;
    }

    final boolean isReadOnly(){
        return (flags&READONLY)!=0;
    }
    //
    // Package methods
    //

    protected int findNamePoint(String name,int start){
        // Binary search
        int i=0;
        if(nodes!=null){
            int first=start;
            int last=nodes.size()-1;
            while(first<=last){
                i=(first+last)/2;
                int test=name.compareTo(((Node)(nodes.get(i))).getNodeName());
                if(test==0){
                    return i; // Name found
                }else if(test<0){
                    last=i-1;
                }else{
                    first=i+1;
                }
            }
            if(first>i){
                i=first;
            }
        }
        return -1-i; // not-found has to be encoded.
    } // findNamePoint(String):int

    public NamedNodeMapImpl cloneMap(NodeImpl ownerNode){
        NamedNodeMapImpl newmap=new NamedNodeMapImpl(ownerNode);
        newmap.cloneContent(this);
        return newmap;
    }
    //
    // Protected methods
    //

    protected void cloneContent(NamedNodeMapImpl srcmap){
        List srcnodes=srcmap.nodes;
        if(srcnodes!=null){
            int size=srcnodes.size();
            if(size!=0){
                if(nodes==null){
                    nodes=new ArrayList(size);
                }else{
                    nodes.clear();
                }
                for(int i=0;i<size;++i){
                    NodeImpl n=(NodeImpl)srcmap.nodes.get(i);
                    NodeImpl clone=(NodeImpl)n.cloneNode(true);
                    clone.isSpecified(n.isSpecified());
                    nodes.add(clone);
                }
            }
        }
    } // cloneMap():NamedNodeMapImpl

    void setReadOnly(boolean readOnly,boolean deep){
        isReadOnly(readOnly);
        if(deep&&nodes!=null){
            for(int i=nodes.size()-1;i>=0;i--){
                ((NodeImpl)nodes.get(i)).setReadOnly(readOnly,deep);
            }
        }
    } // setReadOnly(boolean,boolean)

    final void isReadOnly(boolean value){
        flags=(short)(value?flags|READONLY:flags&~READONLY);
    }

    boolean getReadOnly(){
        return isReadOnly();
    } // getReadOnly()

    protected void setOwnerDocument(CoreDocumentImpl doc){
        if(nodes!=null){
            final int size=nodes.size();
            for(int i=0;i<size;++i){
                ((NodeImpl)item(i)).setOwnerDocument(doc);
            }
        }
    }

    final boolean changed(){
        return (flags&CHANGED)!=0;
    }

    final void changed(boolean value){
        flags=(short)(value?flags|CHANGED:flags&~CHANGED);
    }
    //
    // Private methods
    //

    final boolean hasDefaults(){
        return (flags&HASDEFAULTS)!=0;
    }

    final void hasDefaults(boolean value){
        flags=(short)(value?flags|HASDEFAULTS:flags&~HASDEFAULTS);
    }

    // compare 2 nodes in the map.  If a precedes b, return true, otherwise
    // return false
    protected boolean precedes(Node a,Node b){
        if(nodes!=null){
            final int size=nodes.size();
            for(int i=0;i<size;++i){
                Node n=(Node)nodes.get(i);
                if(n==a) return true;
                if(n==b) return false;
            }
        }
        return false;
    }

    protected void removeItem(int index){
        if(nodes!=null&&index<nodes.size()){
            nodes.remove(index);
        }
    }

    protected Object getItem(int index){
        if(nodes!=null){
            return nodes.get(index);
        }
        return null;
    }

    protected int addItem(Node arg){
        int i=findNamePoint(arg.getNamespaceURI(),arg.getLocalName());
        if(i>=0){
            nodes.set(i,arg);
        }else{
            // If we can't find by namespaceURI, localName, then we find by
            // nodeName so we know where to insert.
            i=findNamePoint(arg.getNodeName(),0);
            if(i>=0){
                nodes.add(i,arg);
            }else{
                i=-1-i; // Insert point (may be end of list)
                if(null==nodes){
                    nodes=new ArrayList(5);
                }
                nodes.add(i,arg);
            }
        }
        return i;
    }

    protected ArrayList cloneMap(ArrayList list){
        if(list==null){
            list=new ArrayList(5);
        }
        list.clear();
        if(nodes!=null){
            final int size=nodes.size();
            for(int i=0;i<size;++i){
                list.add(nodes.get(i));
            }
        }
        return list;
    }

    protected int getNamedItemIndex(String namespaceURI,String localName){
        return findNamePoint(namespaceURI,localName);
    }

    public void removeAll(){
        if(nodes!=null){
            nodes.clear();
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        if(nodes!=null){
            nodes=new ArrayList(nodes);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        List oldNodes=this.nodes;
        try{
            if(oldNodes!=null){
                this.nodes=new Vector(oldNodes);
            }
            out.defaultWriteObject();
        }
        // If the write fails for some reason ensure
        // that we restore the original object.
        finally{
            this.nodes=oldNodes;
        }
    }
} // class NamedNodeMapImpl

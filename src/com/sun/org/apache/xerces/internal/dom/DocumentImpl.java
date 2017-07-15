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

import com.sun.org.apache.xerces.internal.dom.events.EventImpl;
import com.sun.org.apache.xerces.internal.dom.events.MutationEventImpl;
import org.w3c.dom.*;
import org.w3c.dom.events.*;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

import java.io.*;
import java.util.*;

public class DocumentImpl
        extends CoreDocumentImpl
        implements DocumentTraversal, DocumentEvent, DocumentRange{
    //
    // Constants
    //
    static final long serialVersionUID=515687835542616694L;
    private static final ObjectStreamField[] serialPersistentFields=
            new ObjectStreamField[]{
                    new ObjectStreamField("iterators",Vector.class),
                    new ObjectStreamField("ranges",Vector.class),
                    new ObjectStreamField("eventListeners",Hashtable.class),
                    new ObjectStreamField("mutationEvents",boolean.class),
            };
    //
    // Data
    //
    // REVISIT: Should this be transient? -Ac
    protected List<NodeIterator> iterators;
    // REVISIT: Should this be transient? -Ac
    protected List<Range> ranges;
    protected Map<NodeImpl,List<LEntry>> eventListeners;
    protected boolean mutationEvents=false;
    //
    // Constructors
    //
    EnclosingAttr savedEnclosingAttr;

    public DocumentImpl(){
        super();
    }

    public DocumentImpl(boolean grammarAccess){
        super(grammarAccess);
    }

    public DocumentImpl(DocumentType doctype){
        super(doctype);
    }
    //
    // Node methods
    //

    public DocumentImpl(DocumentType doctype,boolean grammarAccess){
        super(doctype,grammarAccess);
    }

    public Node cloneNode(boolean deep){
        DocumentImpl newdoc=new DocumentImpl();
        callUserDataHandlers(this,newdoc,UserDataHandler.NODE_CLONED);
        cloneNode(newdoc,deep);
        // experimental
        newdoc.mutationEvents=mutationEvents;
        return newdoc;
    } // cloneNode(boolean):Node
    //
    // DocumentTraversal methods
    //

    public DOMImplementation getImplementation(){
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return DOMImplementationImpl.getDOMImplementation();
    }

    public NodeIterator createNodeIterator(Node root,
                                           short whatToShow,
                                           NodeFilter filter){
        return createNodeIterator(root,whatToShow,filter,true);
    }

    public NodeIterator createNodeIterator(Node root,
                                           int whatToShow,
                                           NodeFilter filter,
                                           boolean entityReferenceExpansion){
        if(root==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
        NodeIterator iterator=new NodeIteratorImpl(this,
                root,
                whatToShow,
                filter,
                entityReferenceExpansion);
        if(iterators==null){
            iterators=new ArrayList<>();
        }
        iterators.add(iterator);
        return iterator;
    }

    public TreeWalker createTreeWalker(Node root,
                                       int whatToShow,
                                       NodeFilter filter,
                                       boolean entityReferenceExpansion){
        if(root==null){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
        return new TreeWalkerImpl(root,whatToShow,filter,
                entityReferenceExpansion);
    }
    //
    // Not DOM Level 2. Support DocumentTraversal methods.
    //

    public TreeWalker createTreeWalker(Node root,
                                       short whatToShow,
                                       NodeFilter filter){
        return createTreeWalker(root,whatToShow,filter,true);
    }

    void removeNodeIterator(NodeIterator nodeIterator){
        if(nodeIterator==null) return;
        if(iterators==null) return;
        iterators.remove(nodeIterator);
    }

    //
    // DocumentRange methods
    //
    public Range createRange(){
        if(ranges==null){
            ranges=new ArrayList<>();
        }
        Range range=new RangeImpl(this);
        ranges.add(range);
        return range;
    }

    void removeRange(Range range){
        if(range==null) return;
        if(ranges==null) return;
        ranges.remove(range);
    }    void replacedText(NodeImpl node){
        // notify ranges
        if(ranges!=null){
            int size=ranges.size();
            for(int i=0;i!=size;i++){
                ((RangeImpl)ranges.get(i)).receiveReplacedText(node);
            }
        }
    }

    void splitData(Node node,Node newNode,int offset){
        // notify ranges
        if(ranges!=null){
            int size=ranges.size();
            for(int i=0;i!=size;i++){
                ((RangeImpl)ranges.get(i)).receiveSplitData(node,
                        newNode,offset);
            }
        }
    }    void deletedText(NodeImpl node,int offset,int count){
        // notify ranges
        if(ranges!=null){
            int size=ranges.size();
            for(int i=0;i!=size;i++){
                ((RangeImpl)ranges.get(i)).receiveDeletedText(node,
                        offset,count);
            }
        }
    }

    public Event createEvent(String type)
            throws DOMException{
        if(type.equalsIgnoreCase("Events")||"Event".equals(type))
            return new EventImpl();
        if(type.equalsIgnoreCase("MutationEvents")||
                "MutationEvent".equals(type))
            return new MutationEventImpl();
        else{
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NOT_SUPPORTED_ERR",null);
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,msg);
        }
    }    void insertedText(NodeImpl node,int offset,int count){
        // notify ranges
        if(ranges!=null){
            int size=ranges.size();
            for(int i=0;i!=size;i++){
                ((RangeImpl)ranges.get(i)).receiveInsertedText(node,
                        offset,count);
            }
        }
    }

    protected void dispatchEventToSubtree(Node n,Event e){
        ((NodeImpl)n).dispatchEvent(e);
        if(n.getNodeType()==Node.ELEMENT_NODE){
            NamedNodeMap a=n.getAttributes();
            for(int i=a.getLength()-1;i>=0;--i)
                dispatchingEventToSubtree(a.item(i),e);
        }
        dispatchingEventToSubtree(n.getFirstChild(),e);
    } // dispatchEventToSubtree(NodeImpl,Node,Event) :void
    //
    // DocumentEvent methods
    //

    protected void dispatchingEventToSubtree(Node n,Event e){
        if(n==null)
            return;
        // ***** Recursive implementation. This is excessively expensive,
        // and should be replaced in conjunction with optimization
        // mentioned above.
        ((NodeImpl)n).dispatchEvent(e);
        if(n.getNodeType()==Node.ELEMENT_NODE){
            NamedNodeMap a=n.getAttributes();
            for(int i=a.getLength()-1;i>=0;--i)
                dispatchingEventToSubtree(a.item(i),e);
        }
        dispatchingEventToSubtree(n.getFirstChild(),e);
        dispatchingEventToSubtree(n.getNextSibling(),e);
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        // Convert Maps to Hashtables, Lists to Vectors
        Vector<NodeIterator> it=(iterators==null)?null:new Vector<>(iterators);
        Vector<Range> r=(ranges==null)?null:new Vector<>(ranges);
        Hashtable<NodeImpl,Vector<LEntry>> el=null;
        if(eventListeners!=null){
            el=new Hashtable<>();
            for(Map.Entry<NodeImpl,List<LEntry>> e : eventListeners.entrySet()){
                el.put(e.getKey(),new Vector<>(e.getValue()));
            }
        }
        // Write serialized fields
        ObjectOutputStream.PutField pf=out.putFields();
        pf.put("iterators",it);
        pf.put("ranges",r);
        pf.put("eventListeners",el);
        pf.put("mutationEvents",mutationEvents);
        out.writeFields();
    }    void setMutationEvents(boolean set){
        mutationEvents=set;
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // We have to read serialized fields first.
        ObjectInputStream.GetField gf=in.readFields();
        Vector<NodeIterator> it=(Vector<NodeIterator>)gf.get("iterators",null);
        Vector<Range> r=(Vector<Range>)gf.get("ranges",null);
        Hashtable<NodeImpl,Vector<LEntry>> el=
                (Hashtable<NodeImpl,Vector<LEntry>>)gf.get("eventListeners",null);
        mutationEvents=gf.get("mutationEvents",false);
        //convert Hashtables back to HashMaps and Vectors to Lists
        if(it!=null) iterators=new ArrayList<>(it);
        if(r!=null) ranges=new ArrayList<>(r);
        if(el!=null){
            eventListeners=new HashMap<>();
            for(Map.Entry<NodeImpl,Vector<LEntry>> e : el.entrySet()){
                eventListeners.put(e.getKey(),new ArrayList<>(e.getValue()));
            }
        }
    }    boolean getMutationEvents(){
        return mutationEvents;
    }

    class LEntry implements Serializable{
        private static final long serialVersionUID=-8426757059492421631L;
        String type;
        EventListener listener;
        boolean useCapture;

        LEntry(String type,EventListener listener,boolean useCapture){
            this.type=type;
            this.listener=listener;
            this.useCapture=useCapture;
        }
    } // LEntry    private void setEventListeners(NodeImpl n,List<LEntry> listeners){
        if(eventListeners==null){
            eventListeners=new HashMap<>();
        }
        if(listeners==null){
            eventListeners.remove(n);
            if(eventListeners.isEmpty()){
                // stop firing events when there isn't any listener
                mutationEvents=false;
            }
        }else{
            eventListeners.put(n,listeners);
            // turn mutation events on
            mutationEvents=true;
        }
    }

    class EnclosingAttr implements Serializable{
        private static final long serialVersionUID=5208387723391647216L;
        AttrImpl node;
        String oldvalue;
    }    private List<LEntry> getEventListeners(NodeImpl n){
        if(eventListeners==null){
            return null;
        }
        return eventListeners.get(n);
    }
    //
    // EventTarget support (public and internal)
    //
    //
    // Constants
    //



    @Override
    protected void addEventListener(NodeImpl node,String type,
                                    EventListener listener,boolean useCapture){
        // We can't dispatch to blank type-name, and of course we need
        // a listener to dispatch to
        if(type==null||type.equals("")||listener==null)
            return;
        // Each listener may be registered only once per type per phase.
        // Simplest way to code that is to zap the previous entry, if any.
        removeEventListener(node,type,listener,useCapture);
        List<LEntry> nodeListeners=getEventListeners(node);
        if(nodeListeners==null){
            nodeListeners=new ArrayList<>();
            setEventListeners(node,nodeListeners);
        }
        nodeListeners.add(new LEntry(type,listener,useCapture));
        // Record active listener
        LCount lc=LCount.lookup(type);
        if(useCapture){
            ++lc.captures;
            ++lc.total;
        }else{
            ++lc.bubbles;
            ++lc.total;
        }
    } // addEventListener(NodeImpl,String,EventListener,boolean) :void

    @Override
    protected void removeEventListener(NodeImpl node,String type,
                                       EventListener listener,
                                       boolean useCapture){
        // If this couldn't be a valid listener registration, ignore request
        if(type==null||type.equals("")||listener==null)
            return;
        List<LEntry> nodeListeners=getEventListeners(node);
        if(nodeListeners==null)
            return;
        // Note that addListener has previously ensured that
        // each listener may be registered only once per type per phase.
        // count-down is OK for deletions!
        for(int i=nodeListeners.size()-1;i>=0;--i){
            LEntry le=nodeListeners.get(i);
            if(le.useCapture==useCapture&&le.listener==listener&&
                    le.type.equals(type)){
                nodeListeners.remove(i);
                // Storage management: Discard empty listener lists
                if(nodeListeners.isEmpty())
                    setEventListeners(node,null);
                // Remove active listener
                LCount lc=LCount.lookup(type);
                if(useCapture){
                    --lc.captures;
                    --lc.total;
                }else{
                    --lc.bubbles;
                    --lc.total;
                }
                break;  // Found it; no need to loop farther.
            }
        }
    } // removeEventListener(NodeImpl,String,EventListener,boolean) :void

    @Override
    protected void copyEventListeners(NodeImpl src,NodeImpl tgt){
        List<LEntry> nodeListeners=getEventListeners(src);
        if(nodeListeners==null){
            return;
        }
        setEventListeners(tgt,new ArrayList<>(nodeListeners));
    }

    @Override
    protected boolean dispatchEvent(NodeImpl node,Event event){
        if(event==null) return false;
        // Can't use anyone else's implementation, since there's no public
        // API for setting the event's processing-state fields.
        EventImpl evt=(EventImpl)event;
        // VALIDATE -- must have been initialized at least once, must have
        // a non-null non-blank name.
        if(!evt.initialized||evt.type==null||evt.type.equals("")){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"UNSPECIFIED_EVENT_TYPE_ERR",null);
            throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR,msg);
        }
        // If nobody is listening for this event, discard immediately
        LCount lc=LCount.lookup(evt.getType());
        if(lc.total==0)
            return evt.preventDefault;
        // INITIALIZE THE EVENT'S DISPATCH STATUS
        // (Note that Event objects are reusable in our implementation;
        // that doesn't seem to be explicitly guaranteed in the DOM, but
        // I believe it is the intent.)
        evt.target=node;
        evt.stopPropagation=false;
        evt.preventDefault=false;
        // Capture pre-event parentage chain, not including target;
        // use pre-event-dispatch ancestors even if event handlers mutate
        // document and change the target's context.
        // Note that this is parents ONLY; events do not
        // cross the Attr/Element "blood/brain barrier".
        // DOMAttrModified. which looks like an exception,
        // is issued to the Element rather than the Attr
        // and causes a _second_ DOMSubtreeModified in the Element's
        // tree.
        List<Node> pv=new ArrayList<>(10);
        Node p=node;
        Node n=p.getParentNode();
        while(n!=null){
            pv.add(n);
            p=n;
            n=n.getParentNode();
        }
        // CAPTURING_PHASE:
        if(lc.captures>0){
            evt.eventPhase=Event.CAPTURING_PHASE;
            // Ancestors are scanned, root to target, for
            // Capturing listeners.
            for(int j=pv.size()-1;j>=0;--j){
                if(evt.stopPropagation)
                    break;  // Someone set the flag. Phase ends.
                // Handle all capturing listeners on this node
                NodeImpl nn=(NodeImpl)pv.get(j);
                evt.currentTarget=nn;
                List<LEntry> nodeListeners=getEventListeners(nn);
                if(nodeListeners!=null){
                    List<LEntry> nl=(List)((ArrayList)nodeListeners).clone();
                    // call listeners in the order in which they got registered
                    int nlsize=nl.size();
                    for(int i=0;i<nlsize;i++){
                        LEntry le=nl.get(i);
                        if(le.useCapture&&le.type.equals(evt.type)&&
                                nodeListeners.contains(le)){
                            try{
                                le.listener.handleEvent(evt);
                            }catch(Exception e){
                                // All exceptions are ignored.
                            }
                        }
                    }
                }
            }
        }
        // Both AT_TARGET and BUBBLE use non-capturing listeners.
        if(lc.bubbles>0){
            // AT_TARGET PHASE: Event is dispatched to NON-CAPTURING listeners
            // on the target node. Note that capturing listeners on the target
            // node are _not_ invoked, even during the capture phase.
            evt.eventPhase=Event.AT_TARGET;
            evt.currentTarget=node;
            List<LEntry> nodeListeners=getEventListeners(node);
            if(!evt.stopPropagation&&nodeListeners!=null){
                List<LEntry> nl=(List)((ArrayList)nodeListeners).clone();
                // call listeners in the order in which they got registered
                int nlsize=nl.size();
                for(int i=0;i<nlsize;i++){
                    LEntry le=(LEntry)nl.get(i);
                    if(!le.useCapture&&le.type.equals(evt.type)&&
                            nodeListeners.contains(le)){
                        try{
                            le.listener.handleEvent(evt);
                        }catch(Exception e){
                            // All exceptions are ignored.
                        }
                    }
                }
            }
            // BUBBLING_PHASE: Ancestors are scanned, target to root, for
            // non-capturing listeners. If the event's preventBubbling flag
            // has been set before processing of a node commences, we
            // instead immediately advance to the default phase.
            // Note that not all events bubble.
            if(evt.bubbles){
                evt.eventPhase=Event.BUBBLING_PHASE;
                int pvsize=pv.size();
                for(int j=0;j<pvsize;j++){
                    if(evt.stopPropagation)
                        break;  // Someone set the flag. Phase ends.
                    // Handle all bubbling listeners on this node
                    NodeImpl nn=(NodeImpl)pv.get(j);
                    evt.currentTarget=nn;
                    nodeListeners=getEventListeners(nn);
                    if(nodeListeners!=null){
                        List<LEntry> nl=(List)((ArrayList)nodeListeners).clone();
                        // call listeners in the order in which they got
                        // registered
                        int nlsize=nl.size();
                        for(int i=0;i<nlsize;i++){
                            LEntry le=nl.get(i);
                            if(!le.useCapture&&le.type.equals(evt.type)&&
                                    nodeListeners.contains(le)){
                                try{
                                    le.listener.handleEvent(evt);
                                }catch(Exception e){
                                    // All exceptions are ignored.
                                }
                            }
                        }
                    }
                }
            }
        }
        // DEFAULT PHASE: Some DOMs have default behaviors bound to specific
        // nodes. If this DOM does, and if the event's preventDefault flag has
        // not been set, we now return to the target node and process its
        // default handler for this event, if any.
        // No specific phase value defined, since this is DOM-internal
        if(lc.defaults>0&&(!evt.cancelable||!evt.preventDefault)){
            // evt.eventPhase = Event.DEFAULT_PHASE;
            // evt.currentTarget = node;
            // DO_DEFAULT_OPERATION
        }
        return evt.preventDefault;
    } // dispatchEvent(NodeImpl,Event) :boolean









    protected void dispatchAggregateEvents(NodeImpl node,EnclosingAttr ea){
        if(ea!=null)
            dispatchAggregateEvents(node,ea.node,ea.oldvalue,
                    MutationEvent.MODIFICATION);
        else
            dispatchAggregateEvents(node,null,null,(short)0);
    } // dispatchAggregateEvents(NodeImpl,EnclosingAttr) :void

    protected void dispatchAggregateEvents(NodeImpl node,
                                           AttrImpl enclosingAttr,
                                           String oldvalue,short change){
        // We have to send DOMAttrModified.
        NodeImpl owner=null;
        if(enclosingAttr!=null){
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            owner=(NodeImpl)enclosingAttr.getOwnerElement();
            if(lc.total>0){
                if(owner!=null){
                    MutationEventImpl me=new MutationEventImpl();
                    me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED,
                            true,false,enclosingAttr,
                            oldvalue,
                            enclosingAttr.getNodeValue(),
                            enclosingAttr.getNodeName(),
                            change);
                    owner.dispatchEvent(me);
                }
            }
        }
        // DOMSubtreeModified gets sent to the lowest common root of a
        // set of changes.
        // "This event is dispatched after all other events caused by the
        // mutation have been fired."
        LCount lc=LCount.lookup(MutationEventImpl.DOM_SUBTREE_MODIFIED);
        if(lc.total>0){
            MutationEvent me=new MutationEventImpl();
            me.initMutationEvent(MutationEventImpl.DOM_SUBTREE_MODIFIED,
                    true,false,null,null,
                    null,null,(short)0);
            // If we're within an Attr, DStM gets sent to the Attr
            // and to its owningElement. Otherwise we dispatch it
            // locally.
            if(enclosingAttr!=null){
                dispatchEvent(enclosingAttr,me);
                if(owner!=null)
                    dispatchEvent(owner,me);
            }else
                dispatchEvent(node,me);
        }
    } // dispatchAggregateEvents(NodeImpl, AttrImpl,String) :void

    protected void saveEnclosingAttr(NodeImpl node){
        savedEnclosingAttr=null;
        // MUTATION PREPROCESSING AND PRE-EVENTS:
        // If we're within the scope of an Attr and DOMAttrModified
        // was requested, we need to preserve its previous value for
        // that event.
        LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
        if(lc.total>0){
            NodeImpl eventAncestor=node;
            while(true){
                if(eventAncestor==null)
                    return;
                int type=eventAncestor.getNodeType();
                if(type==Node.ATTRIBUTE_NODE){
                    EnclosingAttr retval=new EnclosingAttr();
                    retval.node=(AttrImpl)eventAncestor;
                    retval.oldvalue=retval.node.getNodeValue();
                    savedEnclosingAttr=retval;
                    return;
                }else if(type==Node.ENTITY_REFERENCE_NODE)
                    eventAncestor=eventAncestor.parentNode();
                else if(type==Node.TEXT_NODE)
                    eventAncestor=eventAncestor.parentNode();
                else
                    return;
                // Any other parent means we're not in an Attr
            }
        }
    } // saveEnclosingAttr(NodeImpl) :void

    void modifyingCharacterData(NodeImpl node,boolean replace){
        if(mutationEvents){
            if(!replace){
                saveEnclosingAttr(node);
            }
        }
    }

    void modifiedCharacterData(NodeImpl node,String oldvalue,String value,boolean replace){
        if(mutationEvents){
            if(!replace){
                // MUTATION POST-EVENTS:
                LCount lc=
                        LCount.lookup(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED);
                if(lc.total>0){
                    MutationEvent me=new MutationEventImpl();
                    me.initMutationEvent(
                            MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED,
                            true,false,null,
                            oldvalue,value,null,(short)0);
                    dispatchEvent(node,me);
                }
                // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified,
                // if required. (Common to most kinds of mutation)
                dispatchAggregateEvents(node,savedEnclosingAttr);
            } // End mutation postprocessing
        }
    }

    void replacedCharacterData(NodeImpl node,String oldvalue,String value){
        //now that we have finished replacing data, we need to perform the same actions
        //that are required after a character data node has been modified
        //send the value of false for replace parameter so that mutation
        //events if appropriate will be initiated
        modifiedCharacterData(node,oldvalue,value,false);
    }

    void insertingNode(NodeImpl node,boolean replace){
        if(mutationEvents){
            if(!replace){
                saveEnclosingAttr(node);
            }
        }
    }

    void insertedNode(NodeImpl node,NodeImpl newInternal,boolean replace){
        if(mutationEvents){
            // MUTATION POST-EVENTS:
            // "Local" events (non-aggregated)
            // New child is told it was inserted, and where
            LCount lc=LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED);
            if(lc.total>0){
                MutationEventImpl me=new MutationEventImpl();
                me.initMutationEvent(MutationEventImpl.DOM_NODE_INSERTED,
                        true,false,node,
                        null,null,null,(short)0);
                dispatchEvent(newInternal,me);
            }
            // If within the Document, tell the subtree it's been added
            // to the Doc.
            lc=LCount.lookup(
                    MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT);
            if(lc.total>0){
                NodeImpl eventAncestor=node;
                if(savedEnclosingAttr!=null)
                    eventAncestor=(NodeImpl)
                            savedEnclosingAttr.node.getOwnerElement();
                if(eventAncestor!=null){ // Might have been orphan Attr
                    NodeImpl p=eventAncestor;
                    while(p!=null){
                        eventAncestor=p; // Last non-null ancestor
                        // In this context, ancestry includes
                        // walking back from Attr to Element
                        if(p.getNodeType()==ATTRIBUTE_NODE){
                            p=(NodeImpl)((AttrImpl)p).getOwnerElement();
                        }else{
                            p=p.parentNode();
                        }
                    }
                    if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE){
                        MutationEventImpl me=new MutationEventImpl();
                        me.initMutationEvent(MutationEventImpl
                                        .DOM_NODE_INSERTED_INTO_DOCUMENT,
                                false,false,null,null,
                                null,null,(short)0);
                        dispatchEventToSubtree(newInternal,me);
                    }
                }
            }
            if(!replace){
                // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified
                // (Common to most kinds of mutation)
                dispatchAggregateEvents(node,savedEnclosingAttr);
            }
        }
        // notify the range of insertions
        if(ranges!=null){
            int size=ranges.size();
            for(int i=0;i!=size;i++){
                ((RangeImpl)ranges.get(i)).insertedNodeFromDOM(newInternal);
            }
        }
    }

    void removingNode(NodeImpl node,NodeImpl oldChild,boolean replace){
        // notify iterators
        if(iterators!=null){
            int size=iterators.size();
            for(int i=0;i!=size;i++){
                ((NodeIteratorImpl)iterators.get(i)).removeNode(oldChild);
            }
        }
        // notify ranges
        if(ranges!=null){
            int size=ranges.size();
            for(int i=0;i!=size;i++){
                ((RangeImpl)ranges.get(i)).removeNode(oldChild);
            }
        }
        // mutation events
        if(mutationEvents){
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            // If we're within the scope of an Attr and DOMAttrModified
            // was requested, we need to preserve its previous value for
            // that event.
            if(!replace){
                saveEnclosingAttr(node);
            }
            // Child is told that it is about to be removed
            LCount lc=LCount.lookup(MutationEventImpl.DOM_NODE_REMOVED);
            if(lc.total>0){
                MutationEventImpl me=new MutationEventImpl();
                me.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED,
                        true,false,node,null,
                        null,null,(short)0);
                dispatchEvent(oldChild,me);
            }
            // If within Document, child's subtree is informed that it's
            // losing that status
            lc=LCount.lookup(
                    MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT);
            if(lc.total>0){
                NodeImpl eventAncestor=this;
                if(savedEnclosingAttr!=null)
                    eventAncestor=(NodeImpl)
                            savedEnclosingAttr.node.getOwnerElement();
                if(eventAncestor!=null){ // Might have been orphan Attr
                    for(NodeImpl p=eventAncestor.parentNode();
                        p!=null;p=p.parentNode()){
                        eventAncestor=p; // Last non-null ancestor
                    }
                    if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE){
                        MutationEventImpl me=new MutationEventImpl();
                        me.initMutationEvent(
                                MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT,
                                false,false,null,
                                null,null,null,(short)0);
                        dispatchEventToSubtree(oldChild,me);
                    }
                }
            }
        } // End mutation preprocessing
    }

    void removedNode(NodeImpl node,boolean replace){
        if(mutationEvents){
            // MUTATION POST-EVENTS:
            // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified,
            // if required. (Common to most kinds of mutation)
            if(!replace){
                dispatchAggregateEvents(node,savedEnclosingAttr);
            }
        } // End mutation postprocessing
    }

    void replacingNode(NodeImpl node){
        if(mutationEvents){
            saveEnclosingAttr(node);
        }
    }

    void replacingData(NodeImpl node){
        if(mutationEvents){
            saveEnclosingAttr(node);
        }
    }

    void replacedNode(NodeImpl node){
        if(mutationEvents){
            dispatchAggregateEvents(node,savedEnclosingAttr);
        }
    }

    void modifiedAttrValue(AttrImpl attr,String oldvalue){
        if(mutationEvents){
            // MUTATION POST-EVENTS:
            dispatchAggregateEvents(attr,attr,oldvalue,
                    MutationEvent.MODIFICATION);
        }
    }

    void setAttrNode(AttrImpl attr,AttrImpl previous){
        if(mutationEvents){
            // MUTATION POST-EVENTS:
            if(previous==null){
                dispatchAggregateEvents(attr.ownerNode,attr,null,
                        MutationEvent.ADDITION);
            }else{
                dispatchAggregateEvents(attr.ownerNode,attr,
                        previous.getNodeValue(),
                        MutationEvent.MODIFICATION);
            }
        }
    }

    void removedAttrNode(AttrImpl attr,NodeImpl oldOwner,String name){
        // We can't use the standard dispatchAggregate, since it assumes
        // that the Attr is still attached to an owner. This code is
        // similar but dispatches to the previous owner, "element".
        if(mutationEvents){
            // If we have to send DOMAttrModified (determined earlier),
            // do so.
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.total>0){
                MutationEventImpl me=new MutationEventImpl();
                me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED,
                        true,false,attr,
                        attr.getNodeValue(),null,name,
                        MutationEvent.REMOVAL);
                dispatchEvent(oldOwner,me);
            }
            // We can hand off to process DOMSubtreeModified, though.
            // Note that only the Element needs to be informed; the
            // Attr's subtree has not been changed by this operation.
            dispatchAggregateEvents(oldOwner,null,null,(short)0);
        }
    }

    void renamedAttrNode(Attr oldAt,Attr newAt){
        // REVISIT: To be implemented!!!
    }

    void renamedElement(Element oldEl,Element newEl){
        // REVISIT: To be implemented!!!
    }




} // class DocumentImpl

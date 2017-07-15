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
 * $Id: DTMDefaultBaseTraversers.java,v 1.2.4.1 2005/09/15 08:15:00 suresh_emailid Exp $
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
 * $Id: DTMDefaultBaseTraversers.java,v 1.2.4.1 2005/09/15 08:15:00 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.*;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import com.sun.org.apache.xml.internal.utils.XMLStringFactory;

import javax.xml.transform.Source;

public abstract class DTMDefaultBaseTraversers extends DTMDefaultBase{
    public DTMDefaultBaseTraversers(DTMManager mgr,Source source,
                                    int dtmIdentity,
                                    DTMWSFilter whiteSpaceFilter,
                                    XMLStringFactory xstringfactory,
                                    boolean doIndexing){
        super(mgr,source,dtmIdentity,whiteSpaceFilter,xstringfactory,
                doIndexing);
    }

    public DTMDefaultBaseTraversers(DTMManager mgr,Source source,
                                    int dtmIdentity,
                                    DTMWSFilter whiteSpaceFilter,
                                    XMLStringFactory xstringfactory,
                                    boolean doIndexing,
                                    int blocksize,
                                    boolean usePrevsib,
                                    boolean newNameTable){
        super(mgr,source,dtmIdentity,whiteSpaceFilter,xstringfactory,
                doIndexing,blocksize,usePrevsib,newNameTable);
    }

    public DTMAxisTraverser getAxisTraverser(final int axis){
        DTMAxisTraverser traverser;
        if(null==m_traversers)  // Cache of stateless traversers for this DTM
        {
            m_traversers=new DTMAxisTraverser[Axis.getNamesLength()];
            traverser=null;
        }else{
            traverser=m_traversers[axis];  // Share/reuse existing traverser
            if(traverser!=null)
                return traverser;
        }
        switch(axis)  // Generate new traverser
        {
            case Axis.ANCESTOR:
                traverser=new AncestorTraverser();
                break;
            case Axis.ANCESTORORSELF:
                traverser=new AncestorOrSelfTraverser();
                break;
            case Axis.ATTRIBUTE:
                traverser=new AttributeTraverser();
                break;
            case Axis.CHILD:
                traverser=new ChildTraverser();
                break;
            case Axis.DESCENDANT:
                traverser=new DescendantTraverser();
                break;
            case Axis.DESCENDANTORSELF:
                traverser=new DescendantOrSelfTraverser();
                break;
            case Axis.FOLLOWING:
                traverser=new FollowingTraverser();
                break;
            case Axis.FOLLOWINGSIBLING:
                traverser=new FollowingSiblingTraverser();
                break;
            case Axis.NAMESPACE:
                traverser=new NamespaceTraverser();
                break;
            case Axis.NAMESPACEDECLS:
                traverser=new NamespaceDeclsTraverser();
                break;
            case Axis.PARENT:
                traverser=new ParentTraverser();
                break;
            case Axis.PRECEDING:
                traverser=new PrecedingTraverser();
                break;
            case Axis.PRECEDINGSIBLING:
                traverser=new PrecedingSiblingTraverser();
                break;
            case Axis.SELF:
                traverser=new SelfTraverser();
                break;
            case Axis.ALL:
                traverser=new AllFromRootTraverser();
                break;
            case Axis.ALLFROMNODE:
                traverser=new AllFromNodeTraverser();
                break;
            case Axis.PRECEDINGANDANCESTOR:
                traverser=new PrecedingAndAncestorTraverser();
                break;
            case Axis.DESCENDANTSFROMROOT:
                traverser=new DescendantFromRootTraverser();
                break;
            case Axis.DESCENDANTSORSELFFROMROOT:
                traverser=new DescendantOrSelfFromRootTraverser();
                break;
            case Axis.ROOT:
                traverser=new RootTraverser();
                break;
            case Axis.FILTEREDLIST:
                return null; // Don't want to throw an exception for this one.
            default:
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_UNKNOWN_AXIS_TYPE,new Object[]{Integer.toString(axis)})); //"Unknown axis traversal type: "+axis);
        }
        if(null==traverser)
            throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_AXIS_TRAVERSER_NOT_SUPPORTED,new Object[]{Axis.getNames(axis)}));
        // "Axis traverser not supported: "
        //                       + Axis.names[axis]);
        m_traversers[axis]=traverser;
        return traverser;
    }

    private class AncestorTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            return getParent(current);
        }

        public int next(int context,int current,int expandedTypeID){
            // Process using identities
            current=makeNodeIdentity(current);
            while(DTM.NULL!=(current=m_parent.elementAt(current))){
                if(m_exptype.elementAt(current)==expandedTypeID)
                    return makeNodeHandle(current);
            }
            return NULL;
        }
    }

    private class AncestorOrSelfTraverser extends AncestorTraverser{
        public int first(int context){
            return context;
        }

        public int first(int context,int expandedTypeID){
            return (getExpandedTypeID(context)==expandedTypeID)
                    ?context:next(context,context,expandedTypeID);
        }
    }

    private class AttributeTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            return (context==current)
                    ?getFirstAttribute(context):getNextAttribute(current);
        }

        public int next(int context,int current,int expandedTypeID){
            current=(context==current)
                    ?getFirstAttribute(context):getNextAttribute(current);
            do{
                if(getExpandedTypeID(current)==expandedTypeID)
                    return current;
            }
            while(DTM.NULL!=(current=getNextAttribute(current)));
            return NULL;
        }
    }

    private class ChildTraverser extends DTMAxisTraverser{
        protected int getNextIndexed(int axisRoot,int nextPotential,
                                     int expandedTypeID){
            int nsIndex=m_expandedNameTable.getNamespaceID(expandedTypeID);
            int lnIndex=m_expandedNameTable.getLocalNameID(expandedTypeID);
            for(;;){
                int nextID=findElementFromIndex(nsIndex,lnIndex,nextPotential);
                if(NOTPROCESSED!=nextID){
                    int parentID=m_parent.elementAt(nextID);
                    // Is it a child?
                    if(parentID==axisRoot)
                        return nextID;
                    // If the parent occured before the subtree root, then
                    // we know it is past the child axis.
                    if(parentID<axisRoot)
                        return NULL;
                    // Otherwise, it could be a descendant below the subtree root
                    // children, or it could be after the subtree root.  So we have
                    // to climb up until the parent is less than the subtree root, in
                    // which case we return NULL, or until it is equal to the subtree
                    // root, in which case we continue to look.
                    do{
                        parentID=m_parent.elementAt(parentID);
                        if(parentID<axisRoot)
                            return NULL;
                    }
                    while(parentID>axisRoot);
                    // System.out.println("Found node via index: "+first);
                    nextPotential=nextID+1;
                    continue;
                }
                nextNode();
                if(!(m_nextsib.elementAt(axisRoot)==NOTPROCESSED))
                    break;
            }
            return DTM.NULL;
        }

        public int first(int context){
            return getFirstChild(context);
        }

        public int first(int context,int expandedTypeID){
            if(true){
                int identity=makeNodeIdentity(context);
                int firstMatch=getNextIndexed(identity,_firstch(identity),
                        expandedTypeID);
                return makeNodeHandle(firstMatch);
            }else{
                // %REVIEW% Dead code. Eliminate?
                for(int current=_firstch(makeNodeIdentity(context));
                    DTM.NULL!=current;
                    current=_nextsib(current)){
                    if(m_exptype.elementAt(current)==expandedTypeID)
                        return makeNodeHandle(current);
                }
                return NULL;
            }
        }

        public int next(int context,int current){
            return getNextSibling(current);
        }

        public int next(int context,int current,int expandedTypeID){
            // Process in Identifier space
            for(current=_nextsib(makeNodeIdentity(current));
                DTM.NULL!=current;
                current=_nextsib(current)){
                if(m_exptype.elementAt(current)==expandedTypeID)
                    return makeNodeHandle(current);
            }
            return NULL;
        }
    }

    private abstract class IndexedDTMAxisTraverser extends DTMAxisTraverser{
        protected final boolean isIndexed(int expandedTypeID){
            return (m_indexing
                    &&ExpandedNameTable.ELEMENT
                    ==m_expandedNameTable.getType(expandedTypeID));
        }

        protected int getNextIndexed(int axisRoot,int nextPotential,
                                     int expandedTypeID){
            int nsIndex=m_expandedNameTable.getNamespaceID(expandedTypeID);
            int lnIndex=m_expandedNameTable.getLocalNameID(expandedTypeID);
            while(true){
                int next=findElementFromIndex(nsIndex,lnIndex,nextPotential);
                if(NOTPROCESSED!=next){
                    if(isAfterAxis(axisRoot,next))
                        return NULL;
                    // System.out.println("Found node via index: "+first);
                    return next;
                }else if(axisHasBeenProcessed(axisRoot))
                    break;
                nextNode();
            }
            return DTM.NULL;
        }

        protected abstract boolean isAfterAxis(int axisRoot,int identity);

        protected abstract boolean axisHasBeenProcessed(int axisRoot);
    }

    private class DescendantTraverser extends IndexedDTMAxisTraverser{
        protected boolean isAfterAxis(int axisRoot,int identity){
            // %REVIEW% Is there *any* cheaper way to do this?
            // Yes. In ID space, compare to axisRoot's successor
            // (next-sib or ancestor's-next-sib). Probably shallower search.
            do{
                if(identity==axisRoot)
                    return false;
                identity=m_parent.elementAt(identity);
            }
            while(identity>=axisRoot);
            return true;
        }        protected int getFirstPotential(int identity){
            return identity+1;
        }

        protected boolean axisHasBeenProcessed(int axisRoot){
            return !(m_nextsib.elementAt(axisRoot)==NOTPROCESSED);
        }

        protected int getSubtreeRoot(int handle){
            return makeNodeIdentity(handle);
        }

        protected boolean isDescendant(int subtreeRootIdentity,int identity){
            return _parent(identity)>=subtreeRootIdentity;
        }



        public int first(int context,int expandedTypeID){
            if(isIndexed(expandedTypeID)){
                int identity=getSubtreeRoot(context);
                int firstPotential=getFirstPotential(identity);
                return makeNodeHandle(getNextIndexed(identity,firstPotential,expandedTypeID));
            }
            return next(context,context,expandedTypeID);
        }

        public int next(int context,int current){
            int subtreeRootIdent=getSubtreeRoot(context);
            for(current=makeNodeIdentity(current)+1;;current++){
                int type=_type(current);  // may call nextNode()
                if(!isDescendant(subtreeRootIdent,current))
                    return NULL;
                if(ATTRIBUTE_NODE==type||NAMESPACE_NODE==type)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
        }

        public int next(int context,int current,int expandedTypeID){
            int subtreeRootIdent=getSubtreeRoot(context);
            current=makeNodeIdentity(current)+1;
            if(isIndexed(expandedTypeID)){
                return makeNodeHandle(getNextIndexed(subtreeRootIdent,current,expandedTypeID));
            }
            for(;;current++){
                int exptype=_exptype(current);  // may call nextNode()
                if(!isDescendant(subtreeRootIdent,current))
                    return NULL;
                if(exptype!=expandedTypeID)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
        }
    }

    private class DescendantOrSelfTraverser extends DescendantTraverser{
        protected int getFirstPotential(int identity){
            return identity;
        }

        public int first(int context){
            return context;
        }
    }

    private class AllFromNodeTraverser extends DescendantOrSelfTraverser{
        public int next(int context,int current){
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)+1;;current++){
                // Trickological code: _exptype() has the side-effect of
                // running nextNode until the specified node has been loaded,
                // and thus can be used to ensure that incremental construction of
                // the DTM has gotten this far. Using it just for that side-effect
                // is quite a kluge...
                _exptype(current);  // make sure it's here.
                if(!isDescendant(subtreeRootIdent,current))
                    return NULL;
                return makeNodeHandle(current);  // make handle.
            }
        }
    }

    private class FollowingTraverser extends DescendantTraverser{
        public int first(int context){
            // Compute in ID space
            context=makeNodeIdentity(context);
            int first;
            int type=_type(context);
            if((DTM.ATTRIBUTE_NODE==type)||(DTM.NAMESPACE_NODE==type)){
                context=_parent(context);
                first=_firstch(context);
                if(NULL!=first)
                    return makeNodeHandle(first);
            }
            do{
                first=_nextsib(context);
                if(NULL==first)
                    context=_parent(context);
            }
            while(NULL==first&&NULL!=context);
            return makeNodeHandle(first);
        }

        public int first(int context,int expandedTypeID){
            // %REVIEW% This looks like it might want shift into identity space
            // to avoid repeated conversion in the individual functions
            int first;
            int type=getNodeType(context);
            if((DTM.ATTRIBUTE_NODE==type)||(DTM.NAMESPACE_NODE==type)){
                context=getParent(context);
                first=getFirstChild(context);
                if(NULL!=first){
                    if(getExpandedTypeID(first)==expandedTypeID)
                        return first;
                    else
                        return next(context,first,expandedTypeID);
                }
            }
            do{
                first=getNextSibling(context);
                if(NULL==first)
                    context=getParent(context);
                else{
                    if(getExpandedTypeID(first)==expandedTypeID)
                        return first;
                    else
                        return next(context,first,expandedTypeID);
                }
            }
            while(NULL==first&&NULL!=context);
            return first;
        }

        public int next(int context,int current){
            // Compute in identity space
            current=makeNodeIdentity(current);
            while(true){
                current++; // Only works on IDs, not handles.
                // %REVIEW% Are we using handles or indexes?
                int type=_type(current);  // may call nextNode()
                if(NULL==type)
                    return NULL;
                if(ATTRIBUTE_NODE==type||NAMESPACE_NODE==type)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
        }

        public int next(int context,int current,int expandedTypeID){
            // Compute in ID space
            current=makeNodeIdentity(current);
            while(true){
                current++;
                int etype=_exptype(current);  // may call nextNode()
                if(NULL==etype)
                    return NULL;
                if(etype!=expandedTypeID)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
        }
    }

    private class FollowingSiblingTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            return getNextSibling(current);
        }

        public int next(int context,int current,int expandedTypeID){
            while(DTM.NULL!=(current=getNextSibling(current))){
                if(getExpandedTypeID(current)==expandedTypeID)
                    return current;
            }
            return NULL;
        }
    }

    private class NamespaceDeclsTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            return (context==current)
                    ?getFirstNamespaceNode(context,false)
                    :getNextNamespaceNode(context,current,false);
        }

        public int next(int context,int current,int expandedTypeID){
            current=(context==current)
                    ?getFirstNamespaceNode(context,false)
                    :getNextNamespaceNode(context,current,false);
            do{
                if(getExpandedTypeID(current)==expandedTypeID)
                    return current;
            }
            while(DTM.NULL
                    !=(current=getNextNamespaceNode(context,current,false)));
            return NULL;
        }
    }

    private class NamespaceTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            return (context==current)
                    ?getFirstNamespaceNode(context,true)
                    :getNextNamespaceNode(context,current,true);
        }

        public int next(int context,int current,int expandedTypeID){
            current=(context==current)
                    ?getFirstNamespaceNode(context,true)
                    :getNextNamespaceNode(context,current,true);
            do{
                if(getExpandedTypeID(current)==expandedTypeID)
                    return current;
            }
            while(DTM.NULL
                    !=(current=getNextNamespaceNode(context,current,true)));
            return NULL;
        }
    }

    private class ParentTraverser extends DTMAxisTraverser{
        public int first(int context){
            return getParent(context);
        }

        public int first(int current,int expandedTypeID){
            // Compute in ID space
            current=makeNodeIdentity(current);
            while(NULL!=(current=m_parent.elementAt(current))){
                if(m_exptype.elementAt(current)==expandedTypeID)
                    return makeNodeHandle(current);
            }
            return NULL;
        }

        public int next(int context,int current){
            return NULL;
        }

        public int next(int context,int current,int expandedTypeID){
            return NULL;
        }
    }

    private class PrecedingTraverser extends DTMAxisTraverser{
        protected boolean isAncestor(int contextIdent,int currentIdent){
            // %REVIEW% See comments in IsAfterAxis; using the "successor" of
            // contextIdent is probably more efficient.
            for(contextIdent=m_parent.elementAt(contextIdent);DTM.NULL!=contextIdent;
                contextIdent=m_parent.elementAt(contextIdent)){
                if(contextIdent==currentIdent)
                    return true;
            }
            return false;
        }

        public int next(int context,int current){
            // compute in ID space
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)-1;current>=0;current--){
                short type=_type(current);
                if(ATTRIBUTE_NODE==type||NAMESPACE_NODE==type
                        ||isAncestor(subtreeRootIdent,current))
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
            return NULL;
        }

        public int next(int context,int current,int expandedTypeID){
            // Compute in ID space
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)-1;current>=0;current--){
                int exptype=m_exptype.elementAt(current);
                if(exptype!=expandedTypeID
                        ||isAncestor(subtreeRootIdent,current))
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
            return NULL;
        }
    }

    private class PrecedingAndAncestorTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            // Compute in ID space
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)-1;current>=0;current--){
                short type=_type(current);
                if(ATTRIBUTE_NODE==type||NAMESPACE_NODE==type)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
            return NULL;
        }

        public int next(int context,int current,int expandedTypeID){
            // Compute in ID space
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)-1;current>=0;current--){
                int exptype=m_exptype.elementAt(current);
                if(exptype!=expandedTypeID)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
            return NULL;
        }
    }

    private class PrecedingSiblingTraverser extends DTMAxisTraverser{
        public int next(int context,int current){
            return getPreviousSibling(current);
        }

        public int next(int context,int current,int expandedTypeID){
            while(DTM.NULL!=(current=getPreviousSibling(current))){
                if(getExpandedTypeID(current)==expandedTypeID)
                    return current;
            }
            return NULL;
        }
    }

    private class SelfTraverser extends DTMAxisTraverser{
        public int first(int context){
            return context;
        }

        public int first(int context,int expandedTypeID){
            return (getExpandedTypeID(context)==expandedTypeID)?context:NULL;
        }

        public int next(int context,int current){
            return NULL;
        }

        public int next(int context,int current,int expandedTypeID){
            return NULL;
        }
    }

    private class AllFromRootTraverser extends AllFromNodeTraverser{
        public int first(int context){
            return getDocumentRoot(context);
        }

        public int next(int context,int current){
            // Compute in ID space
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)+1;;current++){
                // Kluge test: Just make sure +1 yielded a real node
                int type=_type(current);  // may call nextNode()
                if(type==NULL)
                    return NULL;
                return makeNodeHandle(current);  // make handle.
            }
        }        public int first(int context,int expandedTypeID){
            return (getExpandedTypeID(getDocumentRoot(context))==expandedTypeID)
                    ?context:next(context,context,expandedTypeID);
        }



        public int next(int context,int current,int expandedTypeID){
            // Compute in ID space
            int subtreeRootIdent=makeNodeIdentity(context);
            for(current=makeNodeIdentity(current)+1;;current++){
                int exptype=_exptype(current);  // may call nextNode()
                if(exptype==NULL)
                    return NULL;
                if(exptype!=expandedTypeID)
                    continue;
                return makeNodeHandle(current);  // make handle.
            }
        }
    }

    private class RootTraverser extends AllFromRootTraverser{
        public int first(int context,int expandedTypeID){
            int root=getDocumentRoot(context);
            return (getExpandedTypeID(root)==expandedTypeID)
                    ?root:NULL;
        }

        public int next(int context,int current){
            return NULL;
        }

        public int next(int context,int current,int expandedTypeID){
            return NULL;
        }
    }

    private class DescendantOrSelfFromRootTraverser extends DescendantTraverser{
        protected int getFirstPotential(int identity){
            return identity;
        }

        protected int getSubtreeRoot(int handle){
            // %REVIEW% Shouldn't this always be 0?
            return makeNodeIdentity(getDocument());
        }

        public int first(int context){
            return getDocumentRoot(context);
        }

        public int first(int context,int expandedTypeID){
            if(isIndexed(expandedTypeID)){
                int identity=0;
                int firstPotential=getFirstPotential(identity);
                return makeNodeHandle(getNextIndexed(identity,firstPotential,expandedTypeID));
            }
            int root=first(context);
            return next(root,root,expandedTypeID);
        }
    }

    private class DescendantFromRootTraverser extends DescendantTraverser{
        protected int getFirstPotential(int identity){
            return _firstch(0);
        }

        protected int getSubtreeRoot(int handle){
            return 0;
        }

        public int first(int context){
            return makeNodeHandle(_firstch(0));
        }

        public int first(int context,int expandedTypeID){
            if(isIndexed(expandedTypeID)){
                int identity=0;
                int firstPotential=getFirstPotential(identity);
                return makeNodeHandle(getNextIndexed(identity,firstPotential,expandedTypeID));
            }
            int root=getDocumentRoot(context);
            return next(root,root,expandedTypeID);
        }
    }
}

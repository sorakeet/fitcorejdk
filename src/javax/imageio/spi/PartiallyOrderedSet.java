/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import java.util.*;

class PartiallyOrderedSet extends AbstractSet{
    // The topological sort (roughly) follows the algorithm described in
    // Horowitz and Sahni, _Fundamentals of Data Structures_ (1976),
    // p. 315.
    // Maps Objects to DigraphNodes that contain them
    private Map poNodes=new HashMap();
    // The set of Objects
    private Set nodes=poNodes.keySet();

    public PartiallyOrderedSet(){
    }

    public Iterator iterator(){
        return new PartialOrderIterator(poNodes.values().iterator());
    }

    public int size(){
        return nodes.size();
    }

    public boolean contains(Object o){
        return nodes.contains(o);
    }

    public boolean add(Object o){
        if(nodes.contains(o)){
            return false;
        }
        DigraphNode node=new DigraphNode(o);
        poNodes.put(o,node);
        return true;
    }

    public boolean remove(Object o){
        DigraphNode node=(DigraphNode)poNodes.get(o);
        if(node==null){
            return false;
        }
        poNodes.remove(o);
        node.dispose();
        return true;
    }

    public void clear(){
        poNodes.clear();
    }

    public boolean setOrdering(Object first,Object second){
        DigraphNode firstPONode=
                (DigraphNode)poNodes.get(first);
        DigraphNode secondPONode=
                (DigraphNode)poNodes.get(second);
        secondPONode.removeEdge(firstPONode);
        return firstPONode.addEdge(secondPONode);
    }

    public boolean unsetOrdering(Object first,Object second){
        DigraphNode firstPONode=
                (DigraphNode)poNodes.get(first);
        DigraphNode secondPONode=
                (DigraphNode)poNodes.get(second);
        return firstPONode.removeEdge(secondPONode)||
                secondPONode.removeEdge(firstPONode);
    }

    public boolean hasOrdering(Object preferred,Object other){
        DigraphNode preferredPONode=
                (DigraphNode)poNodes.get(preferred);
        DigraphNode otherPONode=
                (DigraphNode)poNodes.get(other);
        return preferredPONode.hasEdge(otherPONode);
    }
}

class PartialOrderIterator implements Iterator{
    LinkedList zeroList=new LinkedList();
    Map inDegrees=new HashMap(); // DigraphNode -> Integer

    public PartialOrderIterator(Iterator iter){
        // Initialize scratch in-degree values, zero list
        while(iter.hasNext()){
            DigraphNode node=(DigraphNode)iter.next();
            int inDegree=node.getInDegree();
            inDegrees.put(node,new Integer(inDegree));
            // Add nodes with zero in-degree to the zero list
            if(inDegree==0){
                zeroList.add(node);
            }
        }
    }

    public boolean hasNext(){
        return !zeroList.isEmpty();
    }

    public Object next(){
        DigraphNode first=(DigraphNode)zeroList.removeFirst();
        // For each out node of the output node, decrement its in-degree
        Iterator outNodes=first.getOutNodes();
        while(outNodes.hasNext()){
            DigraphNode node=(DigraphNode)outNodes.next();
            int inDegree=((Integer)inDegrees.get(node)).intValue()-1;
            inDegrees.put(node,new Integer(inDegree));
            // If the in-degree has fallen to 0, place the node on the list
            if(inDegree==0){
                zeroList.add(node);
            }
        }
        return first.getData();
    }

    public void remove(){
        throw new UnsupportedOperationException();
    }
}

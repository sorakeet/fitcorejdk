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
 * <p>
 * $Id: ExsltSets.java,v 1.1.2.1 2005/08/01 02:08:50 jeffsuttor Exp $
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
/**
 * $Id: ExsltSets.java,v 1.1.2.1 2005/08/01 02:08:50 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xml.internal.utils.DOMHelper;
import com.sun.org.apache.xpath.internal.NodeSet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class ExsltSets extends ExsltBase{
    public static NodeList leading(NodeList nl1,NodeList nl2){
        if(nl2.getLength()==0)
            return nl1;
        NodeSet ns1=new NodeSet(nl1);
        NodeSet leadNodes=new NodeSet();
        Node endNode=nl2.item(0);
        if(!ns1.contains(endNode))
            return leadNodes; // empty NodeSet
        for(int i=0;i<nl1.getLength();i++){
            Node testNode=nl1.item(i);
            if(DOMHelper.isNodeAfter(testNode,endNode)
                    &&!DOMHelper.isNodeTheSame(testNode,endNode))
                leadNodes.addElement(testNode);
        }
        return leadNodes;
    }

    public static NodeList trailing(NodeList nl1,NodeList nl2){
        if(nl2.getLength()==0)
            return nl1;
        NodeSet ns1=new NodeSet(nl1);
        NodeSet trailNodes=new NodeSet();
        Node startNode=nl2.item(0);
        if(!ns1.contains(startNode))
            return trailNodes; // empty NodeSet
        for(int i=0;i<nl1.getLength();i++){
            Node testNode=nl1.item(i);
            if(DOMHelper.isNodeAfter(startNode,testNode)
                    &&!DOMHelper.isNodeTheSame(startNode,testNode))
                trailNodes.addElement(testNode);
        }
        return trailNodes;
    }

    public static NodeList intersection(NodeList nl1,NodeList nl2){
        NodeSet ns1=new NodeSet(nl1);
        NodeSet ns2=new NodeSet(nl2);
        NodeSet inter=new NodeSet();
        inter.setShouldCacheNodes(true);
        for(int i=0;i<ns1.getLength();i++){
            Node n=ns1.elementAt(i);
            if(ns2.contains(n))
                inter.addElement(n);
        }
        return inter;
    }

    public static NodeList difference(NodeList nl1,NodeList nl2){
        NodeSet ns1=new NodeSet(nl1);
        NodeSet ns2=new NodeSet(nl2);
        NodeSet diff=new NodeSet();
        diff.setShouldCacheNodes(true);
        for(int i=0;i<ns1.getLength();i++){
            Node n=ns1.elementAt(i);
            if(!ns2.contains(n))
                diff.addElement(n);
        }
        return diff;
    }

    public static NodeList distinct(NodeList nl){
        NodeSet dist=new NodeSet();
        dist.setShouldCacheNodes(true);
        Map<String,Node> stringTable=new HashMap<>();
        for(int i=0;i<nl.getLength();i++){
            Node currNode=nl.item(i);
            String key=toString(currNode);
            if(key==null)
                dist.addElement(currNode);
            else if(!stringTable.containsKey(key)){
                stringTable.put(key,currNode);
                dist.addElement(currNode);
            }
        }
        return dist;
    }

    public static boolean hasSameNode(NodeList nl1,NodeList nl2){
        NodeSet ns1=new NodeSet(nl1);
        NodeSet ns2=new NodeSet(nl2);
        for(int i=0;i<ns1.getLength();i++){
            if(ns2.contains(ns1.elementAt(i)))
                return true;
        }
        return false;
    }
}

/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * Copyright 2003-2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.models;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

public class CMNodeFactory{
    private static final String ERROR_REPORTER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY;
    private static final String SECURITY_MANAGER=
            Constants.XERCES_PROPERTY_PREFIX+Constants.SECURITY_MANAGER_PROPERTY;
    private static final boolean DEBUG=false;
    //
    private static final int MULTIPLICITY=1;
    //count of number of nodes created
    private int nodeCount=0;
    //No. of nodes allowed.
    private int maxNodeLimit;
    private XMLErrorReporter fErrorReporter;
    // stores defaults for different security holes (maxOccurLimit in current context) if it has
    // been set on the configuration.
    private XMLSecurityManager fSecurityManager=null;

    public CMNodeFactory(){
    }

    public void reset(XMLComponentManager componentManager){
        fErrorReporter=(XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        try{
            fSecurityManager=(XMLSecurityManager)componentManager.getProperty(SECURITY_MANAGER);
            //we are setting the limit of number of nodes to 3times the maxOccur value..
            if(fSecurityManager!=null){
                maxNodeLimit=fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_OCCUR_NODE_LIMIT)*MULTIPLICITY;
            }
        }catch(XMLConfigurationException e){
            fSecurityManager=null;
        }
    }//reset()

    public CMNode getCMLeafNode(int type,Object leaf,int id,int position){
        return new XSCMLeaf(type,leaf,id,position);
    }

    public CMNode getCMRepeatingLeafNode(int type,Object leaf,
                                         int minOccurs,int maxOccurs,int id,int position){
        nodeCountCheck();
        return new XSCMRepeatingLeaf(type,leaf,minOccurs,maxOccurs,id,position);
    }

    public void nodeCountCheck(){
        if(fSecurityManager!=null&&!fSecurityManager.isNoLimit(maxNodeLimit)&&
                nodeCount++>maxNodeLimit){
            if(DEBUG){
                System.out.println("nodeCount = "+nodeCount);
                System.out.println("nodeLimit = "+maxNodeLimit);
            }
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,"MaxOccurLimit",new Object[]{new Integer(maxNodeLimit)},XMLErrorReporter.SEVERITY_FATAL_ERROR);
            // similarly to entity manager behaviour, take into accont
            // behaviour if continue-after-fatal-error is set.
            nodeCount=0;
        }
    }//nodeCountCheck()

    public CMNode getCMUniOpNode(int type,CMNode childNode){
        nodeCountCheck();
        return new XSCMUniOp(type,childNode);
    }

    public CMNode getCMBinOpNode(int type,CMNode leftNode,CMNode rightNode){
        return new XSCMBinOp(type,leftNode,rightNode);
    }

    //reset the node count
    public void resetNodeCount(){
        nodeCount=0;
    }

    public void setProperty(String propertyId,Object value)
            throws XMLConfigurationException{
        // Xerces properties
        if(propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)){
            final int suffixLength=propertyId.length()-Constants.XERCES_PROPERTY_PREFIX.length();
            if(suffixLength==Constants.SECURITY_MANAGER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.SECURITY_MANAGER_PROPERTY)){
                fSecurityManager=(XMLSecurityManager)value;
                maxNodeLimit=(fSecurityManager!=null)?
                        fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_OCCUR_NODE_LIMIT)*MULTIPLICITY:0;
                return;
            }
            if(suffixLength==Constants.ERROR_REPORTER_PROPERTY.length()&&
                    propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)){
                fErrorReporter=(XMLErrorReporter)value;
                return;
            }
        }
    } // setProperty(String,Object)
}//CMNodeFactory()

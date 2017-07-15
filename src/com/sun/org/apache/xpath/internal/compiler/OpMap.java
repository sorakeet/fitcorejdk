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
 * $Id: OpMap.java,v 1.1.2.1 2005/08/01 01:30:31 jeffsuttor Exp $
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
 * $Id: OpMap.java,v 1.1.2.1 2005/08/01 01:30:31 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.compiler;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.utils.ObjectVector;
import com.sun.org.apache.xpath.internal.patterns.NodeTest;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public class OpMap{
    // Position indexes
    public static final int MAPINDEX_LENGTH=1;
    static final int MAXTOKENQUEUESIZE=500;
    static final int BLOCKTOKENQUEUESIZE=500;
    protected String m_currentPattern;
    ObjectVector m_tokenQueue=new ObjectVector(MAXTOKENQUEUESIZE,BLOCKTOKENQUEUESIZE);
    OpMapVector m_opMap=null;

    public static int getNextOpPos(int[] opMap,int opPos){
        return opPos+opMap[opPos+1];
    }

    public static int getFirstChildPos(int opPos){
        return opPos+2;
    }
    //  public int m_tokenQueueSize = 0;

    public static int getFirstChildPosOfStep(int opPos){
        return opPos+3;
    }

    public String toString(){
        return m_currentPattern;
    }

    public String getPatternString(){
        return m_currentPattern;
    }

    public ObjectVector getTokenQueue(){
        return m_tokenQueue;
    }

    public Object getToken(int pos){
        return m_tokenQueue.elementAt(pos);
    }

    public int getTokenQueueSize(){
        return m_tokenQueue.size();
    }

    public OpMapVector getOpMap(){
        return m_opMap;
    }

    void shrink(){
        int n=m_opMap.elementAt(MAPINDEX_LENGTH);
        m_opMap.setToSize(n+4);
        m_opMap.setElementAt(0,n);
        m_opMap.setElementAt(0,n+1);
        m_opMap.setElementAt(0,n+2);
        n=m_tokenQueue.size();
        m_tokenQueue.setToSize(n+4);
        m_tokenQueue.setElementAt(null,n);
        m_tokenQueue.setElementAt(null,n+1);
        m_tokenQueue.setElementAt(null,n+2);
    }

    public void setOp(int opPos,int value){
        m_opMap.setElementAt(value,opPos);
    }

    public int getNextStepPos(int opPos){
        int stepType=getOp(opPos);
        if((stepType>=OpCodes.AXES_START_TYPES)
                &&(stepType<=OpCodes.AXES_END_TYPES)){
            return getNextOpPos(opPos);
        }else if((stepType>=OpCodes.FIRST_NODESET_OP)
                &&(stepType<=OpCodes.LAST_NODESET_OP)){
            int newOpPos=getNextOpPos(opPos);
            while(OpCodes.OP_PREDICATE==getOp(newOpPos)){
                newOpPos=getNextOpPos(newOpPos);
            }
            stepType=getOp(newOpPos);
            if(!((stepType>=OpCodes.AXES_START_TYPES)
                    &&(stepType<=OpCodes.AXES_END_TYPES))){
                return OpCodes.ENDOP;
            }
            return newOpPos;
        }else{
            throw new RuntimeException(
                    XSLMessages.createXPATHMessage(XPATHErrorResources.ER_UNKNOWN_STEP,new Object[]{String.valueOf(stepType)}));
            //"Programmer's assertion in getNextStepPos: unknown stepType: " + stepType);
        }
    }

    public int getOp(int opPos){
        return m_opMap.elementAt(opPos);
    }

    public int getNextOpPos(int opPos){
        return opPos+m_opMap.elementAt(opPos+1);
    }

    public int getFirstPredicateOpPos(int opPos)
            throws javax.xml.transform.TransformerException{
        int stepType=m_opMap.elementAt(opPos);
        if((stepType>=OpCodes.AXES_START_TYPES)
                &&(stepType<=OpCodes.AXES_END_TYPES)){
            return opPos+m_opMap.elementAt(opPos+2);
        }else if((stepType>=OpCodes.FIRST_NODESET_OP)
                &&(stepType<=OpCodes.LAST_NODESET_OP)){
            return opPos+m_opMap.elementAt(opPos+1);
        }else if(-2==stepType){
            return -2;
        }else{
            error(XPATHErrorResources.ER_UNKNOWN_OPCODE,
                    new Object[]{String.valueOf(stepType)});  //"ERROR! Unknown op code: "+m_opMap[opPos]);
            return -1;
        }
    }

    public void error(String msg,Object[] args) throws javax.xml.transform.TransformerException{
        String fmsg=XSLMessages.createXPATHMessage(msg,args);
        throw new javax.xml.transform.TransformerException(fmsg);
    }

    public int getArgLength(int opPos){
        return m_opMap.elementAt(opPos+MAPINDEX_LENGTH);
    }

    public int getStepTestType(int opPosOfStep){
        return m_opMap.elementAt(opPosOfStep+3);  // skip past op, len, len without predicates
    }

    public String getStepNS(int opPosOfStep){
        int argLenOfStep=getArgLengthOfStep(opPosOfStep);
        // System.out.println("getStepNS.argLenOfStep: "+argLenOfStep);
        if(argLenOfStep==3){
            int index=m_opMap.elementAt(opPosOfStep+4);
            if(index>=0)
                return (String)m_tokenQueue.elementAt(index);
            else if(OpCodes.ELEMWILDCARD==index)
                return NodeTest.WILD;
            else
                return null;
        }else
            return null;
    }

    public int getArgLengthOfStep(int opPos){
        return m_opMap.elementAt(opPos+MAPINDEX_LENGTH+1)-3;
    }

    public String getStepLocalName(int opPosOfStep){
        int argLenOfStep=getArgLengthOfStep(opPosOfStep);
        // System.out.println("getStepLocalName.argLenOfStep: "+argLenOfStep);
        int index;
        switch(argLenOfStep){
            case 0:
                index=OpCodes.EMPTY;
                break;
            case 1:
                index=OpCodes.ELEMWILDCARD;
                break;
            case 2:
                index=m_opMap.elementAt(opPosOfStep+4);
                break;
            case 3:
                index=m_opMap.elementAt(opPosOfStep+5);
                break;
            default:
                index=OpCodes.EMPTY;
                break;  // Should assert error
        }
        // int index = (argLenOfStep == 3) ? m_opMap[opPosOfStep+5]
        //                                  : ((argLenOfStep == 1) ? -3 : -2);
        if(index>=0)
            return (String)m_tokenQueue.elementAt(index).toString();
        else if(OpCodes.ELEMWILDCARD==index)
            return NodeTest.WILD;
        else
            return null;
    }
}

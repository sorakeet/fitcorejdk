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
 * $Id: DTMStringPool.java,v 1.2.4.1 2005/09/15 08:15:05 suresh_emailid Exp $
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
 * $Id: DTMStringPool.java,v 1.2.4.1 2005/09/15 08:15:05 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.utils.IntVector;

import java.util.Vector;

public class DTMStringPool{
    public static final int NULL=-1;
    static final int HASHPRIME=101;
    Vector m_intToString;
    int[] m_hashStart=new int[HASHPRIME];
    IntVector m_hashChain;

    public DTMStringPool(){
        this(512);
    }

    public DTMStringPool(int chainSize){
        m_intToString=new Vector();
        m_hashChain=new IntVector(chainSize);
        removeAllElements();
        // -sb Add this to force empty strings to be index 0.
        stringToIndex("");
    }

    public void removeAllElements(){
        m_intToString.removeAllElements();
        for(int i=0;i<HASHPRIME;++i)
            m_hashStart[i]=NULL;
        m_hashChain.removeAllElements();
    }

    public int stringToIndex(String s){
        if(s==null) return NULL;
        int hashslot=s.hashCode()%HASHPRIME;
        if(hashslot<0) hashslot=-hashslot;
        // Is it one we already know?
        int hashlast=m_hashStart[hashslot];
        int hashcandidate=hashlast;
        while(hashcandidate!=NULL){
            if(m_intToString.elementAt(hashcandidate).equals(s))
                return hashcandidate;
            hashlast=hashcandidate;
            hashcandidate=m_hashChain.elementAt(hashcandidate);
        }
        // New value. Add to tables.
        int newIndex=m_intToString.size();
        m_intToString.addElement(s);
        m_hashChain.addElement(NULL);     // Initialize to no-following-same-hash
        if(hashlast==NULL)  // First for this hash
            m_hashStart[hashslot]=newIndex;
        else // Link from previous with same hash
            m_hashChain.setElementAt(newIndex,hashlast);
        return newIndex;
    }

    public static void _main(String[] args){
        String[] word={
                "Zero","One","Two","Three","Four","Five",
                "Six","Seven","Eight","Nine","Ten",
                "Eleven","Twelve","Thirteen","Fourteen","Fifteen",
                "Sixteen","Seventeen","Eighteen","Nineteen","Twenty",
                "Twenty-One","Twenty-Two","Twenty-Three","Twenty-Four",
                "Twenty-Five","Twenty-Six","Twenty-Seven","Twenty-Eight",
                "Twenty-Nine","Thirty","Thirty-One","Thirty-Two",
                "Thirty-Three","Thirty-Four","Thirty-Five","Thirty-Six",
                "Thirty-Seven","Thirty-Eight","Thirty-Nine"};
        DTMStringPool pool=new DTMStringPool();
        System.out.println("If no complaints are printed below, we passed initial test.");
        for(int pass=0;pass<=1;++pass){
            int i;
            for(i=0;i<word.length;++i){
                int j=pool.stringToIndex(word[i]);
                if(j!=i)
                    System.out.println("\tMismatch populating pool: assigned "+
                            j+" for create "+i);
            }
            for(i=0;i<word.length;++i){
                int j=pool.stringToIndex(word[i]);
                if(j!=i)
                    System.out.println("\tMismatch in stringToIndex: returned "+
                            j+" for lookup "+i);
            }
            for(i=0;i<word.length;++i){
                String w=pool.indexToString(i);
                if(!word[i].equals(w))
                    System.out.println("\tMismatch in indexToString: returned"+
                            w+" for lookup "+i);
            }
            pool.removeAllElements();
            System.out.println("\nPass "+pass+" complete\n");
        } // end pass loop
    }

    public String indexToString(int i)
            throws ArrayIndexOutOfBoundsException{
        if(i==NULL) return null;
        return (String)m_intToString.elementAt(i);
    }
}

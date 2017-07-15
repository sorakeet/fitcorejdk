/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

public class XMLGrammarPoolImpl implements XMLGrammarPool{
    //
    // Constants
    //
    protected static final int TABLE_SIZE=11;
    private static final boolean DEBUG=false;
    //
    // Data
    //
    protected Entry[] fGrammars=null;
    // whether this pool is locked
    protected boolean fPoolIsLocked;
    // the number of grammars in the pool
    protected int fGrammarCount=0;
    //
    // Constructors
    //

    public XMLGrammarPoolImpl(){
        fGrammars=new Entry[TABLE_SIZE];
        fPoolIsLocked=false;
    } // <init>()

    public XMLGrammarPoolImpl(int initialCapacity){
        fGrammars=new Entry[initialCapacity];
        fPoolIsLocked=false;
    }
    //
    // XMLGrammarPool methods
    //

    public Grammar[] retrieveInitialGrammarSet(String grammarType){
        synchronized(fGrammars){
            int grammarSize=fGrammars.length;
            Grammar[] tempGrammars=new Grammar[fGrammarCount];
            int pos=0;
            for(int i=0;i<grammarSize;i++){
                for(Entry e=fGrammars[i];e!=null;e=e.next){
                    if(e.desc.getGrammarType().equals(grammarType)){
                        tempGrammars[pos++]=e.grammar;
                    }
                }
            }
            Grammar[] toReturn=new Grammar[pos];
            System.arraycopy(tempGrammars,0,toReturn,0,pos);
            return toReturn;
        }
    } // retrieveInitialGrammarSet (String): Grammar[]

    public void cacheGrammars(String grammarType,Grammar[] grammars){
        if(!fPoolIsLocked){
            for(int i=0;i<grammars.length;i++){
                if(DEBUG){
                    System.out.println("CACHED GRAMMAR "+(i+1));
                    Grammar temp=grammars[i];
                    //print(temp.getGrammarDescription());
                }
                putGrammar(grammars[i]);
            }
        }
    } // cacheGrammars(String, Grammar[]);

    public Grammar retrieveGrammar(XMLGrammarDescription desc){
        if(DEBUG){
            System.out.println("RETRIEVING GRAMMAR FROM THE APPLICATION WITH FOLLOWING DESCRIPTION :");
            //print(desc);
        }
        return getGrammar(desc);
    } // retrieveGrammar(XMLGrammarDescription):  Grammar
    //
    // Public methods
    //

    public Grammar getGrammar(XMLGrammarDescription desc){
        synchronized(fGrammars){
            int hash=hashCode(desc);
            int index=(hash&0x7FFFFFFF)%fGrammars.length;
            for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                if((entry.hash==hash)&&equals(entry.desc,desc)){
                    return entry.grammar;
                }
            }
            return null;
        }
    } // getGrammar(XMLGrammarDescription):Grammar

    public void lockPool(){
        fPoolIsLocked=true;
    } // lockPool()

    public void unlockPool(){
        fPoolIsLocked=false;
    } // unlockPool()

    public void clear(){
        for(int i=0;i<fGrammars.length;i++){
            if(fGrammars[i]!=null){
                fGrammars[i].clear();
                fGrammars[i]=null;
            }
        }
        fGrammarCount=0;
    } // clear()

    public void putGrammar(Grammar grammar){
        if(!fPoolIsLocked){
            synchronized(fGrammars){
                XMLGrammarDescription desc=grammar.getGrammarDescription();
                int hash=hashCode(desc);
                int index=(hash&0x7FFFFFFF)%fGrammars.length;
                for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                    if(entry.hash==hash&&equals(entry.desc,desc)){
                        entry.grammar=grammar;
                        return;
                    }
                }
                // create a new entry
                Entry entry=new Entry(hash,desc,grammar,fGrammars[index]);
                fGrammars[index]=entry;
                fGrammarCount++;
            }
        }
    } // putGrammar(Grammar)

    public boolean equals(XMLGrammarDescription desc1,XMLGrammarDescription desc2){
        return desc1.equals(desc2);
    }

    public int hashCode(XMLGrammarDescription desc){
        return desc.hashCode();
    }

    public Grammar removeGrammar(XMLGrammarDescription desc){
        synchronized(fGrammars){
            int hash=hashCode(desc);
            int index=(hash&0x7FFFFFFF)%fGrammars.length;
            for(Entry entry=fGrammars[index], prev=null;entry!=null;prev=entry,entry=entry.next){
                if((entry.hash==hash)&&equals(entry.desc,desc)){
                    if(prev!=null){
                        prev.next=entry.next;
                    }else{
                        fGrammars[index]=entry.next;
                    }
                    Grammar tempGrammar=entry.grammar;
                    entry.grammar=null;
                    fGrammarCount--;
                    return tempGrammar;
                }
            }
            return null;
        }
    } // removeGrammar(XMLGrammarDescription):Grammar

    public boolean containsGrammar(XMLGrammarDescription desc){
        synchronized(fGrammars){
            int hash=hashCode(desc);
            int index=(hash&0x7FFFFFFF)%fGrammars.length;
            for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                if((entry.hash==hash)&&equals(entry.desc,desc)){
                    return true;
                }
            }
            return false;
        }
    } // containsGrammar(XMLGrammarDescription):boolean

    protected static final class Entry{
        public int hash;
        public XMLGrammarDescription desc;
        public Grammar grammar;
        public Entry next;

        protected Entry(int hash,XMLGrammarDescription desc,Grammar grammar,Entry next){
            this.hash=hash;
            this.desc=desc;
            this.grammar=grammar;
            this.next=next;
        }

        // clear this entry; useful to promote garbage collection
        // since reduces reference count of objects to be destroyed
        protected void clear(){
            desc=null;
            grammar=null;
            if(next!=null){
                next.clear();
                next=null;
            }
        } // clear()
    } // class Entry
    /** For DTD build we can't import here XSDDescription. Thus, this method is commented out.. */
    /** public void print(XMLGrammarDescription description){
     if(description.getGrammarType().equals(XMLGrammarDescription.XML_DTD)){

     }
     else if(description.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)){
     XSDDescription schema = (XSDDescription)description ;
     System.out.println("Context = " + schema.getContextType());
     System.out.println("TargetNamespace = " + schema.getTargetNamespace());
     String [] temp = schema.getLocationHints();

     for (int i = 0 ; (temp != null && i < temp.length) ; i++){
     System.out.println("LocationHint " + i + " = "+ temp[i]);
     }

     System.out.println("Triggering Component = " + schema.getTriggeringComponent());
     System.out.println("EnclosingElementName =" + schema.getEnclosingElementName());

     }

     }//print
     */
} // class XMLGrammarPoolImpl

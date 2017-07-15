/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.jaxp.validation;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

final class SoftReferenceGrammarPool implements XMLGrammarPool{
    //
    // Constants
    //
    protected static final int TABLE_SIZE=11;
    protected static final Grammar[] ZERO_LENGTH_GRAMMAR_ARRAY=new Grammar[0];
    protected final ReferenceQueue fReferenceQueue=new ReferenceQueue();
    //
    // Data
    //
    protected Entry[] fGrammars=null;
    protected boolean fPoolIsLocked;
    protected int fGrammarCount=0;
    //
    // Constructors
    //

    public SoftReferenceGrammarPool(){
        fGrammars=new Entry[TABLE_SIZE];
        fPoolIsLocked=false;
    } // <init>()

    public SoftReferenceGrammarPool(int initialCapacity){
        fGrammars=new Entry[initialCapacity];
        fPoolIsLocked=false;
    }
    //
    // XMLGrammarPool methods
    //

    public Grammar[] retrieveInitialGrammarSet(String grammarType){
        synchronized(fGrammars){
            clean();
            // Return no grammars. This allows the garbage collector to sift
            // out grammars which are not in use when memory demand is high.
            // It also allows the pool to return the "right" schema grammar
            // based on schema locations.
            return ZERO_LENGTH_GRAMMAR_ARRAY;
        }
    } // retrieveInitialGrammarSet (String): Grammar[]

    public void cacheGrammars(String grammarType,Grammar[] grammars){
        if(!fPoolIsLocked){
            for(int i=0;i<grammars.length;++i){
                putGrammar(grammars[i]);
            }
        }
    } // cacheGrammars(String, Grammar[]);

    public Grammar retrieveGrammar(XMLGrammarDescription desc){
        return getGrammar(desc);
    } // retrieveGrammar(XMLGrammarDescription):  Grammar
    //
    // Public methods
    //

    public Grammar getGrammar(XMLGrammarDescription desc){
        synchronized(fGrammars){
            clean();
            int hash=hashCode(desc);
            int index=(hash&0x7FFFFFFF)%fGrammars.length;
            for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                Grammar tempGrammar=(Grammar)entry.grammar.get();
                /** If the soft reference has been cleared, remove this entry from the pool. */
                if(tempGrammar==null){
                    removeEntry(entry);
                }else if((entry.hash==hash)&&equals(entry.desc,desc)){
                    return tempGrammar;
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
                clean();
                XMLGrammarDescription desc=grammar.getGrammarDescription();
                int hash=hashCode(desc);
                int index=(hash&0x7FFFFFFF)%fGrammars.length;
                for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                    if(entry.hash==hash&&equals(entry.desc,desc)){
                        if(entry.grammar.get()!=grammar){
                            entry.grammar=new SoftGrammarReference(entry,grammar,fReferenceQueue);
                        }
                        return;
                    }
                }
                // create a new entry
                Entry entry=new Entry(hash,index,desc,grammar,fGrammars[index],fReferenceQueue);
                fGrammars[index]=entry;
                fGrammarCount++;
            }
        }
    } // putGrammar(Grammar)

    public boolean equals(XMLGrammarDescription desc1,XMLGrammarDescription desc2){
        if(desc1 instanceof XMLSchemaDescription){
            if(!(desc2 instanceof XMLSchemaDescription)){
                return false;
            }
            final XMLSchemaDescription sd1=(XMLSchemaDescription)desc1;
            final XMLSchemaDescription sd2=(XMLSchemaDescription)desc2;
            final String targetNamespace=sd1.getTargetNamespace();
            if(targetNamespace!=null){
                if(!targetNamespace.equals(sd2.getTargetNamespace())){
                    return false;
                }
            }else if(sd2.getTargetNamespace()!=null){
                return false;
            }
            // The JAXP 1.3 spec says that the implementation can assume that
            // if two schema location hints are the same they always resolve
            // to the same document. In the default grammar pool implementation
            // we only look at the target namespaces. Here we also compare
            // location hints.
            final String expandedSystemId=sd1.getExpandedSystemId();
            if(expandedSystemId!=null){
                if(!expandedSystemId.equals(sd2.getExpandedSystemId())){
                    return false;
                }
            }else if(sd2.getExpandedSystemId()!=null){
                return false;
            }
            return true;
        }
        return desc1.equals(desc2);
    }

    public int hashCode(XMLGrammarDescription desc){
        if(desc instanceof XMLSchemaDescription){
            final XMLSchemaDescription sd=(XMLSchemaDescription)desc;
            final String targetNamespace=sd.getTargetNamespace();
            final String expandedSystemId=sd.getExpandedSystemId();
            int hash=(targetNamespace!=null)?targetNamespace.hashCode():0;
            hash^=(expandedSystemId!=null)?expandedSystemId.hashCode():0;
            return hash;
        }
        return desc.hashCode();
    }

    private void clean(){
        Reference ref=fReferenceQueue.poll();
        while(ref!=null){
            Entry entry=((SoftGrammarReference)ref).entry;
            if(entry!=null){
                removeEntry(entry);
            }
            ref=fReferenceQueue.poll();
        }
    }

    private Grammar removeEntry(Entry entry){
        if(entry.prev!=null){
            entry.prev.next=entry.next;
        }else{
            fGrammars[entry.bucket]=entry.next;
        }
        if(entry.next!=null){
            entry.next.prev=entry.prev;
        }
        --fGrammarCount;
        entry.grammar.entry=null;
        return (Grammar)entry.grammar.get();
    }

    public Grammar removeGrammar(XMLGrammarDescription desc){
        synchronized(fGrammars){
            clean();
            int hash=hashCode(desc);
            int index=(hash&0x7FFFFFFF)%fGrammars.length;
            for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                if((entry.hash==hash)&&equals(entry.desc,desc)){
                    return removeEntry(entry);
                }
            }
            return null;
        }
    } // removeGrammar(XMLGrammarDescription):Grammar

    public boolean containsGrammar(XMLGrammarDescription desc){
        synchronized(fGrammars){
            clean();
            int hash=hashCode(desc);
            int index=(hash&0x7FFFFFFF)%fGrammars.length;
            for(Entry entry=fGrammars[index];entry!=null;entry=entry.next){
                Grammar tempGrammar=(Grammar)entry.grammar.get();
                /** If the soft reference has been cleared, remove this entry from the pool. */
                if(tempGrammar==null){
                    removeEntry(entry);
                }else if((entry.hash==hash)&&equals(entry.desc,desc)){
                    return true;
                }
            }
            return false;
        }
    } // containsGrammar(XMLGrammarDescription):boolean

    static final class Entry{
        public int hash;
        public int bucket;
        public Entry prev;
        public Entry next;
        public XMLGrammarDescription desc;
        public SoftGrammarReference grammar;

        protected Entry(int hash,int bucket,XMLGrammarDescription desc,Grammar grammar,Entry next,ReferenceQueue queue){
            this.hash=hash;
            this.bucket=bucket;
            this.prev=null;
            this.next=next;
            if(next!=null){
                next.prev=this;
            }
            this.desc=desc;
            this.grammar=new SoftGrammarReference(this,grammar,queue);
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

    static final class SoftGrammarReference extends SoftReference{
        public Entry entry;

        protected SoftGrammarReference(Entry entry,Grammar grammar,ReferenceQueue queue){
            super(grammar,queue);
            this.entry=entry;
        }
    } // class SoftGrammarReference
} // class SoftReferenceGrammarPool

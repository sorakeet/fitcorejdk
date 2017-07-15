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
package com.sun.org.apache.xerces.internal.util;

public class SymbolTable{
    //
    // Constants
    //
    protected static final int TABLE_SIZE=101;
    protected static final int MAX_HASH_COLLISIONS=40;
    protected static final int MULTIPLIERS_SIZE=1<<5;
    protected static final int MULTIPLIERS_MASK=MULTIPLIERS_SIZE-1;
    protected final int fCollisionThreshold;
    //
    // Data
    //
    protected Entry[] fBuckets=null;
    protected int fTableSize;
    protected transient int fCount;
    protected int fThreshold;
    protected float fLoadFactor;
    protected int[] fHashMultipliers;
    //
    // Constructors
    //

    public SymbolTable(int initialCapacity){
        this(initialCapacity,0.75f);
    }

    public SymbolTable(int initialCapacity,float loadFactor){
        if(initialCapacity<0){
            throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
        }
        if(loadFactor<=0||Float.isNaN(loadFactor)){
            throw new IllegalArgumentException("Illegal Load: "+loadFactor);
        }
        if(initialCapacity==0){
            initialCapacity=1;
        }
        fLoadFactor=loadFactor;
        fTableSize=initialCapacity;
        fBuckets=new Entry[fTableSize];
        fThreshold=(int)(fTableSize*loadFactor);
        fCollisionThreshold=(int)(MAX_HASH_COLLISIONS*loadFactor);
        fCount=0;
    }

    public SymbolTable(){
        this(TABLE_SIZE,0.75f);
    }
    //
    // Public methods
    //

    public String addSymbol(String symbol){
        // search for identical symbol
        int collisionCount=0;
        int bucket=hash(symbol)%fTableSize;
        for(Entry entry=fBuckets[bucket];entry!=null;entry=entry.next){
            if(entry.symbol.equals(symbol)){
                return entry.symbol;
            }
            ++collisionCount;
        }
        return addSymbol0(symbol,bucket,collisionCount);
    } // addSymbol(String):String

    private String addSymbol0(String symbol,int bucket,int collisionCount){
        if(fCount>=fThreshold){
            // Rehash the table if the threshold is exceeded
            rehash();
            bucket=hash(symbol)%fTableSize;
        }else if(collisionCount>=fCollisionThreshold){
            // Select a new hash function and rehash the table if
            // the collision threshold is exceeded.
            rebalance();
            bucket=hash(symbol)%fTableSize;
        }
        // create new entry
        Entry entry=new Entry(symbol,fBuckets[bucket]);
        fBuckets[bucket]=entry;
        ++fCount;
        return entry.symbol;
    } // addSymbol0(String,int,int):String

    public String addSymbol(char[] buffer,int offset,int length){
        // search for identical symbol
        int collisionCount=0;
        int bucket=hash(buffer,offset,length)%fTableSize;
        OUTER:
        for(Entry entry=fBuckets[bucket];entry!=null;entry=entry.next){
            if(length==entry.characters.length){
                for(int i=0;i<length;i++){
                    if(buffer[offset+i]!=entry.characters[i]){
                        ++collisionCount;
                        continue OUTER;
                    }
                }
                return entry.symbol;
            }
            ++collisionCount;
        }
        return addSymbol0(buffer,offset,length,bucket,collisionCount);
    } // addSymbol(char[],int,int):String

    private String addSymbol0(char[] buffer,int offset,int length,int bucket,int collisionCount){
        if(fCount>=fThreshold){
            // Rehash the table if the threshold is exceeded
            rehash();
            bucket=hash(buffer,offset,length)%fTableSize;
        }else if(collisionCount>=fCollisionThreshold){
            // Select a new hash function and rehash the table if
            // the collision threshold is exceeded.
            rebalance();
            bucket=hash(buffer,offset,length)%fTableSize;
        }
        // add new entry
        Entry entry=new Entry(buffer,offset,length,fBuckets[bucket]);
        fBuckets[bucket]=entry;
        ++fCount;
        return entry.symbol;
    } // addSymbol0(char[],int,int,int,int):String

    protected void rehash(){
        rehashCommon(fBuckets.length*2+1);
    }

    protected void rebalance(){
        if(fHashMultipliers==null){
            fHashMultipliers=new int[MULTIPLIERS_SIZE];
        }
        PrimeNumberSequenceGenerator.generateSequence(fHashMultipliers);
        rehashCommon(fBuckets.length);
    }

    private void rehashCommon(final int newCapacity){
        int oldCapacity=fBuckets.length;
        Entry[] oldTable=fBuckets;
        Entry[] newTable=new Entry[newCapacity];
        fThreshold=(int)(newCapacity*fLoadFactor);
        fBuckets=newTable;
        fTableSize=fBuckets.length;
        for(int i=oldCapacity;i-->0;){
            for(Entry old=oldTable[i];old!=null;){
                Entry e=old;
                old=old.next;
                int index=hash(e.symbol)%newCapacity;
                e.next=newTable[index];
                newTable[index]=e;
            }
        }
    }

    public boolean containsSymbol(String symbol){
        // search for identical symbol
        int bucket=hash(symbol)%fTableSize;
        int length=symbol.length();
        OUTER:
        for(Entry entry=fBuckets[bucket];entry!=null;entry=entry.next){
            if(length==entry.characters.length){
                for(int i=0;i<length;i++){
                    if(symbol.charAt(i)!=entry.characters[i]){
                        continue OUTER;
                    }
                }
                return true;
            }
        }
        return false;
    } // containsSymbol(String):boolean

    public int hash(String symbol){
        if(fHashMultipliers==null){
            return symbol.hashCode()&0x7FFFFFFF;
        }
        return hash0(symbol);
    } // hash(String):int

    private int hash0(String symbol){
        int code=0;
        final int length=symbol.length();
        final int[] multipliers=fHashMultipliers;
        for(int i=0;i<length;++i){
            code=code*multipliers[i&MULTIPLIERS_MASK]+symbol.charAt(i);
        }
        return code&0x7FFFFFFF;
    } // hash0(String):int

    public boolean containsSymbol(char[] buffer,int offset,int length){
        // search for identical symbol
        int bucket=hash(buffer,offset,length)%fTableSize;
        OUTER:
        for(Entry entry=fBuckets[bucket];entry!=null;entry=entry.next){
            if(length==entry.characters.length){
                for(int i=0;i<length;i++){
                    if(buffer[offset+i]!=entry.characters[i]){
                        continue OUTER;
                    }
                }
                return true;
            }
        }
        return false;
    } // containsSymbol(char[],int,int):boolean

    public int hash(char[] buffer,int offset,int length){
        if(fHashMultipliers==null){
            int code=0;
            for(int i=0;i<length;++i){
                code=code*31+buffer[offset+i];
            }
            return code&0x7FFFFFFF;
        }
        return hash0(buffer,offset,length);
    } // hash(char[],int,int):int

    private int hash0(char[] buffer,int offset,int length){
        int code=0;
        final int[] multipliers=fHashMultipliers;
        for(int i=0;i<length;++i){
            code=code*multipliers[i&MULTIPLIERS_MASK]+buffer[offset+i];
        }
        return code&0x7FFFFFFF;
    } // hash0(char[],int,int):int
    //
    // Classes
    //

    protected static final class Entry{
        //
        // Data
        //
        public final String symbol;
        public final char[] characters;
        public Entry next;
        //
        // Constructors
        //

        public Entry(String symbol,Entry next){
            this.symbol=symbol.intern();
            characters=new char[symbol.length()];
            symbol.getChars(0,characters.length,characters,0);
            this.next=next;
        }

        public Entry(char[] ch,int offset,int length,Entry next){
            characters=new char[length];
            System.arraycopy(ch,offset,characters,0,length);
            symbol=new String(characters).intern();
            this.next=next;
        }
    } // class Entry
} // class SymbolTable

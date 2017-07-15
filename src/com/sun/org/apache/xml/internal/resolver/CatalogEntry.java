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
// CatalogEntry.java - Represents Catalog entries
package com.sun.org.apache.xml.internal.resolver;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CatalogEntry{
    protected static final Map<String,Integer> entryTypes=new ConcurrentHashMap<>();
    protected static AtomicInteger nextEntry=new AtomicInteger(0);
    protected static Vector entryArgs=new Vector();
    protected int entryType=0;
    protected Vector args=null;

    public CatalogEntry(){
    }

    public CatalogEntry(String name,Vector args)
            throws CatalogException{
        Integer iType=entryTypes.get(name);
        if(iType==null){
            throw new CatalogException(CatalogException.INVALID_ENTRY_TYPE);
        }
        int type=iType;
        try{
            Integer iArgs=(Integer)entryArgs.get(type);
            if(iArgs.intValue()!=args.size()){
                throw new CatalogException(CatalogException.INVALID_ENTRY);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            throw new CatalogException(CatalogException.INVALID_ENTRY_TYPE);
        }
        entryType=type;
        this.args=args;
    }

    public CatalogEntry(int type,Vector args)
            throws CatalogException{
        try{
            Integer iArgs=(Integer)entryArgs.get(type);
            if(iArgs.intValue()!=args.size()){
                throw new CatalogException(CatalogException.INVALID_ENTRY);
            }
        }catch(ArrayIndexOutOfBoundsException e){
            throw new CatalogException(CatalogException.INVALID_ENTRY_TYPE);
        }
        entryType=type;
        this.args=args;
    }

    static int addEntryType(String name,int numArgs){
        final int index=nextEntry.getAndIncrement();
        entryTypes.put(name,index);
        entryArgs.add(index,numArgs);
        return index;
    }

    public static int getEntryArgCount(String name)
            throws CatalogException{
        return getEntryArgCount(getEntryType(name));
    }

    public static int getEntryType(String name)
            throws CatalogException{
        if(!entryTypes.containsKey(name)){
            throw new CatalogException(CatalogException.INVALID_ENTRY_TYPE);
        }
        Integer iType=entryTypes.get(name);
        if(iType==null){
            throw new CatalogException(CatalogException.INVALID_ENTRY_TYPE);
        }
        return iType;
    }

    public static int getEntryArgCount(int type)
            throws CatalogException{
        try{
            Integer iArgs=(Integer)entryArgs.get(type);
            return iArgs.intValue();
        }catch(ArrayIndexOutOfBoundsException e){
            throw new CatalogException(CatalogException.INVALID_ENTRY_TYPE);
        }
    }

    public int getEntryType(){
        return entryType;
    }

    public String getEntryArg(int argNum){
        try{
            String arg=(String)args.get(argNum);
            return arg;
        }catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public void setEntryArg(int argNum,String newspec)
            throws ArrayIndexOutOfBoundsException{
        args.set(argNum,newspec);
    }
}

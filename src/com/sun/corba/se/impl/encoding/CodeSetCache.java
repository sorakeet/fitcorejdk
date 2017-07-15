/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.WeakHashMap;

class CodeSetCache{
    private static final int BTC_CACHE_MAP=0;
    private static final int CTB_CACHE_MAP=1;
    private ThreadLocal converterCaches=new ThreadLocal(){
        public Object initialValue(){
            return new Map[]{new WeakHashMap(),new WeakHashMap()};
        }
    };

    CharsetDecoder getByteToCharConverter(Object key){
        Map btcMap=((Map[])converterCaches.get())[BTC_CACHE_MAP];
        return (CharsetDecoder)btcMap.get(key);
    }

    CharsetEncoder getCharToByteConverter(Object key){
        Map ctbMap=((Map[])converterCaches.get())[CTB_CACHE_MAP];
        return (CharsetEncoder)ctbMap.get(key);
    }

    CharsetDecoder setConverter(Object key,CharsetDecoder converter){
        Map btcMap=((Map[])converterCaches.get())[BTC_CACHE_MAP];
        btcMap.put(key,converter);
        return converter;
    }

    CharsetEncoder setConverter(Object key,CharsetEncoder converter){
        Map ctbMap=((Map[])converterCaches.get())[CTB_CACHE_MAP];
        ctbMap.put(key,converter);
        return converter;
    }
}

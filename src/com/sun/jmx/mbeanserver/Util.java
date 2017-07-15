/**
 * Copyright (c) 2005, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.*;

public class Util{
    public static ObjectName newObjectName(String string){
        try{
            return new ObjectName(string);
        }catch(MalformedObjectNameException e){
            throw new IllegalArgumentException(e);
        }
    }

    static <K,V> Map<K,V> newSynchronizedMap(){
        return Collections.synchronizedMap(Util.<K,V>newMap());
    }

    static <K,V> Map<K,V> newMap(){
        return new HashMap<K,V>();
    }

    static <K,V> Map<K,V> newSynchronizedIdentityHashMap(){
        Map<K,V> map=newIdentityHashMap();
        return Collections.synchronizedMap(map);
    }

    static <K,V> IdentityHashMap<K,V> newIdentityHashMap(){
        return new IdentityHashMap<K,V>();
    }

    static <K,V> SortedMap<K,V> newSortedMap(){
        return new TreeMap<K,V>();
    }

    static <K,V> SortedMap<K,V> newSortedMap(Comparator<? super K> comp){
        return new TreeMap<K,V>(comp);
    }

    static <K,V> Map<K,V> newInsertionOrderMap(){
        return new LinkedHashMap<K,V>();
    }

    static <E> Set<E> newSet(){
        return new HashSet<E>();
    }

    static <E> Set<E> newSet(Collection<E> c){
        return new HashSet<E>(c);
    }

    static <E> List<E> newList(){
        return new ArrayList<E>();
    }

    static <E> List<E> newList(Collection<E> c){
        return new ArrayList<E>(c);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object x){
        return (T)x;
    }

    public static int hashCode(String[] names,Object[] values){
        int hash=0;
        for(int i=0;i<names.length;i++){
            Object v=values[i];
            int h;
            if(v==null){
                h=0;
            }else if(v instanceof Object[]){
                h=Arrays.deepHashCode((Object[])v);
            }else if(v.getClass().isArray()){
                h=Arrays.deepHashCode(new Object[]{v})-31;
                // hashcode of a list containing just v is
                // v.hashCode() + 31, see List.hashCode()
            }else{
                h=v.hashCode();
            }
            hash+=names[i].toLowerCase().hashCode()^h;
        }
        return hash;
    }

    public static boolean wildmatch(String str,String pat){
        return wildmatch(str,pat,0,str.length(),0,pat.length());
    }

    private static boolean wildmatch(final String str,final String pat,
                                     int stri,final int strend,int pati,final int patend){
        // System.out.println("matching "+pat.substring(pati,patend)+
        //        " against "+str.substring(stri, strend));
        int starstri; // index for backtrack if "*" attempt fails
        int starpati; // index for backtrack if "*" attempt fails, +1
        starstri=starpati=-1;
        /** On each pass through this loop, we either advance pati,
         or we backtrack pati and advance starstri.  Since starstri
         is only ever assigned from pati, the loop must terminate.  */
        while(true){
            if(pati<patend){
                final char patc=pat.charAt(pati);
                switch(patc){
                    case '?':
                        if(stri==strend)
                            break;
                        stri++;
                        pati++;
                        continue;
                    case '*':
                        pati++;
                        starpati=pati;
                        starstri=stri;
                        continue;
                    default:
                        if(stri<strend&&str.charAt(stri)==patc){
                            stri++;
                            pati++;
                            continue;
                        }
                        break;
                }
            }else if(stri==strend)
                return true;
            // Mismatched, can we backtrack to a "*"?
            if(starpati<0||starstri==strend)
                return false;
            // Retry the match one position later in str
            pati=starpati;
            starstri++;
            stri=starstri;
        }
    }
}

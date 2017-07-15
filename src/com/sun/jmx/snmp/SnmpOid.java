/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java imports
//

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class SnmpOid extends SnmpValue{
    final static String name="Object Identifier";
    static final long serialVersionUID=8956237235607885096L;
    private static SnmpOidTable meta=null;
    // VARIABLES
    //----------
    protected long components[]=null;
    protected int componentCount=0;

    // CONSTRUCTORS
    //-------------
    public SnmpOid(){
        components=new long[15];
        componentCount=0;
    }

    public SnmpOid(long[] oidComponents){
        components=oidComponents.clone();
        componentCount=components.length;
    }

    public SnmpOid(long id){
        components=new long[1];
        components[0]=id;
        componentCount=components.length;
    }

    public SnmpOid(long id1,long id2,long id3,long id4){
        components=new long[4];
        components[0]=id1;
        components[1]=id2;
        components[2]=id3;
        components[3]=id4;
        componentCount=components.length;
    }

    public SnmpOid(String s) throws IllegalArgumentException{
        String dotString=s;
        if(s.startsWith(".")==false){
            try{
                dotString=resolveVarName(s);
            }catch(SnmpStatusException e){
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        StringTokenizer st=new StringTokenizer(dotString,".",false);
        componentCount=st.countTokens();
        // Now extract the ids
        //
        if(componentCount==0){
            components=new long[15];
        }else{
            components=new long[componentCount];
            try{
                for(int i=0;i<componentCount;i++){
                    try{
                        components[i]=Long.parseLong(st.nextToken());
                    }catch(NoSuchElementException e){
                    }
                }
            }catch(NumberFormatException e){
                throw new IllegalArgumentException(s);
            }
        }
    }

    public String resolveVarName(String s) throws SnmpStatusException{
        int index=s.indexOf('.');
        // First handle the case where oid is expressed as 1.2.3.4
        //
        try{
            return handleLong(s,index);
        }catch(NumberFormatException e){
        }
        SnmpOidTable table=getSnmpOidTable();
        // if we are here, it means we have something to resolve..
        //
        if(table==null)
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        // Ok assume there is a variable name to resolve ...
        //
        if(index<=0){
            SnmpOidRecord rec=table.resolveVarName(s);
            return rec.getOid();
        }else{
            SnmpOidRecord rec=table.resolveVarName(s.substring(0,index));
            return (rec.getOid()+s.substring(index));
        }
    }

    public static SnmpOidTable getSnmpOidTable(){
        return meta;
    }

    public static void setSnmpOidTable(SnmpOidTable db){
        final SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new SnmpPermission("setSnmpOidTable"));
        }
        meta=db;
    }

    // PRIVATE METHODS
    //----------------
    private String handleLong(String oid,int index) throws NumberFormatException, SnmpStatusException{
        String str;
        if(index>0){
            str=oid.substring(0,index);
        }else{
            str=oid;
        }
        // just parse the element.
        //
        Long.parseLong(str);
        return oid;
    }

    public static SnmpOid toOid(long[] index,int start) throws SnmpStatusException{
        try{
            if(index[start]>Integer.MAX_VALUE){
                throw new SnmpStatusException(SnmpStatusException.noSuchName);
            }
            int idCount=(int)index[start++];
            long[] ids=new long[idCount];
            for(int i=0;i<idCount;i++){
                ids[i]=index[start+i];
            }
            return new SnmpOid(ids);
        }catch(IndexOutOfBoundsException e){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }

    public static int nextOid(long[] index,int start) throws SnmpStatusException{
        try{
            if(index[start]>Integer.MAX_VALUE){
                throw new SnmpStatusException(SnmpStatusException.noSuchName);
            }
            int idCount=(int)index[start++];
            start+=idCount;
            if(start<=index.length){
                return start;
            }else{
                throw new SnmpStatusException(SnmpStatusException.noSuchName);
            }
        }catch(IndexOutOfBoundsException e){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }

    public static void appendToOid(SnmpOid source,SnmpOid dest){
        dest.append(source.getLength());
        dest.append(source);
    }

    // PUBLIC METHODS
    //---------------
    public int getLength(){
        return componentCount;
    }

    public final long[] longValue(boolean duplicate){
        return longValue();
    }

    public long[] longValue(){
        long[] result=new long[componentCount];
        System.arraycopy(components,0,result,0,componentCount);
        return result;
    }

    public final long getOidArc(int pos) throws SnmpStatusException{
        try{
            return components[pos];
        }catch(Exception e){
            throw new SnmpStatusException(SnmpStatusException.noAccess);
        }
    }

    public Long toLong(){
        if(componentCount!=1){
            throw new IllegalArgumentException();
        }
        return new Long(components[0]);
    }

    public Integer toInteger(){
        if((componentCount!=1)||(components[0]>Integer.MAX_VALUE)){
            throw new IllegalArgumentException();
        }
        return new Integer((int)components[0]);
    }

    public Boolean toBoolean(){
        if((componentCount!=1)&&(components[0]!=1)&&(components[0]!=2)){
            throw new IllegalArgumentException();
        }
        return Boolean.valueOf(components[0]==1);
    }

    public Byte[] toByte(){
        Byte[] result=new Byte[componentCount];
        for(int i=0;i<componentCount;i++){
            if(components[0]>255){
                throw new IllegalArgumentException();
            }
            result[i]=new Byte((byte)components[i]);
        }
        return result;
    }

    public SnmpOid toOid(){
        long[] ids=new long[componentCount];
        for(int i=0;i<componentCount;i++){
            ids[i]=components[i];
        }
        return new SnmpOid(ids);
    }

    public String getTypeName(){
        return name;
    }

    final synchronized public SnmpValue duplicate(){
        return (SnmpValue)clone();
    }

    public void insert(int id){
        insert((long)id);
    }

    public void insert(long id){
        enlargeIfNeeded(1);
        for(int i=componentCount-1;i>=0;i--){
            components[i+1]=components[i];
        }
        components[0]=id;
        componentCount++;
    }

    private void enlargeIfNeeded(int n){
        int neededSize=components.length;
        while(componentCount+n>neededSize){
            neededSize=neededSize*2;
        }
        if(neededSize>components.length){
            long[] newComponents=new long[neededSize];
            for(int i=0;i<components.length;i++){
                newComponents[i]=components[i];
            }
            components=newComponents;
        }
    }

    public void append(long id){
        enlargeIfNeeded(1);
        components[componentCount]=id;
        componentCount++;
    }

    public void addToOid(String s) throws SnmpStatusException{
        SnmpOid suffix=new SnmpOid(s);
        this.append(suffix);
    }

    public void append(SnmpOid oid){
        enlargeIfNeeded(oid.componentCount);
        for(int i=0;i<oid.componentCount;i++){
            components[componentCount+i]=oid.components[i];
        }
        componentCount+=oid.componentCount;
    }

    public void addToOid(long[] oid) throws SnmpStatusException{
        SnmpOid suffix=new SnmpOid(oid);
        this.append(suffix);
    }
    // PRIVATE METHODS
    //------------------

    public boolean isValid(){
        return ((componentCount>=2)&&
                ((0<=components[0])&&(components[0]<3))&&
                ((0<=components[1])&&(components[1]<40)));
    }

    public int hashCode(){
        long acc=0;
        for(int i=0;i<componentCount;i++){
            acc=acc*31+components[i];
        }
        return (int)acc;
    }

    public boolean equals(Object o){
        boolean result=false;
        if(o instanceof SnmpOid){
            SnmpOid oid=(SnmpOid)o;
            if(oid.componentCount==componentCount){
                int i=0;
                long[] objoid=oid.components;
                while((i<componentCount)&&(components[i]==objoid[i]))
                    i++;
                result=(i==componentCount);
            }
        }
        return result;
    }

    public Object clone(){
        try{
            SnmpOid obj=(SnmpOid)super.clone();
            obj.components=new long[this.componentCount];
            System.arraycopy(this.components,0,obj.components,0,
                    this.componentCount);
            return obj;
        }catch(CloneNotSupportedException e){
            throw new InternalError();  // should never happen. VM bug.
        }
    }

    public String toString(){
        String result="";
        if(componentCount>=1){
            for(int i=0;i<componentCount-1;i++){
                result=result+components[i]+".";
            }
            result=result+components[componentCount-1];
        }
        return result;
    }

    public int compareTo(SnmpOid other){
        int result=0;
        int i=0;
        int cmplen=Math.min(componentCount,other.componentCount);
        long[] otheroid=other.components;
        for(i=0;i<cmplen;i++){
            if(components[i]!=otheroid[i]){
                break;
            }
        }
        if((i==componentCount)&&(i==other.componentCount)){
            result=0;
        }else if(i==componentCount){
            result=-1;
        }else if(i==other.componentCount){
            result=1;
        }else{
            result=(components[i]<otheroid[i])?-1:1;
        }
        return result;
    }

    public String toOctetString(){
        return new String(tobyte());
    }

    private byte[] tobyte(){
        byte[] result=new byte[componentCount];
        for(int i=0;i<componentCount;i++){
            if(components[0]>255){
                throw new IllegalArgumentException();
            }
            result[i]=(byte)components[i];
        }
        return result;
    }
}

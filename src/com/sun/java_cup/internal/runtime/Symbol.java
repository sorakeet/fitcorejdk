/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java_cup.internal.runtime;

public class Symbol{
    public int sym;
    public int parse_state;
    public int left, right;
    public Object value;
    boolean used_by_parser=false;

    public Symbol(int id,int l,int r,Object o){
        this(id);
        left=l;
        right=r;
        value=o;
    }
    public Symbol(int sym_num){
        this(sym_num,-1);
        left=-1;
        right=-1;
        value=null;
    }
    public Symbol(int sym_num,int state){
        sym=sym_num;
        parse_state=state;
    }
    public Symbol(int id,Object o){
        this(id);
        left=-1;
        right=-1;
        value=o;
    }
    public Symbol(int sym_num,int l,int r){
        sym=sym_num;
        left=l;
        right=r;
        value=null;
    }

    public String toString(){
        return "#"+sym;
    }
}

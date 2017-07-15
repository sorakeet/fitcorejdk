/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

public class CoderResult{
    private static final int CR_UNDERFLOW=0;
    public static final CoderResult UNDERFLOW
            =new CoderResult(CR_UNDERFLOW,0);
    private static final int CR_OVERFLOW=1;
    public static final CoderResult OVERFLOW
            =new CoderResult(CR_OVERFLOW,0);
    private static final int CR_ERROR_MIN=2;
    private static final int CR_MALFORMED=2;
    private static final int CR_UNMAPPABLE=3;
    private static final String[] names
            ={"UNDERFLOW","OVERFLOW","MALFORMED","UNMAPPABLE"};
    private static Cache malformedCache
            =new Cache(){
        public CoderResult create(int len){
            return new CoderResult(CR_MALFORMED,len);
        }
    };
    private static Cache unmappableCache
            =new Cache(){
        public CoderResult create(int len){
            return new CoderResult(CR_UNMAPPABLE,len);
        }
    };
    private final int type;
    private final int length;

    private CoderResult(int type,int length){
        this.type=type;
        this.length=length;
    }

    public static CoderResult malformedForLength(int length){
        return malformedCache.get(length);
    }

    public static CoderResult unmappableForLength(int length){
        return unmappableCache.get(length);
    }

    public String toString(){
        String nm=names[type];
        return isError()?nm+"["+length+"]":nm;
    }

    public boolean isError(){
        return (type>=CR_ERROR_MIN);
    }

    public boolean isUnderflow(){
        return (type==CR_UNDERFLOW);
    }

    public boolean isOverflow(){
        return (type==CR_OVERFLOW);
    }

    public boolean isMalformed(){
        return (type==CR_MALFORMED);
    }

    public boolean isUnmappable(){
        return (type==CR_UNMAPPABLE);
    }

    public int length(){
        if(!isError())
            throw new UnsupportedOperationException();
        return length;
    }

    public void throwException()
            throws CharacterCodingException{
        switch(type){
            case CR_UNDERFLOW:
                throw new BufferUnderflowException();
            case CR_OVERFLOW:
                throw new BufferOverflowException();
            case CR_MALFORMED:
                throw new MalformedInputException(length);
            case CR_UNMAPPABLE:
                throw new UnmappableCharacterException(length);
            default:
                assert false;
        }
    }

    private static abstract class Cache{
        private Map<Integer,WeakReference<CoderResult>> cache=null;

        private synchronized CoderResult get(int len){
            if(len<=0)
                throw new IllegalArgumentException("Non-positive length");
            Integer k=new Integer(len);
            WeakReference<CoderResult> w;
            CoderResult e=null;
            if(cache==null){
                cache=new HashMap<Integer,WeakReference<CoderResult>>();
            }else if((w=cache.get(k))!=null){
                e=w.get();
            }
            if(e==null){
                e=create(len);
                cache.put(k,new WeakReference<CoderResult>(e));
            }
            return e;
        }

        protected abstract CoderResult create(int len);
    }
}

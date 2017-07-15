/**
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import com.sun.beans.finder.PersistenceDelegateFinder;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class Encoder{
    private final PersistenceDelegateFinder finder=new PersistenceDelegateFinder();
    boolean executeStatements=true;
    private Map<Object,Expression> bindings=new IdentityHashMap<>();
    private ExceptionListener exceptionListener;
    private Map<Object,Object> attributes;

    public void setPersistenceDelegate(Class<?> type,PersistenceDelegate delegate){
        this.finder.register(type,delegate);
    }

    public Object remove(Object oldInstance){
        Expression exp=bindings.remove(oldInstance);
        return getValue(exp);
    }

    Object getValue(Expression exp){
        try{
            return (exp==null)?null:exp.getValue();
        }catch(Exception e){
            getExceptionListener().exceptionThrown(e);
            throw new RuntimeException("failed to evaluate: "+exp.toString());
        }
    }

    public ExceptionListener getExceptionListener(){
        return (exceptionListener!=null)?exceptionListener:Statement.defaultExceptionListener;
    }

    public void setExceptionListener(ExceptionListener exceptionListener){
        this.exceptionListener=exceptionListener;
    }

    public void writeStatement(Statement oldStm){
        // System.out.println("writeStatement: " + oldExp);
        Statement newStm=cloneStatement(oldStm);
        if(oldStm.getTarget()!=this&&executeStatements){
            try{
                newStm.execute();
            }catch(Exception e){
                getExceptionListener().exceptionThrown(new Exception("Encoder: discarding statement "
                        +newStm,e));
            }
        }
    }

    private Statement cloneStatement(Statement oldExp){
        Object oldTarget=oldExp.getTarget();
        Object newTarget=writeObject1(oldTarget);
        Object[] oldArgs=oldExp.getArguments();
        Object[] newArgs=new Object[oldArgs.length];
        for(int i=0;i<oldArgs.length;i++){
            newArgs[i]=writeObject1(oldArgs[i]);
        }
        Statement newExp=Statement.class.equals(oldExp.getClass())
                ?new Statement(newTarget,oldExp.getMethodName(),newArgs)
                :new Expression(newTarget,oldExp.getMethodName(),newArgs);
        newExp.loader=oldExp.loader;
        return newExp;
    }

    private Object writeObject1(Object oldInstance){
        Object o=get(oldInstance);
        if(o==null){
            writeObject(oldInstance);
            o=get(oldInstance);
        }
        return o;
    }

    protected void writeObject(Object o){
        if(o==this){
            return;
        }
        PersistenceDelegate info=getPersistenceDelegate(o==null?null:o.getClass());
        info.writeObject(o,this);
    }

    public PersistenceDelegate getPersistenceDelegate(Class<?> type){
        PersistenceDelegate pd=this.finder.find(type);
        if(pd==null){
            pd=MetaData.getPersistenceDelegate(type);
            if(pd!=null){
                this.finder.register(type,pd);
            }
        }
        return pd;
    }

    public Object get(Object oldInstance){
        if(oldInstance==null||oldInstance==this||
                oldInstance.getClass()==String.class){
            return oldInstance;
        }
        Expression exp=bindings.get(oldInstance);
        return getValue(exp);
    }

    public void writeExpression(Expression oldExp){
        // System.out.println("Encoder::writeExpression: " + oldExp);
        Object oldValue=getValue(oldExp);
        if(get(oldValue)!=null){
            return;
        }
        bindings.put(oldValue,(Expression)cloneStatement(oldExp));
        writeObject(oldValue);
    }

    void clear(){
        bindings.clear();
    }

    // Package private method for setting an attributes table for the encoder
    void setAttribute(Object key,Object value){
        if(attributes==null){
            attributes=new HashMap<>();
        }
        attributes.put(key,value);
    }

    Object getAttribute(Object key){
        if(attributes==null){
            return null;
        }
        return attributes.get(key);
    }
}

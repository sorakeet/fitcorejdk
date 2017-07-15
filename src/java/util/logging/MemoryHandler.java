/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

public class MemoryHandler extends Handler{
    private final static int DEFAULT_SIZE=1000;
    int start, count;
    private volatile Level pushLevel;
    private int size;
    private Handler target;
    private LogRecord buffer[];

    public MemoryHandler(){
        sealed=false;
        configure();
        sealed=true;
        LogManager manager=LogManager.getLogManager();
        String handlerName=getClass().getName();
        String targetName=manager.getProperty(handlerName+".target");
        if(targetName==null){
            throw new RuntimeException("The handler "+handlerName
                    +" does not specify a target");
        }
        Class<?> clz;
        try{
            clz=ClassLoader.getSystemClassLoader().loadClass(targetName);
            target=(Handler)clz.newInstance();
        }catch(ClassNotFoundException|InstantiationException|IllegalAccessException e){
            throw new RuntimeException("MemoryHandler can't load handler target \""+targetName+"\"",e);
        }
        init();
    }

    // Private method to configure a MemoryHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private void configure(){
        LogManager manager=LogManager.getLogManager();
        String cname=getClass().getName();
        pushLevel=manager.getLevelProperty(cname+".push",Level.SEVERE);
        size=manager.getIntProperty(cname+".size",DEFAULT_SIZE);
        if(size<=0){
            size=DEFAULT_SIZE;
        }
        setLevel(manager.getLevelProperty(cname+".level",Level.ALL));
        setFilter(manager.getFilterProperty(cname+".filter",null));
        setFormatter(manager.getFormatterProperty(cname+".formatter",new SimpleFormatter()));
    }

    // Initialize.  Size is a count of LogRecords.
    private void init(){
        buffer=new LogRecord[size];
        start=0;
        count=0;
    }

    public MemoryHandler(Handler target,int size,Level pushLevel){
        if(target==null||pushLevel==null){
            throw new NullPointerException();
        }
        if(size<=0){
            throw new IllegalArgumentException();
        }
        sealed=false;
        configure();
        sealed=true;
        this.target=target;
        this.pushLevel=pushLevel;
        this.size=size;
        init();
    }

    @Override
    public synchronized void publish(LogRecord record){
        if(!isLoggable(record)){
            return;
        }
        int ix=(start+count)%buffer.length;
        buffer[ix]=record;
        if(count<buffer.length){
            count++;
        }else{
            start++;
            start%=buffer.length;
        }
        if(record.getLevel().intValue()>=pushLevel.intValue()){
            push();
        }
    }

    public synchronized void push(){
        for(int i=0;i<count;i++){
            int ix=(start+i)%buffer.length;
            LogRecord record=buffer[ix];
            target.publish(record);
        }
        // Empty the buffer.
        start=0;
        count=0;
    }

    @Override
    public void flush(){
        target.flush();
    }

    @Override
    public void close() throws SecurityException{
        target.close();
        setLevel(Level.OFF);
    }

    @Override
    public boolean isLoggable(LogRecord record){
        return super.isLoggable(record);
    }

    public Level getPushLevel(){
        return pushLevel;
    }

    public synchronized void setPushLevel(Level newLevel) throws SecurityException{
        if(newLevel==null){
            throw new NullPointerException();
        }
        checkPermission();
        pushLevel=newLevel;
    }
}

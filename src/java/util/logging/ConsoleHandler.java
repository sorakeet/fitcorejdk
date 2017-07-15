/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

public class ConsoleHandler extends StreamHandler{
    public ConsoleHandler(){
        sealed=false;
        configure();
        setOutputStream(System.err);
        sealed=true;
    }

    // Private method to configure a ConsoleHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private void configure(){
        LogManager manager=LogManager.getLogManager();
        String cname=getClass().getName();
        setLevel(manager.getLevelProperty(cname+".level",Level.INFO));
        setFilter(manager.getFilterProperty(cname+".filter",null));
        setFormatter(manager.getFormatterProperty(cname+".formatter",new SimpleFormatter()));
        try{
            setEncoding(manager.getStringProperty(cname+".encoding",null));
        }catch(Exception ex){
            try{
                setEncoding(null);
            }catch(Exception ex2){
                // doing a setEncoding with null should always work.
                // assert false;
            }
        }
    }

    @Override
    public void publish(LogRecord record){
        super.publish(record);
        flush();
    }

    @Override
    public void close(){
        flush();
    }
}

/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

class Logging implements LoggingMXBean{
    private static LogManager logManager=LogManager.getLogManager();
    private static String EMPTY_STRING="";

    Logging(){
    }

    public List<String> getLoggerNames(){
        Enumeration<String> loggers=logManager.getLoggerNames();
        ArrayList<String> array=new ArrayList<>();
        for(;loggers.hasMoreElements();){
            array.add(loggers.nextElement());
        }
        return array;
    }

    public String getLoggerLevel(String loggerName){
        Logger l=logManager.getLogger(loggerName);
        if(l==null){
            return null;
        }
        Level level=l.getLevel();
        if(level==null){
            return EMPTY_STRING;
        }else{
            return level.getLevelName();
        }
    }

    public void setLoggerLevel(String loggerName,String levelName){
        if(loggerName==null){
            throw new NullPointerException("loggerName is null");
        }
        Logger logger=logManager.getLogger(loggerName);
        if(logger==null){
            throw new IllegalArgumentException("Logger "+loggerName+
                    "does not exist");
        }
        Level level=null;
        if(levelName!=null){
            // parse will throw IAE if logLevel is invalid
            level=Level.findLevel(levelName);
            if(level==null){
                throw new IllegalArgumentException("Unknown level \""+levelName+"\"");
            }
        }
        logger.setLevel(level);
    }

    public String getParentLoggerName(String loggerName){
        Logger l=logManager.getLogger(loggerName);
        if(l==null){
            return null;
        }
        Logger p=l.getParent();
        if(p==null){
            // root logger
            return EMPTY_STRING;
        }else{
            return p.getName();
        }
    }
}
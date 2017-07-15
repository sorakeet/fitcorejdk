/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

import sun.util.logging.LoggingSupport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class SimpleFormatter extends Formatter{
    // format string for printing the log record
    private static final String format=LoggingSupport.getSimpleFormat();
    private final Date dat=new Date();

    public synchronized String format(LogRecord record){
        dat.setTime(record.getMillis());
        String source;
        if(record.getSourceClassName()!=null){
            source=record.getSourceClassName();
            if(record.getSourceMethodName()!=null){
                source+=" "+record.getSourceMethodName();
            }
        }else{
            source=record.getLoggerName();
        }
        String message=formatMessage(record);
        String throwable="";
        if(record.getThrown()!=null){
            StringWriter sw=new StringWriter();
            PrintWriter pw=new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable=sw.toString();
        }
        return String.format(format,
                dat,
                source,
                record.getLoggerName(),
                record.getLevel().getLocalizedLevelName(),
                message,
                throwable);
    }
}

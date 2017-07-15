/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class DateFormatter extends InternationalFormatter{
    public DateFormatter(){
        this(DateFormat.getDateInstance());
    }

    public DateFormatter(DateFormat format){
        super(format);
        setFormat(format);
    }

    public void setFormat(DateFormat format){
        super.setFormat(format);
    }

    Object getAdjustField(int start,Map attributes){
        Iterator attrs=attributes.keySet().iterator();
        while(attrs.hasNext()){
            Object key=attrs.next();
            if((key instanceof DateFormat.Field)&&
                    (key==DateFormat.Field.HOUR1||
                            ((DateFormat.Field)key).getCalendarField()!=-1)){
                return key;
            }
        }
        return null;
    }

    Object adjustValue(Object value,Map attributes,Object key,
                       int direction) throws
            BadLocationException, ParseException{
        if(key!=null){
            int field;
            // HOUR1 has no corresponding calendar field, thus, map
            // it to HOUR0 which will give the correct behavior.
            if(key==DateFormat.Field.HOUR1){
                key=DateFormat.Field.HOUR0;
            }
            field=((DateFormat.Field)key).getCalendarField();
            Calendar calendar=getCalendar();
            if(calendar!=null){
                calendar.setTime((Date)value);
                int fieldValue=calendar.get(field);
                try{
                    calendar.add(field,direction);
                    value=calendar.getTime();
                }catch(Throwable th){
                    value=null;
                }
                return value;
            }
        }
        return null;
    }

    private Calendar getCalendar(){
        Format f=getFormat();
        if(f instanceof DateFormat){
            return ((DateFormat)f).getCalendar();
        }
        return Calendar.getInstance();
    }

    boolean getSupportsIncrement(){
        return true;
    }
}

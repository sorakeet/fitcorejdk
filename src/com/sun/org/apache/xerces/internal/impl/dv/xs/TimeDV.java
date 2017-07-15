/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.impl.dv.xs;

import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

public class TimeDV extends AbstractDateTimeDV{
    public Object getActualValue(String content,ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return parse(content);
        }catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1",new Object[]{content,"time"});
        }
    }

    protected DateTimeData parse(String str) throws SchemaDateTimeException{
        DateTimeData date=new DateTimeData(str,this);
        int len=str.length();
        // time
        // initialize to default values
        date.year=YEAR;
        date.month=MONTH;
        date.day=15;
        getTime(str,0,len,date);
        //validate and normalize
        validateDateTime(date);
        //save unnormalized values
        saveUnnormalized(date);
        if(date.utc!=0&&date.utc!='Z'){
            normalize(date);
        }
        date.position=2;
        return date;
    }

    protected String dateToString(DateTimeData date){
        StringBuffer message=new StringBuffer(16);
        append(message,date.hour,2);
        message.append(':');
        append(message,date.minute,2);
        message.append(':');
        append(message,date.second);
        append(message,(char)date.utc,0);
        return message.toString();
    }

    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData date){
        return datatypeFactory.newXMLGregorianCalendar(null,DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED,date.unNormHour,date.unNormMinute,
                (int)date.unNormSecond,date.unNormSecond!=0?getFractionalSecondsAsBigDecimal(date):null,
                date.hasTimeZone()?(date.timezoneHr*60+date.timezoneMin):DatatypeConstants.FIELD_UNDEFINED);
    }
}

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

public class MonthDV extends AbstractDateTimeDV{
    public Object getActualValue(String content,ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return parse(content);
        }catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1",new Object[]{content,"gMonth"});
        }
    }

    protected DateTimeData parse(String str) throws SchemaDateTimeException{
        DateTimeData date=new DateTimeData(str,this);
        int len=str.length();
        //set constants
        date.year=YEAR;
        date.day=DAY;
        if(str.charAt(0)!='-'||str.charAt(1)!='-'){
            throw new SchemaDateTimeException("Invalid format for gMonth: "+str);
        }
        int stop=4;
        date.month=parseInt(str,2,stop);
        // REVISIT: allow both --MM and --MM-- now.
        // need to remove the following 4 lines to disallow --MM--
        // when the errata is offically in the rec.
        if(str.length()>=stop+2&&
                str.charAt(stop)=='-'&&str.charAt(stop+1)=='-'){
            stop+=2;
        }
        if(stop<len){
            if(!isNextCharUTCSign(str,stop,len)){
                throw new SchemaDateTimeException("Error in month parsing: "+str);
            }else{
                getTimeZone(str,date,stop,len);
            }
        }
        //validate and normalize
        validateDateTime(date);
        //save unnormalized values
        saveUnnormalized(date);
        if(date.utc!=0&&date.utc!='Z'){
            normalize(date);
        }
        date.position=1;
        return date;
    }

    /**
     * Overwrite compare algorithm to optimize month comparison
     *
     * REVISIT: this one is lack of the third parameter: boolean strict, so it
     *          doesn't override the method in the base. But maybe this method
     *          is not correctly implemented, and I did encounter errors when
     *          trying to add the extra parameter. I'm leaving it as is. -SG
     *
     * @param date1
     * @param date2
     * @return less, greater, equal, indeterminate
     */
    protected String dateToString(DateTimeData date){
        StringBuffer message=new StringBuffer(5);
        message.append('-');
        message.append('-');
        append(message,date.month,2);
        append(message,(char)date.utc,0);
        return message.toString();
    }

    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData date){
        return datatypeFactory.newXMLGregorianCalendar(DatatypeConstants.FIELD_UNDEFINED,date.unNormMonth,
                DatatypeConstants.FIELD_UNDEFINED,DatatypeConstants.FIELD_UNDEFINED,DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED,DatatypeConstants.FIELD_UNDEFINED,
                date.hasTimeZone()?date.timezoneHr*60+date.timezoneMin:DatatypeConstants.FIELD_UNDEFINED);
    }
}

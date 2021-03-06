/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2005 The Apache Software Foundation.
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
 * Copyright 1999-2005 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl;
import com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

public abstract class AbstractDateTimeDV extends TypeValidator{
    //define shared variables for date/time
    //define constants to be used in assigning default values for
    //all date/time excluding duration
    protected final static int YEAR=2000;
    protected final static int MONTH=01;
    protected final static int DAY=01;
    protected static final DatatypeFactory datatypeFactory=new DatatypeFactoryImpl();
    //debugging
    private static final boolean DEBUG=false;

    @Override
    public short getAllowedFacets(){
        return (XSSimpleTypeDecl.FACET_PATTERN|XSSimpleTypeDecl.FACET_WHITESPACE|XSSimpleTypeDecl.FACET_ENUMERATION|XSSimpleTypeDecl.FACET_MAXINCLUSIVE|XSSimpleTypeDecl.FACET_MININCLUSIVE|XSSimpleTypeDecl.FACET_MAXEXCLUSIVE|XSSimpleTypeDecl.FACET_MINEXCLUSIVE);
    }//getAllowedFacets()

    // distinguishes between identity and equality for date/time values
    // ie: two values representing the same "moment in time" but with different
    // remembered timezones are now equal but not identical.
    @Override
    public boolean isIdentical(Object value1,Object value2){
        if(!(value1 instanceof DateTimeData)||!(value2 instanceof DateTimeData)){
            return false;
        }
        DateTimeData v1=(DateTimeData)value1;
        DateTimeData v2=(DateTimeData)value2;
        // original timezones must be the same in addition to date/time values
        // being 'equal'
        if((v1.timezoneHr==v2.timezoneHr)&&(v1.timezoneMin==v2.timezoneMin)){
            return v1.equals(v2);
        }
        return false;
    }//isIdentical()

    // the parameters are in compiled form (from getActualValue)
    @Override
    public int compare(Object value1,Object value2){
        return compareDates(((DateTimeData)value1),
                ((DateTimeData)value2),true);
    }//compare()

    protected short compareDates(DateTimeData date1,DateTimeData date2,boolean strict){
        if(date1.utc==date2.utc){
            return compareOrder(date1,date2);
        }
        short c1, c2;
        DateTimeData tempDate=new DateTimeData(null,this);
        if(date1.utc=='Z'){
            //compare date1<=date1<=(date2 with time zone -14)
            //
            cloneDate(date2,tempDate); //clones date1 value to global temporary storage: fTempDate
            tempDate.timezoneHr=14;
            tempDate.timezoneMin=0;
            tempDate.utc='+';
            normalize(tempDate);
            c1=compareOrder(date1,tempDate);
            if(c1==LESS_THAN){
                return c1;
            }
            //compare date1>=(date2 with time zone +14)
            //
            cloneDate(date2,tempDate); //clones date1 value to global temporary storage: tempDate
            tempDate.timezoneHr=-14;
            tempDate.timezoneMin=0;
            tempDate.utc='-';
            normalize(tempDate);
            c2=compareOrder(date1,tempDate);
            if(c2==GREATER_THAN){
                return c2;
            }
            return INDETERMINATE;
        }else if(date2.utc=='Z'){
            //compare (date1 with time zone -14)<=date2
            //
            cloneDate(date1,tempDate); //clones date1 value to global temporary storage: tempDate
            tempDate.timezoneHr=-14;
            tempDate.timezoneMin=0;
            tempDate.utc='-';
            if(DEBUG){
                System.out.println("tempDate="+dateToString(tempDate));
            }
            normalize(tempDate);
            c1=compareOrder(tempDate,date2);
            if(DEBUG){
                System.out.println("date="+dateToString(date2));
                System.out.println("tempDate="+dateToString(tempDate));
            }
            if(c1==LESS_THAN){
                return c1;
            }
            //compare (date1 with time zone +14)<=date2
            //
            cloneDate(date1,tempDate); //clones date1 value to global temporary storage: tempDate
            tempDate.timezoneHr=14;
            tempDate.timezoneMin=0;
            tempDate.utc='+';
            normalize(tempDate);
            c2=compareOrder(tempDate,date2);
            if(DEBUG){
                System.out.println("tempDate="+dateToString(tempDate));
            }
            if(c2==GREATER_THAN){
                return c2;
            }
            return INDETERMINATE;
        }
        return INDETERMINATE;
    }

    protected short compareOrder(DateTimeData date1,DateTimeData date2){
        if(date1.position<1){
            if(date1.year<date2.year){
                return -1;
            }
            if(date1.year>date2.year){
                return 1;
            }
        }
        if(date1.position<2){
            if(date1.month<date2.month){
                return -1;
            }
            if(date1.month>date2.month){
                return 1;
            }
        }
        if(date1.day<date2.day){
            return -1;
        }
        if(date1.day>date2.day){
            return 1;
        }
        if(date1.hour<date2.hour){
            return -1;
        }
        if(date1.hour>date2.hour){
            return 1;
        }
        if(date1.minute<date2.minute){
            return -1;
        }
        if(date1.minute>date2.minute){
            return 1;
        }
        if(date1.second<date2.second){
            return -1;
        }
        if(date1.second>date2.second){
            return 1;
        }
        if(date1.utc<date2.utc){
            return -1;
        }
        if(date1.utc>date2.utc){
            return 1;
        }
        return 0;
    }

    protected void normalize(DateTimeData date){
        // REVISIT: we have common code in addDuration() for durations
        //          should consider reorganizing it.
        //
        //add minutes (from time zone)
        int negate=-1;
        if(DEBUG){
            System.out.println("==>date.minute"+date.minute);
            System.out.println("==>date.timezoneMin"+date.timezoneMin);
        }
        int temp=date.minute+negate*date.timezoneMin;
        int carry=fQuotient(temp,60);
        date.minute=mod(temp,60,carry);
        if(DEBUG){
            System.out.println("==>carry: "+carry);
        }
        //add hours
        temp=date.hour+negate*date.timezoneHr+carry;
        carry=fQuotient(temp,24);
        date.hour=mod(temp,24,carry);
        if(DEBUG){
            System.out.println("==>date.hour"+date.hour);
            System.out.println("==>carry: "+carry);
        }
        date.day=date.day+carry;
        while(true){
            temp=maxDayInMonthFor(date.year,date.month);
            if(date.day<1){
                date.day=date.day+maxDayInMonthFor(date.year,date.month-1);
                carry=-1;
            }else if(date.day>temp){
                date.day=date.day-temp;
                carry=1;
            }else{
                break;
            }
            temp=date.month+carry;
            date.month=modulo(temp,1,13);
            date.year=date.year+fQuotient(temp,1,13);
            if(date.year==0&&!Constants.SCHEMA_1_1_SUPPORT){
                date.year=(date.timezoneHr<0||date.timezoneMin<0)?1:-1;
            }
        }
        date.utc='Z';
    }

    protected int maxDayInMonthFor(int year,int month){
        //validate days
        if(month==4||month==6||month==9||month==11){
            return 30;
        }else if(month==2){
            if(isLeapYear(year)){
                return 29;
            }else{
                return 28;
            }
        }else{
            return 31;
        }
    }

    private boolean isLeapYear(int year){
        //REVISIT: should we take care about Julian calendar?
        return ((year%4==0)&&((year%100!=0)||(year%400==0)));
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected int mod(int a,int b,int quotient){
        //modulo(a, b) = a - fQuotient(a,b)*b
        return (a-quotient*b);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected int fQuotient(int a,int b){
        //fQuotient(a, b) = the greatest integer less than or equal to a/b
        return (int)Math.floor((float)a/b);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected int modulo(int temp,int low,int high){
        //modulo(a - low, high - low) + low
        int a=temp-low;
        int b=high-low;
        return (mod(a,b,fQuotient(a,b))+low);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected int fQuotient(int temp,int low,int high){
        //fQuotient(a - low, high - low)
        return fQuotient(temp-low,high-low);
    }

    protected String dateToString(DateTimeData date){
        StringBuffer message=new StringBuffer(25);
        append(message,date.year,4);
        message.append('-');
        append(message,date.month,2);
        message.append('-');
        append(message,date.day,2);
        message.append('T');
        append(message,date.hour,2);
        message.append(':');
        append(message,date.minute,2);
        message.append(':');
        append(message,date.second);
        append(message,(char)date.utc,0);
        return message.toString();
    }

    protected final void append(StringBuffer message,int value,int nch){
        if(value==Integer.MIN_VALUE){
            message.append(value);
            return;
        }
        if(value<0){
            message.append('-');
            value=-value;
        }
        if(nch==4){
            if(value<10){
                message.append("000");
            }else if(value<100){
                message.append("00");
            }else if(value<1000){
                message.append('0');
            }
            message.append(value);
        }else if(nch==2){
            if(value<10){
                message.append('0');
            }
            message.append(value);
        }else{
            if(value!=0){
                message.append((char)value);
            }
        }
    }

    protected final void append(StringBuffer message,double value){
        if(value<0){
            message.append('-');
            value=-value;
        }
        if(value<10){
            message.append('0');
        }
        append2(message,value);
    }

    protected final void append2(StringBuffer message,double value){
        final int intValue=(int)value;
        if(value==intValue){
            message.append(intValue);
        }else{
            append3(message,value);
        }
    }

    private void append3(StringBuffer message,double value){
        String d=String.valueOf(value);
        int eIndex=d.indexOf('E');
        if(eIndex==-1){
            message.append(d);
            return;
        }
        int exp;
        if(value<1){
            // Need to convert from scientific notation of the form
            // n.nnn...E-N (N >= 4) to a normal decimal value.
            try{
                exp=parseInt(d,eIndex+2,d.length());
            } // This should never happen.
            // It's only possible if String.valueOf(double) is broken.
            catch(Exception e){
                message.append(d);
                return;
            }
            message.append("0.");
            for(int i=1;i<exp;++i){
                message.append('0');
            }
            // Remove trailing zeros.
            int end=eIndex-1;
            while(end>0){
                char c=d.charAt(end);
                if(c!='0'){
                    break;
                }
                --end;
            }
            // Now append the digits to the end. Skip over the decimal point.
            for(int i=0;i<=end;++i){
                char c=d.charAt(i);
                if(c!='.'){
                    message.append(c);
                }
            }
        }else{
            // Need to convert from scientific notation of the form
            // n.nnn...EN (N >= 7) to a normal decimal value.
            try{
                exp=parseInt(d,eIndex+1,d.length());
            } // This should never happen.
            // It's only possible if String.valueOf(double) is broken.
            catch(Exception e){
                message.append(d);
                return;
            }
            final int integerEnd=exp+2;
            for(int i=0;i<eIndex;++i){
                char c=d.charAt(i);
                if(c!='.'){
                    if(i==integerEnd){
                        message.append('.');
                    }
                    message.append(c);
                }
            }
            // Append trailing zeroes if necessary.
            for(int i=integerEnd-eIndex;i>0;--i){
                message.append('0');
            }
        }
    }

    protected int parseInt(String buffer,int start,int end)
            throws NumberFormatException{
        //REVISIT: more testing on this parsing needs to be done.
        int radix=10;
        int result=0;
        int digit=0;
        int limit=-Integer.MAX_VALUE;
        int multmin=limit/radix;
        int i=start;
        do{
            digit=getDigit(buffer.charAt(i));
            if(digit<0){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
            if(result<multmin){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
            result*=radix;
            if(result<limit+digit){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
            result-=digit;
        }while(++i<end);
        return -result;
    }

    //
    //Private help functions
    //
    private void cloneDate(DateTimeData finalValue,DateTimeData tempDate){
        tempDate.year=finalValue.year;
        tempDate.month=finalValue.month;
        tempDate.day=finalValue.day;
        tempDate.hour=finalValue.hour;
        tempDate.minute=finalValue.minute;
        tempDate.second=finalValue.second;
        tempDate.utc=finalValue.utc;
        tempDate.timezoneHr=finalValue.timezoneHr;
        tempDate.timezoneMin=finalValue.timezoneMin;
    }

    protected void getTime(String buffer,int start,int end,DateTimeData data) throws RuntimeException{
        int stop=start+2;
        //get hours (hh)
        data.hour=parseInt(buffer,start,stop);
        //get minutes (mm)
        if(buffer.charAt(stop++)!=':'){
            throw new RuntimeException("Error in parsing time zone");
        }
        start=stop;
        stop=stop+2;
        data.minute=parseInt(buffer,start,stop);
        //get seconds (ss)
        if(buffer.charAt(stop++)!=':'){
            throw new RuntimeException("Error in parsing time zone");
        }
        //find UTC sign if any
        int sign=findUTCSign(buffer,start,end);
        //get seconds (ms)
        start=stop;
        stop=sign<0?end:sign;
        data.second=parseSecond(buffer,start,stop);
        //parse UTC time zone (hh:mm)
        if(sign>0){
            getTimeZone(buffer,data,sign,end);
        }
    }

    protected void getTimeZone(String buffer,DateTimeData data,int sign,int end) throws RuntimeException{
        data.utc=buffer.charAt(sign);
        if(buffer.charAt(sign)=='Z'){
            if(end>(++sign)){
                throw new RuntimeException("Error in parsing time zone");
            }
            return;
        }
        if(sign<=(end-6)){
            int negate=buffer.charAt(sign)=='-'?-1:1;
            //parse hr
            int stop=++sign+2;
            data.timezoneHr=negate*parseInt(buffer,sign,stop);
            if(buffer.charAt(stop++)!=':'){
                throw new RuntimeException("Error in parsing time zone");
            }
            //parse min
            data.timezoneMin=negate*parseInt(buffer,stop,stop+2);
            if(stop+2!=end){
                throw new RuntimeException("Error in parsing time zone");
            }
            if(data.timezoneHr!=0||data.timezoneMin!=0){
                data.normalized=false;
            }
        }else{
            throw new RuntimeException("Error in parsing time zone");
        }
        if(DEBUG){
            System.out.println("time[hh]="+data.timezoneHr+" time[mm]="+data.timezoneMin);
        }
    }

    protected int findUTCSign(String buffer,int start,int end){
        int c;
        for(int i=start;i<end;i++){
            c=buffer.charAt(i);
            if(c=='Z'||c=='+'||c=='-'){
                return i;
            }
        }
        return -1;
    }

    protected double parseSecond(String buffer,int start,int end)
            throws NumberFormatException{
        int dot=-1;
        for(int i=start;i<end;i++){
            char ch=buffer.charAt(i);
            if(ch=='.'){
                dot=i;
            }else if(ch>'9'||ch<'0'){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
        }
        if(dot==-1){
            if(start+2!=end){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
        }else if(start+2!=dot||dot+1==end){
            throw new NumberFormatException("'"+buffer+"' has wrong format");
        }
        return Double.parseDouble(buffer.substring(start,end));
    }

    protected int getDate(String buffer,int start,int end,DateTimeData date) throws RuntimeException{
        start=getYearMonth(buffer,start,end,date);
        if(buffer.charAt(start++)!='-'){
            throw new RuntimeException("CCYY-MM must be followed by '-' sign");
        }
        int stop=start+2;
        date.day=parseInt(buffer,start,stop);
        return stop;
    }

    protected int getYearMonth(String buffer,int start,int end,DateTimeData date) throws RuntimeException{
        if(buffer.charAt(0)=='-'){
            // REVISIT: date starts with preceding '-' sign
            //          do we have to do anything with it?
            //
            start++;
        }
        int i=indexOf(buffer,start,end,'-');
        if(i==-1){
            throw new RuntimeException("Year separator is missing or misplaced");
        }
        int length=i-start;
        if(length<4){
            throw new RuntimeException("Year must have 'CCYY' format");
        }else if(length>4&&buffer.charAt(start)=='0'){
            throw new RuntimeException("Leading zeros are required if the year value would otherwise have fewer than four digits; otherwise they are forbidden");
        }
        date.year=parseIntYear(buffer,i);
        if(buffer.charAt(i)!='-'){
            throw new RuntimeException("CCYY must be followed by '-' sign");
        }
        start=++i;
        i=start+2;
        date.month=parseInt(buffer,start,i);
        return i; //fStart points right after the MONTH
    }

    protected int indexOf(String buffer,int start,int end,char ch){
        for(int i=start;i<end;i++){
            if(buffer.charAt(i)==ch){
                return i;
            }
        }
        return -1;
    }

    // parse Year differently to support negative value.
    protected int parseIntYear(String buffer,int end){
        int radix=10;
        int result=0;
        boolean negative=false;
        int i=0;
        int limit;
        int multmin;
        int digit=0;
        if(buffer.charAt(0)=='-'){
            negative=true;
            limit=Integer.MIN_VALUE;
            i++;
        }else{
            limit=-Integer.MAX_VALUE;
        }
        multmin=limit/radix;
        while(i<end){
            digit=getDigit(buffer.charAt(i++));
            if(digit<0){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
            if(result<multmin){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
            result*=radix;
            if(result<limit+digit){
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
            result-=digit;
        }
        if(negative){
            if(i>1){
                return result;
            }else{
                throw new NumberFormatException("'"+buffer+"' has wrong format");
            }
        }
        return -result;
    }

    protected void parseTimeZone(String buffer,int start,int end,DateTimeData date) throws RuntimeException{
        //fStart points right after the date
        if(start<end){
            if(!isNextCharUTCSign(buffer,start,end)){
                throw new RuntimeException("Error in month parsing");
            }else{
                getTimeZone(buffer,date,start,end);
            }
        }
    }

    protected final boolean isNextCharUTCSign(String buffer,int start,int end){
        if(start<end){
            char c=buffer.charAt(start);
            return (c=='Z'||c=='+'||c=='-');
        }
        return false;
    }

    protected void validateDateTime(DateTimeData data){
        //REVISIT: should we throw an exception for not valid dates
        //          or reporting an error message should be sufficient?
        /**
         * XML Schema 1.1 - RQ-123: Allow year 0000 in date related types.
         */
        if(!Constants.SCHEMA_1_1_SUPPORT&&data.year==0){
            throw new RuntimeException("The year \"0000\" is an illegal year value");
        }
        if(data.month<1||data.month>12){
            throw new RuntimeException("The month must have values 1 to 12");
        }
        //validate days
        if(data.day>maxDayInMonthFor(data.year,data.month)||data.day<1){
            throw new RuntimeException("The day must have values 1 to 31");
        }
        //validate hours
        if(data.hour>23||data.hour<0){
            if(data.hour==24&&data.minute==0&&data.second==0){
                data.hour=0;
                if(++data.day>maxDayInMonthFor(data.year,data.month)){
                    data.day=1;
                    if(++data.month>12){
                        data.month=1;
                        if(Constants.SCHEMA_1_1_SUPPORT){
                            ++data.year;
                        }else if(++data.year==0){
                            data.year=1;
                        }
                    }
                }
            }else{
                throw new RuntimeException("Hour must have values 0-23, unless 24:00:00");
            }
        }
        //validate
        if(data.minute>59||data.minute<0){
            throw new RuntimeException("Minute must have values 0-59");
        }
        //validate
        if(data.second>=60||data.second<0){
            throw new RuntimeException("Second must have values 0-59");
        }
        //validate
        if(data.timezoneHr>14||data.timezoneHr<-14){
            throw new RuntimeException("Time zone should have range -14:00 to +14:00");
        }else{
            if((data.timezoneHr==14||data.timezoneHr==-14)&&data.timezoneMin!=0){
                throw new RuntimeException("Time zone should have range -14:00 to +14:00");
            }else if(data.timezoneMin>59||data.timezoneMin<-59){
                throw new RuntimeException("Minute must have values 0-59");
            }
        }
    }

    protected void saveUnnormalized(DateTimeData date){
        date.unNormYear=date.year;
        date.unNormMonth=date.month;
        date.unNormDay=date.day;
        date.unNormHour=date.hour;
        date.unNormMinute=date.minute;
        date.unNormSecond=date.second;
    }

    protected void resetDateObj(DateTimeData data){
        data.year=0;
        data.month=0;
        data.day=0;
        data.hour=0;
        data.minute=0;
        data.second=0;
        data.utc=0;
        data.timezoneHr=0;
        data.timezoneMin=0;
    }

    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData data){
        return null;
    }

    protected Duration getDuration(DateTimeData data){
        return null;
    }

    protected final BigDecimal getFractionalSecondsAsBigDecimal(DateTimeData data){
        final StringBuffer buf=new StringBuffer();
        append3(buf,data.unNormSecond);
        String value=buf.toString();
        final int index=value.indexOf('.');
        if(index==-1){
            return null;
        }
        value=value.substring(index);
        final BigDecimal _val=new BigDecimal(value);
        if(_val.compareTo(BigDecimal.valueOf(0))==0){
            return null;
        }
        return _val;
    }

    static final class DateTimeData implements XSDateTime{
        // a pointer to the type that was used go generate this data
        // note that this is not the actual simple type, but one of the
        // statically created XXXDV objects, so this won't cause any GC problem.
        final AbstractDateTimeDV type;
        int year, month, day, hour, minute, utc;
        double second;
        int timezoneHr, timezoneMin;
        boolean normalized=true;
        int unNormYear;
        int unNormMonth;
        int unNormDay;
        int unNormHour;
        int unNormMinute;
        double unNormSecond;
        // used for comparisons - to decide the 'interesting' portions of
        // a date/time based data type.
        int position;
        private String originalValue;
        private volatile String canonical;

        public DateTimeData(String originalValue,AbstractDateTimeDV type){
            this.originalValue=originalValue;
            this.type=type;
        }

        public DateTimeData(int year,int month,int day,int hour,int minute,
                            double second,int utc,String originalValue,boolean normalized,AbstractDateTimeDV type){
            this.year=year;
            this.month=month;
            this.day=day;
            this.hour=hour;
            this.minute=minute;
            this.second=second;
            this.utc=utc;
            this.type=type;
            this.originalValue=originalValue;
        }

        // If two DateTimeData are equals - then they should have the same
        // hashcode. This means we need to convert the date to UTC before
        // we return its hashcode.
        // The DateTimeData is unfortunately mutable - so we cannot
        // cache the result of the conversion...
        //
        @Override
        public int hashCode(){
            final DateTimeData tempDate=new DateTimeData(null,type);
            type.cloneDate(this,tempDate);
            type.normalize(tempDate);
            return type.dateToString(tempDate).hashCode();
        }

        @Override
        public boolean equals(Object obj){
            if(!(obj instanceof DateTimeData)){
                return false;
            }
            return type.compareDates(this,(DateTimeData)obj,true)==0;
        }

        @Override
        public Object clone(){
            DateTimeData dt=new DateTimeData(this.year,this.month,this.day,this.hour,
                    this.minute,this.second,this.utc,this.originalValue,this.normalized,this.type);
            dt.canonical=this.canonical;
            dt.position=position;
            dt.timezoneHr=this.timezoneHr;
            dt.timezoneMin=this.timezoneMin;
            dt.unNormYear=this.unNormYear;
            dt.unNormMonth=this.unNormMonth;
            dt.unNormDay=this.unNormDay;
            dt.unNormHour=this.unNormHour;
            dt.unNormMinute=this.unNormMinute;
            dt.unNormSecond=this.unNormSecond;
            return dt;
        }

        @Override
        public String toString(){
            if(canonical==null){
                canonical=type.dateToString(this);
            }
            return canonical;
        }

        @Override
        public int getYears(){
            if(type instanceof DurationDV){
                return 0;
            }
            return normalized?year:unNormYear;
        }

        @Override
        public int getMonths(){
            if(type instanceof DurationDV){
                return year*12+month;
            }
            return normalized?month:unNormMonth;
        }

        @Override
        public int getDays(){
            if(type instanceof DurationDV){
                return 0;
            }
            return normalized?day:unNormDay;
        }

        @Override
        public int getHours(){
            if(type instanceof DurationDV){
                return 0;
            }
            return normalized?hour:unNormHour;
        }

        @Override
        public int getMinutes(){
            if(type instanceof DurationDV){
                return 0;
            }
            return normalized?minute:unNormMinute;
        }

        @Override
        public double getSeconds(){
            if(type instanceof DurationDV){
                return day*24*60*60+hour*60*60+minute*60+second;
            }
            return normalized?second:unNormSecond;
        }

        @Override
        public boolean hasTimeZone(){
            return utc!=0;
        }

        @Override
        public int getTimeZoneHours(){
            return timezoneHr;
        }

        @Override
        public int getTimeZoneMinutes(){
            return timezoneMin;
        }

        @Override
        public String getLexicalValue(){
            return originalValue;
        }

        @Override
        public XSDateTime normalize(){
            if(!normalized){
                DateTimeData dt=(DateTimeData)this.clone();
                dt.normalized=true;
                return dt;
            }
            return this;
        }

        @Override
        public boolean isNormalized(){
            return normalized;
        }

        @Override
        public XMLGregorianCalendar getXMLGregorianCalendar(){
            return type.getXMLGregorianCalendar(this);
        }

        @Override
        public Duration getDuration(){
            return type.getDuration(this);
        }
    }
}

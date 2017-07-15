/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: ExsltDatetime.java,v 1.2.4.1 2005/09/10 18:50:49 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: ExsltDatetime.java,v 1.2.4.1 2005/09/10 18:50:49 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xpath.internal.objects.XBoolean;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ExsltDatetime{
    // Datetime formats (era and zone handled separately).
    static final String dt="yyyy-MM-dd'T'HH:mm:ss";
    static final String d="yyyy-MM-dd";
    static final String gym="yyyy-MM";
    static final String gy="yyyy";
    static final String gmd="--MM-dd";
    static final String gm="--MM--";
    static final String gd="---dd";
    static final String t="HH:mm:ss";
    static final String EMPTY_STR="";

    public static String date(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String leader=edz[0];
        String datetime=edz[1];
        String zone=edz[2];
        if(datetime==null||zone==null)
            return EMPTY_STR;
        String[] formatsIn={dt,d};
        String formatOut=d;
        Date date=testFormats(datetime,formatsIn);
        if(date==null) return EMPTY_STR;
        SimpleDateFormat dateFormat=new SimpleDateFormat(formatOut);
        dateFormat.setLenient(false);
        String dateOut=dateFormat.format(date);
        if(dateOut.length()==0)
            return EMPTY_STR;
        else
            return (leader+dateOut+zone);
    }

    private static String[] getEraDatetimeZone(String in){
        String leader="";
        String datetime=in;
        String zone="";
        if(in.charAt(0)=='-'&&!in.startsWith("--")){
            leader="-"; //  '+' is implicit , not allowed
            datetime=in.substring(1);
        }
        int z=getZoneStart(datetime);
        if(z>0){
            zone=datetime.substring(z);
            datetime=datetime.substring(0,z);
        }else if(z==-2)
            zone=null;
        //System.out.println("'" + leader + "' " + datetime + " " + zone);
        return new String[]{leader,datetime,zone};
    }

    private static int getZoneStart(String datetime){
        if(datetime.indexOf("Z")==datetime.length()-1)
            return datetime.length()-1;
        else if(datetime.length()>=6
                &&datetime.charAt(datetime.length()-3)==':'
                &&(datetime.charAt(datetime.length()-6)=='+'
                ||datetime.charAt(datetime.length()-6)=='-')){
            try{
                SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
                dateFormat.setLenient(false);
                Date d=dateFormat.parse(datetime.substring(datetime.length()-5));
                return datetime.length()-6;
            }catch(ParseException pe){
                System.out.println("ParseException "+pe.getErrorOffset());
                return -2; // Invalid.
            }
        }
        return -1; // No zone information.
    }

    private static Date testFormats(String in,String[] formats)
            throws ParseException{
        for(int i=0;i<formats.length;i++){
            try{
                SimpleDateFormat dateFormat=new SimpleDateFormat(formats[i]);
                dateFormat.setLenient(false);
                return dateFormat.parse(in);
            }catch(ParseException pe){
            }
        }
        return null;
    }

    public static String date(){
        String datetime=dateTime().toString();
        String date=datetime.substring(0,datetime.indexOf("T"));
        String zone=datetime.substring(getZoneStart(datetime));
        return (date+zone);
    }

    public static String dateTime(){
        Calendar cal=Calendar.getInstance();
        Date datetime=cal.getTime();
        // Format for date and time.
        SimpleDateFormat dateFormat=new SimpleDateFormat(dt);
        StringBuffer buff=new StringBuffer(dateFormat.format(datetime));
        // Must also include offset from UTF.
        // Get the offset (in milliseconds).
        int offset=cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET);
        // If there is no offset, we have "Coordinated
        // Universal Time."
        if(offset==0)
            buff.append("Z");
        else{
            // Convert milliseconds to hours and minutes
            int hrs=offset/(60*60*1000);
            // In a few cases, the time zone may be +/-hh:30.
            int min=offset%(60*60*1000);
            char posneg=hrs<0?'-':'+';
            buff.append(posneg).append(formatDigits(hrs)).append(':').append(formatDigits(min));
        }
        return buff.toString();
    }

    private static String formatDigits(int q){
        String dd=String.valueOf(Math.abs(q));
        return dd.length()==1?'0'+dd:dd;
    }

    public static String time(String timeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(timeIn);
        String time=edz[1];
        String zone=edz[2];
        if(time==null||zone==null)
            return EMPTY_STR;
        String[] formatsIn={dt,d,t};
        String formatOut=t;
        Date date=testFormats(time,formatsIn);
        if(date==null) return EMPTY_STR;
        SimpleDateFormat dateFormat=new SimpleDateFormat(formatOut);
        String out=dateFormat.format(date);
        return (out+zone);
    }

    public static String time(){
        String datetime=dateTime().toString();
        String time=datetime.substring(datetime.indexOf("T")+1);
        // The datetime() function returns the zone on the datetime string.  If we
        // append it, we get the zone substring duplicated.
        // Fix for JIRA 2013
        // String zone = datetime.substring(getZoneStart(datetime));
        // return (time + zone);
        return (time);
    }

    public static double year(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        boolean ad=edz[0].length()==0; // AD (Common Era -- empty leader)
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,d,gym,gy};
        double yr=getNumber(datetime,formats,Calendar.YEAR);
        if(ad||yr==Double.NaN)
            return yr;
        else
            return -yr;
    }

    private static double getNumber(String in,String[] formats,int calField)
            throws ParseException{
        Calendar cal=Calendar.getInstance();
        cal.setLenient(false);
        // Try the allowed formats, from longest to shortest.
        Date date=testFormats(in,formats);
        if(date==null) return Double.NaN;
        cal.setTime(date);
        return cal.get(calField);
    }

    public static double year(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static double monthInYear(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,d,gym,gm,gmd};
        return getNumber(datetime,formats,Calendar.MONTH)+1;
    }

    public static double monthInYear(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.MONTH)+1;
    }

    public static double weekInYear(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,d};
        return getNumber(datetime,formats,Calendar.WEEK_OF_YEAR);
    }

    public static double weekInYear(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static double dayInYear(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,d};
        return getNumber(datetime,formats,Calendar.DAY_OF_YEAR);
    }

    public static double dayInYear(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static double dayInMonth(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        String[] formats={dt,d,gmd,gd};
        double day=getNumber(datetime,formats,Calendar.DAY_OF_MONTH);
        return day;
    }

    public static double dayInMonth(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static double dayOfWeekInMonth(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,d};
        return getNumber(datetime,formats,Calendar.DAY_OF_WEEK_IN_MONTH);
    }

    public static double dayOfWeekInMonth(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    }

    public static double dayInWeek(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,d};
        return getNumber(datetime,formats,Calendar.DAY_OF_WEEK);
    }

    public static double dayInWeek(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static double hourInDay(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,t};
        return getNumber(datetime,formats,Calendar.HOUR_OF_DAY);
    }

    public static double hourInDay(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static double minuteInHour(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,t};
        return getNumber(datetime,formats,Calendar.MINUTE);
    }

    public static double minuteInHour(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.MINUTE);
    }

    public static double secondInMinute(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return Double.NaN;
        String[] formats={dt,t};
        return getNumber(datetime,formats,Calendar.SECOND);
    }

    public static double secondInMinute(){
        Calendar cal=Calendar.getInstance();
        return cal.get(Calendar.SECOND);
    }

    public static XObject leapYear(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return new XNumber(Double.NaN);
        String[] formats={dt,d,gym,gy};
        double dbl=getNumber(datetime,formats,Calendar.YEAR);
        if(dbl==Double.NaN)
            return new XNumber(Double.NaN);
        int yr=(int)dbl;
        return new XBoolean(yr%400==0||(yr%100!=0&&yr%4==0));
    }

    public static boolean leapYear(){
        Calendar cal=Calendar.getInstance();
        int yr=(int)cal.get(Calendar.YEAR);
        return (yr%400==0||(yr%100!=0&&yr%4==0));
    }

    public static String monthName(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return EMPTY_STR;
        String[] formatsIn={dt,d,gym,gm};
        String formatOut="MMMM";
        return getNameOrAbbrev(datetimeIn,formatsIn,formatOut);
    }

    private static String getNameOrAbbrev(String in,
                                          String[] formatsIn,
                                          String formatOut)
            throws ParseException{
        for(int i=0;i<formatsIn.length;i++) // from longest to shortest.
        {
            try{
                SimpleDateFormat dateFormat=new SimpleDateFormat(formatsIn[i],Locale.ENGLISH);
                dateFormat.setLenient(false);
                Date dt=dateFormat.parse(in);
                dateFormat.applyPattern(formatOut);
                return dateFormat.format(dt);
            }catch(ParseException pe){
            }
        }
        return "";
    }

    public static String monthName(){
        Calendar cal=Calendar.getInstance();
        String format="MMMM";
        return getNameOrAbbrev(format);
    }

    private static String getNameOrAbbrev(String format){
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat(format,Locale.ENGLISH);
        return dateFormat.format(cal.getTime());
    }

    public static String monthAbbreviation(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return EMPTY_STR;
        String[] formatsIn={dt,d,gym,gm};
        String formatOut="MMM";
        return getNameOrAbbrev(datetimeIn,formatsIn,formatOut);
    }

    public static String monthAbbreviation(){
        String format="MMM";
        return getNameOrAbbrev(format);
    }

    public static String dayName(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return EMPTY_STR;
        String[] formatsIn={dt,d};
        String formatOut="EEEE";
        return getNameOrAbbrev(datetimeIn,formatsIn,formatOut);
    }

    public static String dayName(){
        String format="EEEE";
        return getNameOrAbbrev(format);
    }

    public static String dayAbbreviation(String datetimeIn)
            throws ParseException{
        String[] edz=getEraDatetimeZone(datetimeIn);
        String datetime=edz[1];
        if(datetime==null)
            return EMPTY_STR;
        String[] formatsIn={dt,d};
        String formatOut="EEE";
        return getNameOrAbbrev(datetimeIn,formatsIn,formatOut);
    }

    public static String dayAbbreviation(){
        String format="EEE";
        return getNameOrAbbrev(format);
    }

    public static String formatDate(String dateTime,String pattern){
        final String yearSymbols="Gy";
        final String monthSymbols="M";
        final String daySymbols="dDEFwW";
        TimeZone timeZone;
        String zone;
        // Get the timezone information if it was supplied and modify the
        // dateTime so that SimpleDateFormat will understand it.
        if(dateTime.endsWith("Z")||dateTime.endsWith("z")){
            timeZone=TimeZone.getTimeZone("GMT");
            dateTime=dateTime.substring(0,dateTime.length()-1)+"GMT";
            zone="z";
        }else if((dateTime.length()>=6)
                &&(dateTime.charAt(dateTime.length()-3)==':')
                &&((dateTime.charAt(dateTime.length()-6)=='+')
                ||(dateTime.charAt(dateTime.length()-6)=='-'))){
            String offset=dateTime.substring(dateTime.length()-6);
            if("+00:00".equals(offset)||"-00:00".equals(offset)){
                timeZone=TimeZone.getTimeZone("GMT");
            }else{
                timeZone=TimeZone.getTimeZone("GMT"+offset);
            }
            zone="z";
            // Need to adjust it since SimpleDateFormat requires GMT+hh:mm but
            // we have +hh:mm.
            dateTime=dateTime.substring(0,dateTime.length()-6)+"GMT"+offset;
        }else{
            // Assume local time.
            timeZone=TimeZone.getDefault();
            zone="";
            // Leave off the timezone since SimpleDateFormat will assume local
            // time if time zone is not included.
        }
        String[] formats={dt+zone,d,gym,gy};
        // Try the time format first. We need to do this to prevent
        // SimpleDateFormat from interpreting a time as a year. i.e we just need
        // to check if it's a time before we check it's a year.
        try{
            SimpleDateFormat inFormat=new SimpleDateFormat(t+zone);
            inFormat.setLenient(false);
            Date d=inFormat.parse(dateTime);
            SimpleDateFormat outFormat=new SimpleDateFormat(strip
                    (yearSymbols+monthSymbols+daySymbols,pattern));
            outFormat.setTimeZone(timeZone);
            return outFormat.format(d);
        }catch(ParseException pe){
        }
        // Try the right truncated formats.
        for(int i=0;i<formats.length;i++){
            try{
                SimpleDateFormat inFormat=new SimpleDateFormat(formats[i]);
                inFormat.setLenient(false);
                Date d=inFormat.parse(dateTime);
                SimpleDateFormat outFormat=new SimpleDateFormat(pattern);
                outFormat.setTimeZone(timeZone);
                return outFormat.format(d);
            }catch(ParseException pe){
            }
        }
        // Now try the left truncated ones. The Java format() function doesn't
        // return the correct strings in this case. We strip any pattern
        // symbols that shouldn't be output so that they are not defaulted to
        // inappropriate values in the output.
        try{
            SimpleDateFormat inFormat=new SimpleDateFormat(gmd);
            inFormat.setLenient(false);
            Date d=inFormat.parse(dateTime);
            SimpleDateFormat outFormat=new SimpleDateFormat(strip(yearSymbols,pattern));
            outFormat.setTimeZone(timeZone);
            return outFormat.format(d);
        }catch(ParseException pe){
        }
        try{
            SimpleDateFormat inFormat=new SimpleDateFormat(gm);
            inFormat.setLenient(false);
            Date d=inFormat.parse(dateTime);
            SimpleDateFormat outFormat=new SimpleDateFormat(strip(yearSymbols,pattern));
            outFormat.setTimeZone(timeZone);
            return outFormat.format(d);
        }catch(ParseException pe){
        }
        try{
            SimpleDateFormat inFormat=new SimpleDateFormat(gd);
            inFormat.setLenient(false);
            Date d=inFormat.parse(dateTime);
            SimpleDateFormat outFormat=new SimpleDateFormat(strip(yearSymbols+monthSymbols,pattern));
            outFormat.setTimeZone(timeZone);
            return outFormat.format(d);
        }catch(ParseException pe){
        }
        return EMPTY_STR;
    }

    private static String strip(String symbols,String pattern){
        int quoteSemaphore=0;
        int i=0;
        StringBuffer result=new StringBuffer(pattern.length());
        while(i<pattern.length()){
            char ch=pattern.charAt(i);
            if(ch=='\''){
                // Assume it's an openening quote so simply copy the quoted
                // text to the result. There is nothing to strip here.
                int endQuote=pattern.indexOf('\'',i+1);
                if(endQuote==-1){
                    endQuote=pattern.length();
                }
                result.append(pattern.substring(i,endQuote));
                i=endQuote++;
            }else if(symbols.indexOf(ch)>-1){
                // The char needs to be stripped.
                i++;
            }else{
                result.append(ch);
                i++;
            }
        }
        return result.toString();
    }
}

/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * (C) Copyright Taligent, Inc. 1996-1998 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996-1998 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.util;

import sun.util.calendar.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class GregorianCalendar extends Calendar{
    //////////////////
// Class Variables
//////////////////
    public static final int BC=0;
    public static final int AD=1;
    static final int BCE=0;
    static final int CE=1;
    static final int MONTH_LENGTH[]
            ={31,28,31,30,31,30,31,31,30,31,30,31}; // 0-based
    static final int LEAP_MONTH_LENGTH[]
            ={31,29,31,30,31,30,31,31,30,31,30,31}; // 0-based
    // Proclaim serialization compatibility with JDK 1.1
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final long serialVersionUID=-8125100834729963327L;
    // The default value of gregorianCutover.
    static final long DEFAULT_GREGORIAN_CUTOVER=-12219292800000L;
    private static final int EPOCH_OFFSET=719163; // Fixed date of January 1, 1970 (Gregorian)
    private static final int EPOCH_YEAR=1970;
    // Useful millisecond constants.  Although ONE_DAY and ONE_WEEK can fit
    // into ints, they must be longs in order to prevent arithmetic overflow
    // when performing (bug 4173516).
    private static final int ONE_SECOND=1000;
    private static final int ONE_MINUTE=60*ONE_SECOND;
    private static final int ONE_HOUR=60*ONE_MINUTE;
    static final int MIN_VALUES[]={
            BCE,            // ERA
            1,              // YEAR
            JANUARY,        // MONTH
            1,              // WEEK_OF_YEAR
            0,              // WEEK_OF_MONTH
            1,              // DAY_OF_MONTH
            1,              // DAY_OF_YEAR
            SUNDAY,         // DAY_OF_WEEK
            1,              // DAY_OF_WEEK_IN_MONTH
            AM,             // AM_PM
            0,              // HOUR
            0,              // HOUR_OF_DAY
            0,              // MINUTE
            0,              // SECOND
            0,              // MILLISECOND
            -13*ONE_HOUR,   // ZONE_OFFSET (UNIX compatibility)
            0               // DST_OFFSET
    };
    static final int LEAST_MAX_VALUES[]={
            CE,             // ERA
            292269054,      // YEAR
            DECEMBER,       // MONTH
            52,             // WEEK_OF_YEAR
            4,              // WEEK_OF_MONTH
            28,             // DAY_OF_MONTH
            365,            // DAY_OF_YEAR
            SATURDAY,       // DAY_OF_WEEK
            4,              // DAY_OF_WEEK_IN
            PM,             // AM_PM
            11,             // HOUR
            23,             // HOUR_OF_DAY
            59,             // MINUTE
            59,             // SECOND
            999,            // MILLISECOND
            14*ONE_HOUR,    // ZONE_OFFSET
            20*ONE_MINUTE   // DST_OFFSET (historical least maximum)
    };
    static final int MAX_VALUES[]={
            CE,             // ERA
            292278994,      // YEAR
            DECEMBER,       // MONTH
            53,             // WEEK_OF_YEAR
            6,              // WEEK_OF_MONTH
            31,             // DAY_OF_MONTH
            366,            // DAY_OF_YEAR
            SATURDAY,       // DAY_OF_WEEK
            6,              // DAY_OF_WEEK_IN
            PM,             // AM_PM
            11,             // HOUR
            23,             // HOUR_OF_DAY
            59,             // MINUTE
            59,             // SECOND
            999,            // MILLISECOND
            14*ONE_HOUR,    // ZONE_OFFSET
            2*ONE_HOUR      // DST_OFFSET (double summer time)
    };
    private static final long ONE_DAY=24*ONE_HOUR;
    private static final long ONE_WEEK=7*ONE_DAY;
    // Reference to the sun.util.calendar.Gregorian instance (singleton).
    private static final Gregorian gcal=
            CalendarSystem.getGregorianCalendar();
    // Reference to the JulianCalendar instance (singleton), set as needed. See
    // getJulianCalendarSystem().
    private static JulianCalendar jcal;
    // JulianCalendar eras. See getJulianCalendarSystem().
    private static Era[] jeras;
    /////////////////////
// Instance Variables
/////////////////////
    private long gregorianCutover=DEFAULT_GREGORIAN_CUTOVER;
    private transient long gregorianCutoverDate=
            (((DEFAULT_GREGORIAN_CUTOVER+1)/ONE_DAY)-1)+EPOCH_OFFSET; // == 577736
    private transient int gregorianCutoverYear=1582;
    private transient int gregorianCutoverYearJulian=1582;
    private transient BaseCalendar.Date gdate;
    private transient BaseCalendar.Date cdate;
    private transient BaseCalendar calsys;
    private transient int[] zoneOffsets;
    private transient int[] originalFields;
    ///////////////
// Constructors
///////////////
/////////////////////////////
// Time => Fields computation
/////////////////////////////
    transient private long cachedFixedDate=Long.MIN_VALUE;

    public GregorianCalendar(){
        this(TimeZone.getDefaultRef(),Locale.getDefault(Locale.Category.FORMAT));
        setZoneShared(true);
    }

    public GregorianCalendar(TimeZone zone,Locale aLocale){
        super(zone,aLocale);
        gdate=(BaseCalendar.Date)gcal.newCalendarDate(zone);
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(TimeZone zone){
        this(zone,Locale.getDefault(Locale.Category.FORMAT));
    }

    public GregorianCalendar(Locale aLocale){
        this(TimeZone.getDefaultRef(),aLocale);
        setZoneShared(true);
    }

    public GregorianCalendar(int year,int month,int dayOfMonth){
        this(year,month,dayOfMonth,0,0,0,0);
    }

    GregorianCalendar(int year,int month,int dayOfMonth,
                      int hourOfDay,int minute,int second,int millis){
        super();
        gdate=(BaseCalendar.Date)gcal.newCalendarDate(getZone());
        this.set(YEAR,year);
        this.set(MONTH,month);
        this.set(DAY_OF_MONTH,dayOfMonth);
        // Set AM_PM and HOUR here to set their stamp values before
        // setting HOUR_OF_DAY (6178071).
        if(hourOfDay>=12&&hourOfDay<=23){
            // If hourOfDay is a valid PM hour, set the correct PM values
            // so that it won't throw an exception in case it's set to
            // non-lenient later.
            this.internalSet(AM_PM,PM);
            this.internalSet(HOUR,hourOfDay-12);
        }else{
            // The default value for AM_PM is AM.
            // We don't care any out of range value here for leniency.
            this.internalSet(HOUR,hourOfDay);
        }
        // The stamp values of AM_PM and HOUR must be COMPUTED. (6440854)
        setFieldsComputed(HOUR_MASK|AM_PM_MASK);
        this.set(HOUR_OF_DAY,hourOfDay);
        this.set(MINUTE,minute);
        this.set(SECOND,second);
        // should be changed to set() when this constructor is made
        // public.
        this.internalSet(MILLISECOND,millis);
    }

    public GregorianCalendar(int year,int month,int dayOfMonth,int hourOfDay,
                             int minute){
        this(year,month,dayOfMonth,hourOfDay,minute,0,0);
    }

    public GregorianCalendar(int year,int month,int dayOfMonth,int hourOfDay,
                             int minute,int second){
        this(year,month,dayOfMonth,hourOfDay,minute,second,0);
    }
/////////////////
// Public methods
/////////////////

    GregorianCalendar(TimeZone zone,Locale locale,boolean flag){
        super(zone,locale);
        gdate=(BaseCalendar.Date)gcal.newCalendarDate(getZone());
    }

    private static int getRolledValue(int value,int amount,int min,int max){
        assert value>=min&&value<=max;
        int range=max-min+1;
        amount%=range;
        int n=value+amount;
        if(n>max){
            n-=range;
        }else if(n<min){
            n+=range;
        }
        assert n>=min&&n<=max;
        return n;
    }

    public static GregorianCalendar from(ZonedDateTime zdt){
        GregorianCalendar cal=new GregorianCalendar(TimeZone.getTimeZone(zdt.getZone()));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        cal.setFirstDayOfWeek(MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        try{
            cal.setTimeInMillis(Math.addExact(Math.multiplyExact(zdt.toEpochSecond(),1000),
                    zdt.get(ChronoField.MILLI_OF_SECOND)));
        }catch(ArithmeticException ex){
            throw new IllegalArgumentException(ex);
        }
        return cal;
    }

    public void setGregorianChange(Date date){
        long cutoverTime=date.getTime();
        if(cutoverTime==gregorianCutover){
            return;
        }
        // Before changing the cutover date, make sure to have the
        // time of this calendar.
        complete();
        setGregorianChange(cutoverTime);
    }

    public final Date getGregorianChange(){
        return new Date(gregorianCutover);
    }

    private void setGregorianChange(long cutoverTime){
        gregorianCutover=cutoverTime;
        gregorianCutoverDate=CalendarUtils.floorDivide(cutoverTime,ONE_DAY)
                +EPOCH_OFFSET;
        // To provide the "pure" Julian calendar as advertised.
        // Strictly speaking, the last millisecond should be a
        // Gregorian date. However, the API doc specifies that setting
        // the cutover date to Long.MAX_VALUE will make this calendar
        // a pure Julian calendar. (See 4167995)
        if(cutoverTime==Long.MAX_VALUE){
            gregorianCutoverDate++;
        }
        BaseCalendar.Date d=getGregorianCutoverDate();
        // Set the cutover year (in the Gregorian year numbering)
        gregorianCutoverYear=d.getYear();
        BaseCalendar julianCal=getJulianCalendarSystem();
        d=(BaseCalendar.Date)julianCal.newCalendarDate(TimeZone.NO_TIMEZONE);
        julianCal.getCalendarDateFromFixedDate(d,gregorianCutoverDate-1);
        gregorianCutoverYearJulian=d.getNormalizedYear();
        if(time<gregorianCutover){
            // The field values are no longer valid under the new
            // cutover date.
            setUnnormalized();
        }
    }

    private BaseCalendar.Date getGregorianCutoverDate(){
        return getCalendarDate(gregorianCutoverDate);
    }

    private BaseCalendar.Date getCalendarDate(long fd){
        BaseCalendar cal=(fd>=gregorianCutoverDate)?gcal:getJulianCalendarSystem();
        BaseCalendar.Date d=(BaseCalendar.Date)cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cal.getCalendarDateFromFixedDate(d,fd);
        return d;
    }

    private static synchronized BaseCalendar getJulianCalendarSystem(){
        if(jcal==null){
            jcal=(JulianCalendar)CalendarSystem.forName("julian");
            jeras=jcal.getEras();
        }
        return jcal;
    }

    private long getYearOffsetInMillis(){
        long t=(internalGet(DAY_OF_YEAR)-1)*24;
        t+=internalGet(HOUR_OF_DAY);
        t*=60;
        t+=internalGet(MINUTE);
        t*=60;
        t+=internalGet(SECOND);
        t*=1000;
        return t+internalGet(MILLISECOND)-
                (internalGet(ZONE_OFFSET)+internalGet(DST_OFFSET));
    }

    private int computeFields(int fieldMask,int tzMask){
        int zoneOffset=0;
        TimeZone tz=getZone();
        if(zoneOffsets==null){
            zoneOffsets=new int[2];
        }
        if(tzMask!=(ZONE_OFFSET_MASK|DST_OFFSET_MASK)){
            if(tz instanceof ZoneInfo){
                zoneOffset=((ZoneInfo)tz).getOffsets(time,zoneOffsets);
            }else{
                zoneOffset=tz.getOffset(time);
                zoneOffsets[0]=tz.getRawOffset();
                zoneOffsets[1]=zoneOffset-zoneOffsets[0];
            }
        }
        if(tzMask!=0){
            if(isFieldSet(tzMask,ZONE_OFFSET)){
                zoneOffsets[0]=internalGet(ZONE_OFFSET);
            }
            if(isFieldSet(tzMask,DST_OFFSET)){
                zoneOffsets[1]=internalGet(DST_OFFSET);
            }
            zoneOffset=zoneOffsets[0]+zoneOffsets[1];
        }
        // By computing time and zoneOffset separately, we can take
        // the wider range of time+zoneOffset than the previous
        // implementation.
        long fixedDate=zoneOffset/ONE_DAY;
        int timeOfDay=zoneOffset%(int)ONE_DAY;
        fixedDate+=time/ONE_DAY;
        timeOfDay+=(int)(time%ONE_DAY);
        if(timeOfDay>=ONE_DAY){
            timeOfDay-=ONE_DAY;
            ++fixedDate;
        }else{
            while(timeOfDay<0){
                timeOfDay+=ONE_DAY;
                --fixedDate;
            }
        }
        fixedDate+=EPOCH_OFFSET;
        int era=CE;
        int year;
        if(fixedDate>=gregorianCutoverDate){
            // Handle Gregorian dates.
            assert cachedFixedDate==Long.MIN_VALUE||gdate.isNormalized()
                    :"cache control: not normalized";
            assert cachedFixedDate==Long.MIN_VALUE||
                    gcal.getFixedDate(gdate.getNormalizedYear(),
                            gdate.getMonth(),
                            gdate.getDayOfMonth(),gdate)
                            ==cachedFixedDate
                    :"cache control: inconsictency"+
                    ", cachedFixedDate="+cachedFixedDate+
                    ", computed="+
                    gcal.getFixedDate(gdate.getNormalizedYear(),
                            gdate.getMonth(),
                            gdate.getDayOfMonth(),
                            gdate)+
                    ", date="+gdate;
            // See if we can use gdate to avoid date calculation.
            if(fixedDate!=cachedFixedDate){
                gcal.getCalendarDateFromFixedDate(gdate,fixedDate);
                cachedFixedDate=fixedDate;
            }
            year=gdate.getYear();
            if(year<=0){
                year=1-year;
                era=BCE;
            }
            calsys=gcal;
            cdate=gdate;
            assert cdate.getDayOfWeek()>0:"dow="+cdate.getDayOfWeek()+", date="+cdate;
        }else{
            // Handle Julian calendar dates.
            calsys=getJulianCalendarSystem();
            cdate=(BaseCalendar.Date)jcal.newCalendarDate(getZone());
            jcal.getCalendarDateFromFixedDate(cdate,fixedDate);
            Era e=cdate.getEra();
            if(e==jeras[0]){
                era=BCE;
            }
            year=cdate.getYear();
        }
        // Always set the ERA and YEAR values.
        internalSet(ERA,era);
        internalSet(YEAR,year);
        int mask=fieldMask|(ERA_MASK|YEAR_MASK);
        int month=cdate.getMonth()-1; // 0-based
        int dayOfMonth=cdate.getDayOfMonth();
        // Set the basic date fields.
        if((fieldMask&(MONTH_MASK|DAY_OF_MONTH_MASK|DAY_OF_WEEK_MASK))
                !=0){
            internalSet(MONTH,month);
            internalSet(DAY_OF_MONTH,dayOfMonth);
            internalSet(DAY_OF_WEEK,cdate.getDayOfWeek());
            mask|=MONTH_MASK|DAY_OF_MONTH_MASK|DAY_OF_WEEK_MASK;
        }
        if((fieldMask&(HOUR_OF_DAY_MASK|AM_PM_MASK|HOUR_MASK
                |MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK))!=0){
            if(timeOfDay!=0){
                int hours=timeOfDay/ONE_HOUR;
                internalSet(HOUR_OF_DAY,hours);
                internalSet(AM_PM,hours/12); // Assume AM == 0
                internalSet(HOUR,hours%12);
                int r=timeOfDay%ONE_HOUR;
                internalSet(MINUTE,r/ONE_MINUTE);
                r%=ONE_MINUTE;
                internalSet(SECOND,r/ONE_SECOND);
                internalSet(MILLISECOND,r%ONE_SECOND);
            }else{
                internalSet(HOUR_OF_DAY,0);
                internalSet(AM_PM,AM);
                internalSet(HOUR,0);
                internalSet(MINUTE,0);
                internalSet(SECOND,0);
                internalSet(MILLISECOND,0);
            }
            mask|=(HOUR_OF_DAY_MASK|AM_PM_MASK|HOUR_MASK
                    |MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK);
        }
        if((fieldMask&(ZONE_OFFSET_MASK|DST_OFFSET_MASK))!=0){
            internalSet(ZONE_OFFSET,zoneOffsets[0]);
            internalSet(DST_OFFSET,zoneOffsets[1]);
            mask|=(ZONE_OFFSET_MASK|DST_OFFSET_MASK);
        }
        if((fieldMask&(DAY_OF_YEAR_MASK|WEEK_OF_YEAR_MASK|WEEK_OF_MONTH_MASK|DAY_OF_WEEK_IN_MONTH_MASK))!=0){
            int normalizedYear=cdate.getNormalizedYear();
            long fixedDateJan1=calsys.getFixedDate(normalizedYear,1,1,cdate);
            int dayOfYear=(int)(fixedDate-fixedDateJan1)+1;
            long fixedDateMonth1=fixedDate-dayOfMonth+1;
            int cutoverGap=0;
            int cutoverYear=(calsys==gcal)?gregorianCutoverYear:gregorianCutoverYearJulian;
            int relativeDayOfMonth=dayOfMonth-1;
            // If we are in the cutover year, we need some special handling.
            if(normalizedYear==cutoverYear){
                // Need to take care of the "missing" days.
                if(gregorianCutoverYearJulian<=gregorianCutoverYear){
                    // We need to find out where we are. The cutover
                    // gap could even be more than one year.  (One
                    // year difference in ~48667 years.)
                    fixedDateJan1=getFixedDateJan1(cdate,fixedDate);
                    if(fixedDate>=gregorianCutoverDate){
                        fixedDateMonth1=getFixedDateMonth1(cdate,fixedDate);
                    }
                }
                int realDayOfYear=(int)(fixedDate-fixedDateJan1)+1;
                cutoverGap=dayOfYear-realDayOfYear;
                dayOfYear=realDayOfYear;
                relativeDayOfMonth=(int)(fixedDate-fixedDateMonth1);
            }
            internalSet(DAY_OF_YEAR,dayOfYear);
            internalSet(DAY_OF_WEEK_IN_MONTH,relativeDayOfMonth/7+1);
            int weekOfYear=getWeekNumber(fixedDateJan1,fixedDate);
            // The spec is to calculate WEEK_OF_YEAR in the
            // ISO8601-style. This creates problems, though.
            if(weekOfYear==0){
                // If the date belongs to the last week of the
                // previous year, use the week number of "12/31" of
                // the "previous" year. Again, if the previous year is
                // the Gregorian cutover year, we need to take care of
                // it.  Usually the previous day of January 1 is
                // December 31, which is not always true in
                // GregorianCalendar.
                long fixedDec31=fixedDateJan1-1;
                long prevJan1=fixedDateJan1-365;
                if(normalizedYear>(cutoverYear+1)){
                    if(CalendarUtils.isGregorianLeapYear(normalizedYear-1)){
                        --prevJan1;
                    }
                }else if(normalizedYear<=gregorianCutoverYearJulian){
                    if(CalendarUtils.isJulianLeapYear(normalizedYear-1)){
                        --prevJan1;
                    }
                }else{
                    BaseCalendar calForJan1=calsys;
                    //int prevYear = normalizedYear - 1;
                    int prevYear=getCalendarDate(fixedDec31).getNormalizedYear();
                    if(prevYear==gregorianCutoverYear){
                        calForJan1=getCutoverCalendarSystem();
                        if(calForJan1==jcal){
                            prevJan1=calForJan1.getFixedDate(prevYear,
                                    BaseCalendar.JANUARY,
                                    1,
                                    null);
                        }else{
                            prevJan1=gregorianCutoverDate;
                            calForJan1=gcal;
                        }
                    }else if(prevYear<=gregorianCutoverYearJulian){
                        calForJan1=getJulianCalendarSystem();
                        prevJan1=calForJan1.getFixedDate(prevYear,
                                BaseCalendar.JANUARY,
                                1,
                                null);
                    }
                }
                weekOfYear=getWeekNumber(prevJan1,fixedDec31);
            }else{
                if(normalizedYear>gregorianCutoverYear||
                        normalizedYear<(gregorianCutoverYearJulian-1)){
                    // Regular years
                    if(weekOfYear>=52){
                        long nextJan1=fixedDateJan1+365;
                        if(cdate.isLeapYear()){
                            nextJan1++;
                        }
                        long nextJan1st=BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1+6,
                                getFirstDayOfWeek());
                        int ndays=(int)(nextJan1st-nextJan1);
                        if(ndays>=getMinimalDaysInFirstWeek()&&fixedDate>=(nextJan1st-7)){
                            // The first days forms a week in which the date is included.
                            weekOfYear=1;
                        }
                    }
                }else{
                    BaseCalendar calForJan1=calsys;
                    int nextYear=normalizedYear+1;
                    if(nextYear==(gregorianCutoverYearJulian+1)&&
                            nextYear<gregorianCutoverYear){
                        // In case the gap is more than one year.
                        nextYear=gregorianCutoverYear;
                    }
                    if(nextYear==gregorianCutoverYear){
                        calForJan1=getCutoverCalendarSystem();
                    }
                    long nextJan1;
                    if(nextYear>gregorianCutoverYear
                            ||gregorianCutoverYearJulian==gregorianCutoverYear
                            ||nextYear==gregorianCutoverYearJulian){
                        nextJan1=calForJan1.getFixedDate(nextYear,
                                BaseCalendar.JANUARY,
                                1,
                                null);
                    }else{
                        nextJan1=gregorianCutoverDate;
                        calForJan1=gcal;
                    }
                    long nextJan1st=BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1+6,
                            getFirstDayOfWeek());
                    int ndays=(int)(nextJan1st-nextJan1);
                    if(ndays>=getMinimalDaysInFirstWeek()&&fixedDate>=(nextJan1st-7)){
                        // The first days forms a week in which the date is included.
                        weekOfYear=1;
                    }
                }
            }
            internalSet(WEEK_OF_YEAR,weekOfYear);
            internalSet(WEEK_OF_MONTH,getWeekNumber(fixedDateMonth1,fixedDate));
            mask|=(DAY_OF_YEAR_MASK|WEEK_OF_YEAR_MASK|WEEK_OF_MONTH_MASK|DAY_OF_WEEK_IN_MONTH_MASK);
        }
        return mask;
    }

    private int getWeekNumber(long fixedDay1,long fixedDate){
        // We can always use `gcal' since Julian and Gregorian are the
        // same thing for this calculation.
        long fixedDay1st=Gregorian.getDayOfWeekDateOnOrBefore(fixedDay1+6,
                getFirstDayOfWeek());
        int ndays=(int)(fixedDay1st-fixedDay1);
        assert ndays<=7;
        if(ndays>=getMinimalDaysInFirstWeek()){
            fixedDay1st-=7;
        }
        int normalizedDayOfPeriod=(int)(fixedDate-fixedDay1st);
        if(normalizedDayOfPeriod>=0){
            return normalizedDayOfPeriod/7+1;
        }
        return CalendarUtils.floorDivide(normalizedDayOfPeriod,7)+1;
    }    @Override
    public int getMinimum(int field){
        return MIN_VALUES[field];
    }

    @Override
    protected void computeTime(){
        // In non-lenient mode, perform brief checking of calendar
        // fields which have been set externally. Through this
        // checking, the field values are stored in originalFields[]
        // to see if any of them are normalized later.
        if(!isLenient()){
            if(originalFields==null){
                originalFields=new int[FIELD_COUNT];
            }
            for(int field=0;field<FIELD_COUNT;field++){
                int value=internalGet(field);
                if(isExternallySet(field)){
                    // Quick validation for any out of range values
                    if(value<getMinimum(field)||value>getMaximum(field)){
                        throw new IllegalArgumentException(getFieldName(field));
                    }
                }
                originalFields[field]=value;
            }
        }
        // Let the super class determine which calendar fields to be
        // used to calculate the time.
        int fieldMask=selectFields();
        // The year defaults to the epoch start. We don't check
        // fieldMask for YEAR because YEAR is a mandatory field to
        // determine the date.
        int year=isSet(YEAR)?internalGet(YEAR):EPOCH_YEAR;
        int era=internalGetEra();
        if(era==BCE){
            year=1-year;
        }else if(era!=CE){
            // Even in lenient mode we disallow ERA values other than CE & BCE.
            // (The same normalization rule as add()/roll() could be
            // applied here in lenient mode. But this checking is kept
            // unchanged for compatibility as of 1.5.)
            throw new IllegalArgumentException("Invalid era");
        }
        // If year is 0 or negative, we need to set the ERA value later.
        if(year<=0&&!isSet(ERA)){
            fieldMask|=ERA_MASK;
            setFieldsComputed(ERA_MASK);
        }
        // Calculate the time of day. We rely on the convention that
        // an UNSET field has 0.
        long timeOfDay=0;
        if(isFieldSet(fieldMask,HOUR_OF_DAY)){
            timeOfDay+=(long)internalGet(HOUR_OF_DAY);
        }else{
            timeOfDay+=internalGet(HOUR);
            // The default value of AM_PM is 0 which designates AM.
            if(isFieldSet(fieldMask,AM_PM)){
                timeOfDay+=12*internalGet(AM_PM);
            }
        }
        timeOfDay*=60;
        timeOfDay+=internalGet(MINUTE);
        timeOfDay*=60;
        timeOfDay+=internalGet(SECOND);
        timeOfDay*=1000;
        timeOfDay+=internalGet(MILLISECOND);
        // Convert the time of day to the number of days and the
        // millisecond offset from midnight.
        long fixedDate=timeOfDay/ONE_DAY;
        timeOfDay%=ONE_DAY;
        while(timeOfDay<0){
            timeOfDay+=ONE_DAY;
            --fixedDate;
        }
        // Calculate the fixed date since January 1, 1 (Gregorian).
        calculateFixedDate:
        {
            long gfd, jfd;
            if(year>gregorianCutoverYear&&year>gregorianCutoverYearJulian){
                gfd=fixedDate+getFixedDate(gcal,year,fieldMask);
                if(gfd>=gregorianCutoverDate){
                    fixedDate=gfd;
                    break calculateFixedDate;
                }
                jfd=fixedDate+getFixedDate(getJulianCalendarSystem(),year,fieldMask);
            }else if(year<gregorianCutoverYear&&year<gregorianCutoverYearJulian){
                jfd=fixedDate+getFixedDate(getJulianCalendarSystem(),year,fieldMask);
                if(jfd<gregorianCutoverDate){
                    fixedDate=jfd;
                    break calculateFixedDate;
                }
                gfd=jfd;
            }else{
                jfd=fixedDate+getFixedDate(getJulianCalendarSystem(),year,fieldMask);
                gfd=fixedDate+getFixedDate(gcal,year,fieldMask);
            }
            // Now we have to determine which calendar date it is.
            // If the date is relative from the beginning of the year
            // in the Julian calendar, then use jfd;
            if(isFieldSet(fieldMask,DAY_OF_YEAR)||isFieldSet(fieldMask,WEEK_OF_YEAR)){
                if(gregorianCutoverYear==gregorianCutoverYearJulian){
                    fixedDate=jfd;
                    break calculateFixedDate;
                }else if(year==gregorianCutoverYear){
                    fixedDate=gfd;
                    break calculateFixedDate;
                }
            }
            if(gfd>=gregorianCutoverDate){
                if(jfd>=gregorianCutoverDate){
                    fixedDate=gfd;
                }else{
                    // The date is in an "overlapping" period. No way
                    // to disambiguate it. Determine it using the
                    // previous date calculation.
                    if(calsys==gcal||calsys==null){
                        fixedDate=gfd;
                    }else{
                        fixedDate=jfd;
                    }
                }
            }else{
                if(jfd<gregorianCutoverDate){
                    fixedDate=jfd;
                }else{
                    // The date is in a "missing" period.
                    if(!isLenient()){
                        throw new IllegalArgumentException("the specified date doesn't exist");
                    }
                    // Take the Julian date for compatibility, which
                    // will produce a Gregorian date.
                    fixedDate=jfd;
                }
            }
        }
        // millis represents local wall-clock time in milliseconds.
        long millis=(fixedDate-EPOCH_OFFSET)*ONE_DAY+timeOfDay;
        // Compute the time zone offset and DST offset.  There are two potential
        // ambiguities here.  We'll assume a 2:00 am (wall time) switchover time
        // for discussion purposes here.
        // 1. The transition into DST.  Here, a designated time of 2:00 am - 2:59 am
        //    can be in standard or in DST depending.  However, 2:00 am is an invalid
        //    representation (the representation jumps from 1:59:59 am Std to 3:00:00 am DST).
        //    We assume standard time.
        // 2. The transition out of DST.  Here, a designated time of 1:00 am - 1:59 am
        //    can be in standard or DST.  Both are valid representations (the rep
        //    jumps from 1:59:59 DST to 1:00:00 Std).
        //    Again, we assume standard time.
        // We use the TimeZone object, unless the user has explicitly set the ZONE_OFFSET
        // or DST_OFFSET fields; then we use those fields.
        TimeZone zone=getZone();
        if(zoneOffsets==null){
            zoneOffsets=new int[2];
        }
        int tzMask=fieldMask&(ZONE_OFFSET_MASK|DST_OFFSET_MASK);
        if(tzMask!=(ZONE_OFFSET_MASK|DST_OFFSET_MASK)){
            if(zone instanceof ZoneInfo){
                ((ZoneInfo)zone).getOffsetsByWall(millis,zoneOffsets);
            }else{
                int gmtOffset=isFieldSet(fieldMask,ZONE_OFFSET)?
                        internalGet(ZONE_OFFSET):zone.getRawOffset();
                zone.getOffsets(millis-gmtOffset,zoneOffsets);
            }
        }
        if(tzMask!=0){
            if(isFieldSet(tzMask,ZONE_OFFSET)){
                zoneOffsets[0]=internalGet(ZONE_OFFSET);
            }
            if(isFieldSet(tzMask,DST_OFFSET)){
                zoneOffsets[1]=internalGet(DST_OFFSET);
            }
        }
        // Adjust the time zone offset values to get the UTC time.
        millis-=zoneOffsets[0]+zoneOffsets[1];
        // Set this calendar's time in milliseconds
        time=millis;
        int mask=computeFields(fieldMask|getSetStateFields(),tzMask);
        if(!isLenient()){
            for(int field=0;field<FIELD_COUNT;field++){
                if(!isExternallySet(field)){
                    continue;
                }
                if(originalFields[field]!=internalGet(field)){
                    String s=originalFields[field]+" -> "+internalGet(field);
                    // Restore the original field values
                    System.arraycopy(originalFields,0,fields,0,fields.length);
                    throw new IllegalArgumentException(getFieldName(field)+": "+s);
                }
            }
        }
        setFieldsNormalized(mask);
    }

    @Override
    protected void computeFields(){
        int mask;
        if(isPartiallyNormalized()){
            // Determine which calendar fields need to be computed.
            mask=getSetStateFields();
            int fieldMask=~mask&ALL_FIELDS;
            // We have to call computTime in case calsys == null in
            // order to set calsys and cdate. (6263644)
            if(fieldMask!=0||calsys==null){
                mask|=computeFields(fieldMask,
                        mask&(ZONE_OFFSET_MASK|DST_OFFSET_MASK));
                assert mask==ALL_FIELDS;
            }
        }else{
            mask=ALL_FIELDS;
            computeFields(mask,0);
        }
        // After computing all the fields, set the field state to `COMPUTED'.
        setFieldsComputed(mask);
    }    @Override
    public int getMaximum(int field){
        switch(field){
            case MONTH:
            case DAY_OF_MONTH:
            case DAY_OF_YEAR:
            case WEEK_OF_YEAR:
            case WEEK_OF_MONTH:
            case DAY_OF_WEEK_IN_MONTH:
            case YEAR:{
                // On or after Gregorian 200-3-1, Julian and Gregorian
                // calendar dates are the same or Gregorian dates are
                // larger (i.e., there is a "gap") after 300-3-1.
                if(gregorianCutoverYear>200){
                    break;
                }
                // There might be "overlapping" dates.
                GregorianCalendar gc=(GregorianCalendar)clone();
                gc.setLenient(true);
                gc.setTimeInMillis(gregorianCutover);
                int v1=gc.getActualMaximum(field);
                gc.setTimeInMillis(gregorianCutover-1);
                int v2=gc.getActualMaximum(field);
                return Math.max(MAX_VALUES[field],Math.max(v1,v2));
            }
        }
        return MAX_VALUES[field];
    }

    @Override
    public String getCalendarType(){
        return "gregory";
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof GregorianCalendar&&
                super.equals(obj)&&
                gregorianCutover==((GregorianCalendar)obj).gregorianCutover;
    }    @Override
    public int getGreatestMinimum(int field){
        if(field==DAY_OF_MONTH){
            BaseCalendar.Date d=getGregorianCutoverDate();
            long mon1=getFixedDateMonth1(d,gregorianCutoverDate);
            d=getCalendarDate(mon1);
            return Math.max(MIN_VALUES[field],d.getDayOfMonth());
        }
        return MIN_VALUES[field];
    }

    @Override
    public int hashCode(){
        return super.hashCode()^(int)gregorianCutoverDate;
    }

    @Override
    public void add(int field,int amount){
        // If amount == 0, do nothing even the given field is out of
        // range. This is tested by JCK.
        if(amount==0){
            return;   // Do nothing!
        }
        if(field<0||field>=ZONE_OFFSET){
            throw new IllegalArgumentException();
        }
        // Sync the time and calendar fields.
        complete();
        if(field==YEAR){
            int year=internalGet(YEAR);
            if(internalGetEra()==CE){
                year+=amount;
                if(year>0){
                    set(YEAR,year);
                }else{ // year <= 0
                    set(YEAR,1-year);
                    // if year == 0, you get 1 BCE.
                    set(ERA,BCE);
                }
            }else{ // era == BCE
                year-=amount;
                if(year>0){
                    set(YEAR,year);
                }else{ // year <= 0
                    set(YEAR,1-year);
                    // if year == 0, you get 1 CE
                    set(ERA,CE);
                }
            }
            pinDayOfMonth();
        }else if(field==MONTH){
            int month=internalGet(MONTH)+amount;
            int year=internalGet(YEAR);
            int y_amount;
            if(month>=0){
                y_amount=month/12;
            }else{
                y_amount=(month+1)/12-1;
            }
            if(y_amount!=0){
                if(internalGetEra()==CE){
                    year+=y_amount;
                    if(year>0){
                        set(YEAR,year);
                    }else{ // year <= 0
                        set(YEAR,1-year);
                        // if year == 0, you get 1 BCE
                        set(ERA,BCE);
                    }
                }else{ // era == BCE
                    year-=y_amount;
                    if(year>0){
                        set(YEAR,year);
                    }else{ // year <= 0
                        set(YEAR,1-year);
                        // if year == 0, you get 1 CE
                        set(ERA,CE);
                    }
                }
            }
            if(month>=0){
                set(MONTH,month%12);
            }else{
                // month < 0
                month%=12;
                if(month<0){
                    month+=12;
                }
                set(MONTH,JANUARY+month);
            }
            pinDayOfMonth();
        }else if(field==ERA){
            int era=internalGet(ERA)+amount;
            if(era<0){
                era=0;
            }
            if(era>1){
                era=1;
            }
            set(ERA,era);
        }else{
            long delta=amount;
            long timeOfDay=0;
            switch(field){
                // Handle the time fields here. Convert the given
                // amount to milliseconds and call setTimeInMillis.
                case HOUR:
                case HOUR_OF_DAY:
                    delta*=60*60*1000;        // hours to minutes
                    break;
                case MINUTE:
                    delta*=60*1000;             // minutes to seconds
                    break;
                case SECOND:
                    delta*=1000;                  // seconds to milliseconds
                    break;
                case MILLISECOND:
                    break;
                // Handle week, day and AM_PM fields which involves
                // time zone offset change adjustment. Convert the
                // given amount to the number of days.
                case WEEK_OF_YEAR:
                case WEEK_OF_MONTH:
                case DAY_OF_WEEK_IN_MONTH:
                    delta*=7;
                    break;
                case DAY_OF_MONTH: // synonym of DATE
                case DAY_OF_YEAR:
                case DAY_OF_WEEK:
                    break;
                case AM_PM:
                    // Convert the amount to the number of days (delta)
                    // and +12 or -12 hours (timeOfDay).
                    delta=amount/2;
                    timeOfDay=12*(amount%2);
                    break;
            }
            // The time fields don't require time zone offset change
            // adjustment.
            if(field>=HOUR){
                setTimeInMillis(time+delta);
                return;
            }
            // The rest of the fields (week, day or AM_PM fields)
            // require time zone offset (both GMT and DST) change
            // adjustment.
            // Translate the current time to the fixed date and time
            // of the day.
            long fd=getCurrentFixedDate();
            timeOfDay+=internalGet(HOUR_OF_DAY);
            timeOfDay*=60;
            timeOfDay+=internalGet(MINUTE);
            timeOfDay*=60;
            timeOfDay+=internalGet(SECOND);
            timeOfDay*=1000;
            timeOfDay+=internalGet(MILLISECOND);
            if(timeOfDay>=ONE_DAY){
                fd++;
                timeOfDay-=ONE_DAY;
            }else if(timeOfDay<0){
                fd--;
                timeOfDay+=ONE_DAY;
            }
            fd+=delta; // fd is the expected fixed date after the calculation
            int zoneOffset=internalGet(ZONE_OFFSET)+internalGet(DST_OFFSET);
            setTimeInMillis((fd-EPOCH_OFFSET)*ONE_DAY+timeOfDay-zoneOffset);
            zoneOffset-=internalGet(ZONE_OFFSET)+internalGet(DST_OFFSET);
            // If the time zone offset has changed, then adjust the difference.
            if(zoneOffset!=0){
                setTimeInMillis(time+zoneOffset);
                long fd2=getCurrentFixedDate();
                // If the adjustment has changed the date, then take
                // the previous one.
                if(fd2!=fd){
                    setTimeInMillis(time-zoneOffset);
                }
            }
        }
    }    @Override
    public int getLeastMaximum(int field){
        switch(field){
            case MONTH:
            case DAY_OF_MONTH:
            case DAY_OF_YEAR:
            case WEEK_OF_YEAR:
            case WEEK_OF_MONTH:
            case DAY_OF_WEEK_IN_MONTH:
            case YEAR:{
                GregorianCalendar gc=(GregorianCalendar)clone();
                gc.setLenient(true);
                gc.setTimeInMillis(gregorianCutover);
                int v1=gc.getActualMaximum(field);
                gc.setTimeInMillis(gregorianCutover-1);
                int v2=gc.getActualMaximum(field);
                return Math.min(LEAST_MAX_VALUES[field],Math.min(v1,v2));
            }
        }
        return LEAST_MAX_VALUES[field];
    }

    @Override
    public void roll(int field,boolean up){
        roll(field,up?+1:-1);
    }

    @Override
    public void roll(int field,int amount){
        // If amount == 0, do nothing even the given field is out of
        // range. This is tested by JCK.
        if(amount==0){
            return;
        }
        if(field<0||field>=ZONE_OFFSET){
            throw new IllegalArgumentException();
        }
        // Sync the time and calendar fields.
        complete();
        int min=getMinimum(field);
        int max=getMaximum(field);
        switch(field){
            case AM_PM:
            case ERA:
            case YEAR:
            case MINUTE:
            case SECOND:
            case MILLISECOND:
                // These fields are handled simply, since they have fixed minima
                // and maxima.  The field DAY_OF_MONTH is almost as simple.  Other
                // fields are complicated, since the range within they must roll
                // varies depending on the date.
                break;
            case HOUR:
            case HOUR_OF_DAY:{
                int unit=max+1; // 12 or 24 hours
                int h=internalGet(field);
                int nh=(h+amount)%unit;
                if(nh<0){
                    nh+=unit;
                }
                time+=ONE_HOUR*(nh-h);
                // The day might have changed, which could happen if
                // the daylight saving time transition brings it to
                // the next day, although it's very unlikely. But we
                // have to make sure not to change the larger fields.
                CalendarDate d=calsys.getCalendarDate(time,getZone());
                if(internalGet(DAY_OF_MONTH)!=d.getDayOfMonth()){
                    d.setDate(internalGet(YEAR),
                            internalGet(MONTH)+1,
                            internalGet(DAY_OF_MONTH));
                    if(field==HOUR){
                        assert (internalGet(AM_PM)==PM);
                        d.addHours(+12); // restore PM
                    }
                    time=calsys.getTime(d);
                }
                int hourOfDay=d.getHours();
                internalSet(field,hourOfDay%unit);
                if(field==HOUR){
                    internalSet(HOUR_OF_DAY,hourOfDay);
                }else{
                    internalSet(AM_PM,hourOfDay/12);
                    internalSet(HOUR,hourOfDay%12);
                }
                // Time zone offset and/or daylight saving might have changed.
                int zoneOffset=d.getZoneOffset();
                int saving=d.getDaylightSaving();
                internalSet(ZONE_OFFSET,zoneOffset-saving);
                internalSet(DST_OFFSET,saving);
                return;
            }
            case MONTH:
                // Rolling the month involves both pinning the final value to [0, 11]
                // and adjusting the DAY_OF_MONTH if necessary.  We only adjust the
                // DAY_OF_MONTH if, after updating the MONTH field, it is illegal.
                // E.g., <jan31>.roll(MONTH, 1) -> <feb28> or <feb29>.
            {
                if(!isCutoverYear(cdate.getNormalizedYear())){
                    int mon=(internalGet(MONTH)+amount)%12;
                    if(mon<0){
                        mon+=12;
                    }
                    set(MONTH,mon);
                    // Keep the day of month in the range.  We don't want to spill over
                    // into the next month; e.g., we don't want jan31 + 1 mo -> feb31 ->
                    // mar3.
                    int monthLen=monthLength(mon);
                    if(internalGet(DAY_OF_MONTH)>monthLen){
                        set(DAY_OF_MONTH,monthLen);
                    }
                }else{
                    // We need to take care of different lengths in
                    // year and month due to the cutover.
                    int yearLength=getActualMaximum(MONTH)+1;
                    int mon=(internalGet(MONTH)+amount)%yearLength;
                    if(mon<0){
                        mon+=yearLength;
                    }
                    set(MONTH,mon);
                    int monthLen=getActualMaximum(DAY_OF_MONTH);
                    if(internalGet(DAY_OF_MONTH)>monthLen){
                        set(DAY_OF_MONTH,monthLen);
                    }
                }
                return;
            }
            case WEEK_OF_YEAR:{
                int y=cdate.getNormalizedYear();
                max=getActualMaximum(WEEK_OF_YEAR);
                set(DAY_OF_WEEK,internalGet(DAY_OF_WEEK));
                int woy=internalGet(WEEK_OF_YEAR);
                int value=woy+amount;
                if(!isCutoverYear(y)){
                    int weekYear=getWeekYear();
                    if(weekYear==y){
                        // If the new value is in between min and max
                        // (exclusive), then we can use the value.
                        if(value>min&&value<max){
                            set(WEEK_OF_YEAR,value);
                            return;
                        }
                        long fd=getCurrentFixedDate();
                        // Make sure that the min week has the current DAY_OF_WEEK
                        // in the calendar year
                        long day1=fd-(7*(woy-min));
                        if(calsys.getYearFromFixedDate(day1)!=y){
                            min++;
                        }
                        // Make sure the same thing for the max week
                        fd+=7*(max-internalGet(WEEK_OF_YEAR));
                        if(calsys.getYearFromFixedDate(fd)!=y){
                            max--;
                        }
                    }else{
                        // When WEEK_OF_YEAR and YEAR are out of sync,
                        // adjust woy and amount to stay in the calendar year.
                        if(weekYear>y){
                            if(amount<0){
                                amount++;
                            }
                            woy=max;
                        }else{
                            if(amount>0){
                                amount-=woy-max;
                            }
                            woy=min;
                        }
                    }
                    set(field,getRolledValue(woy,amount,min,max));
                    return;
                }
                // Handle cutover here.
                long fd=getCurrentFixedDate();
                BaseCalendar cal;
                if(gregorianCutoverYear==gregorianCutoverYearJulian){
                    cal=getCutoverCalendarSystem();
                }else if(y==gregorianCutoverYear){
                    cal=gcal;
                }else{
                    cal=getJulianCalendarSystem();
                }
                long day1=fd-(7*(woy-min));
                // Make sure that the min week has the current DAY_OF_WEEK
                if(cal.getYearFromFixedDate(day1)!=y){
                    min++;
                }
                // Make sure the same thing for the max week
                fd+=7*(max-woy);
                cal=(fd>=gregorianCutoverDate)?gcal:getJulianCalendarSystem();
                if(cal.getYearFromFixedDate(fd)!=y){
                    max--;
                }
                // value: the new WEEK_OF_YEAR which must be converted
                // to month and day of month.
                value=getRolledValue(woy,amount,min,max)-1;
                BaseCalendar.Date d=getCalendarDate(day1+value*7);
                set(MONTH,d.getMonth()-1);
                set(DAY_OF_MONTH,d.getDayOfMonth());
                return;
            }
            case WEEK_OF_MONTH:{
                boolean isCutoverYear=isCutoverYear(cdate.getNormalizedYear());
                // dow: relative day of week from first day of week
                int dow=internalGet(DAY_OF_WEEK)-getFirstDayOfWeek();
                if(dow<0){
                    dow+=7;
                }
                long fd=getCurrentFixedDate();
                long month1;     // fixed date of the first day (usually 1) of the month
                int monthLength; // actual month length
                if(isCutoverYear){
                    month1=getFixedDateMonth1(cdate,fd);
                    monthLength=actualMonthLength();
                }else{
                    month1=fd-internalGet(DAY_OF_MONTH)+1;
                    monthLength=calsys.getMonthLength(cdate);
                }
                // the first day of week of the month.
                long monthDay1st=BaseCalendar.getDayOfWeekDateOnOrBefore(month1+6,
                        getFirstDayOfWeek());
                // if the week has enough days to form a week, the
                // week starts from the previous month.
                if((int)(monthDay1st-month1)>=getMinimalDaysInFirstWeek()){
                    monthDay1st-=7;
                }
                max=getActualMaximum(field);
                // value: the new WEEK_OF_MONTH value
                int value=getRolledValue(internalGet(field),amount,1,max)-1;
                // nfd: fixed date of the rolled date
                long nfd=monthDay1st+value*7+dow;
                // Unlike WEEK_OF_YEAR, we need to change day of week if the
                // nfd is out of the month.
                if(nfd<month1){
                    nfd=month1;
                }else if(nfd>=(month1+monthLength)){
                    nfd=month1+monthLength-1;
                }
                int dayOfMonth;
                if(isCutoverYear){
                    // If we are in the cutover year, convert nfd to
                    // its calendar date and use dayOfMonth.
                    BaseCalendar.Date d=getCalendarDate(nfd);
                    dayOfMonth=d.getDayOfMonth();
                }else{
                    dayOfMonth=(int)(nfd-month1)+1;
                }
                set(DAY_OF_MONTH,dayOfMonth);
                return;
            }
            case DAY_OF_MONTH:{
                if(!isCutoverYear(cdate.getNormalizedYear())){
                    max=calsys.getMonthLength(cdate);
                    break;
                }
                // Cutover year handling
                long fd=getCurrentFixedDate();
                long month1=getFixedDateMonth1(cdate,fd);
                // It may not be a regular month. Convert the date and range to
                // the relative values, perform the roll, and
                // convert the result back to the rolled date.
                int value=getRolledValue((int)(fd-month1),amount,0,actualMonthLength()-1);
                BaseCalendar.Date d=getCalendarDate(month1+value);
                assert d.getMonth()-1==internalGet(MONTH);
                set(DAY_OF_MONTH,d.getDayOfMonth());
                return;
            }
            case DAY_OF_YEAR:{
                max=getActualMaximum(field);
                if(!isCutoverYear(cdate.getNormalizedYear())){
                    break;
                }
                // Handle cutover here.
                long fd=getCurrentFixedDate();
                long jan1=fd-internalGet(DAY_OF_YEAR)+1;
                int value=getRolledValue((int)(fd-jan1)+1,amount,min,max);
                BaseCalendar.Date d=getCalendarDate(jan1+value-1);
                set(MONTH,d.getMonth()-1);
                set(DAY_OF_MONTH,d.getDayOfMonth());
                return;
            }
            case DAY_OF_WEEK:{
                if(!isCutoverYear(cdate.getNormalizedYear())){
                    // If the week of year is in the same year, we can
                    // just change DAY_OF_WEEK.
                    int weekOfYear=internalGet(WEEK_OF_YEAR);
                    if(weekOfYear>1&&weekOfYear<52){
                        set(WEEK_OF_YEAR,weekOfYear); // update stamp[WEEK_OF_YEAR]
                        max=SATURDAY;
                        break;
                    }
                }
                // We need to handle it in a different way around year
                // boundaries and in the cutover year. Note that
                // changing era and year values violates the roll
                // rule: not changing larger calendar fields...
                amount%=7;
                if(amount==0){
                    return;
                }
                long fd=getCurrentFixedDate();
                long dowFirst=BaseCalendar.getDayOfWeekDateOnOrBefore(fd,getFirstDayOfWeek());
                fd+=amount;
                if(fd<dowFirst){
                    fd+=7;
                }else if(fd>=dowFirst+7){
                    fd-=7;
                }
                BaseCalendar.Date d=getCalendarDate(fd);
                set(ERA,(d.getNormalizedYear()<=0?BCE:CE));
                set(d.getYear(),d.getMonth()-1,d.getDayOfMonth());
                return;
            }
            case DAY_OF_WEEK_IN_MONTH:{
                min=1; // after normalized, min should be 1.
                if(!isCutoverYear(cdate.getNormalizedYear())){
                    int dom=internalGet(DAY_OF_MONTH);
                    int monthLength=calsys.getMonthLength(cdate);
                    int lastDays=monthLength%7;
                    max=monthLength/7;
                    int x=(dom-1)%7;
                    if(x<lastDays){
                        max++;
                    }
                    set(DAY_OF_WEEK,internalGet(DAY_OF_WEEK));
                    break;
                }
                // Cutover year handling
                long fd=getCurrentFixedDate();
                long month1=getFixedDateMonth1(cdate,fd);
                int monthLength=actualMonthLength();
                int lastDays=monthLength%7;
                max=monthLength/7;
                int x=(int)(fd-month1)%7;
                if(x<lastDays){
                    max++;
                }
                int value=getRolledValue(internalGet(field),amount,min,max)-1;
                fd=month1+value*7+x;
                BaseCalendar cal=(fd>=gregorianCutoverDate)?gcal:getJulianCalendarSystem();
                BaseCalendar.Date d=(BaseCalendar.Date)cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                cal.getCalendarDateFromFixedDate(d,fd);
                set(DAY_OF_MONTH,d.getDayOfMonth());
                return;
            }
        }
        set(field,getRolledValue(internalGet(field),amount,min,max));
    }    @Override
    public int getActualMinimum(int field){
        if(field==DAY_OF_MONTH){
            GregorianCalendar gc=getNormalizedCalendar();
            int year=gc.cdate.getNormalizedYear();
            if(year==gregorianCutoverYear||year==gregorianCutoverYearJulian){
                long month1=getFixedDateMonth1(gc.cdate,gc.calsys.getFixedDate(gc.cdate));
                BaseCalendar.Date d=getCalendarDate(month1);
                return d.getDayOfMonth();
            }
        }
        return getMinimum(field);
    }

    private void pinDayOfMonth(){
        int year=internalGet(YEAR);
        int monthLen;
        if(year>gregorianCutoverYear||year<gregorianCutoverYearJulian){
            monthLen=monthLength(internalGet(MONTH));
        }else{
            GregorianCalendar gc=getNormalizedCalendar();
            monthLen=gc.getActualMaximum(DAY_OF_MONTH);
        }
        int dom=internalGet(DAY_OF_MONTH);
        if(dom>monthLen){
            set(DAY_OF_MONTH,monthLen);
        }
    }

    private GregorianCalendar getNormalizedCalendar(){
        GregorianCalendar gc;
        if(isFullyNormalized()){
            gc=this;
        }else{
            // Create a clone and normalize the calendar fields
            gc=(GregorianCalendar)this.clone();
            gc.setLenient(true);
            gc.complete();
        }
        return gc;
    }    @Override
    public int getActualMaximum(int field){
        final int fieldsForFixedMax=ERA_MASK|DAY_OF_WEEK_MASK|HOUR_MASK|AM_PM_MASK|
                HOUR_OF_DAY_MASK|MINUTE_MASK|SECOND_MASK|MILLISECOND_MASK|
                ZONE_OFFSET_MASK|DST_OFFSET_MASK;
        if((fieldsForFixedMax&(1<<field))!=0){
            return getMaximum(field);
        }
        GregorianCalendar gc=getNormalizedCalendar();
        BaseCalendar.Date date=gc.cdate;
        BaseCalendar cal=gc.calsys;
        int normalizedYear=date.getNormalizedYear();
        int value=-1;
        switch(field){
            case MONTH:{
                if(!gc.isCutoverYear(normalizedYear)){
                    value=DECEMBER;
                    break;
                }
                // January 1 of the next year may or may not exist.
                long nextJan1;
                do{
                    nextJan1=gcal.getFixedDate(++normalizedYear,BaseCalendar.JANUARY,1,null);
                }while(nextJan1<gregorianCutoverDate);
                BaseCalendar.Date d=(BaseCalendar.Date)date.clone();
                cal.getCalendarDateFromFixedDate(d,nextJan1-1);
                value=d.getMonth()-1;
            }
            break;
            case DAY_OF_MONTH:{
                value=cal.getMonthLength(date);
                if(!gc.isCutoverYear(normalizedYear)||date.getDayOfMonth()==value){
                    break;
                }
                // Handle cutover year.
                long fd=gc.getCurrentFixedDate();
                if(fd>=gregorianCutoverDate){
                    break;
                }
                int monthLength=gc.actualMonthLength();
                long monthEnd=gc.getFixedDateMonth1(gc.cdate,fd)+monthLength-1;
                // Convert the fixed date to its calendar date.
                BaseCalendar.Date d=gc.getCalendarDate(monthEnd);
                value=d.getDayOfMonth();
            }
            break;
            case DAY_OF_YEAR:{
                if(!gc.isCutoverYear(normalizedYear)){
                    value=cal.getYearLength(date);
                    break;
                }
                // Handle cutover year.
                long jan1;
                if(gregorianCutoverYear==gregorianCutoverYearJulian){
                    BaseCalendar cocal=gc.getCutoverCalendarSystem();
                    jan1=cocal.getFixedDate(normalizedYear,1,1,null);
                }else if(normalizedYear==gregorianCutoverYearJulian){
                    jan1=cal.getFixedDate(normalizedYear,1,1,null);
                }else{
                    jan1=gregorianCutoverDate;
                }
                // January 1 of the next year may or may not exist.
                long nextJan1=gcal.getFixedDate(++normalizedYear,1,1,null);
                if(nextJan1<gregorianCutoverDate){
                    nextJan1=gregorianCutoverDate;
                }
                assert jan1<=cal.getFixedDate(date.getNormalizedYear(),date.getMonth(),
                        date.getDayOfMonth(),date);
                assert nextJan1>=cal.getFixedDate(date.getNormalizedYear(),date.getMonth(),
                        date.getDayOfMonth(),date);
                value=(int)(nextJan1-jan1);
            }
            break;
            case WEEK_OF_YEAR:{
                if(!gc.isCutoverYear(normalizedYear)){
                    // Get the day of week of January 1 of the year
                    CalendarDate d=cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d.setDate(date.getYear(),BaseCalendar.JANUARY,1);
                    int dayOfWeek=cal.getDayOfWeek(d);
                    // Normalize the day of week with the firstDayOfWeek value
                    dayOfWeek-=getFirstDayOfWeek();
                    if(dayOfWeek<0){
                        dayOfWeek+=7;
                    }
                    value=52;
                    int magic=dayOfWeek+getMinimalDaysInFirstWeek()-1;
                    if((magic==6)||
                            (date.isLeapYear()&&(magic==5||magic==12))){
                        value++;
                    }
                    break;
                }
                if(gc==this){
                    gc=(GregorianCalendar)gc.clone();
                }
                int maxDayOfYear=getActualMaximum(DAY_OF_YEAR);
                gc.set(DAY_OF_YEAR,maxDayOfYear);
                value=gc.get(WEEK_OF_YEAR);
                if(internalGet(YEAR)!=gc.getWeekYear()){
                    gc.set(DAY_OF_YEAR,maxDayOfYear-7);
                    value=gc.get(WEEK_OF_YEAR);
                }
            }
            break;
            case WEEK_OF_MONTH:{
                if(!gc.isCutoverYear(normalizedYear)){
                    CalendarDate d=cal.newCalendarDate(null);
                    d.setDate(date.getYear(),date.getMonth(),1);
                    int dayOfWeek=cal.getDayOfWeek(d);
                    int monthLength=cal.getMonthLength(d);
                    dayOfWeek-=getFirstDayOfWeek();
                    if(dayOfWeek<0){
                        dayOfWeek+=7;
                    }
                    int nDaysFirstWeek=7-dayOfWeek; // # of days in the first week
                    value=3;
                    if(nDaysFirstWeek>=getMinimalDaysInFirstWeek()){
                        value++;
                    }
                    monthLength-=nDaysFirstWeek+7*3;
                    if(monthLength>0){
                        value++;
                        if(monthLength>7){
                            value++;
                        }
                    }
                    break;
                }
                // Cutover year handling
                if(gc==this){
                    gc=(GregorianCalendar)gc.clone();
                }
                int y=gc.internalGet(YEAR);
                int m=gc.internalGet(MONTH);
                do{
                    value=gc.get(WEEK_OF_MONTH);
                    gc.add(WEEK_OF_MONTH,+1);
                }while(gc.get(YEAR)==y&&gc.get(MONTH)==m);
            }
            break;
            case DAY_OF_WEEK_IN_MONTH:{
                // may be in the Gregorian cutover month
                int ndays, dow1;
                int dow=date.getDayOfWeek();
                if(!gc.isCutoverYear(normalizedYear)){
                    BaseCalendar.Date d=(BaseCalendar.Date)date.clone();
                    ndays=cal.getMonthLength(d);
                    d.setDayOfMonth(1);
                    cal.normalize(d);
                    dow1=d.getDayOfWeek();
                }else{
                    // Let a cloned GregorianCalendar take care of the cutover cases.
                    if(gc==this){
                        gc=(GregorianCalendar)clone();
                    }
                    ndays=gc.actualMonthLength();
                    gc.set(DAY_OF_MONTH,gc.getActualMinimum(DAY_OF_MONTH));
                    dow1=gc.get(DAY_OF_WEEK);
                }
                int x=dow-dow1;
                if(x<0){
                    x+=7;
                }
                ndays-=x;
                value=(ndays+6)/7;
            }
            break;
            case YEAR:
                /** The year computation is no different, in principle, from the
                 * others, however, the range of possible maxima is large.  In
                 * addition, the way we know we've exceeded the range is different.
                 * For these reasons, we use the special case code below to handle
                 * this field.
                 *
                 * The actual maxima for YEAR depend on the type of calendar:
                 *
                 *     Gregorian = May 17, 292275056 BCE - Aug 17, 292278994 CE
                 *     Julian    = Dec  2, 292269055 BCE - Jan  3, 292272993 CE
                 *     Hybrid    = Dec  2, 292269055 BCE - Aug 17, 292278994 CE
                 *
                 * We know we've exceeded the maximum when either the month, date,
                 * time, or era changes in response to setting the year.  We don't
                 * check for month, date, and time here because the year and era are
                 * sufficient to detect an invalid year setting.  NOTE: If code is
                 * added to check the month and date in the future for some reason,
                 * Feb 29 must be allowed to shift to Mar 1 when setting the year.
                 */
            {
                if(gc==this){
                    gc=(GregorianCalendar)clone();
                }
                // Calculate the millisecond offset from the beginning
                // of the year of this calendar and adjust the max
                // year value if we are beyond the limit in the max
                // year.
                long current=gc.getYearOffsetInMillis();
                if(gc.internalGetEra()==CE){
                    gc.setTimeInMillis(Long.MAX_VALUE);
                    value=gc.get(YEAR);
                    long maxEnd=gc.getYearOffsetInMillis();
                    if(current>maxEnd){
                        value--;
                    }
                }else{
                    CalendarSystem mincal=gc.getTimeInMillis()>=gregorianCutover?
                            gcal:getJulianCalendarSystem();
                    CalendarDate d=mincal.getCalendarDate(Long.MIN_VALUE,getZone());
                    long maxEnd=(cal.getDayOfYear(d)-1)*24+d.getHours();
                    maxEnd*=60;
                    maxEnd+=d.getMinutes();
                    maxEnd*=60;
                    maxEnd+=d.getSeconds();
                    maxEnd*=1000;
                    maxEnd+=d.getMillis();
                    value=d.getYear();
                    if(value<=0){
                        assert mincal==gcal;
                        value=1-value;
                    }
                    if(current<maxEnd){
                        value--;
                    }
                }
            }
            break;
            default:
                throw new ArrayIndexOutOfBoundsException(field);
        }
        return value;
    }

    private int monthLength(int month){
        int year=internalGet(YEAR);
        if(internalGetEra()==BCE){
            year=1-year;
        }
        return monthLength(month,year);
    }

    private int monthLength(int month,int year){
        return isLeapYear(year)?LEAP_MONTH_LENGTH[month]:MONTH_LENGTH[month];
    }

    public boolean isLeapYear(int year){
        if((year&3)!=0){
            return false;
        }
        if(year>gregorianCutoverYear){
            return (year%100!=0)||(year%400==0); // Gregorian
        }
        if(year<gregorianCutoverYearJulian){
            return true; // Julian
        }
        boolean gregorian;
        // If the given year is the Gregorian cutover year, we need to
        // determine which calendar system to be applied to February in the year.
        if(gregorianCutoverYear==gregorianCutoverYearJulian){
            BaseCalendar.Date d=getCalendarDate(gregorianCutoverDate); // Gregorian
            gregorian=d.getMonth()<BaseCalendar.MARCH;
        }else{
            gregorian=year==gregorianCutoverYear;
        }
        return gregorian?(year%100!=0)||(year%400==0):true;
    }    @Override
    public Object clone(){
        GregorianCalendar other=(GregorianCalendar)super.clone();
        other.gdate=(BaseCalendar.Date)gdate.clone();
        if(cdate!=null){
            if(cdate!=gdate){
                other.cdate=(BaseCalendar.Date)cdate.clone();
            }else{
                other.cdate=other.gdate;
            }
        }
        other.originalFields=null;
        other.zoneOffsets=null;
        return other;
    }

    private long getCurrentFixedDate(){
        return (calsys==gcal)?cachedFixedDate:calsys.getFixedDate(cdate);
    }

    private int internalGetEra(){
        return isSet(ERA)?internalGet(ERA):CE;
    }    @Override
    public TimeZone getTimeZone(){
        TimeZone zone=super.getTimeZone();
        // To share the zone by CalendarDates
        gdate.setZone(zone);
        if(cdate!=null&&cdate!=gdate){
            cdate.setZone(zone);
        }
        return zone;
    }

    private long getFixedDate(BaseCalendar cal,int year,int fieldMask){
        int month=JANUARY;
        if(isFieldSet(fieldMask,MONTH)){
            // No need to check if MONTH has been set (no isSet(MONTH)
            // call) since its unset value happens to be JANUARY (0).
            month=internalGet(MONTH);
            // If the month is out of range, adjust it into range
            if(month>DECEMBER){
                year+=month/12;
                month%=12;
            }else if(month<JANUARY){
                int[] rem=new int[1];
                year+=CalendarUtils.floorDivide(month,12,rem);
                month=rem[0];
            }
        }
        // Get the fixed date since Jan 1, 1 (Gregorian). We are on
        // the first day of either `month' or January in 'year'.
        long fixedDate=cal.getFixedDate(year,month+1,1,
                cal==gcal?gdate:null);
        if(isFieldSet(fieldMask,MONTH)){
            // Month-based calculations
            if(isFieldSet(fieldMask,DAY_OF_MONTH)){
                // We are on the first day of the month. Just add the
                // offset if DAY_OF_MONTH is set. If the isSet call
                // returns false, that means DAY_OF_MONTH has been
                // selected just because of the selected
                // combination. We don't need to add any since the
                // default value is the 1st.
                if(isSet(DAY_OF_MONTH)){
                    // To avoid underflow with DAY_OF_MONTH-1, add
                    // DAY_OF_MONTH, then subtract 1.
                    fixedDate+=internalGet(DAY_OF_MONTH);
                    fixedDate--;
                }
            }else{
                if(isFieldSet(fieldMask,WEEK_OF_MONTH)){
                    long firstDayOfWeek=BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate+6,
                            getFirstDayOfWeek());
                    // If we have enough days in the first week, then
                    // move to the previous week.
                    if((firstDayOfWeek-fixedDate)>=getMinimalDaysInFirstWeek()){
                        firstDayOfWeek-=7;
                    }
                    if(isFieldSet(fieldMask,DAY_OF_WEEK)){
                        firstDayOfWeek=BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek+6,
                                internalGet(DAY_OF_WEEK));
                    }
                    // In lenient mode, we treat days of the previous
                    // months as a part of the specified
                    // WEEK_OF_MONTH. See 4633646.
                    fixedDate=firstDayOfWeek+7*(internalGet(WEEK_OF_MONTH)-1);
                }else{
                    int dayOfWeek;
                    if(isFieldSet(fieldMask,DAY_OF_WEEK)){
                        dayOfWeek=internalGet(DAY_OF_WEEK);
                    }else{
                        dayOfWeek=getFirstDayOfWeek();
                    }
                    // We are basing this on the day-of-week-in-month.  The only
                    // trickiness occurs if the day-of-week-in-month is
                    // negative.
                    int dowim;
                    if(isFieldSet(fieldMask,DAY_OF_WEEK_IN_MONTH)){
                        dowim=internalGet(DAY_OF_WEEK_IN_MONTH);
                    }else{
                        dowim=1;
                    }
                    if(dowim>=0){
                        fixedDate=BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate+(7*dowim)-1,
                                dayOfWeek);
                    }else{
                        // Go to the first day of the next week of
                        // the specified week boundary.
                        int lastDate=monthLength(month,year)+(7*(dowim+1));
                        // Then, get the day of week date on or before the last date.
                        fixedDate=BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate+lastDate-1,
                                dayOfWeek);
                    }
                }
            }
        }else{
            if(year==gregorianCutoverYear&&cal==gcal
                    &&fixedDate<gregorianCutoverDate
                    &&gregorianCutoverYear!=gregorianCutoverYearJulian){
                // January 1 of the year doesn't exist.  Use
                // gregorianCutoverDate as the first day of the
                // year.
                fixedDate=gregorianCutoverDate;
            }
            // We are on the first day of the year.
            if(isFieldSet(fieldMask,DAY_OF_YEAR)){
                // Add the offset, then subtract 1. (Make sure to avoid underflow.)
                fixedDate+=internalGet(DAY_OF_YEAR);
                fixedDate--;
            }else{
                long firstDayOfWeek=BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate+6,
                        getFirstDayOfWeek());
                // If we have enough days in the first week, then move
                // to the previous week.
                if((firstDayOfWeek-fixedDate)>=getMinimalDaysInFirstWeek()){
                    firstDayOfWeek-=7;
                }
                if(isFieldSet(fieldMask,DAY_OF_WEEK)){
                    int dayOfWeek=internalGet(DAY_OF_WEEK);
                    if(dayOfWeek!=getFirstDayOfWeek()){
                        firstDayOfWeek=BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek+6,
                                dayOfWeek);
                    }
                }
                fixedDate=firstDayOfWeek+7*((long)internalGet(WEEK_OF_YEAR)-1);
            }
        }
        return fixedDate;
    }

    private BaseCalendar getCutoverCalendarSystem(){
        if(gregorianCutoverYearJulian<gregorianCutoverYear){
            return gcal;
        }
        return getJulianCalendarSystem();
    }    @Override
    public void setTimeZone(TimeZone zone){
        super.setTimeZone(zone);
        // To share the zone by CalendarDates
        gdate.setZone(zone);
        if(cdate!=null&&cdate!=gdate){
            cdate.setZone(zone);
        }
    }

    private boolean isCutoverYear(int normalizedYear){
        int cutoverYear=(calsys==gcal)?gregorianCutoverYear:gregorianCutoverYearJulian;
        return normalizedYear==cutoverYear;
    }

    private long getFixedDateJan1(BaseCalendar.Date date,long fixedDate){
        assert date.getNormalizedYear()==gregorianCutoverYear||
                date.getNormalizedYear()==gregorianCutoverYearJulian;
        if(gregorianCutoverYear!=gregorianCutoverYearJulian){
            if(fixedDate>=gregorianCutoverDate){
                // Dates before the cutover date don't exist
                // in the same (Gregorian) year. So, no
                // January 1 exists in the year. Use the
                // cutover date as the first day of the year.
                return gregorianCutoverDate;
            }
        }
        // January 1 of the normalized year should exist.
        BaseCalendar juliancal=getJulianCalendarSystem();
        return juliancal.getFixedDate(date.getNormalizedYear(),BaseCalendar.JANUARY,1,null);
    }    @Override
    public final boolean isWeekDateSupported(){
        return true;
    }

    private long getFixedDateMonth1(BaseCalendar.Date date,long fixedDate){
        assert date.getNormalizedYear()==gregorianCutoverYear||
                date.getNormalizedYear()==gregorianCutoverYearJulian;
        BaseCalendar.Date gCutover=getGregorianCutoverDate();
        if(gCutover.getMonth()==BaseCalendar.JANUARY
                &&gCutover.getDayOfMonth()==1){
            // The cutover happened on January 1.
            return fixedDate-date.getDayOfMonth()+1;
        }
        long fixedDateMonth1;
        // The cutover happened sometime during the year.
        if(date.getMonth()==gCutover.getMonth()){
            // The cutover happened in the month.
            BaseCalendar.Date jLastDate=getLastJulianDate();
            if(gregorianCutoverYear==gregorianCutoverYearJulian
                    &&gCutover.getMonth()==jLastDate.getMonth()){
                // The "gap" fits in the same month.
                fixedDateMonth1=jcal.getFixedDate(date.getNormalizedYear(),
                        date.getMonth(),
                        1,
                        null);
            }else{
                // Use the cutover date as the first day of the month.
                fixedDateMonth1=gregorianCutoverDate;
            }
        }else{
            // The cutover happened before the month.
            fixedDateMonth1=fixedDate-date.getDayOfMonth()+1;
        }
        return fixedDateMonth1;
    }

    private BaseCalendar.Date getLastJulianDate(){
        return getCalendarDate(gregorianCutoverDate-1);
    }    @Override
    public int getWeekYear(){
        int year=get(YEAR); // implicitly calls complete()
        if(internalGetEra()==BCE){
            year=1-year;
        }
        // Fast path for the Gregorian calendar years that are never
        // affected by the Julian-Gregorian transition
        if(year>gregorianCutoverYear+1){
            int weekOfYear=internalGet(WEEK_OF_YEAR);
            if(internalGet(MONTH)==JANUARY){
                if(weekOfYear>=52){
                    --year;
                }
            }else{
                if(weekOfYear==1){
                    ++year;
                }
            }
            return year;
        }
        // General (slow) path
        int dayOfYear=internalGet(DAY_OF_YEAR);
        int maxDayOfYear=getActualMaximum(DAY_OF_YEAR);
        int minimalDays=getMinimalDaysInFirstWeek();
        // Quickly check the possibility of year adjustments before
        // cloning this GregorianCalendar.
        if(dayOfYear>minimalDays&&dayOfYear<(maxDayOfYear-6)){
            return year;
        }
        // Create a clone to work on the calculation
        GregorianCalendar cal=(GregorianCalendar)clone();
        cal.setLenient(true);
        // Use GMT so that intermediate date calculations won't
        // affect the time of day fields.
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Go to the first day of the year, which is usually January 1.
        cal.set(DAY_OF_YEAR,1);
        cal.complete();
        // Get the first day of the first day-of-week in the year.
        int delta=getFirstDayOfWeek()-cal.get(DAY_OF_WEEK);
        if(delta!=0){
            if(delta<0){
                delta+=7;
            }
            cal.add(DAY_OF_YEAR,delta);
        }
        int minDayOfYear=cal.get(DAY_OF_YEAR);
        if(dayOfYear<minDayOfYear){
            if(minDayOfYear<=minimalDays){
                --year;
            }
        }else{
            cal.set(YEAR,year+1);
            cal.set(DAY_OF_YEAR,1);
            cal.complete();
            int del=getFirstDayOfWeek()-cal.get(DAY_OF_WEEK);
            if(del!=0){
                if(del<0){
                    del+=7;
                }
                cal.add(DAY_OF_YEAR,del);
            }
            minDayOfYear=cal.get(DAY_OF_YEAR)-1;
            if(minDayOfYear==0){
                minDayOfYear=7;
            }
            if(minDayOfYear>=minimalDays){
                int days=maxDayOfYear-dayOfYear+1;
                if(days<=(7-minDayOfYear)){
                    ++year;
                }
            }
        }
        return year;
    }

    private int actualMonthLength(){
        int year=cdate.getNormalizedYear();
        if(year!=gregorianCutoverYear&&year!=gregorianCutoverYearJulian){
            return calsys.getMonthLength(cdate);
        }
        BaseCalendar.Date date=(BaseCalendar.Date)cdate.clone();
        long fd=calsys.getFixedDate(date);
        long month1=getFixedDateMonth1(date,fd);
        long next1=month1+calsys.getMonthLength(date);
        if(next1<gregorianCutoverDate){
            return (int)(next1-month1);
        }
        if(cdate!=gdate){
            date=(BaseCalendar.Date)gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        }
        gcal.getCalendarDateFromFixedDate(date,next1);
        next1=getFixedDateMonth1(date,next1);
        return (int)(next1-month1);
    }

    private int yearLength(){
        int year=internalGet(YEAR);
        if(internalGetEra()==BCE){
            year=1-year;
        }
        return yearLength(year);
    }    @Override
    public void setWeekDate(int weekYear,int weekOfYear,int dayOfWeek){
        if(dayOfWeek<SUNDAY||dayOfWeek>SATURDAY){
            throw new IllegalArgumentException("invalid dayOfWeek: "+dayOfWeek);
        }
        // To avoid changing the time of day fields by date
        // calculations, use a clone with the GMT time zone.
        GregorianCalendar gc=(GregorianCalendar)clone();
        gc.setLenient(true);
        int era=gc.get(ERA);
        gc.clear();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.set(ERA,era);
        gc.set(YEAR,weekYear);
        gc.set(WEEK_OF_YEAR,1);
        gc.set(DAY_OF_WEEK,getFirstDayOfWeek());
        int days=dayOfWeek-getFirstDayOfWeek();
        if(days<0){
            days+=7;
        }
        days+=7*(weekOfYear-1);
        if(days!=0){
            gc.add(DAY_OF_YEAR,days);
        }else{
            gc.complete();
        }
        if(!isLenient()&&
                (gc.getWeekYear()!=weekYear
                        ||gc.internalGet(WEEK_OF_YEAR)!=weekOfYear
                        ||gc.internalGet(DAY_OF_WEEK)!=dayOfWeek)){
            throw new IllegalArgumentException();
        }
        set(ERA,gc.internalGet(ERA));
        set(YEAR,gc.internalGet(YEAR));
        set(MONTH,gc.internalGet(MONTH));
        set(DAY_OF_MONTH,gc.internalGet(DAY_OF_MONTH));
        // to avoid throwing an IllegalArgumentException in
        // non-lenient, set WEEK_OF_YEAR internally
        internalSet(WEEK_OF_YEAR,weekOfYear);
        complete();
    }

    private int yearLength(int year){
        return isLeapYear(year)?366:365;
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException{
        stream.defaultReadObject();
        if(gdate==null){
            gdate=(BaseCalendar.Date)gcal.newCalendarDate(getZone());
            cachedFixedDate=Long.MIN_VALUE;
        }
        setGregorianChange(gregorianCutover);
    }    @Override
    public int getWeeksInWeekYear(){
        GregorianCalendar gc=getNormalizedCalendar();
        int weekYear=gc.getWeekYear();
        if(weekYear==gc.internalGet(YEAR)){
            return gc.getActualMaximum(WEEK_OF_YEAR);
        }
        // Use the 2nd week for calculating the max of WEEK_OF_YEAR
        if(gc==this){
            gc=(GregorianCalendar)gc.clone();
        }
        gc.setWeekDate(weekYear,2,internalGet(DAY_OF_WEEK));
        return gc.getActualMaximum(WEEK_OF_YEAR);
    }

    public ZonedDateTime toZonedDateTime(){
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getTimeInMillis()),
                getTimeZone().toZoneId());
    }


























}

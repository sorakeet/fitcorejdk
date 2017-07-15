/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * * Neither the name of JSR-310 nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java.time.format;

import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.temporal.ChronoField.*;

class DateTimeTextProvider{
    private static final ConcurrentMap<Entry<TemporalField,Locale>,Object> CACHE=new ConcurrentHashMap<>(16,0.75f,2);
    private static final Comparator<Entry<String,Long>> COMPARATOR=new Comparator<Entry<String,Long>>(){
        @Override
        public int compare(Entry<String,Long> obj1,Entry<String,Long> obj2){
            return obj2.getKey().length()-obj1.getKey().length();  // longest to shortest
        }
    };

    DateTimeTextProvider(){
    }

    static DateTimeTextProvider getInstance(){
        return new DateTimeTextProvider();
    }

    public String getText(Chronology chrono,TemporalField field,long value,
                          TextStyle style,Locale locale){
        if(chrono==IsoChronology.INSTANCE
                ||!(field instanceof ChronoField)){
            return getText(field,value,style,locale);
        }
        int fieldIndex;
        int fieldValue;
        if(field==ERA){
            fieldIndex=Calendar.ERA;
            if(chrono==JapaneseChronology.INSTANCE){
                if(value==-999){
                    fieldValue=0;
                }else{
                    fieldValue=(int)value+2;
                }
            }else{
                fieldValue=(int)value;
            }
        }else if(field==MONTH_OF_YEAR){
            fieldIndex=Calendar.MONTH;
            fieldValue=(int)value-1;
        }else if(field==DAY_OF_WEEK){
            fieldIndex=Calendar.DAY_OF_WEEK;
            fieldValue=(int)value+1;
            if(fieldValue>7){
                fieldValue=Calendar.SUNDAY;
            }
        }else if(field==AMPM_OF_DAY){
            fieldIndex=Calendar.AM_PM;
            fieldValue=(int)value;
        }else{
            return null;
        }
        return CalendarDataUtility.retrieveJavaTimeFieldValueName(
                chrono.getCalendarType(),fieldIndex,fieldValue,style.toCalendarStyle(),locale);
    }

    public String getText(TemporalField field,long value,TextStyle style,Locale locale){
        Object store=findStore(field,locale);
        if(store instanceof LocaleStore){
            return ((LocaleStore)store).getText(value,style);
        }
        return null;
    }

    private Object findStore(TemporalField field,Locale locale){
        Entry<TemporalField,Locale> key=createEntry(field,locale);
        Object store=CACHE.get(key);
        if(store==null){
            store=createStore(field,locale);
            CACHE.putIfAbsent(key,store);
            store=CACHE.get(key);
        }
        return store;
    }

    private Object createStore(TemporalField field,Locale locale){
        Map<TextStyle,Map<Long,String>> styleMap=new HashMap<>();
        if(field==ERA){
            for(TextStyle textStyle : TextStyle.values()){
                if(textStyle.isStandalone()){
                    // Stand-alone isn't applicable to era names.
                    continue;
                }
                Map<String,Integer> displayNames=CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory",Calendar.ERA,textStyle.toCalendarStyle(),locale);
                if(displayNames!=null){
                    Map<Long,String> map=new HashMap<>();
                    for(Entry<String,Integer> entry : displayNames.entrySet()){
                        map.put((long)entry.getValue(),entry.getKey());
                    }
                    if(!map.isEmpty()){
                        styleMap.put(textStyle,map);
                    }
                }
            }
            return new LocaleStore(styleMap);
        }
        if(field==MONTH_OF_YEAR){
            for(TextStyle textStyle : TextStyle.values()){
                Map<String,Integer> displayNames=CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory",Calendar.MONTH,textStyle.toCalendarStyle(),locale);
                Map<Long,String> map=new HashMap<>();
                if(displayNames!=null){
                    for(Entry<String,Integer> entry : displayNames.entrySet()){
                        map.put((long)(entry.getValue()+1),entry.getKey());
                    }
                }else{
                    // Narrow names may have duplicated names, such as "J" for January, Jun, July.
                    // Get names one by one in that case.
                    for(int month=Calendar.JANUARY;month<=Calendar.DECEMBER;month++){
                        String name;
                        name=CalendarDataUtility.retrieveJavaTimeFieldValueName(
                                "gregory",Calendar.MONTH,month,textStyle.toCalendarStyle(),locale);
                        if(name==null){
                            break;
                        }
                        map.put((long)(month+1),name);
                    }
                }
                if(!map.isEmpty()){
                    styleMap.put(textStyle,map);
                }
            }
            return new LocaleStore(styleMap);
        }
        if(field==DAY_OF_WEEK){
            for(TextStyle textStyle : TextStyle.values()){
                Map<String,Integer> displayNames=CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory",Calendar.DAY_OF_WEEK,textStyle.toCalendarStyle(),locale);
                Map<Long,String> map=new HashMap<>();
                if(displayNames!=null){
                    for(Entry<String,Integer> entry : displayNames.entrySet()){
                        map.put((long)toWeekDay(entry.getValue()),entry.getKey());
                    }
                }else{
                    // Narrow names may have duplicated names, such as "S" for Sunday and Saturday.
                    // Get names one by one in that case.
                    for(int wday=Calendar.SUNDAY;wday<=Calendar.SATURDAY;wday++){
                        String name;
                        name=CalendarDataUtility.retrieveJavaTimeFieldValueName(
                                "gregory",Calendar.DAY_OF_WEEK,wday,textStyle.toCalendarStyle(),locale);
                        if(name==null){
                            break;
                        }
                        map.put((long)toWeekDay(wday),name);
                    }
                }
                if(!map.isEmpty()){
                    styleMap.put(textStyle,map);
                }
            }
            return new LocaleStore(styleMap);
        }
        if(field==AMPM_OF_DAY){
            for(TextStyle textStyle : TextStyle.values()){
                if(textStyle.isStandalone()){
                    // Stand-alone isn't applicable to AM/PM.
                    continue;
                }
                Map<String,Integer> displayNames=CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                        "gregory",Calendar.AM_PM,textStyle.toCalendarStyle(),locale);
                if(displayNames!=null){
                    Map<Long,String> map=new HashMap<>();
                    for(Entry<String,Integer> entry : displayNames.entrySet()){
                        map.put((long)entry.getValue(),entry.getKey());
                    }
                    if(!map.isEmpty()){
                        styleMap.put(textStyle,map);
                    }
                }
            }
            return new LocaleStore(styleMap);
        }
        if(field==IsoFields.QUARTER_OF_YEAR){
            // The order of keys must correspond to the TextStyle.values() order.
            final String[] keys={
                    "QuarterNames",
                    "standalone.QuarterNames",
                    "QuarterAbbreviations",
                    "standalone.QuarterAbbreviations",
                    "QuarterNarrows",
                    "standalone.QuarterNarrows",
            };
            for(int i=0;i<keys.length;i++){
                String[] names=getLocalizedResource(keys[i],locale);
                if(names!=null){
                    Map<Long,String> map=new HashMap<>();
                    for(int q=0;q<names.length;q++){
                        map.put((long)(q+1),names[q]);
                    }
                    styleMap.put(TextStyle.values()[i],map);
                }
            }
            return new LocaleStore(styleMap);
        }
        return "";  // null marker for map
    }

    private static int toWeekDay(int calWeekDay){
        if(calWeekDay==Calendar.SUNDAY){
            return 7;
        }else{
            return calWeekDay-1;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T getLocalizedResource(String key,Locale locale){
        LocaleResources lr=LocaleProviderAdapter.getResourceBundleBased()
                .getLocaleResources(locale);
        ResourceBundle rb=lr.getJavaTimeFormatData();
        return rb.containsKey(key)?(T)rb.getObject(key):null;
    }

    private static <A,B> Entry<A,B> createEntry(A text,B field){
        return new SimpleImmutableEntry<>(text,field);
    }

    public Iterator<Entry<String,Long>> getTextIterator(Chronology chrono,TemporalField field,
                                                        TextStyle style,Locale locale){
        if(chrono==IsoChronology.INSTANCE
                ||!(field instanceof ChronoField)){
            return getTextIterator(field,style,locale);
        }
        int fieldIndex;
        switch((ChronoField)field){
            case ERA:
                fieldIndex=Calendar.ERA;
                break;
            case MONTH_OF_YEAR:
                fieldIndex=Calendar.MONTH;
                break;
            case DAY_OF_WEEK:
                fieldIndex=Calendar.DAY_OF_WEEK;
                break;
            case AMPM_OF_DAY:
                fieldIndex=Calendar.AM_PM;
                break;
            default:
                return null;
        }
        int calendarStyle=(style==null)?Calendar.ALL_STYLES:style.toCalendarStyle();
        Map<String,Integer> map=CalendarDataUtility.retrieveJavaTimeFieldValueNames(
                chrono.getCalendarType(),fieldIndex,calendarStyle,locale);
        if(map==null){
            return null;
        }
        List<Entry<String,Long>> list=new ArrayList<>(map.size());
        switch(fieldIndex){
            case Calendar.ERA:
                for(Entry<String,Integer> entry : map.entrySet()){
                    int era=entry.getValue();
                    if(chrono==JapaneseChronology.INSTANCE){
                        if(era==0){
                            era=-999;
                        }else{
                            era-=2;
                        }
                    }
                    list.add(createEntry(entry.getKey(),(long)era));
                }
                break;
            case Calendar.MONTH:
                for(Entry<String,Integer> entry : map.entrySet()){
                    list.add(createEntry(entry.getKey(),(long)(entry.getValue()+1)));
                }
                break;
            case Calendar.DAY_OF_WEEK:
                for(Entry<String,Integer> entry : map.entrySet()){
                    list.add(createEntry(entry.getKey(),(long)toWeekDay(entry.getValue())));
                }
                break;
            default:
                for(Entry<String,Integer> entry : map.entrySet()){
                    list.add(createEntry(entry.getKey(),(long)entry.getValue()));
                }
                break;
        }
        return list.iterator();
    }

    public Iterator<Entry<String,Long>> getTextIterator(TemporalField field,TextStyle style,Locale locale){
        Object store=findStore(field,locale);
        if(store instanceof LocaleStore){
            return ((LocaleStore)store).getTextIterator(style);
        }
        return null;
    }

    static final class LocaleStore{
        private final Map<TextStyle,Map<Long,String>> valueTextMap;
        private final Map<TextStyle,List<Entry<String,Long>>> parsable;

        LocaleStore(Map<TextStyle,Map<Long,String>> valueTextMap){
            this.valueTextMap=valueTextMap;
            Map<TextStyle,List<Entry<String,Long>>> map=new HashMap<>();
            List<Entry<String,Long>> allList=new ArrayList<>();
            for(Entry<TextStyle,Map<Long,String>> vtmEntry : valueTextMap.entrySet()){
                Map<String,Entry<String,Long>> reverse=new HashMap<>();
                for(Entry<Long,String> entry : vtmEntry.getValue().entrySet()){
                    if(reverse.put(entry.getValue(),createEntry(entry.getValue(),entry.getKey()))!=null){
                        // TODO: BUG: this has no effect
                        continue;  // not parsable, try next style
                    }
                }
                List<Entry<String,Long>> list=new ArrayList<>(reverse.values());
                Collections.sort(list,COMPARATOR);
                map.put(vtmEntry.getKey(),list);
                allList.addAll(list);
                map.put(null,allList);
            }
            Collections.sort(allList,COMPARATOR);
            this.parsable=map;
        }

        String getText(long value,TextStyle style){
            Map<Long,String> map=valueTextMap.get(style);
            return map!=null?map.get(value):null;
        }

        Iterator<Entry<String,Long>> getTextIterator(TextStyle style){
            List<Entry<String,Long>> list=parsable.get(style);
            return list!=null?list.iterator():null;
        }
    }
}

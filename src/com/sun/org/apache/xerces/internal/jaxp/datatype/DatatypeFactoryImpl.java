/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * Copyright 2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.jaxp.datatype;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;

public class DatatypeFactoryImpl
        extends DatatypeFactory{
    public DatatypeFactoryImpl(){
    }

    public Duration newDuration(final String lexicalRepresentation){
        return new DurationImpl(lexicalRepresentation);
    }

    public Duration newDuration(final long durationInMilliseconds){
        return new DurationImpl(durationInMilliseconds);
    }

    public Duration newDuration(
            final boolean isPositive,
            final BigInteger years,
            final BigInteger months,
            final BigInteger days,
            final BigInteger hours,
            final BigInteger minutes,
            final BigDecimal seconds){
        return new DurationImpl(
                isPositive,
                years,
                months,
                days,
                hours,
                minutes,
                seconds
        );
    }

    public Duration newDurationDayTime(final String lexicalRepresentation){
        // lexicalRepresentation must be non-null
        if(lexicalRepresentation==null){
            throw new NullPointerException(
                    "Trying to create an xdt:dayTimeDuration with an invalid"
                            +" lexical representation of \"null\"");
        }
        return new DurationDayTimeImpl(lexicalRepresentation);
    }

    public Duration newDurationDayTime(final long durationInMilliseconds){
        return new DurationDayTimeImpl(durationInMilliseconds);
    }

    public Duration newDurationDayTime(
            final boolean isPositive,
            final BigInteger day,
            final BigInteger hour,
            final BigInteger minute,
            final BigInteger second){
        return new DurationDayTimeImpl(
                isPositive,
                day,
                hour,
                minute,
                (second!=null)?new BigDecimal(second):null
        );
    }

    public Duration newDurationDayTime(
            final boolean isPositive,
            final int day,
            final int hour,
            final int minute,
            final int second){
        return new DurationDayTimeImpl(
                isPositive,
                day,
                hour,
                minute,
                second
        );
    }

    public Duration newDurationYearMonth(
            final String lexicalRepresentation){
        return new DurationYearMonthImpl(lexicalRepresentation);
    }

    public Duration newDurationYearMonth(
            final long durationInMilliseconds){
        return new DurationYearMonthImpl(durationInMilliseconds);
    }

    public Duration newDurationYearMonth(
            final boolean isPositive,
            final BigInteger year,
            final BigInteger month){
        return new DurationYearMonthImpl(
                isPositive,
                year,
                month
        );
    }

    @Override
    public Duration newDurationYearMonth(
            final boolean isPositive,
            final int year,
            final int month){
        return new DurationYearMonthImpl(
                isPositive,
                year,
                month);
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(){
        return new XMLGregorianCalendarImpl();
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(final String lexicalRepresentation){
        return new XMLGregorianCalendarImpl(lexicalRepresentation);
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(final GregorianCalendar cal){
        return new XMLGregorianCalendarImpl(cal);
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(
            final BigInteger year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final BigDecimal fractionalSecond,
            final int timezone){
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hour,
                minute,
                second,
                fractionalSecond,
                timezone
        );
    }
}

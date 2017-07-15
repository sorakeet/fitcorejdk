/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public class StatisticMonitoredAttribute extends MonitoredAttributeBase{
    // Every StatisticMonitoredAttribute will have a StatisticAccumulator. User
    // will use Statisticsaccumulator to accumulate the samples associated with
    // this Monitored Attribute
    private StatisticsAccumulator statisticsAccumulator;
    // Mutex is passed from the user class which is providing the sample values.
    // getValue() and clearState() is synchronized on this user provided mutex
    private Object mutex;
    ///////////////////////////////////////
    // operations

    public StatisticMonitoredAttribute(String name,String desc,
                                       StatisticsAccumulator s,Object mutex){
        super(name);
        MonitoredAttributeInfoFactory f=
                MonitoringFactories.getMonitoredAttributeInfoFactory();
        MonitoredAttributeInfo maInfo=f.createMonitoredAttributeInfo(
                desc,String.class,false,true);
        this.setMonitoredAttributeInfo(maInfo);
        this.statisticsAccumulator=s;
        this.mutex=mutex;
    } // end StatisticMonitoredAttribute

    public void clearState(){
        synchronized(mutex){
            statisticsAccumulator.clearState();
        }
    }

    public Object getValue(){
        synchronized(mutex){
            return statisticsAccumulator.getValue();
        }
    }

    public StatisticsAccumulator getStatisticsAccumulator(){
        return statisticsAccumulator;
    }
} // end StatisticMonitoredAttribute

/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

import sun.management.MemoryNotifInfoCompositeData;

import javax.management.openmbean.CompositeData;

public class MemoryNotificationInfo{
    public static final String MEMORY_THRESHOLD_EXCEEDED=
            "java.management.memory.threshold.exceeded";
    public static final String MEMORY_COLLECTION_THRESHOLD_EXCEEDED=
            "java.management.memory.collection.threshold.exceeded";
    private final String poolName;
    private final MemoryUsage usage;
    private final long count;

    public MemoryNotificationInfo(String poolName,
                                  MemoryUsage usage,
                                  long count){
        if(poolName==null){
            throw new NullPointerException("Null poolName");
        }
        if(usage==null){
            throw new NullPointerException("Null usage");
        }
        this.poolName=poolName;
        this.usage=usage;
        this.count=count;
    }

    MemoryNotificationInfo(CompositeData cd){
        MemoryNotifInfoCompositeData.validateCompositeData(cd);
        this.poolName=MemoryNotifInfoCompositeData.getPoolName(cd);
        this.usage=MemoryNotifInfoCompositeData.getUsage(cd);
        this.count=MemoryNotifInfoCompositeData.getCount(cd);
    }

    public static MemoryNotificationInfo from(CompositeData cd){
        if(cd==null){
            return null;
        }
        if(cd instanceof MemoryNotifInfoCompositeData){
            return ((MemoryNotifInfoCompositeData)cd).getMemoryNotifInfo();
        }else{
            return new MemoryNotificationInfo(cd);
        }
    }

    public String getPoolName(){
        return poolName;
    }

    public MemoryUsage getUsage(){
        return usage;
    }

    public long getCount(){
        return count;
    }
}

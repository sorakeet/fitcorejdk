/**
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;

public final class UID implements Serializable{
    private static final Object lock=new Object();
    private static final long serialVersionUID=1086053664494604050L;
    private static int hostUnique;
    private static boolean hostUniqueSet=false;
    private static long lastTime=System.currentTimeMillis();
    private static short lastCount=Short.MIN_VALUE;
    private final int unique;
    private final long time;
    private final short count;

    public UID(){
        synchronized(lock){
            if(!hostUniqueSet){
                hostUnique=(new SecureRandom()).nextInt();
                hostUniqueSet=true;
            }
            unique=hostUnique;
            if(lastCount==Short.MAX_VALUE){
                boolean interrupted=Thread.interrupted();
                boolean done=false;
                while(!done){
                    long now=System.currentTimeMillis();
                    if(now==lastTime){
                        // wait for time to change
                        try{
                            Thread.sleep(1);
                        }catch(InterruptedException e){
                            interrupted=true;
                        }
                    }else{
                        // If system time has gone backwards increase
                        // original by 1ms to maintain uniqueness
                        lastTime=(now<lastTime)?lastTime+1:now;
                        lastCount=Short.MIN_VALUE;
                        done=true;
                    }
                }
                if(interrupted){
                    Thread.currentThread().interrupt();
                }
            }
            time=lastTime;
            count=lastCount++;
        }
    }

    public UID(short num){
        unique=0;
        time=0;
        count=num;
    }

    private UID(int unique,long time,short count){
        this.unique=unique;
        this.time=time;
        this.count=count;
    }

    public static UID read(DataInput in) throws IOException{
        int unique=in.readInt();
        long time=in.readLong();
        short count=in.readShort();
        return new UID(unique,time,count);
    }

    public int hashCode(){
        return (int)time+(int)count;
    }

    public boolean equals(Object obj){
        if(obj instanceof UID){
            UID uid=(UID)obj;
            return (unique==uid.unique&&
                    count==uid.count&&
                    time==uid.time);
        }else{
            return false;
        }
    }

    public String toString(){
        return Integer.toString(unique,16)+":"+
                Long.toString(time,16)+":"+
                Integer.toString(count,16);
    }

    public void write(DataOutput out) throws IOException{
        out.writeInt(unique);
        out.writeLong(time);
        out.writeShort(count);
    }
}

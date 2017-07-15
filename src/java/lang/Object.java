/**
 * Copyright (c) 1994, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public class Object{
    static{
        registerNatives();
    }

    private static native void registerNatives();

    /*
    * 本身作用域 this 和被比较的对象
    * */
    public boolean equals(Object obj){
        return (this==obj);
    }

    protected native Object clone() throws CloneNotSupportedException;

    /*
    * 本身作用域 this 调用一些当前方法
    * */
    public String toString(){
        //return this.getClass().getName()+"@"+Integer.toHexString(this.hashCode());
        return getClass().getName()+"@"+Integer.toHexString(hashCode());
    }

    public final native Class<?> getClass();

    public native int hashCode();

    public final native void notify();

    public final native void notifyAll();

    public final void wait(long timeout,int nanos) throws InterruptedException{
        if(timeout<0){
            throw new IllegalArgumentException("超时时间值是负的");
        }
        if(nanos<0||nanos>999999){
            throw new IllegalArgumentException("纳秒超时时间值的范围无效");
        }
        if(nanos>0){
            timeout++;
        }
        wait(timeout);
    }

    public final native void wait(long timeout) throws InterruptedException;

    public final void wait() throws InterruptedException{
        wait(0);
    }

    protected void finalize() throws Throwable{
    }
}

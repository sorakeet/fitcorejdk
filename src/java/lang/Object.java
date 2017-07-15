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
    * ���������� this �ͱ��ȽϵĶ���
    * */
    public boolean equals(Object obj){
        return (this==obj);
    }

    protected native Object clone() throws CloneNotSupportedException;

    /*
    * ���������� this ����һЩ��ǰ����
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
            throw new IllegalArgumentException("��ʱʱ��ֵ�Ǹ���");
        }
        if(nanos<0||nanos>999999){
            throw new IllegalArgumentException("���볬ʱʱ��ֵ�ķ�Χ��Ч");
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

package org.omg.PortableServer;

public class ThreadPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _ORB_CTRL_MODEL=0;
    public static final ThreadPolicyValue ORB_CTRL_MODEL=new ThreadPolicyValue(_ORB_CTRL_MODEL);
    public static final int _SINGLE_THREAD_MODEL=1;
    public static final ThreadPolicyValue SINGLE_THREAD_MODEL=new ThreadPolicyValue(_SINGLE_THREAD_MODEL);
    private static int __size=2;
    private static ThreadPolicyValue[] __array=new ThreadPolicyValue[__size];
    private int __value;

    protected ThreadPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static ThreadPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class ThreadPolicyValue

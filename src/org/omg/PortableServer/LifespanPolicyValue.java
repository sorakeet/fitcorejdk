package org.omg.PortableServer;

public class LifespanPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _TRANSIENT=0;
    public static final LifespanPolicyValue TRANSIENT=new LifespanPolicyValue(_TRANSIENT);
    public static final int _PERSISTENT=1;
    public static final LifespanPolicyValue PERSISTENT=new LifespanPolicyValue(_PERSISTENT);
    private static int __size=2;
    private static LifespanPolicyValue[] __array=new LifespanPolicyValue[__size];
    private int __value;

    protected LifespanPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static LifespanPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class LifespanPolicyValue

package org.omg.PortableServer;

public class ServantRetentionPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _RETAIN=0;
    public static final ServantRetentionPolicyValue RETAIN=new ServantRetentionPolicyValue(_RETAIN);
    public static final int _NON_RETAIN=1;
    public static final ServantRetentionPolicyValue NON_RETAIN=new ServantRetentionPolicyValue(_NON_RETAIN);
    private static int __size=2;
    private static ServantRetentionPolicyValue[] __array=new ServantRetentionPolicyValue[__size];
    private int __value;

    protected ServantRetentionPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static ServantRetentionPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class ServantRetentionPolicyValue

package org.omg.PortableServer;

public class RequestProcessingPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _USE_ACTIVE_OBJECT_MAP_ONLY=0;
    public static final RequestProcessingPolicyValue USE_ACTIVE_OBJECT_MAP_ONLY=new RequestProcessingPolicyValue(_USE_ACTIVE_OBJECT_MAP_ONLY);
    public static final int _USE_DEFAULT_SERVANT=1;
    public static final RequestProcessingPolicyValue USE_DEFAULT_SERVANT=new RequestProcessingPolicyValue(_USE_DEFAULT_SERVANT);
    public static final int _USE_SERVANT_MANAGER=2;
    public static final RequestProcessingPolicyValue USE_SERVANT_MANAGER=new RequestProcessingPolicyValue(_USE_SERVANT_MANAGER);
    private static int __size=3;
    private static RequestProcessingPolicyValue[] __array=new RequestProcessingPolicyValue[__size];
    private int __value;

    protected RequestProcessingPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static RequestProcessingPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class RequestProcessingPolicyValue

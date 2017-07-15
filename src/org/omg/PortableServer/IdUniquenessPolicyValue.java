package org.omg.PortableServer;

public class IdUniquenessPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _UNIQUE_ID=0;
    public static final IdUniquenessPolicyValue UNIQUE_ID=new IdUniquenessPolicyValue(_UNIQUE_ID);
    public static final int _MULTIPLE_ID=1;
    public static final IdUniquenessPolicyValue MULTIPLE_ID=new IdUniquenessPolicyValue(_MULTIPLE_ID);
    private static int __size=2;
    private static IdUniquenessPolicyValue[] __array=new IdUniquenessPolicyValue[__size];
    private int __value;

    protected IdUniquenessPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static IdUniquenessPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class IdUniquenessPolicyValue

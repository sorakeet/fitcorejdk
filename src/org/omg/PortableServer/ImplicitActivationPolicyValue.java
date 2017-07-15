package org.omg.PortableServer;

public class ImplicitActivationPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _IMPLICIT_ACTIVATION=0;
    public static final ImplicitActivationPolicyValue IMPLICIT_ACTIVATION=new ImplicitActivationPolicyValue(_IMPLICIT_ACTIVATION);
    public static final int _NO_IMPLICIT_ACTIVATION=1;
    public static final ImplicitActivationPolicyValue NO_IMPLICIT_ACTIVATION=new ImplicitActivationPolicyValue(_NO_IMPLICIT_ACTIVATION);
    private static int __size=2;
    private static ImplicitActivationPolicyValue[] __array=new ImplicitActivationPolicyValue[__size];
    private int __value;

    protected ImplicitActivationPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static ImplicitActivationPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class ImplicitActivationPolicyValue

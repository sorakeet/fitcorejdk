package org.omg.PortableServer;

public class IdAssignmentPolicyValue implements org.omg.CORBA.portable.IDLEntity{
    public static final int _USER_ID=0;
    public static final IdAssignmentPolicyValue USER_ID=new IdAssignmentPolicyValue(_USER_ID);
    public static final int _SYSTEM_ID=1;
    public static final IdAssignmentPolicyValue SYSTEM_ID=new IdAssignmentPolicyValue(_SYSTEM_ID);
    private static int __size=2;
    private static IdAssignmentPolicyValue[] __array=new IdAssignmentPolicyValue[__size];
    private int __value;

    protected IdAssignmentPolicyValue(int value){
        __value=value;
        __array[__value]=this;
    }

    public static IdAssignmentPolicyValue from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class IdAssignmentPolicyValue

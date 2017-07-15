package org.omg.PortableServer.POAManagerPackage;

public class State implements org.omg.CORBA.portable.IDLEntity{
    public static final int _HOLDING=0;
    public static final State HOLDING=new State(_HOLDING);
    public static final int _ACTIVE=1;
    public static final State ACTIVE=new State(_ACTIVE);
    public static final int _DISCARDING=2;
    public static final State DISCARDING=new State(_DISCARDING);
    public static final int _INACTIVE=3;
    public static final State INACTIVE=new State(_INACTIVE);
    private static int __size=4;
    private static State[] __array=new State[__size];
    private int __value;

    protected State(int value){
        __value=value;
        __array[__value]=this;
    }

    public static State from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class State

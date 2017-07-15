package org.omg.CORBA;

public class ParameterMode implements org.omg.CORBA.portable.IDLEntity{
    public static final int _PARAM_IN=0;
    public static final ParameterMode PARAM_IN=new ParameterMode(_PARAM_IN);
    public static final int _PARAM_OUT=1;
    public static final ParameterMode PARAM_OUT=new ParameterMode(_PARAM_OUT);
    public static final int _PARAM_INOUT=2;
    public static final ParameterMode PARAM_INOUT=new ParameterMode(_PARAM_INOUT);
    private static int __size=3;
    private static ParameterMode[] __array=new ParameterMode[__size];
    private int __value;

    protected ParameterMode(int value){
        __value=value;
        __array[__value]=this;
    }

    public static ParameterMode from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class ParameterMode

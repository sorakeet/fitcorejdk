package org.omg.CosNaming;

public class BindingType implements org.omg.CORBA.portable.IDLEntity{
    public static final int _nobject=0;
    public static final BindingType nobject=new BindingType(_nobject);
    public static final int _ncontext=1;
    public static final BindingType ncontext=new BindingType(_ncontext);
    private static int __size=2;
    private static BindingType[] __array=new BindingType[__size];
    private int __value;

    protected BindingType(int value){
        __value=value;
        __array[__value]=this;
    }

    public static BindingType from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class BindingType

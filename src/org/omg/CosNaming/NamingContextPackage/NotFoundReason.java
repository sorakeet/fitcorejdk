package org.omg.CosNaming.NamingContextPackage;

public class NotFoundReason implements org.omg.CORBA.portable.IDLEntity{
    public static final int _missing_node=0;
    public static final NotFoundReason missing_node=new NotFoundReason(_missing_node);
    public static final int _not_context=1;
    public static final NotFoundReason not_context=new NotFoundReason(_not_context);
    public static final int _not_object=2;
    public static final NotFoundReason not_object=new NotFoundReason(_not_object);
    private static int __size=3;
    private static NotFoundReason[] __array=new NotFoundReason[__size];
    private int __value;

    protected NotFoundReason(int value){
        __value=value;
        __array[__value]=this;
    }

    public static NotFoundReason from_int(int value){
        if(value>=0&&value<__size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    public int value(){
        return __value;
    }
} // class NotFoundReason

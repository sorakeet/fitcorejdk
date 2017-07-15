package com.sun.corba.se.spi.activation;

public abstract class _InitialNameServiceImplBase extends org.omg.CORBA.portable.ObjectImpl
        implements InitialNameService, org.omg.CORBA.portable.InvokeHandler{
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:activation/InitialNameService:1.0"};

    static{
        _methods.put("bind",new Integer(0));
    }

    // Constructors
    public _InitialNameServiceImplBase(){
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        org.omg.CORBA.portable.OutputStream out=null;
        Integer __method=(Integer)_methods.get($method);
        if(__method==null)
            throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        switch(__method.intValue()){
            // bind initial name
            case 0:  // activation/InitialNameService/bind
            {
                try{
                    String name=in.read_string();
                    org.omg.CORBA.Object obj=org.omg.CORBA.ObjectHelper.read(in);
                    boolean isPersistant=in.read_boolean();
                    this.bind(name,obj,isPersistant);
                    out=$rh.createReply();
                }catch(com.sun.corba.se.spi.activation.InitialNameServicePackage.NameAlreadyBound $ex){
                    out=$rh.createExceptionReply();
                    com.sun.corba.se.spi.activation.InitialNameServicePackage.NameAlreadyBoundHelper.write(out,$ex);
                }
                break;
            }
            default:
                throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }
        return out;
    } // _invoke

    public String[] _ids(){
        return (String[])__ids.clone();
    }
} // class _InitialNameServiceImplBase

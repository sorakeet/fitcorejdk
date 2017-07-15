package org.omg.CosNaming;

public abstract class BindingIteratorPOA extends org.omg.PortableServer.Servant
        implements BindingIteratorOperations, org.omg.CORBA.portable.InvokeHandler{
    // Constructors
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/CosNaming/BindingIterator:1.0"};

    static{
        _methods.put("next_one",new Integer(0));
        _methods.put("next_n",new Integer(1));
        _methods.put("destroy",new Integer(2));
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh){
        org.omg.CORBA.portable.OutputStream out=null;
        Integer __method=(Integer)_methods.get($method);
        if(__method==null)
            throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        switch(__method.intValue()){
            /**
             * This operation returns the next binding. If there are no more
             * bindings, false is returned.
             *
             * @param b the returned binding
             */
            case 0:  // CosNaming/BindingIterator/next_one
            {
                BindingHolder b=new BindingHolder();
                boolean $result=false;
                $result=this.next_one(b);
                out=$rh.createReply();
                out.write_boolean($result);
                BindingHelper.write(out,b.value);
                break;
            }
            /**
             * This operation returns at most the requested number of bindings.
             *
             * @param how_many the maximum number of bindings tro return <p>
             *
             * @param bl the returned bindings
             */
            case 1:  // CosNaming/BindingIterator/next_n
            {
                int how_many=in.read_ulong();
                BindingListHolder bl=new BindingListHolder();
                boolean $result=false;
                $result=this.next_n(how_many,bl);
                out=$rh.createReply();
                out.write_boolean($result);
                BindingListHelper.write(out,bl.value);
                break;
            }
            /**
             * This operation destroys the iterator.
             */
            case 2:  // CosNaming/BindingIterator/destroy
            {
                this.destroy();
                out=$rh.createReply();
                break;
            }
            default:
                throw new org.omg.CORBA.BAD_OPERATION(0,org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }
        return out;
    } // _invoke

    public String[] _all_interfaces(org.omg.PortableServer.POA poa,byte[] objectId){
        return (String[])__ids.clone();
    }

    public BindingIterator _this(){
        return BindingIteratorHelper.narrow(
                super._this_object());
    }

    public BindingIterator _this(org.omg.CORBA.ORB orb){
        return BindingIteratorHelper.narrow(
                super._this_object(orb));
    }
} // class BindingIteratorPOA

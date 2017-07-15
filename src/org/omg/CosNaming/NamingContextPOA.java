package org.omg.CosNaming;

public abstract class NamingContextPOA extends org.omg.PortableServer.Servant
        implements NamingContextOperations, org.omg.CORBA.portable.InvokeHandler{
    // Constructors
    private static java.util.Hashtable _methods=new java.util.Hashtable();
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/CosNaming/NamingContext:1.0"};

    static{
        _methods.put("bind",new Integer(0));
        _methods.put("bind_context",new Integer(1));
        _methods.put("rebind",new Integer(2));
        _methods.put("rebind_context",new Integer(3));
        _methods.put("resolve",new Integer(4));
        _methods.put("unbind",new Integer(5));
        _methods.put("list",new Integer(6));
        _methods.put("new_context",new Integer(7));
        _methods.put("bind_new_context",new Integer(8));
        _methods.put("destroy",new Integer(9));
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
             * Creates a binding of a name and an object in the naming context.
             * Naming contexts that are bound using bind do not participate in name
             * resolution when compound names are passed to be resolved.
             *
             * @param n Name of the object <p>
             *
             * @param obj The Object to bind with the given name<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates
             * the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed
             * Indicates that the implementation has given up for some reason.
             * The client, however, may be able to continue the operation
             * at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName
             * Indicates that the name is invalid. <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound
             * Indicates an object is already bound to the specified name.<p>
             */
            case 0:  // CosNaming/NamingContext/bind
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    org.omg.CORBA.Object obj=org.omg.CORBA.ObjectHelper.read(in);
                    this.bind(n,obj);
                    out=$rh.createReply();
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.AlreadyBound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.write(out,$ex);
                }
                break;
            }
            /**
             * Names an object that is a naming context. Naming contexts that
             * are bound using bind_context() participate in name resolution
             * when compound names are passed to be resolved.
             *
             * @param n Name of the object <p>
             *
             * @param nc NamingContect object to bind with the given name <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Indicates that the implementation has
             * given up for some reason. The client, however, may be able to
             * continue the operation at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName Indicates that the name is invalid. <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound Indicates an object is already
             * bound to the specified name.<p>
             */
            case 1:  // CosNaming/NamingContext/bind_context
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    NamingContext nc=NamingContextHelper.read(in);
                    this.bind_context(n,nc);
                    out=$rh.createReply();
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.AlreadyBound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.write(out,$ex);
                }
                break;
            }
            /**
             * Creates a binding of a name and an object in the naming context
             * even if the name is already bound in the context. Naming contexts
             * that are bound using rebind do not participate in name resolution
             * when compound names are passed to be resolved.
             *
             * @param  n Name of the object <p>
             *
             * @param obj The Object to rebind with the given name <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Indicates that the implementation has
             * given up for some reason. The client, however, may be able to
             * continue the operation at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName Indicates that the name is invalid. <p>
             */
            case 2:  // CosNaming/NamingContext/rebind
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    org.omg.CORBA.Object obj=org.omg.CORBA.ObjectHelper.read(in);
                    this.rebind(n,obj);
                    out=$rh.createReply();
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }
                break;
            }
            /**
             * Creates a binding of a name and a naming context in the naming
             * context even if the name is already bound in the context. Naming
             * contexts that are bound using rebind_context() participate in name
             * resolution when compound names are passed to be resolved.
             *
             * @param n Name of the object <p>
             *
             * @param nc NamingContect object to rebind with the given name <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Indicates that the implementation has
             * given up for some reason. The client, however, may be able to
             * continue the operation at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName Indicates that the name is invalid. <p>
             */
            case 3:  // CosNaming/NamingContext/rebind_context
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    NamingContext nc=NamingContextHelper.read(in);
                    this.rebind_context(n,nc);
                    out=$rh.createReply();
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }
                break;
            }
            /**
             * The resolve operation is the process of retrieving an object
             * bound to a name in a given context. The given name must exactly
             * match the bound name. The naming service does not return the type
             * of the object. Clients are responsible for "narrowing" the object
             * to the appropriate type. That is, clients typically cast the returned
             * object from Object to a more specialized interface.
             *
             * @param n Name of the object <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Indicates that the implementation has
             * given up for some reason. The client, however, may be able to
             * continue the operation at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName Indicates that the name is invalid. <p>
             */
            case 4:  // CosNaming/NamingContext/resolve
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    org.omg.CORBA.Object $result=null;
                    $result=this.resolve(n);
                    out=$rh.createReply();
                    org.omg.CORBA.ObjectHelper.write(out,$result);
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }
                break;
            }
            /**
             * The unbind operation removes a name binding from a context.
             *
             * @param n Name of the object <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Indicates that the implementation has
             * given up for some reason. The client, however, may be able to
             * continue the operation at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName Indicates that the name is invalid. <p>
             */
            case 5:  // CosNaming/NamingContext/unbind
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    this.unbind(n);
                    out=$rh.createReply();
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }
                break;
            }
            /**
             * The list operation allows a client to iterate through a set of
             * bindings in a naming context. <p>
             *
             * The list operation returns at most the requested number of
             * bindings in BindingList bl.
             * <ul>
             * <li>If the naming context contains additional
             * bindings, the list operation returns a BindingIterator with the
             * additional bindings.
             * <li>If the naming context does not contain additional
             * bindings, the binding iterator is a nil object reference.
             * </ul>
             *
             * @param how_many the maximum number of bindings to return <p>
             *
             * @param bl the returned list of bindings <p>
             *
             * @param bi the returned binding iterator <p>
             */
            case 6:  // CosNaming/NamingContext/list
            {
                int how_many=in.read_ulong();
                BindingListHolder bl=new BindingListHolder();
                BindingIteratorHolder bi=new BindingIteratorHolder();
                this.list(how_many,bl,bi);
                out=$rh.createReply();
                BindingListHelper.write(out,bl.value);
                BindingIteratorHelper.write(out,bi.value);
                break;
            }
            /**
             * This operation returns a naming context implemented by the same
             * naming server as the context on which the operation was invoked.
             * The new context is not bound to any name.
             */
            case 7:  // CosNaming/NamingContext/new_context
            {
                NamingContext $result=null;
                $result=this.new_context();
                out=$rh.createReply();
                NamingContextHelper.write(out,$result);
                break;
            }
            /**
             * This operation creates a new context and binds it to the name
             * supplied as an argument. The newly-created context is implemented
             * by the same naming server as the context in which it was bound (that
             * is, the naming server that implements the context denoted by the
             * name argument excluding the last component).
             *
             * @param n Name of the object <p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotFound Indicates the name does not identify a binding.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound Indicates an object is already
             * bound to the specified name.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Indicates that the implementation has
             * given up for some reason. The client, however, may be able to
             * continue the operation at the returned naming context.<p>
             *
             * @exception org.omg.CosNaming.NamingContextPackage.InvalidName Indicates that the name is invalid. <p>
             */
            case 8:  // CosNaming/NamingContext/bind_new_context
            {
                try{
                    NameComponent n[]=NameHelper.read(in);
                    NamingContext $result=null;
                    $result=this.bind_new_context(n);
                    out=$rh.createReply();
                    NamingContextHelper.write(out,$result);
                }catch(org.omg.CosNaming.NamingContextPackage.NotFound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.AlreadyBound $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.CannotProceed $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(out,$ex);
                }catch(org.omg.CosNaming.NamingContextPackage.InvalidName $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.write(out,$ex);
                }
                break;
            }
            /**
             * The destroy operation deletes a naming context. If the naming
             * context contains bindings, the NotEmpty exception is raised.
             *
             * @exception org.omg.CosNaming.NamingContextPackage.NotEmpty Indicates that the Naming Context contains bindings.
             */
            case 9:  // CosNaming/NamingContext/destroy
            {
                try{
                    this.destroy();
                    out=$rh.createReply();
                }catch(org.omg.CosNaming.NamingContextPackage.NotEmpty $ex){
                    out=$rh.createExceptionReply();
                    org.omg.CosNaming.NamingContextPackage.NotEmptyHelper.write(out,$ex);
                }
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

    public NamingContext _this(){
        return NamingContextHelper.narrow(
                super._this_object());
    }

    public NamingContext _this(org.omg.CORBA.ORB orb){
        return NamingContextHelper.narrow(
                super._this_object(orb));
    }
} // class NamingContextPOA
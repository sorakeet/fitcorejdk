package org.omg.CosNaming;

public class _NamingContextStub extends org.omg.CORBA.portable.ObjectImpl implements NamingContext{
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/CosNaming/NamingContext:1.0"};

    public void bind(NameComponent[] n,org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, org.omg.CosNaming.NamingContextPackage.AlreadyBound{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("bind",true);
            NameHelper.write($out,n);
            org.omg.CORBA.ObjectHelper.write($out,obj);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/AlreadyBound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            bind(n,obj);
        }finally{
            _releaseReply($in);
        }
    } // bind

    public void bind_context(NameComponent[] n,NamingContext nc) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, org.omg.CosNaming.NamingContextPackage.AlreadyBound{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("bind_context",true);
            NameHelper.write($out,n);
            NamingContextHelper.write($out,nc);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/AlreadyBound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            bind_context(n,nc);
        }finally{
            _releaseReply($in);
        }
    } // bind_context

    public void rebind(NameComponent[] n,org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("rebind",true);
            NameHelper.write($out,n);
            org.omg.CORBA.ObjectHelper.write($out,obj);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            rebind(n,obj);
        }finally{
            _releaseReply($in);
        }
    } // rebind

    public void rebind_context(NameComponent[] n,NamingContext nc) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("rebind_context",true);
            NameHelper.write($out,n);
            NamingContextHelper.write($out,nc);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            rebind_context(n,nc);
        }finally{
            _releaseReply($in);
        }
    } // rebind_context

    public org.omg.CORBA.Object resolve(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("resolve",true);
            NameHelper.write($out,n);
            $in=_invoke($out);
            org.omg.CORBA.Object $result=org.omg.CORBA.ObjectHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return resolve(n);
        }finally{
            _releaseReply($in);
        }
    } // resolve

    public void unbind(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("unbind",true);
            NameHelper.write($out,n);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            unbind(n);
        }finally{
            _releaseReply($in);
        }
    } // unbind

    public void list(int how_many,BindingListHolder bl,BindingIteratorHolder bi){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("list",true);
            $out.write_ulong(how_many);
            $in=_invoke($out);
            bl.value=BindingListHelper.read($in);
            bi.value=BindingIteratorHelper.read($in);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            list(how_many,bl,bi);
        }finally{
            _releaseReply($in);
        }
    } // list

    public NamingContext new_context(){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("new_context",true);
            $in=_invoke($out);
            NamingContext $result=NamingContextHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return new_context();
        }finally{
            _releaseReply($in);
        }
    } // new_context

    public NamingContext bind_new_context(NameComponent[] n) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.AlreadyBound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("bind_new_context",true);
            NameHelper.write($out,n);
            $in=_invoke($out);
            NamingContext $result=NamingContextHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/AlreadyBound:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read($in);
            else if(_id.equals("IDL:omg.org/CosNaming/NamingContext/InvalidName:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.InvalidNameHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return bind_new_context(n);
        }finally{
            _releaseReply($in);
        }
    } // bind_new_context

    public void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty{
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("destroy",true);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            if(_id.equals("IDL:omg.org/CosNaming/NamingContext/NotEmpty:1.0"))
                throw org.omg.CosNaming.NamingContextPackage.NotEmptyHelper.read($in);
            else
                throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            destroy();
        }finally{
            _releaseReply($in);
        }
    } // destroy

    public String[] _ids(){
        return (String[])__ids.clone();
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException{
        String str=s.readUTF();
        String[] args=null;
        java.util.Properties props=null;
        org.omg.CORBA.ORB orb=org.omg.CORBA.ORB.init(args,props);
        try{
            org.omg.CORBA.Object obj=orb.string_to_object(str);
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _set_delegate(delegate);
        }finally{
            orb.destroy();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        String[] args=null;
        java.util.Properties props=null;
        org.omg.CORBA.ORB orb=org.omg.CORBA.ORB.init(args,props);
        try{
            String str=orb.object_to_string(this);
            s.writeUTF(str);
        }finally{
            orb.destroy();
        }
    }
} // class _NamingContextStub

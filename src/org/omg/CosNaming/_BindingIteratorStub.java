package org.omg.CosNaming;

public class _BindingIteratorStub extends org.omg.CORBA.portable.ObjectImpl implements BindingIterator{
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/CosNaming/BindingIterator:1.0"};

    public boolean next_one(BindingHolder b){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("next_one",true);
            $in=_invoke($out);
            boolean $result=$in.read_boolean();
            b.value=BindingHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return next_one(b);
        }finally{
            _releaseReply($in);
        }
    } // next_one

    public boolean next_n(int how_many,BindingListHolder bl){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("next_n",true);
            $out.write_ulong(how_many);
            $in=_invoke($out);
            boolean $result=$in.read_boolean();
            bl.value=BindingListHelper.read($in);
            return $result;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException $rm){
            return next_n(how_many,bl);
        }finally{
            _releaseReply($in);
        }
    } // next_n

    public void destroy(){
        org.omg.CORBA.portable.InputStream $in=null;
        try{
            org.omg.CORBA.portable.OutputStream $out=_request("destroy",true);
            $in=_invoke($out);
            return;
        }catch(org.omg.CORBA.portable.ApplicationException $ex){
            $in=$ex.getInputStream();
            String _id=$ex.getId();
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
} // class _BindingIteratorStub

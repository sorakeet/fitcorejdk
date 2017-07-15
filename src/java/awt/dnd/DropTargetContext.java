/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class DropTargetContext implements Serializable{
    private static final long serialVersionUID=-634158968993743371L;
    private DropTarget dropTarget;
    private transient DropTargetContextPeer dropTargetContextPeer;
    private transient Transferable transferable;

    DropTargetContext(DropTarget dt){
        super();
        dropTarget=dt;
    }

    public Component getComponent(){
        return dropTarget.getComponent();
    }

    public void addNotify(DropTargetContextPeer dtcp){
        dropTargetContextPeer=dtcp;
    }

    public void removeNotify(){
        dropTargetContextPeer=null;
        transferable=null;
    }

    protected int getTargetActions(){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        return ((peer!=null)
                ?peer.getTargetActions()
                :dropTarget.getDefaultActions()
        );
    }

    protected void setTargetActions(int actions){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer!=null){
            synchronized(peer){
                peer.setTargetActions(actions);
                getDropTarget().doSetDefaultActions(actions);
            }
        }else{
            getDropTarget().doSetDefaultActions(actions);
        }
    }

    public DropTarget getDropTarget(){
        return dropTarget;
    }

    DropTargetContextPeer getDropTargetContextPeer(){
        return dropTargetContextPeer;
    }

    public void dropComplete(boolean success) throws InvalidDnDOperationException{
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer!=null){
            peer.dropComplete(success);
        }
    }

    protected void acceptDrag(int dragOperation){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer!=null){
            peer.acceptDrag(dragOperation);
        }
    }

    protected void rejectDrag(){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer!=null){
            peer.rejectDrag();
        }
    }

    protected void acceptDrop(int dropOperation){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer!=null){
            peer.acceptDrop(dropOperation);
        }
    }

    protected void rejectDrop(){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer!=null){
            peer.rejectDrop();
        }
    }

    protected boolean isDataFlavorSupported(DataFlavor df){
        return getCurrentDataFlavorsAsList().contains(df);
    }

    protected List<DataFlavor> getCurrentDataFlavorsAsList(){
        return Arrays.asList(getCurrentDataFlavors());
    }

    protected DataFlavor[] getCurrentDataFlavors(){
        DropTargetContextPeer peer=getDropTargetContextPeer();
        return peer!=null?peer.getTransferDataFlavors():new DataFlavor[0];
    }

    protected Transferable getTransferable() throws InvalidDnDOperationException{
        DropTargetContextPeer peer=getDropTargetContextPeer();
        if(peer==null){
            throw new InvalidDnDOperationException();
        }else{
            if(transferable==null){
                Transferable t=peer.getTransferable();
                boolean isLocal=peer.isTransferableJVMLocal();
                synchronized(this){
                    if(transferable==null){
                        transferable=createTransferableProxy(t,isLocal);
                    }
                }
            }
            return transferable;
        }
    }

    protected Transferable createTransferableProxy(Transferable t,boolean local){
        return new TransferableProxy(t,local);
    }

    protected class TransferableProxy implements Transferable{
        // We don't need to worry about client code changing the values of
        // these variables. Since TransferableProxy is a protected class, only
        // subclasses of DropTargetContext can access it. And DropTargetContext
        // cannot be subclassed by client code because it does not have a
        // public constructor.
        protected Transferable transferable;
        protected boolean isLocal;
        private sun.awt.datatransfer.TransferableProxy proxy;

        TransferableProxy(Transferable t,boolean local){
            proxy=new sun.awt.datatransfer.TransferableProxy(t,local);
            transferable=t;
            isLocal=local;
        }

        public DataFlavor[] getTransferDataFlavors(){
            return proxy.getTransferDataFlavors();
        }

        public boolean isDataFlavorSupported(DataFlavor flavor){
            return proxy.isDataFlavorSupported(flavor);
        }

        public Object getTransferData(DataFlavor df)
                throws UnsupportedFlavorException, IOException{
            return proxy.getTransferData(df);
        }
    }
}

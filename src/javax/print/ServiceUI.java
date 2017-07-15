/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import sun.print.ServiceDialog;
import sun.print.SunAlternateMedia;

import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Fidelity;
import java.awt.*;

public class ServiceUI{
    public static PrintService printDialog(GraphicsConfiguration gc,
                                           int x,int y,
                                           PrintService[] services,
                                           PrintService defaultService,
                                           DocFlavor flavor,
                                           PrintRequestAttributeSet attributes)
            throws HeadlessException{
        int defaultIndex=-1;
        if(GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }else if((services==null)||(services.length==0)){
            throw new IllegalArgumentException("services must be non-null "+
                    "and non-empty");
        }else if(attributes==null){
            throw new IllegalArgumentException("attributes must be non-null");
        }
        if(defaultService!=null){
            for(int i=0;i<services.length;i++){
                if(services[i].equals(defaultService)){
                    defaultIndex=i;
                    break;
                }
            }
            if(defaultIndex<0){
                throw new IllegalArgumentException("services must contain "+
                        "defaultService");
            }
        }else{
            defaultIndex=0;
        }
        // For now we set owner to null. In the future, it may be passed
        // as an argument.
        Window owner=null;
        Rectangle gcBounds=(gc==null)?GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration().getBounds():gc.getBounds();
        ServiceDialog dialog;
        if(owner instanceof Frame){
            dialog=new ServiceDialog(gc,
                    x+gcBounds.x,
                    y+gcBounds.y,
                    services,defaultIndex,
                    flavor,attributes,
                    (Frame)owner);
        }else{
            dialog=new ServiceDialog(gc,
                    x+gcBounds.x,
                    y+gcBounds.y,
                    services,defaultIndex,
                    flavor,attributes,
                    (Dialog)owner);
        }
        Rectangle dlgBounds=dialog.getBounds();
        // get union of all GC bounds
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs=ge.getScreenDevices();
        for(int j=0;j<gs.length;j++){
            gcBounds=
                    gcBounds.union(gs[j].getDefaultConfiguration().getBounds());
        }
        // if portion of dialog is not within the gc boundary
        if(!gcBounds.contains(dlgBounds)){
            // put in the center relative to parent frame/dialog
            dialog.setLocationRelativeTo(owner);
        }
        dialog.show();
        if(dialog.getStatus()==ServiceDialog.APPROVE){
            PrintRequestAttributeSet newas=dialog.getAttributes();
            Class dstCategory=Destination.class;
            Class amCategory=SunAlternateMedia.class;
            Class fdCategory=Fidelity.class;
            if(attributes.containsKey(dstCategory)&&
                    !newas.containsKey(dstCategory)){
                attributes.remove(dstCategory);
            }
            if(attributes.containsKey(amCategory)&&
                    !newas.containsKey(amCategory)){
                attributes.remove(amCategory);
            }
            attributes.addAll(newas);
            Fidelity fd=(Fidelity)attributes.get(fdCategory);
            if(fd!=null){
                if(fd==Fidelity.FIDELITY_TRUE){
                    removeUnsupportedAttributes(dialog.getPrintService(),
                            flavor,attributes);
                }
            }
        }
        return dialog.getPrintService();
    }

    private static void removeUnsupportedAttributes(PrintService ps,
                                                    DocFlavor flavor,
                                                    AttributeSet aset){
        AttributeSet asUnsupported=ps.getUnsupportedAttributes(flavor,
                aset);
        if(asUnsupported!=null){
            Attribute[] usAttrs=asUnsupported.toArray();
            for(int i=0;i<usAttrs.length;i++){
                Class category=usAttrs[i].getCategory();
                if(ps.isAttributeCategorySupported(category)){
                    Attribute attr=
                            (Attribute)ps.getDefaultAttributeValue(category);
                    if(attr!=null){
                        aset.add(attr);
                    }else{
                        aset.remove(category);
                    }
                }else{
                    aset.remove(category);
                }
            }
        }
    }
}

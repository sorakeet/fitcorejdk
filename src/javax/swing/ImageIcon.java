/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AppContext;

import javax.accessibility.*;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Locale;

public class ImageIcon implements Icon, Serializable, Accessible{
    @Deprecated
    protected final static Component component;
    @Deprecated
    protected final static MediaTracker tracker;
    private final static Object TRACKER_KEY=new StringBuilder("TRACKER_KEY");
    private static int mediaTrackerID;

    static{
        component=AccessController.doPrivileged(new PrivilegedAction<Component>(){
            public Component run(){
                try{
                    final Component component=createNoPermsComponent();
                    // 6482575 - clear the appContext field so as not to leak it
                    Field appContextField=
                            Component.class.getDeclaredField("appContext");
                    appContextField.setAccessible(true);
                    appContextField.set(component,null);
                    return component;
                }catch(Throwable e){
                    // We don't care about component.
                    // So don't prevent class initialisation.
                    e.printStackTrace();
                    return null;
                }
            }
        });
        tracker=new MediaTracker(component);
    }

    transient Image image;
    transient int loadStatus=0;
    ImageObserver imageObserver;
    String description=null;
    int width=-1;
    int height=-1;
    transient private String filename;
    transient private URL location;
    private AccessibleImageIcon accessibleContext=null;

    @ConstructorProperties({"description"})
    public ImageIcon(String filename){
        this(filename,filename);
    }

    public ImageIcon(String filename,String description){
        image=Toolkit.getDefaultToolkit().getImage(filename);
        if(image==null){
            return;
        }
        this.filename=filename;
        this.description=description;
        loadImage(image);
    }

    protected void loadImage(Image image){
        MediaTracker mTracker=getTracker();
        synchronized(mTracker){
            int id=getNextID();
            mTracker.addImage(image,id);
            try{
                mTracker.waitForID(id,0);
            }catch(InterruptedException e){
                System.out.println("INTERRUPTED while loading Image");
            }
            loadStatus=mTracker.statusID(id,false);
            mTracker.removeImage(image,id);
            width=image.getWidth(imageObserver);
            height=image.getHeight(imageObserver);
        }
    }

    private int getNextID(){
        synchronized(getTracker()){
            return ++mediaTrackerID;
        }
    }

    private MediaTracker getTracker(){
        Object trackerObj;
        AppContext ac=AppContext.getAppContext();
        // Opt: Only synchronize if trackerObj comes back null?
        // If null, synchronize, re-check for null, and put new tracker
        synchronized(ac){
            trackerObj=ac.get(TRACKER_KEY);
            if(trackerObj==null){
                Component comp=new Component(){
                };
                trackerObj=new MediaTracker(comp);
                ac.put(TRACKER_KEY,trackerObj);
            }
        }
        return (MediaTracker)trackerObj;
    }

    public ImageIcon(URL location){
        this(location,location.toExternalForm());
    }

    public ImageIcon(URL location,String description){
        image=Toolkit.getDefaultToolkit().getImage(location);
        if(image==null){
            return;
        }
        this.location=location;
        this.description=description;
        loadImage(image);
    }

    public ImageIcon(Image image,String description){
        this(image);
        this.description=description;
    }

    public ImageIcon(Image image){
        this.image=image;
        Object o=image.getProperty("comment",imageObserver);
        if(o instanceof String){
            description=(String)o;
        }
        loadImage(image);
    }

    public ImageIcon(byte[] imageData,String description){
        this.image=Toolkit.getDefaultToolkit().createImage(imageData);
        if(image==null){
            return;
        }
        this.description=description;
        loadImage(image);
    }

    public ImageIcon(byte[] imageData){
        this.image=Toolkit.getDefaultToolkit().createImage(imageData);
        if(image==null){
            return;
        }
        Object o=image.getProperty("comment",imageObserver);
        if(o instanceof String){
            description=(String)o;
        }
        loadImage(image);
    }

    public ImageIcon(){
    }

    private static Component createNoPermsComponent(){
        // 7020198 - set acc field to no permissions and no subject
        // Note, will have appContext set.
        return AccessController.doPrivileged(
                new PrivilegedAction<Component>(){
                    public Component run(){
                        return new Component(){
                        };
                    }
                },
                new AccessControlContext(new ProtectionDomain[]{
                        new ProtectionDomain(null,null)
                })
        );
    }

    public int getImageLoadStatus(){
        return loadStatus;
    }

    @Transient
    public Image getImage(){
        return image;
    }

    public void setImage(Image image){
        this.image=image;
        loadImage(image);
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description=description;
    }

    public synchronized void paintIcon(Component c,Graphics g,int x,int y){
        if(imageObserver==null){
            g.drawImage(image,x,y,c);
        }else{
            g.drawImage(image,x,y,imageObserver);
        }
    }

    public int getIconWidth(){
        return width;
    }

    public int getIconHeight(){
        return height;
    }

    @Transient
    public ImageObserver getImageObserver(){
        return imageObserver;
    }

    public void setImageObserver(ImageObserver observer){
        imageObserver=observer;
    }

    public String toString(){
        if(description!=null){
            return description;
        }
        return super.toString();
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException{
        s.defaultReadObject();
        int w=s.readInt();
        int h=s.readInt();
        int[] pixels=(int[])(s.readObject());
        if(pixels!=null){
            Toolkit tk=Toolkit.getDefaultToolkit();
            ColorModel cm=ColorModel.getRGBdefault();
            image=tk.createImage(new MemoryImageSource(w,h,cm,pixels,0,w));
            loadImage(image);
        }
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        int w=getIconWidth();
        int h=getIconHeight();
        int[] pixels=image!=null?new int[w*h]:null;
        if(image!=null){
            try{
                PixelGrabber pg=new PixelGrabber(image,0,0,w,h,pixels,0,w);
                pg.grabPixels();
                if((pg.getStatus()&ImageObserver.ABORT)!=0){
                    throw new IOException("failed to load image contents");
                }
            }catch(InterruptedException e){
                throw new IOException("image load interrupted");
            }
        }
        s.writeInt(w);
        s.writeInt(h);
        s.writeObject(pixels);
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleImageIcon();
        }
        return accessibleContext;
    }

    protected class AccessibleImageIcon extends AccessibleContext
            implements AccessibleIcon, Serializable{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.ICON;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            return null;
        }

        public Accessible getAccessibleParent(){
            return null;
        }

        public int getAccessibleIndexInParent(){
            return -1;
        }

        public int getAccessibleChildrenCount(){
            return 0;
        }

        public Accessible getAccessibleChild(int i){
            return null;
        }

        public Locale getLocale() throws IllegalComponentStateException{
            return null;
        }

        public String getAccessibleIconDescription(){
            return ImageIcon.this.getDescription();
        }

        public void setAccessibleIconDescription(String description){
            ImageIcon.this.setDescription(description);
        }

        public int getAccessibleIconWidth(){
            return ImageIcon.this.width;
        }

        public int getAccessibleIconHeight(){
            return ImageIcon.this.height;
        }

        private void readObject(ObjectInputStream s)
                throws ClassNotFoundException, IOException{
            s.defaultReadObject();
        }

        private void writeObject(ObjectOutputStream s)
                throws IOException{
            s.defaultWriteObject();
        }
    }  // AccessibleImageIcon
}

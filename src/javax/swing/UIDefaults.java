/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;
import sun.swing.SwingUtilities2;
import sun.util.CoreResourceBundleControl;

import javax.swing.border.Border;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.ResourceBundle.Control;

public class UIDefaults extends Hashtable<Object,Object>{
    private static final Object PENDING=new Object();
    private SwingPropertyChangeSupport changeSupport;
    private Vector<String> resourceBundles;
    private Locale defaultLocale=Locale.getDefault();
    private Map<Locale,Map<String,Object>> resourceCache;

    public UIDefaults(){
        this(700,.75f);
    }

    public UIDefaults(int initialCapacity,float loadFactor){
        super(initialCapacity,loadFactor);
        resourceCache=new HashMap<Locale,Map<String,Object>>();
    }

    public UIDefaults(Object[] keyValueList){
        super(keyValueList.length/2);
        for(int i=0;i<keyValueList.length;i+=2){
            super.put(keyValueList[i],keyValueList[i+1]);
        }
    }

    public void putDefaults(Object[] keyValueList){
        for(int i=0, max=keyValueList.length;i<max;i+=2){
            Object value=keyValueList[i+1];
            if(value==null){
                super.remove(keyValueList[i]);
            }else{
                super.put(keyValueList[i],value);
            }
        }
        firePropertyChange("UIDefaults",null,null);
    }

    protected void firePropertyChange(String propertyName,Object oldValue,Object newValue){
        if(changeSupport!=null){
            changeSupport.firePropertyChange(propertyName,oldValue,newValue);
        }
    }

    public Font getFont(Object key){
        Object value=get(key);
        return (value instanceof Font)?(Font)value:null;
    }

    public Object get(Object key){
        Object value=getFromHashtable(key);
        return (value!=null)?value:getFromResourceBundle(key,null);
    }

    private Object getFromHashtable(final Object key){
        /** Quickly handle the common case, without grabbing
         * a lock.
         */
        Object value=super.get(key);
        if((value!=PENDING)&&
                !(value instanceof ActiveValue)&&
                !(value instanceof LazyValue)){
            return value;
        }
        /** If the LazyValue for key is being constructed by another
         * thread then wait and then return the new value, otherwise drop
         * the lock and construct the ActiveValue or the LazyValue.
         * We use the special value PENDING to mark LazyValues that
         * are being constructed.
         */
        synchronized(this){
            value=super.get(key);
            if(value==PENDING){
                do{
                    try{
                        this.wait();
                    }catch(InterruptedException e){
                    }
                    value=super.get(key);
                }
                while(value==PENDING);
                return value;
            }else if(value instanceof LazyValue){
                super.put(key,PENDING);
            }else if(!(value instanceof ActiveValue)){
                return value;
            }
        }
        /** At this point we know that the value of key was
         * a LazyValue or an ActiveValue.
         */
        if(value instanceof LazyValue){
            try{
                /** If an exception is thrown we'll just put the LazyValue
                 * back in the table.
                 */
                value=((LazyValue)value).createValue(this);
            }finally{
                synchronized(this){
                    if(value==null){
                        super.remove(key);
                    }else{
                        super.put(key,value);
                    }
                    this.notifyAll();
                }
            }
        }else{
            value=((ActiveValue)value).createValue(this);
        }
        return value;
    }

    private Object getFromResourceBundle(Object key,Locale l){
        if(resourceBundles==null||
                resourceBundles.isEmpty()||
                !(key instanceof String)){
            return null;
        }
        // A null locale means use the default locale.
        if(l==null){
            if(defaultLocale==null)
                return null;
            else
                l=defaultLocale;
        }
        synchronized(this){
            return getResourceCache(l).get(key);
        }
    }

    private Map<String,Object> getResourceCache(Locale l){
        Map<String,Object> values=resourceCache.get(l);
        if(values==null){
            values=new TextAndMnemonicHashMap();
            for(int i=resourceBundles.size()-1;i>=0;i--){
                String bundleName=resourceBundles.get(i);
                try{
                    Control c=CoreResourceBundleControl.getRBControlInstance(bundleName);
                    ResourceBundle b;
                    if(c!=null){
                        b=ResourceBundle.getBundle(bundleName,l,c);
                    }else{
                        b=ResourceBundle.getBundle(bundleName,l);
                    }
                    Enumeration keys=b.getKeys();
                    while(keys.hasMoreElements()){
                        String key=(String)keys.nextElement();
                        if(values.get(key)==null){
                            Object value=b.getObject(key);
                            values.put(key,value);
                        }
                    }
                }catch(MissingResourceException mre){
                    // Keep looking
                }
            }
            resourceCache.put(l,values);
        }
        return values;
    }

    public Object put(Object key,Object value){
        Object oldValue=(value==null)?super.remove(key):super.put(key,value);
        if(key instanceof String){
            firePropertyChange((String)key,oldValue,value);
        }
        return oldValue;
    }

    public Font getFont(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Font)?(Font)value:null;
    }

    public Object get(Object key,Locale l){
        Object value=getFromHashtable(key);
        return (value!=null)?value:getFromResourceBundle(key,l);
    }

    public Color getColor(Object key){
        Object value=get(key);
        return (value instanceof Color)?(Color)value:null;
    }

    public Color getColor(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Color)?(Color)value:null;
    }

    public Icon getIcon(Object key){
        Object value=get(key);
        return (value instanceof Icon)?(Icon)value:null;
    }

    public Icon getIcon(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Icon)?(Icon)value:null;
    }

    public Border getBorder(Object key){
        Object value=get(key);
        return (value instanceof Border)?(Border)value:null;
    }

    public Border getBorder(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Border)?(Border)value:null;
    }

    public String getString(Object key){
        Object value=get(key);
        return (value instanceof String)?(String)value:null;
    }

    public String getString(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof String)?(String)value:null;
    }

    public int getInt(Object key){
        Object value=get(key);
        return (value instanceof Integer)?((Integer)value).intValue():0;
    }

    public int getInt(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Integer)?((Integer)value).intValue():0;
    }

    public boolean getBoolean(Object key){
        Object value=get(key);
        return (value instanceof Boolean)?((Boolean)value).booleanValue():false;
    }

    public boolean getBoolean(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Boolean)?((Boolean)value).booleanValue():false;
    }

    public Insets getInsets(Object key){
        Object value=get(key);
        return (value instanceof Insets)?(Insets)value:null;
    }

    public Insets getInsets(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Insets)?(Insets)value:null;
    }

    public Dimension getDimension(Object key){
        Object value=get(key);
        return (value instanceof Dimension)?(Dimension)value:null;
    }

    public Dimension getDimension(Object key,Locale l){
        Object value=get(key,l);
        return (value instanceof Dimension)?(Dimension)value:null;
    }

    public Class<? extends ComponentUI> getUIClass(String uiClassID){
        return getUIClass(uiClassID,null);
    }

    public Class<? extends ComponentUI>
    getUIClass(String uiClassID,ClassLoader uiClassLoader){
        try{
            String className=(String)get(uiClassID);
            if(className!=null){
                ReflectUtil.checkPackageAccess(className);
                Class cls=(Class)get(className);
                if(cls==null){
                    if(uiClassLoader==null){
                        cls=SwingUtilities.loadSystemClass(className);
                    }else{
                        cls=uiClassLoader.loadClass(className);
                    }
                    if(cls!=null){
                        // Save lookup for future use, as forName is slow.
                        put(className,cls);
                    }
                }
                return cls;
            }
        }catch(ClassNotFoundException e){
            return null;
        }catch(ClassCastException e){
            return null;
        }
        return null;
    }

    protected void getUIError(String msg){
        System.err.println("UIDefaults.getUI() failed: "+msg);
        try{
            throw new Error();
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    public ComponentUI getUI(JComponent target){
        Object cl=get("ClassLoader");
        ClassLoader uiClassLoader=
                (cl!=null)?(ClassLoader)cl:target.getClass().getClassLoader();
        Class<? extends ComponentUI> uiClass=getUIClass(target.getUIClassID(),uiClassLoader);
        Object uiObject=null;
        if(uiClass==null){
            getUIError("no ComponentUI class for: "+target);
        }else{
            try{
                Method m=(Method)get(uiClass);
                if(m==null){
                    m=uiClass.getMethod("createUI",new Class[]{JComponent.class});
                    put(uiClass,m);
                }
                uiObject=MethodUtil.invoke(m,null,new Object[]{target});
            }catch(NoSuchMethodException e){
                getUIError("static createUI() method not found in "+uiClass);
            }catch(Exception e){
                getUIError("createUI() failed for "+target+" "+e);
            }
        }
        return (ComponentUI)uiObject;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener){
        if(changeSupport==null){
            changeSupport=new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener){
        if(changeSupport!=null){
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(){
        if(changeSupport==null){
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }

    public synchronized void addResourceBundle(String bundleName){
        if(bundleName==null){
            return;
        }
        if(resourceBundles==null){
            resourceBundles=new Vector<String>(5);
        }
        if(!resourceBundles.contains(bundleName)){
            resourceBundles.add(bundleName);
            resourceCache.clear();
        }
    }

    public synchronized void removeResourceBundle(String bundleName){
        if(resourceBundles!=null){
            resourceBundles.remove(bundleName);
        }
        resourceCache.clear();
    }

    public Locale getDefaultLocale(){
        return defaultLocale;
    }

    public void setDefaultLocale(Locale l){
        defaultLocale=l;
    }

    public interface LazyValue{
        Object createValue(UIDefaults table);
    }

    public interface ActiveValue{
        Object createValue(UIDefaults table);
    }

    public static class ProxyLazyValue implements LazyValue{
        private AccessControlContext acc;
        private String className;
        private String methodName;
        private Object[] args;

        public ProxyLazyValue(String c){
            this(c,(String)null);
        }

        public ProxyLazyValue(String c,String m){
            this(c,m,null);
        }

        public ProxyLazyValue(String c,String m,Object[] o){
            acc=AccessController.getContext();
            className=c;
            methodName=m;
            if(o!=null){
                args=o.clone();
            }
        }

        public ProxyLazyValue(String c,Object[] o){
            this(c,null,o);
        }

        public Object createValue(final UIDefaults table){
            // In order to pick up the security policy in effect at the
            // time of creation we use a doPrivileged with the
            // AccessControlContext that was in place when this was created.
            if(acc==null&&System.getSecurityManager()!=null){
                throw new SecurityException("null AccessControlContext");
            }
            return AccessController.doPrivileged(new PrivilegedAction<Object>(){
                public Object run(){
                    try{
                        Class<?> c;
                        Object cl;
                        // See if we should use a separate ClassLoader
                        if(table==null||!((cl=table.get("ClassLoader"))
                                instanceof ClassLoader)){
                            cl=Thread.currentThread().
                                    getContextClassLoader();
                            if(cl==null){
                                // Fallback to the system class loader.
                                cl=ClassLoader.getSystemClassLoader();
                            }
                        }
                        ReflectUtil.checkPackageAccess(className);
                        c=Class.forName(className,true,(ClassLoader)cl);
                        SwingUtilities2.checkAccess(c.getModifiers());
                        if(methodName!=null){
                            Class[] types=getClassArray(args);
                            Method m=c.getMethod(methodName,types);
                            return MethodUtil.invoke(m,c,args);
                        }else{
                            Class[] types=getClassArray(args);
                            Constructor constructor=c.getConstructor(types);
                            SwingUtilities2.checkAccess(constructor.getModifiers());
                            return constructor.newInstance(args);
                        }
                    }catch(Exception e){
                        // Ideally we would throw an exception, unfortunately
                        // often times there are errors as an initial look and
                        // feel is loaded before one can be switched. Perhaps a
                        // flag should be added for debugging, so that if true
                        // the exception would be thrown.
                    }
                    return null;
                }
            },acc);
        }

        private Class[] getClassArray(Object[] args){
            Class[] types=null;
            if(args!=null){
                types=new Class[args.length];
                for(int i=0;i<args.length;i++){
                    /** PENDING(ges): At present only the primitive types
                     used are handled correctly; this should eventually
                     handle all primitive types */
                    if(args[i] instanceof Integer){
                        types[i]=Integer.TYPE;
                    }else if(args[i] instanceof Boolean){
                        types[i]=Boolean.TYPE;
                    }else if(args[i] instanceof javax.swing.plaf.ColorUIResource){
                        /** PENDING(ges) Currently the Reflection APIs do not
                         search superclasses of parameters supplied for
                         constructor/method lookup.  Since we only have
                         one case where this is needed, we substitute
                         directly instead of adding a massive amount
                         of mechanism for this.  Eventually this will
                         probably need to handle the general case as well.
                         */
                        types[i]=Color.class;
                    }else{
                        types[i]=args[i].getClass();
                    }
                }
            }
            return types;
        }

        private String printArgs(Object[] array){
            String s="{";
            if(array!=null){
                for(int i=0;i<array.length-1;i++){
                    s=s.concat(array[i]+",");
                }
                s=s.concat(array[array.length-1]+"}");
            }else{
                s=s.concat("}");
            }
            return s;
        }
    }

    public static class LazyInputMap implements LazyValue{
        private Object[] bindings;

        public LazyInputMap(Object[] bindings){
            this.bindings=bindings;
        }

        public Object createValue(UIDefaults table){
            if(bindings!=null){
                InputMap km=LookAndFeel.makeInputMap(bindings);
                return km;
            }
            return null;
        }
    }

    private static class TextAndMnemonicHashMap extends HashMap<String,Object>{
        static final String AND_MNEMONIC="AndMnemonic";
        static final String TITLE_SUFFIX=".titleAndMnemonic";
        static final String TEXT_SUFFIX=".textAndMnemonic";

        @Override
        public Object get(Object key){
            Object value=super.get(key);
            if(value==null){
                boolean checkTitle=false;
                String stringKey=key.toString();
                String compositeKey=null;
                if(stringKey.endsWith(AND_MNEMONIC)){
                    return null;
                }
                if(stringKey.endsWith(".mnemonic")){
                    compositeKey=composeKey(stringKey,9,TEXT_SUFFIX);
                }else if(stringKey.endsWith("NameMnemonic")){
                    compositeKey=composeKey(stringKey,12,TEXT_SUFFIX);
                }else if(stringKey.endsWith("Mnemonic")){
                    compositeKey=composeKey(stringKey,8,TEXT_SUFFIX);
                    checkTitle=true;
                }
                if(compositeKey!=null){
                    value=super.get(compositeKey);
                    if(value==null&&checkTitle){
                        compositeKey=composeKey(stringKey,8,TITLE_SUFFIX);
                        value=super.get(compositeKey);
                    }
                    return value==null?null:getMnemonicFromProperty(value.toString());
                }
                if(stringKey.endsWith("NameText")){
                    compositeKey=composeKey(stringKey,8,TEXT_SUFFIX);
                }else if(stringKey.endsWith(".nameText")){
                    compositeKey=composeKey(stringKey,9,TEXT_SUFFIX);
                }else if(stringKey.endsWith("Text")){
                    compositeKey=composeKey(stringKey,4,TEXT_SUFFIX);
                }else if(stringKey.endsWith("Title")){
                    compositeKey=composeKey(stringKey,5,TITLE_SUFFIX);
                }
                if(compositeKey!=null){
                    value=super.get(compositeKey);
                    return value==null?null:getTextFromProperty(value.toString());
                }
                if(stringKey.endsWith("DisplayedMnemonicIndex")){
                    compositeKey=composeKey(stringKey,22,TEXT_SUFFIX);
                    value=super.get(compositeKey);
                    if(value==null){
                        compositeKey=composeKey(stringKey,22,TITLE_SUFFIX);
                        value=super.get(compositeKey);
                    }
                    return value==null?null:getIndexFromProperty(value.toString());
                }
            }
            return value;
        }

        String composeKey(String key,int reduce,String sufix){
            return key.substring(0,key.length()-reduce)+sufix;
        }

        String getTextFromProperty(String text){
            return text.replace("&","");
        }

        String getMnemonicFromProperty(String text){
            int index=text.indexOf('&');
            if(0<=index&&index<text.length()-1){
                char c=text.charAt(index+1);
                return Integer.toString((int)Character.toUpperCase(c));
            }
            return null;
        }

        String getIndexFromProperty(String text){
            int index=text.indexOf('&');
            return (index==-1)?null:Integer.toString(index);
        }
    }
}

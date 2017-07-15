/**
 * Copyright (c) 2002, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.ActionMapUIResource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class LazyActionMap extends ActionMapUIResource{
    private transient Object _loader;

    private LazyActionMap(Class loader){
        _loader=loader;
    }

    static void installLazyActionMap(JComponent c,Class loaderClass,
                                     String defaultsKey){
        ActionMap map=(ActionMap)UIManager.get(defaultsKey);
        if(map==null){
            map=new LazyActionMap(loaderClass);
            UIManager.getLookAndFeelDefaults().put(defaultsKey,map);
        }
        SwingUtilities.replaceUIActionMap(c,map);
    }

    static ActionMap getActionMap(Class loaderClass,
                                  String defaultsKey){
        ActionMap map=(ActionMap)UIManager.get(defaultsKey);
        if(map==null){
            map=new LazyActionMap(loaderClass);
            UIManager.getLookAndFeelDefaults().put(defaultsKey,map);
        }
        return map;
    }

    public void put(Action action){
        put(action.getValue(Action.NAME),action);
    }

    public void setParent(ActionMap map){
        loadIfNecessary();
        super.setParent(map);
    }

    public void put(Object key,Action action){
        loadIfNecessary();
        super.put(key,action);
    }

    public Action get(Object key){
        loadIfNecessary();
        return super.get(key);
    }

    public void remove(Object key){
        loadIfNecessary();
        super.remove(key);
    }

    public void clear(){
        loadIfNecessary();
        super.clear();
    }

    public Object[] keys(){
        loadIfNecessary();
        return super.keys();
    }

    public int size(){
        loadIfNecessary();
        return super.size();
    }

    public Object[] allKeys(){
        loadIfNecessary();
        return super.allKeys();
    }

    private void loadIfNecessary(){
        if(_loader!=null){
            Object loader=_loader;
            _loader=null;
            Class<?> klass=(Class<?>)loader;
            try{
                Method method=klass.getDeclaredMethod("loadActionMap",
                        new Class[]{LazyActionMap.class});
                method.invoke(klass,new Object[]{this});
            }catch(NoSuchMethodException nsme){
                assert false:"LazyActionMap unable to load actions "+
                        klass;
            }catch(IllegalAccessException iae){
                assert false:"LazyActionMap unable to load actions "+
                        iae;
            }catch(InvocationTargetException ite){
                assert false:"LazyActionMap unable to load actions "+
                        ite;
            }catch(IllegalArgumentException iae){
                assert false:"LazyActionMap unable to load actions "+
                        iae;
            }
        }
    }
}

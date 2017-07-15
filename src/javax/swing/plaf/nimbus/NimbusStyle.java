/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;

import static javax.swing.plaf.synth.SynthConstants.*;

public final class NimbusStyle extends SynthStyle{
    public static final String LARGE_KEY="large";
    public static final String SMALL_KEY="small";
    public static final String MINI_KEY="mini";
    public static final double LARGE_SCALE=1.15;
    public static final double SMALL_SCALE=0.857;
    public static final double MINI_SCALE=0.714;
    private static final Object NULL='\0';
    private static final Color DEFAULT_COLOR=new ColorUIResource(Color.BLACK);
    private static final Comparator<RuntimeState> STATE_COMPARATOR=
            new Comparator<RuntimeState>(){
                @Override
                public int compare(RuntimeState a,RuntimeState b){
                    return a.state-b.state;
                }
            };
    private String prefix;
    private SynthPainter painter;
    private Values values;
    private CacheKey tmpKey=new CacheKey("",0);
    private WeakReference<JComponent> component;

    NimbusStyle(String prefix,JComponent c){
        if(c!=null){
            this.component=new WeakReference<JComponent>(c);
        }
        this.prefix=prefix;
        this.painter=new SynthPainterImpl(this);
    }

    @Override
    protected Color getColorForState(SynthContext ctx,ColorType type){
        String key=null;
        if(type==ColorType.BACKGROUND){
            key="background";
        }else if(type==ColorType.FOREGROUND){
            //map FOREGROUND as TEXT_FOREGROUND
            key="textForeground";
        }else if(type==ColorType.TEXT_BACKGROUND){
            key="textBackground";
        }else if(type==ColorType.TEXT_FOREGROUND){
            key="textForeground";
        }else if(type==ColorType.FOCUS){
            key="focus";
        }else if(type!=null){
            key=type.toString();
        }else{
            return DEFAULT_COLOR;
        }
        Color c=(Color)get(ctx,key);
        //if all else fails, return a default color (which is a ColorUIResource)
        if(c==null) c=DEFAULT_COLOR;
        return c;
    }

    @Override
    protected Font getFontForState(SynthContext ctx){
        Font f=(Font)get(ctx,"font");
        if(f==null) f=UIManager.getFont("defaultFont");
        // Account for scale
        // The key "JComponent.sizeVariant" is used to match Apple's LAF
        String scaleKey=(String)ctx.getComponent().getClientProperty(
                "JComponent.sizeVariant");
        if(scaleKey!=null){
            if(LARGE_KEY.equals(scaleKey)){
                f=f.deriveFont(Math.round(f.getSize2D()*LARGE_SCALE));
            }else if(SMALL_KEY.equals(scaleKey)){
                f=f.deriveFont(Math.round(f.getSize2D()*SMALL_SCALE));
            }else if(MINI_KEY.equals(scaleKey)){
                f=f.deriveFont(Math.round(f.getSize2D()*MINI_SCALE));
            }
        }
        return f;
    }

    @Override
    public Insets getInsets(SynthContext ctx,Insets in){
        if(in==null){
            in=new Insets(0,0,0,0);
        }
        Values v=getValues(ctx);
        if(v.contentMargins==null){
            in.bottom=in.top=in.left=in.right=0;
            return in;
        }else{
            in.bottom=v.contentMargins.bottom;
            in.top=v.contentMargins.top;
            in.left=v.contentMargins.left;
            in.right=v.contentMargins.right;
            // Account for scale
            // The key "JComponent.sizeVariant" is used to match Apple's LAF
            String scaleKey=(String)ctx.getComponent().getClientProperty(
                    "JComponent.sizeVariant");
            if(scaleKey!=null){
                if(LARGE_KEY.equals(scaleKey)){
                    in.bottom*=LARGE_SCALE;
                    in.top*=LARGE_SCALE;
                    in.left*=LARGE_SCALE;
                    in.right*=LARGE_SCALE;
                }else if(SMALL_KEY.equals(scaleKey)){
                    in.bottom*=SMALL_SCALE;
                    in.top*=SMALL_SCALE;
                    in.left*=SMALL_SCALE;
                    in.right*=SMALL_SCALE;
                }else if(MINI_KEY.equals(scaleKey)){
                    in.bottom*=MINI_SCALE;
                    in.top*=MINI_SCALE;
                    in.left*=MINI_SCALE;
                    in.right*=MINI_SCALE;
                }
            }
            return in;
        }
    }

    @Override
    public SynthPainter getPainter(SynthContext ctx){
        return painter;
    }

    @Override
    public boolean isOpaque(SynthContext ctx){
        // Force Table CellRenderers to be opaque
        if("Table.cellRenderer".equals(ctx.getComponent().getName())){
            return true;
        }
        Boolean opaque=(Boolean)get(ctx,"opaque");
        return opaque==null?false:opaque;
    }

    @Override
    public Object get(SynthContext ctx,Object key){
        Values v=getValues(ctx);
        // strip off the prefix, if there is one.
        String fullKey=key.toString();
        String partialKey=fullKey.substring(fullKey.indexOf(".")+1);
        Object obj=null;
        int xstate=getExtendedState(ctx,v);
        // check the cache
        tmpKey.init(partialKey,xstate);
        obj=v.cache.get(tmpKey);
        boolean wasInCache=obj!=null;
        if(!wasInCache){
            // Search exact matching states and then lesser matching states
            RuntimeState s=null;
            int[] lastIndex=new int[]{-1};
            while(obj==null&&
                    (s=getNextState(v.states,lastIndex,xstate))!=null){
                obj=s.defaults.get(partialKey);
            }
            // Search Region Defaults
            if(obj==null&&v.defaults!=null){
                obj=v.defaults.get(partialKey);
            }
            // return found object
            // Search UIManager Defaults
            if(obj==null) obj=UIManager.get(fullKey);
            // Search Synth Defaults for InputMaps
            if(obj==null&&partialKey.equals("focusInputMap")){
                obj=super.get(ctx,fullKey);
            }
            // if all we got was a null, store this fact for later use
            v.cache.put(new CacheKey(partialKey,xstate),
                    obj==null?NULL:obj);
        }
        // return found object
        return obj==NULL?null:obj;
    }

    @Override
    public void installDefaults(SynthContext ctx){
        validate();
        //delegate to the superclass to install defaults such as background,
        //foreground, font, and opaque onto the swing component.
        super.installDefaults(ctx);
    }

    private void validate(){
        // a non-null values object is the flag we use to determine whether
        // to reparse from UIManager.
        if(values!=null) return;
        // reconstruct this NimbusStyle based on the entries in the UIManager
        // and possibly based on any overrides within the component's
        // client properties (assuming such a component exists and contains
        // any Nimbus.Overrides)
        values=new Values();
        Map<String,Object> defaults=
                ((NimbusLookAndFeel)UIManager.getLookAndFeel()).
                        getDefaultsForPrefix(prefix);
        // inspect the client properties for the key "Nimbus.Overrides". If the
        // value is an instance of UIDefaults, then these defaults are used
        // in place of, or in addition to, the defaults in UIManager.
        if(component!=null){
            // We know component.get() is non-null here, as if the component
            // were GC'ed, we wouldn't be processing its style.
            Object o=component.get().getClientProperty("Nimbus.Overrides");
            if(o instanceof UIDefaults){
                Object i=component.get().getClientProperty(
                        "Nimbus.Overrides.InheritDefaults");
                boolean inherit=i instanceof Boolean?(Boolean)i:true;
                UIDefaults d=(UIDefaults)o;
                TreeMap<String,Object> map=new TreeMap<String,Object>();
                for(Object obj : d.keySet()){
                    if(obj instanceof String){
                        String key=(String)obj;
                        if(key.startsWith(prefix)){
                            map.put(key,d.get(key));
                        }
                    }
                }
                if(inherit){
                    defaults.putAll(map);
                }else{
                    defaults=map;
                }
            }
        }
        //a list of the different types of states used by this style. This
        //list may contain only "standard" states (those defined by Synth),
        //or it may contain custom states, or it may contain only "standard"
        //states but list them in a non-standard order.
        List<State> states=new ArrayList<State>();
        //a map of state name to code
        Map<String,Integer> stateCodes=new HashMap<String,Integer>();
        //This is a list of runtime "state" context objects. These contain
        //the values associated with each state.
        List<RuntimeState> runtimeStates=new ArrayList<RuntimeState>();
        //determine whether there are any custom states, or custom state
        //order. If so, then read all those custom states and define the
        //"values" stateTypes to be a non-null array.
        //Otherwise, let the "values" stateTypes be null to indicate that
        //there are no custom states or custom state ordering
        String statesString=(String)defaults.get(prefix+".States");
        if(statesString!=null){
            String s[]=statesString.split(",");
            for(int i=0;i<s.length;i++){
                s[i]=s[i].trim();
                if(!State.isStandardStateName(s[i])){
                    //this is a non-standard state name, so look for the
                    //custom state associated with it
                    String stateName=prefix+"."+s[i];
                    State customState=(State)defaults.get(stateName);
                    if(customState!=null){
                        states.add(customState);
                    }
                }else{
                    states.add(State.getStandardState(s[i]));
                }
            }
            //if there were any states defined, then set the stateTypes array
            //to be non-null. Otherwise, leave it null (meaning, use the
            //standard synth states).
            if(states.size()>0){
                values.stateTypes=states.toArray(new State[states.size()]);
            }
            //assign codes for each of the state types
            int code=1;
            for(State state : states){
                stateCodes.put(state.getName(),code);
                code<<=1;
            }
        }else{
            //since there were no custom states defined, setup the list of
            //standard synth states. Note that the "v.stateTypes" is not
            //being set here, indicating that at runtime the state selection
            //routines should use standard synth states instead of custom
            //states. I do need to popuplate this temp list now though, so that
            //the remainder of this method will function as expected.
            states.add(State.Enabled);
            states.add(State.MouseOver);
            states.add(State.Pressed);
            states.add(State.Disabled);
            states.add(State.Focused);
            states.add(State.Selected);
            states.add(State.Default);
            //assign codes for the states
            stateCodes.put("Enabled",ENABLED);
            stateCodes.put("MouseOver",MOUSE_OVER);
            stateCodes.put("Pressed",PRESSED);
            stateCodes.put("Disabled",DISABLED);
            stateCodes.put("Focused",FOCUSED);
            stateCodes.put("Selected",SELECTED);
            stateCodes.put("Default",DEFAULT);
        }
        //Now iterate over all the keys in the defaults table
        for(String key : defaults.keySet()){
            //The key is something like JButton.Enabled.backgroundPainter,
            //or JButton.States, or JButton.background.
            //Remove the "JButton." portion of the key
            String temp=key.substring(prefix.length());
            //if there is a " or : then we skip it because it is a subregion
            //of some kind
            if(temp.indexOf('"')!=-1||temp.indexOf(':')!=-1) continue;
            //remove the separator
            temp=temp.substring(1);
            //At this point, temp may be any of the following:
            //background
            //[Enabled].background
            //[Enabled+MouseOver].background
            //property.foo
            //parse out the states and the property
            String stateString=null;
            String property=null;
            int bracketIndex=temp.indexOf(']');
            if(bracketIndex<0){
                //there is not a state string, so property = temp
                property=temp;
            }else{
                stateString=temp.substring(0,bracketIndex);
                property=temp.substring(bracketIndex+2);
            }
            //now that I have the state (if any) and the property, get the
            //value for this property and install it where it belongs
            if(stateString==null){
                //there was no state, just a property. Check for the custom
                //"contentMargins" property (which is handled specially by
                //Synth/Nimbus). Also check for the property being "States",
                //in which case it is not a real property and should be ignored.
                //otherwise, assume it is a property and install it on the
                //values object
                if("contentMargins".equals(property)){
                    values.contentMargins=(Insets)defaults.get(key);
                }else if("States".equals(property)){
                    //ignore
                }else{
                    values.defaults.put(property,defaults.get(key));
                }
            }else{
                //it is possible that the developer has a malformed UIDefaults
                //entry, such that something was specified in the place of
                //the State portion of the key but it wasn't a state. In this
                //case, skip will be set to true
                boolean skip=false;
                //this variable keeps track of the int value associated with
                //the state. See SynthState for details.
                int componentState=0;
                //Multiple states may be specified in the string, such as
                //Enabled+MouseOver
                String[] stateParts=stateString.split("\\+");
                //For each state, we need to find the State object associated
                //with it, or skip it if it cannot be found.
                for(String s : stateParts){
                    if(stateCodes.containsKey(s)){
                        componentState|=stateCodes.get(s);
                    }else{
                        //Was not a state. Maybe it was a subregion or something
                        //skip it.
                        skip=true;
                        break;
                    }
                }
                if(skip) continue;
                //find the RuntimeState for this State
                RuntimeState rs=null;
                for(RuntimeState s : runtimeStates){
                    if(s.state==componentState){
                        rs=s;
                        break;
                    }
                }
                //couldn't find the runtime state, so create a new one
                if(rs==null){
                    rs=new RuntimeState(componentState,stateString);
                    runtimeStates.add(rs);
                }
                //check for a couple special properties, such as for the
                //painters. If these are found, then set the specially on
                //the runtime state. Else, it is just a normal property,
                //so put it in the UIDefaults associated with that runtime
                //state
                if("backgroundPainter".equals(property)){
                    rs.backgroundPainter=getPainter(defaults,key);
                }else if("foregroundPainter".equals(property)){
                    rs.foregroundPainter=getPainter(defaults,key);
                }else if("borderPainter".equals(property)){
                    rs.borderPainter=getPainter(defaults,key);
                }else{
                    rs.defaults.put(property,defaults.get(key));
                }
            }
        }
        //now that I've collected all the runtime states, I'll sort them based
        //on their integer "state" (see SynthState for how this works).
        Collections.sort(runtimeStates,STATE_COMPARATOR);
        //finally, set the array of runtime states on the values object
        values.states=runtimeStates.toArray(new RuntimeState[runtimeStates.size()]);
    }

    private Painter getPainter(Map<String,Object> defaults,String key){
        Object p=defaults.get(key);
        if(p instanceof UIDefaults.LazyValue){
            p=((UIDefaults.LazyValue)p).createValue(UIManager.getDefaults());
        }
        return (p instanceof Painter?(Painter)p:null);
    }

    private int getExtendedState(SynthContext ctx,Values v){
        JComponent c=ctx.getComponent();
        int xstate=0;
        int mask=1;
        //check for the Nimbus.State client property
        //Performance NOTE: getClientProperty ends up inside a synchronized
        //block, so there is some potential for performance issues here, however
        //I'm not certain that there is one on a modern VM.
        Object property=c.getClientProperty("Nimbus.State");
        if(property!=null){
            String stateNames=property.toString();
            String[] states=stateNames.split("\\+");
            if(v.stateTypes==null){
                // standard states only
                for(String stateStr : states){
                    State.StandardState s=State.getStandardState(stateStr);
                    if(s!=null) xstate|=s.getState();
                }
            }else{
                // custom states
                for(State s : v.stateTypes){
                    if(contains(states,s.getName())){
                        xstate|=mask;
                    }
                    mask<<=1;
                }
            }
        }else{
            //if there are no custom states defined, then simply return the
            //state that Synth reported
            if(v.stateTypes==null) return ctx.getComponentState();
            //there are custom states on this values, so I'll have to iterate
            //over them all and return a custom extended state
            int state=ctx.getComponentState();
            for(State s : v.stateTypes){
                if(s.isInState(c,state)){
                    xstate|=mask;
                }
                mask<<=1;
            }
        }
        return xstate;
    }

    private boolean contains(String[] names,String name){
        assert name!=null;
        for(int i=0;i<names.length;i++){
            if(name.equals(names[i])){
                return true;
            }
        }
        return false;
    }

    private RuntimeState getNextState(RuntimeState[] states,
                                      int[] lastState,
                                      int xstate){
        // Use the StateInfo with the most bits that matches that of state.
        // If there are none, then fallback to
        // the StateInfo with a state of 0, indicating it'll match anything.
        // Consider if we have 3 StateInfos a, b and c with states:
        // SELECTED, SELECTED | ENABLED, 0
        //
        // Input                          Return Value
        // -----                          ------------
        // SELECTED                       a
        // SELECTED | ENABLED             b
        // MOUSE_OVER                     c
        // SELECTED | ENABLED | FOCUSED   b
        // ENABLED                        c
        if(states!=null&&states.length>0){
            int bestCount=0;
            int bestIndex=-1;
            int wildIndex=-1;
            //if xstate is 0, then search for the runtime state with component
            //state of 0. That is, find the exact match and return it.
            if(xstate==0){
                for(int counter=states.length-1;counter>=0;counter--){
                    if(states[counter].state==0){
                        lastState[0]=counter;
                        return states[counter];
                    }
                }
                //an exact match couldn't be found, so there was no match.
                lastState[0]=-1;
                return null;
            }
            //xstate is some value != 0
            //determine from which index to start looking. If lastState[0] is -1
            //then we know to start from the end of the state array. Otherwise,
            //we start at the lastIndex - 1.
            int lastStateIndex=lastState==null||lastState[0]==-1?
                    states.length:lastState[0];
            for(int counter=lastStateIndex-1;counter>=0;counter--){
                int oState=states[counter].state;
                if(oState==0){
                    if(wildIndex==-1){
                        wildIndex=counter;
                    }
                }else if((xstate&oState)==oState){
                    // This is key, we need to make sure all bits of the
                    // StateInfo match, otherwise a StateInfo with
                    // SELECTED | ENABLED would match ENABLED, which we
                    // don't want.
                    // This comes from BigInteger.bitCnt
                    int bitCount=oState;
                    bitCount-=(0xaaaaaaaa&bitCount)>>>1;
                    bitCount=(bitCount&0x33333333)+((bitCount>>>2)&
                            0x33333333);
                    bitCount=bitCount+(bitCount>>>4)&0x0f0f0f0f;
                    bitCount+=bitCount>>>8;
                    bitCount+=bitCount>>>16;
                    bitCount=bitCount&0xff;
                    if(bitCount>bestCount){
                        bestIndex=counter;
                        bestCount=bitCount;
                    }
                }
            }
            if(bestIndex!=-1){
                lastState[0]=bestIndex;
                return states[bestIndex];
            }
            if(wildIndex!=-1){
                lastState[0]=wildIndex;
                return states[wildIndex];
            }
        }
        lastState[0]=-1;
        return null;
    }

    private Values getValues(SynthContext ctx){
        validate();
        return values;
    }

    public Painter getBackgroundPainter(SynthContext ctx){
        Values v=getValues(ctx);
        int xstate=getExtendedState(ctx,v);
        Painter p=null;
        // check the cache
        tmpKey.init("backgroundPainter$$instance",xstate);
        p=(Painter)v.cache.get(tmpKey);
        if(p!=null) return p;
        // not in cache, so lookup and store in cache
        RuntimeState s=null;
        int[] lastIndex=new int[]{-1};
        while((s=getNextState(v.states,lastIndex,xstate))!=null){
            if(s.backgroundPainter!=null){
                p=s.backgroundPainter;
                break;
            }
        }
        if(p==null) p=(Painter)get(ctx,"backgroundPainter");
        if(p!=null){
            v.cache.put(new CacheKey("backgroundPainter$$instance",xstate),p);
        }
        return p;
    }

    public Painter getForegroundPainter(SynthContext ctx){
        Values v=getValues(ctx);
        int xstate=getExtendedState(ctx,v);
        Painter p=null;
        // check the cache
        tmpKey.init("foregroundPainter$$instance",xstate);
        p=(Painter)v.cache.get(tmpKey);
        if(p!=null) return p;
        // not in cache, so lookup and store in cache
        RuntimeState s=null;
        int[] lastIndex=new int[]{-1};
        while((s=getNextState(v.states,lastIndex,xstate))!=null){
            if(s.foregroundPainter!=null){
                p=s.foregroundPainter;
                break;
            }
        }
        if(p==null) p=(Painter)get(ctx,"foregroundPainter");
        if(p!=null){
            v.cache.put(new CacheKey("foregroundPainter$$instance",xstate),p);
        }
        return p;
    }

    public Painter getBorderPainter(SynthContext ctx){
        Values v=getValues(ctx);
        int xstate=getExtendedState(ctx,v);
        Painter p=null;
        // check the cache
        tmpKey.init("borderPainter$$instance",xstate);
        p=(Painter)v.cache.get(tmpKey);
        if(p!=null) return p;
        // not in cache, so lookup and store in cache
        RuntimeState s=null;
        int[] lastIndex=new int[]{-1};
        while((s=getNextState(v.states,lastIndex,xstate))!=null){
            if(s.borderPainter!=null){
                p=s.borderPainter;
                break;
            }
        }
        if(p==null) p=(Painter)get(ctx,"borderPainter");
        if(p!=null){
            v.cache.put(new CacheKey("borderPainter$$instance",xstate),p);
        }
        return p;
    }

    private static final class Values{
        State[] stateTypes=null;
        RuntimeState[] states=null;
        Insets contentMargins;
        UIDefaults defaults=new UIDefaults(10,.7f);
        Map<CacheKey,Object> cache=new HashMap<CacheKey,Object>();
    }

    private static final class CacheKey{
        private String key;
        private int xstate;

        CacheKey(Object key,int xstate){
            init(key,xstate);
        }

        void init(Object key,int xstate){
            this.key=key.toString();
            this.xstate=xstate;
        }

        @Override
        public int hashCode(){
            int hash=3;
            hash=29*hash+this.key.hashCode();
            hash=29*hash+this.xstate;
            return hash;
        }

        @Override
        public boolean equals(Object obj){
            final CacheKey other=(CacheKey)obj;
            if(obj==null) return false;
            if(this.xstate!=other.xstate) return false;
            if(!this.key.equals(other.key)) return false;
            return true;
        }
    }

    private final class RuntimeState implements Cloneable{
        int state;
        Painter backgroundPainter;
        Painter foregroundPainter;
        Painter borderPainter;
        String stateName;
        UIDefaults defaults=new UIDefaults(10,.7f);

        private RuntimeState(int state,String stateName){
            this.state=state;
            this.stateName=stateName;
        }

        @Override
        public String toString(){
            return stateName;
        }

        @Override
        public RuntimeState clone(){
            RuntimeState clone=new RuntimeState(state,stateName);
            clone.backgroundPainter=backgroundPainter;
            clone.foregroundPainter=foregroundPainter;
            clone.borderPainter=borderPainter;
            clone.defaults.putAll(defaults);
            return clone;
        }
    }
}

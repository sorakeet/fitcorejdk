/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public interface Line extends AutoCloseable{
    public Info getLineInfo();

    public void open() throws LineUnavailableException;

    public void close();

    public boolean isOpen();

    public Control[] getControls();

    public boolean isControlSupported(Control.Type control);

    public Control getControl(Control.Type control);

    public void addLineListener(LineListener listener);

    public void removeLineListener(LineListener listener);

    public static class Info{
        private final Class lineClass;

        public Info(Class<?> lineClass){
            if(lineClass==null){
                this.lineClass=Line.class;
            }else{
                this.lineClass=lineClass;
            }
        }

        public boolean matches(Info info){
            // $$kk: 08.30.99: is this backwards?
            // dataLine.matches(targetDataLine) == true: targetDataLine is always dataLine
            // targetDataLine.matches(dataLine) == false
            // so if i want to make sure i get a targetDataLine, i need:
            // targetDataLine.matches(prospective_match) == true
            // => prospective_match may be other things as well, but it is at least a targetDataLine
            // targetDataLine defines the requirements which prospective_match must meet.
            // "if this Class object represents a declared class, this method returns
            // true if the specified Object argument is an instance of the represented
            // class (or of any of its subclasses)"
            // GainControlClass.isInstance(MyGainObj) => true
            // GainControlClass.isInstance(MySpecialGainInterfaceObj) => true
            // this_class.isInstance(that_object)       => that object can by cast to this class
            //                                                                          => that_object's class may be a subtype of this_class
            //                                                                          => that may be more specific (subtype) of this
            // "If this Class object represents an interface, this method returns true
            // if the class or any superclass of the specified Object argument implements
            // this interface"
            // GainControlClass.isInstance(MyGainObj) => true
            // GainControlClass.isInstance(GenericControlObj) => may be false
            // => that may be more specific
            if(!(this.getClass().isInstance(info))){
                return false;
            }
            // this.isAssignableFrom(that)  =>  this is same or super to that
            //                                                          =>      this is at least as general as that
            //                                                          =>      that may be subtype of this
            if(!(getLineClass().isAssignableFrom(info.getLineClass()))){
                return false;
            }
            return true;
        }

        public Class<?> getLineClass(){
            return lineClass;
        }

        public String toString(){
            String fullPackagePath="javax.sound.sampled.";
            String initialString=new String(getLineClass().toString());
            String finalString;
            int index=initialString.indexOf(fullPackagePath);
            if(index!=-1){
                finalString=initialString.substring(0,index)+initialString.substring((index+fullPackagePath.length()),initialString.length());
            }else{
                finalString=initialString;
            }
            return finalString;
        }
    } // class Info
} // interface Line

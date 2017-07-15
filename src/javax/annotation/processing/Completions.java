/**
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.annotation.processing;

public class Completions{
    // No instances for you.
    private Completions(){
    }

    public static Completion of(String value,String message){
        return new SimpleCompletion(value,message);
    }

    public static Completion of(String value){
        return new SimpleCompletion(value,"");
    }

    private static class SimpleCompletion implements Completion{
        private String value;
        private String message;

        SimpleCompletion(String value,String message){
            if(value==null||message==null)
                throw new NullPointerException("Null completion strings not accepted.");
            this.value=value;
            this.message=message;
        }

        public String getValue(){
            return value;
        }

        public String getMessage(){
            return message;
        }

        @Override
        public String toString(){
            return "[\""+value+"\", \""+message+"\"]";
        }
        // Default equals and hashCode are fine.
    }
}

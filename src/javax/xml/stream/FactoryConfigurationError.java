/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
/** Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
package javax.xml.stream;

public class FactoryConfigurationError extends Error{
    private static final long serialVersionUID=-2994412584589975744L;
    Exception nested;

    public FactoryConfigurationError(){
    }

    public FactoryConfigurationError(Exception e){
        nested=e;
    }

    public FactoryConfigurationError(Exception e,String msg){
        super(msg);
        nested=e;
    }

    public FactoryConfigurationError(String msg,Exception e){
        super(msg);
        nested=e;
    }

    public FactoryConfigurationError(String msg){
        super(msg);
    }

    public Exception getException(){
        return nested;
    }

    public String getMessage(){
        String msg=super.getMessage();
        if(msg!=null)
            return msg;
        if(nested!=null){
            msg=nested.getMessage();
            if(msg==null)
                msg=nested.getClass().toString();
        }
        return msg;
    }

    @Override
    public Throwable getCause(){
        return nested;
    }
}

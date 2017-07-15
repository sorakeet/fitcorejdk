/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: DTMException.java,v 1.3 2005/09/28 13:48:50 pvedula Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: DTMException.java,v 1.3 2005/09/28 13:48:50 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;

import javax.xml.transform.SourceLocator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DTMException extends RuntimeException{
    static final long serialVersionUID=-775576419181334734L;
    SourceLocator locator;
    Throwable containedException;

    public DTMException(String message){
        super(message);
        this.containedException=null;
        this.locator=null;
    }

    public DTMException(Throwable e){
        super(e.getMessage());
        this.containedException=e;
        this.locator=null;
    }

    public DTMException(String message,Throwable e){
        super(((message==null)||(message.length()==0))
                ?e.getMessage()
                :message);
        this.containedException=e;
        this.locator=null;
    }

    public DTMException(String message,SourceLocator locator){
        super(message);
        this.containedException=null;
        this.locator=locator;
    }

    public DTMException(String message,SourceLocator locator,
                        Throwable e){
        super(message);
        this.containedException=e;
        this.locator=locator;
    }

    public SourceLocator getLocator(){
        return locator;
    }

    public void setLocator(SourceLocator location){
        locator=location;
    }

    public Throwable getCause(){
        return ((containedException==this)
                ?null
                :containedException);
    }

    public synchronized Throwable initCause(Throwable cause){
        if((this.containedException==null)&&(cause!=null)){
            throw new IllegalStateException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CANNOT_OVERWRITE_CAUSE,null)); //"Can't overwrite cause");
        }
        if(cause==this){
            throw new IllegalArgumentException(
                    XMLMessages.createXMLMessage(XMLErrorResources.ER_SELF_CAUSATION_NOT_PERMITTED,null)); //"Self-causation not permitted");
        }
        this.containedException=cause;
        return this;
    }

    public void printStackTrace(){
        printStackTrace(new java.io.PrintWriter(System.err,true));
    }

    public void printStackTrace(java.io.PrintStream s){
        printStackTrace(new java.io.PrintWriter(s));
    }

    public void printStackTrace(java.io.PrintWriter s){
        if(s==null){
            s=new java.io.PrintWriter(System.err,true);
        }
        try{
            String locInfo=getLocationAsString();
            if(null!=locInfo){
                s.println(locInfo);
            }
            super.printStackTrace(s);
        }catch(Throwable e){
        }
        boolean isJdk14OrHigher=false;
        try{
            Throwable.class.getMethod("getCause",(Class[])null);
            isJdk14OrHigher=true;
        }catch(NoSuchMethodException nsme){
            // do nothing
        }
        // The printStackTrace method of the Throwable class in jdk 1.4
        // and higher will include the cause when printing the backtrace.
        // The following code is only required when using jdk 1.3 or lower
        if(!isJdk14OrHigher){
            Throwable exception=getException();
            for(int i=0;(i<10)&&(null!=exception);i++){
                s.println("---------");
                try{
                    if(exception instanceof DTMException){
                        String locInfo=
                                ((DTMException)exception)
                                        .getLocationAsString();
                        if(null!=locInfo){
                            s.println(locInfo);
                        }
                    }
                    exception.printStackTrace(s);
                }catch(Throwable e){
                    s.println("Could not print stack trace...");
                }
                try{
                    Method meth=
                            ((Object)exception).getClass().getMethod("getException",
                                    (Class[])null);
                    if(null!=meth){
                        Throwable prev=exception;
                        exception=(Throwable)meth.invoke(exception,(Object[])null);
                        if(prev==exception){
                            break;
                        }
                    }else{
                        exception=null;
                    }
                }catch(InvocationTargetException ite){
                    exception=null;
                }catch(IllegalAccessException iae){
                    exception=null;
                }catch(NoSuchMethodException nsme){
                    exception=null;
                }
            }
        }
    }

    public Throwable getException(){
        return containedException;
    }

    public String getLocationAsString(){
        if(null!=locator){
            StringBuffer sbuffer=new StringBuffer();
            String systemID=locator.getSystemId();
            int line=locator.getLineNumber();
            int column=locator.getColumnNumber();
            if(null!=systemID){
                sbuffer.append("; SystemID: ");
                sbuffer.append(systemID);
            }
            if(0!=line){
                sbuffer.append("; Line#: ");
                sbuffer.append(line);
            }
            if(0!=column){
                sbuffer.append("; Column#: ");
                sbuffer.append(column);
            }
            return sbuffer.toString();
        }else{
            return null;
        }
    }

    public String getMessageAndLocation(){
        StringBuffer sbuffer=new StringBuffer();
        String message=super.getMessage();
        if(null!=message){
            sbuffer.append(message);
        }
        if(null!=locator){
            String systemID=locator.getSystemId();
            int line=locator.getLineNumber();
            int column=locator.getColumnNumber();
            if(null!=systemID){
                sbuffer.append("; SystemID: ");
                sbuffer.append(systemID);
            }
            if(0!=line){
                sbuffer.append("; Line#: ");
                sbuffer.append(line);
            }
            if(0!=column){
                sbuffer.append("; Column#: ");
                sbuffer.append(column);
            }
        }
        return sbuffer.toString();
    }
}

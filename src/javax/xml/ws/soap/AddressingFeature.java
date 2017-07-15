/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.soap;

import javax.xml.ws.WebServiceFeature;

public final class AddressingFeature extends WebServiceFeature{
    public static final String ID="http://www.w3.org/2005/08/addressing/module";
    private final Responses responses;
    // should be private final, keeping original modifier due to backwards compatibility
    protected boolean required;

    public AddressingFeature(){
        this(true,false,Responses.ALL);
    }

    public AddressingFeature(boolean enabled,boolean required,Responses responses){
        this.enabled=enabled;
        this.required=required;
        this.responses=responses;
    }

    public AddressingFeature(boolean enabled){
        this(enabled,false,Responses.ALL);
    }

    public AddressingFeature(boolean enabled,boolean required){
        this(enabled,required,Responses.ALL);
    }

    public String getID(){
        return ID;
    }

    public boolean isRequired(){
        return required;
    }

    public Responses getResponses(){
        return responses;
    }

    public enum Responses{
        ANONYMOUS,
        NON_ANONYMOUS,
        ALL
    }
}

/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
 * <p>
 * $Id: HMACParameterSpec.java,v 1.4 2005/05/10 16:40:17 mullan Exp $
 */
/**
 * $Id: HMACParameterSpec.java,v 1.4 2005/05/10 16:40:17 mullan Exp $
 */
package javax.xml.crypto.dsig.spec;

public final class HMACParameterSpec implements SignatureMethodParameterSpec{
    private int outputLength;

    public HMACParameterSpec(int outputLength){
        this.outputLength=outputLength;
    }

    public int getOutputLength(){
        return outputLength;
    }
}

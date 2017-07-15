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
 * $Id: XSLTTransformParameterSpec.java,v 1.4 2005/05/10 16:40:18 mullan Exp $
 */
/**
 * $Id: XSLTTransformParameterSpec.java,v 1.4 2005/05/10 16:40:18 mullan Exp $
 */
package javax.xml.crypto.dsig.spec;

import javax.xml.crypto.XMLStructure;

public final class XSLTTransformParameterSpec implements TransformParameterSpec{
    private XMLStructure stylesheet;

    public XSLTTransformParameterSpec(XMLStructure stylesheet){
        if(stylesheet==null){
            throw new NullPointerException();
        }
        this.stylesheet=stylesheet;
    }

    public XMLStructure getStylesheet(){
        return stylesheet;
    }
}

/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 * <p>
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.awt.font;

import java.awt.geom.AffineTransform;
import java.io.ObjectStreamException;
import java.io.Serializable;

public final class TransformAttribute implements Serializable{
    public static final TransformAttribute IDENTITY=new TransformAttribute(null);
    // Added for serial backwards compatibility (4348425)
    static final long serialVersionUID=3356247357827709530L;
    private AffineTransform transform;

    public TransformAttribute(AffineTransform transform){
        if(transform!=null&&!transform.isIdentity()){
            this.transform=new AffineTransform(transform);
        }
    }

    public AffineTransform getTransform(){
        AffineTransform at=transform;
        return (at==null)?new AffineTransform():new AffineTransform(at);
    }

    public boolean isIdentity(){
        return transform==null;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws ClassNotFoundException,
            java.io.IOException{
        // sigh -- 1.3 expects transform is never null, so we need to always write one out
        if(this.transform==null){
            this.transform=new AffineTransform();
        }
        s.defaultWriteObject();
    }

    private Object readResolve() throws ObjectStreamException{
        if(transform==null||transform.isIdentity()){
            return IDENTITY;
        }
        return this;
    }

    public int hashCode(){
        return transform==null?0:transform.hashCode();
    }

    public boolean equals(Object rhs){
        if(rhs!=null){
            try{
                TransformAttribute that=(TransformAttribute)rhs;
                if(transform==null){
                    return that.transform==null;
                }
                return transform.equals(that.transform);
            }catch(ClassCastException e){
            }
        }
        return false;
    }
}

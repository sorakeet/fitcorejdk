/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;

import static javax.lang.model.SourceVersion.RELEASE_6;

@SupportedSourceVersion(RELEASE_6)
public class TypeKindVisitor6<R,P> extends SimpleTypeVisitor6<R,P>{
    protected TypeKindVisitor6(){
        super(null);
    }

    protected TypeKindVisitor6(R defaultValue){
        super(defaultValue);
    }

    @Override
    public R visitPrimitive(PrimitiveType t,P p){
        TypeKind k=t.getKind();
        switch(k){
            case BOOLEAN:
                return visitPrimitiveAsBoolean(t,p);
            case BYTE:
                return visitPrimitiveAsByte(t,p);
            case SHORT:
                return visitPrimitiveAsShort(t,p);
            case INT:
                return visitPrimitiveAsInt(t,p);
            case LONG:
                return visitPrimitiveAsLong(t,p);
            case CHAR:
                return visitPrimitiveAsChar(t,p);
            case FLOAT:
                return visitPrimitiveAsFloat(t,p);
            case DOUBLE:
                return visitPrimitiveAsDouble(t,p);
            default:
                throw new AssertionError("Bad kind "+k+" for PrimitiveType"+t);
        }
    }

    public R visitPrimitiveAsBoolean(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsByte(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsShort(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsInt(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsLong(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsChar(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsFloat(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    public R visitPrimitiveAsDouble(PrimitiveType t,P p){
        return defaultAction(t,p);
    }

    @Override
    public R visitNoType(NoType t,P p){
        TypeKind k=t.getKind();
        switch(k){
            case VOID:
                return visitNoTypeAsVoid(t,p);
            case PACKAGE:
                return visitNoTypeAsPackage(t,p);
            case NONE:
                return visitNoTypeAsNone(t,p);
            default:
                throw new AssertionError("Bad kind "+k+" for NoType"+t);
        }
    }

    public R visitNoTypeAsVoid(NoType t,P p){
        return defaultAction(t,p);
    }

    public R visitNoTypeAsPackage(NoType t,P p){
        return defaultAction(t,p);
    }

    public R visitNoTypeAsNone(NoType t,P p){
        return defaultAction(t,p);
    }
}

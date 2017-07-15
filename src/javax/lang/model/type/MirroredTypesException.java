/**
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MirroredTypesException extends RuntimeException{
    private static final long serialVersionUID=269;
    transient List<? extends TypeMirror> types; // cannot be serialized

    MirroredTypesException(String message,TypeMirror type){
        super(message);
        List<TypeMirror> tmp=(new ArrayList<TypeMirror>());
        tmp.add(type);
        types=Collections.unmodifiableList(tmp);
    }

    public MirroredTypesException(List<? extends TypeMirror> types){
        super("Attempt to access Class objects for TypeMirrors "+
                (types= // defensive copy
                        new ArrayList<TypeMirror>(types)).toString());
        this.types=Collections.unmodifiableList(types);
    }

    public List<? extends TypeMirror> getTypeMirrors(){
        return types;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        types=null;
    }
}

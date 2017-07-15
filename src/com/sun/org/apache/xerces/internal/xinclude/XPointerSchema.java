package com.sun.org.apache.xerces.internal.xinclude;

import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;

public interface XPointerSchema extends XMLComponent, XMLDocumentFilter{
    public void setXPointerSchemaName(String schemaName);

    public String getXpointerSchemaName();

    public Object getParent();

    public void setParent(Object parent);

    public String getXPointerSchemaPointer();

    public void setXPointerSchemaPointer(String content);

    public boolean isSubResourceIndentified();

    public void reset();
}

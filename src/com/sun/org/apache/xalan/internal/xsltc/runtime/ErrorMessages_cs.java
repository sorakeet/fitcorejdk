/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: ErrorMessages_cs.java,v 1.1.6.1 2005/09/06 10:45:37 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: ErrorMessages_cs.java,v 1.1.6.1 2005/09/06 10:45:37 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.runtime;

import java.util.ListResourceBundle;

public class ErrorMessages_cs extends ListResourceBundle{
    // These message should be read from a locale-specific resource bundle
    public Object[][] getContents(){
        return new Object[][]{
                /**
                 * Note to translators:  the substitution text in the following message
                 * is a class name.  Used for internal errors in the processor.
                */
                {BasisLibrary.RUN_TIME_INTERNAL_ERR,
                        "Vnit\u0159n\u00ed b\u011bhov\u00e1 chyba v ''{0}''"},
                /**
                 * Note to translators:  <xsl:copy> is a keyword that should not be
                 * translated.
                */
                {BasisLibrary.RUN_TIME_COPY_ERR,
                        "Vnit\u0159n\u00ed b\u011bhov\u00e1 chyba p\u0159i prov\u00e1d\u011bn\u00ed funkce <xsl:copy>."},
                /**
                 * Note to translators:  The substitution text refers to data types.
                 * The message is displayed if a value in a particular context needs to
                 * be converted to type {1}, but that's not possible for a value of type
                 * {0}.
                */
                {BasisLibrary.DATA_CONVERSION_ERR,
                        "Neplatn\u00e1 konverze z ''{0}'' do ''{1}''."},
                /**
                 * Note to translators:  This message is displayed if the function named
                 * by the substitution text is not a function that is supported.  XSLTC
                 * is the acronym naming the product.
                */
                {BasisLibrary.EXTERNAL_FUNC_ERR,
                        "Extern\u00ed funkce ''{0}'' nen\u00ed podporov\u00e1na produktem SLTC."},
                /**
                 * Note to translators:  This message is displayed if two values are
                 * compared for equality, but the data type of one of the values is
                 * unknown.
                */
                {BasisLibrary.EQUALITY_EXPR_ERR,
                        "Nezn\u00e1m\u00fd typ argumentu ve v\u00fdrazu rovnosti."},
                /**
                 * Note to translators:  The substitution text for {0} will be a data
                 * type; the substitution text for {1} will be the name of a function.
                 * This is displayed if an argument of the particular data type is not
                 * permitted for a call to this function.
                */
                {BasisLibrary.INVALID_ARGUMENT_ERR,
                        "Neplatn\u00fd typ argumentu ''{0}'' p\u0159i vol\u00e1n\u00ed ''{1}''"},
                /**
                 * Note to translators:  There is way of specifying a format for a
                 * number using a pattern; the processor was unable to format the
                 * particular value using the specified pattern.
                */
                {BasisLibrary.FORMAT_NUMBER_ERR,
                        "Pokus form\u00e1tovat \u010d\u00edslo ''{0}'' pou\u017eit\u00edm vzorku ''{1}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor was unable to create a copy of an
                 * iterator.  (See definition of iterator above.)
                */
                {BasisLibrary.ITERATOR_CLONE_ERR,
                        "Nelze klonovat iter\u00e1tor ''{0}''."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.AXIS_SUPPORT_ERR,
                        "Iter\u00e1tor pro osu ''{0}'' nen\u00ed podporov\u00e1n."},
                /**
                 * Note to translators:  The following represents an internal error
                 * situation in XSLTC.  The processor attempted to create an iterator
                 * for a particular axis (see definition above) that it does not
                 * support.
                */
                {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
                        "Iter\u00e1tor pro typizovanou osu ''{0}'' nen\u00ed podporov\u00e1n."},
                /**
                 * Note to translators:  This message is reported if the stylesheet
                 * being processed attempted to construct an XML document with an
                 * attribute in a place other than on an element.  The substitution text
                 * specifies the name of the attribute.
                */
                {BasisLibrary.STRAY_ATTRIBUTE_ERR,
                        "Atribut ''{0}'' je vn\u011b prvku."},
                /**
                 * Note to translators:  As with the preceding message, a namespace
                 * declaration has the form of an attribute and is only permitted to
                 * appear on an element.  The substitution text {0} is the namespace
                 * prefix and {1} is the URI that was being used in the erroneous
                 * namespace declaration.
                */
                {BasisLibrary.STRAY_NAMESPACE_ERR,
                        "Deklarace oboru n\u00e1zv\u016f ''{0}''=''{1}'' je vn\u011b prvku."},
                /**
                 * Note to translators:  The stylesheet contained a reference to a
                 * namespace prefix that was undefined.  The value of the substitution
                 * text is the name of the prefix.
                */
                {BasisLibrary.NAMESPACE_PREFIX_ERR,
                        "Obor n\u00e1zv\u016f pro p\u0159edponu ''{0}'' nebyl deklarov\u00e1n."},
                /**
                 * Note to translators:  The following represents an internal error.
                 * DOMAdapter is a Java class in XSLTC.
                */
                {BasisLibrary.DOM_ADAPTER_INIT_ERR,
                        "DOMAdapter byl vytvo\u0159en s pou\u017eit\u00edm chybn\u00e9ho typu zdroje DOM."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not describe to XSLTC the structure of the input XML document's
                 * DTD.
                */
                {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
                        "Pou\u017eit\u00fd analyz\u00e1tor SAX nem\u016f\u017ee manipulovat s deklara\u010dn\u00edmi ud\u00e1lostmi DTD."},
                /**
                 * Note to translators:  The following message indicates that the XML
                 * parser that is providing input to XSLTC cannot be used because it
                 * does not distinguish between ordinary XML attributes and namespace
                 * declarations.
                */
                {BasisLibrary.NAMESPACES_SUPPORT_ERR,
                        "Pou\u017eit\u00fd analyz\u00e1tor SAX nem\u016f\u017ee podporovat obory n\u00e1zv\u016f pro XML."},
                /**
                 * Note to translators:  The substitution text is the URI that was in
                 * error.
                */
                {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
                        "Nelze p\u0159elo\u017eit odkazy URI ''{0}''."}
        };
    }
}

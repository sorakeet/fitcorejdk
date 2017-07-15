/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 * <p>
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 * <p>
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
/**
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */
package java.text;

import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;

import java.lang.ref.SoftReference;
import java.text.spi.BreakIteratorProvider;
import java.util.Locale;

public abstract class BreakIterator implements Cloneable{
    public static final int DONE=-1;
    private static final int CHARACTER_INDEX=0;
    private static final int WORD_INDEX=1;
    private static final int LINE_INDEX=2;
    private static final int SENTENCE_INDEX=3;
    @SuppressWarnings("unchecked")
    private static final SoftReference<BreakIteratorCache>[] iterCache=(SoftReference<BreakIteratorCache>[])new SoftReference<?>[4];

    protected BreakIterator(){
    }

    public static BreakIterator getWordInstance(){
        return getWordInstance(Locale.getDefault());
    }

    public static BreakIterator getWordInstance(Locale locale){
        return getBreakInstance(locale,WORD_INDEX);
    }

    private static BreakIterator getBreakInstance(Locale locale,int type){
        if(iterCache[type]!=null){
            BreakIteratorCache cache=iterCache[type].get();
            if(cache!=null){
                if(cache.getLocale().equals(locale)){
                    return cache.createBreakInstance();
                }
            }
        }
        BreakIterator result=createBreakInstance(locale,type);
        BreakIteratorCache cache=new BreakIteratorCache(locale,result);
        iterCache[type]=new SoftReference<>(cache);
        return result;
    }

    private static BreakIterator createBreakInstance(Locale locale,
                                                     int type){
        LocaleProviderAdapter adapter=LocaleProviderAdapter.getAdapter(BreakIteratorProvider.class,locale);
        BreakIterator iterator=createBreakInstance(adapter,locale,type);
        if(iterator==null){
            iterator=createBreakInstance(LocaleProviderAdapter.forJRE(),locale,type);
        }
        return iterator;
    }

    private static BreakIterator createBreakInstance(LocaleProviderAdapter adapter,Locale locale,int type){
        BreakIteratorProvider breakIteratorProvider=adapter.getBreakIteratorProvider();
        BreakIterator iterator=null;
        switch(type){
            case CHARACTER_INDEX:
                iterator=breakIteratorProvider.getCharacterInstance(locale);
                break;
            case WORD_INDEX:
                iterator=breakIteratorProvider.getWordInstance(locale);
                break;
            case LINE_INDEX:
                iterator=breakIteratorProvider.getLineInstance(locale);
                break;
            case SENTENCE_INDEX:
                iterator=breakIteratorProvider.getSentenceInstance(locale);
                break;
        }
        return iterator;
    }

    public static BreakIterator getLineInstance(){
        return getLineInstance(Locale.getDefault());
    }

    public static BreakIterator getLineInstance(Locale locale){
        return getBreakInstance(locale,LINE_INDEX);
    }

    public static BreakIterator getCharacterInstance(){
        return getCharacterInstance(Locale.getDefault());
    }

    public static BreakIterator getCharacterInstance(Locale locale){
        return getBreakInstance(locale,CHARACTER_INDEX);
    }

    public static BreakIterator getSentenceInstance(){
        return getSentenceInstance(Locale.getDefault());
    }

    public static BreakIterator getSentenceInstance(Locale locale){
        return getBreakInstance(locale,SENTENCE_INDEX);
    }

    public static synchronized Locale[] getAvailableLocales(){
        LocaleServiceProviderPool pool=
                LocaleServiceProviderPool.getPool(BreakIteratorProvider.class);
        return pool.getAvailableLocales();
    }

    @Override
    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            throw new InternalError(e);
        }
    }

    public abstract int first();

    public abstract int last();

    public abstract int next(int n);

    public abstract int next();

    public int preceding(int offset){
        // NOTE:  This implementation is here solely because we can't add new
        // abstract methods to an existing class.  There is almost ALWAYS a
        // better, faster way to do this.
        int pos=following(offset);
        while(pos>=offset&&pos!=DONE){
            pos=previous();
        }
        return pos;
    }

    public abstract int previous();

    public abstract int following(int offset);

    public boolean isBoundary(int offset){
        // NOTE: This implementation probably is wrong for most situations
        // because it fails to take into account the possibility that a
        // CharacterIterator passed to setText() may not have a begin offset
        // of 0.  But since the abstract BreakIterator doesn't have that
        // knowledge, it assumes the begin offset is 0.  If you subclass
        // BreakIterator, copy the SimpleTextBoundary implementation of this
        // function into your subclass.  [This should have been abstract at
        // this level, but it's too late to fix that now.]
        if(offset==0){
            return true;
        }
        int boundary=following(offset-1);
        if(boundary==DONE){
            throw new IllegalArgumentException();
        }
        return boundary==offset;
    }

    public abstract int current();

    public abstract CharacterIterator getText();

    public void setText(String newText){
        setText(new StringCharacterIterator(newText));
    }

    public abstract void setText(CharacterIterator newText);

    private static final class BreakIteratorCache{
        private BreakIterator iter;
        private Locale locale;

        BreakIteratorCache(Locale locale,BreakIterator iter){
            this.locale=locale;
            this.iter=(BreakIterator)iter.clone();
        }

        Locale getLocale(){
            return locale;
        }

        BreakIterator createBreakInstance(){
            return (BreakIterator)iter.clone();
        }
    }
}

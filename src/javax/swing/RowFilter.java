/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RowFilter<M,I>{
    private static void checkIndices(int[] columns){
        for(int i=columns.length-1;i>=0;i--){
            if(columns[i]<0){
                throw new IllegalArgumentException("Index must be >= 0");
            }
        }
    }

    public static <M,I> RowFilter<M,I> regexFilter(String regex,
                                                   int... indices){
        return (RowFilter<M,I>)new RegexFilter(Pattern.compile(regex),
                indices);
    }

    public static <M,I> RowFilter<M,I> dateFilter(ComparisonType type,
                                                  Date date,int... indices){
        return (RowFilter<M,I>)new DateFilter(type,date.getTime(),indices);
    }

    public static <M,I> RowFilter<M,I> numberFilter(ComparisonType type,
                                                    Number number,int... indices){
        return (RowFilter<M,I>)new NumberFilter(type,number,indices);
    }

    public static <M,I> RowFilter<M,I> orFilter(
            Iterable<? extends RowFilter<? super M,? super I>> filters){
        return new OrFilter<M,I>(filters);
    }

    public static <M,I> RowFilter<M,I> andFilter(
            Iterable<? extends RowFilter<? super M,? super I>> filters){
        return new AndFilter<M,I>(filters);
    }

    public static <M,I> RowFilter<M,I> notFilter(RowFilter<M,I> filter){
        return new NotFilter<M,I>(filter);
    }

    public abstract boolean include(Entry<? extends M,? extends I> entry);

    public enum ComparisonType{
        BEFORE,
        AFTER,
        EQUAL,
        NOT_EQUAL
    }
    //
    // WARNING:
    // Because of the method signature of dateFilter/numberFilter/regexFilter
    // we can NEVER add a method to RowFilter that returns M,I. If we were
    // to do so it would be possible to get a ClassCastException during normal
    // usage.
    //

    public static abstract class Entry<M,I>{
        public Entry(){
        }

        public abstract M getModel();

        public abstract int getValueCount();

        public String getStringValue(int index){
            Object value=getValue(index);
            return (value==null)?"":value.toString();
        }

        public abstract Object getValue(int index);

        public abstract I getIdentifier();
    }

    private static abstract class GeneralFilter extends RowFilter<Object,Object>{
        private int[] columns;

        GeneralFilter(int[] columns){
            checkIndices(columns);
            this.columns=columns;
        }

        public boolean include(Entry<? extends Object,? extends Object> value){
            int count=value.getValueCount();
            if(columns.length>0){
                for(int i=columns.length-1;i>=0;i--){
                    int index=columns[i];
                    if(index<count){
                        if(include(value,index)){
                            return true;
                        }
                    }
                }
            }else{
                while(--count>=0){
                    if(include(value,count)){
                        return true;
                    }
                }
            }
            return false;
        }

        protected abstract boolean include(
                Entry<? extends Object,? extends Object> value,int index);
    }

    private static class RegexFilter extends GeneralFilter{
        private Matcher matcher;

        RegexFilter(Pattern regex,int[] columns){
            super(columns);
            if(regex==null){
                throw new IllegalArgumentException("Pattern must be non-null");
            }
            matcher=regex.matcher("");
        }

        protected boolean include(
                Entry<? extends Object,? extends Object> value,int index){
            matcher.reset(value.getStringValue(index));
            return matcher.find();
        }
    }

    private static class DateFilter extends GeneralFilter{
        private long date;
        private ComparisonType type;

        DateFilter(ComparisonType type,long date,int[] columns){
            super(columns);
            if(type==null){
                throw new IllegalArgumentException("type must be non-null");
            }
            this.type=type;
            this.date=date;
        }

        protected boolean include(
                Entry<? extends Object,? extends Object> value,int index){
            Object v=value.getValue(index);
            if(v instanceof Date){
                long vDate=((Date)v).getTime();
                switch(type){
                    case BEFORE:
                        return (vDate<date);
                    case AFTER:
                        return (vDate>date);
                    case EQUAL:
                        return (vDate==date);
                    case NOT_EQUAL:
                        return (vDate!=date);
                    default:
                        break;
                }
            }
            return false;
        }
    }

    private static class NumberFilter extends GeneralFilter{
        private boolean isComparable;
        private Number number;
        private ComparisonType type;

        NumberFilter(ComparisonType type,Number number,int[] columns){
            super(columns);
            if(type==null||number==null){
                throw new IllegalArgumentException(
                        "type and number must be non-null");
            }
            this.type=type;
            this.number=number;
            isComparable=(number instanceof Comparable);
        }

        @SuppressWarnings("unchecked")
        protected boolean include(
                Entry<? extends Object,? extends Object> value,int index){
            Object v=value.getValue(index);
            if(v instanceof Number){
                boolean compared=true;
                int compareResult;
                Class vClass=v.getClass();
                if(number.getClass()==vClass&&isComparable){
                    compareResult=((Comparable)number).compareTo(v);
                }else{
                    compareResult=longCompare((Number)v);
                }
                switch(type){
                    case BEFORE:
                        return (compareResult>0);
                    case AFTER:
                        return (compareResult<0);
                    case EQUAL:
                        return (compareResult==0);
                    case NOT_EQUAL:
                        return (compareResult!=0);
                    default:
                        break;
                }
            }
            return false;
        }

        private int longCompare(Number o){
            long diff=number.longValue()-o.longValue();
            if(diff<0){
                return -1;
            }else if(diff>0){
                return 1;
            }
            return 0;
        }
    }

    private static class OrFilter<M,I> extends RowFilter<M,I>{
        List<RowFilter<? super M,? super I>> filters;

        OrFilter(Iterable<? extends RowFilter<? super M,? super I>> filters){
            this.filters=new ArrayList<RowFilter<? super M,? super I>>();
            for(RowFilter<? super M,? super I> filter : filters){
                if(filter==null){
                    throw new IllegalArgumentException(
                            "Filter must be non-null");
                }
                this.filters.add(filter);
            }
        }

        public boolean include(Entry<? extends M,? extends I> value){
            for(RowFilter<? super M,? super I> filter : filters){
                if(filter.include(value)){
                    return true;
                }
            }
            return false;
        }
    }

    private static class AndFilter<M,I> extends OrFilter<M,I>{
        AndFilter(Iterable<? extends RowFilter<? super M,? super I>> filters){
            super(filters);
        }

        public boolean include(Entry<? extends M,? extends I> value){
            for(RowFilter<? super M,? super I> filter : filters){
                if(!filter.include(value)){
                    return false;
                }
            }
            return true;
        }
    }

    private static class NotFilter<M,I> extends RowFilter<M,I>{
        private RowFilter<M,I> filter;

        NotFilter(RowFilter<M,I> filter){
            if(filter==null){
                throw new IllegalArgumentException(
                        "filter must be non-null");
            }
            this.filter=filter;
        }

        public boolean include(Entry<? extends M,? extends I> value){
            return !filter.include(value);
        }
    }
}

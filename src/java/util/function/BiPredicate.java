package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiPredicate<T,U>{
    boolean test(T t,U u);

    /*
    * 这里取得非值
    * BiPredicate<Integer, String> condition3 = (i,s)-> i>100 && s.endsWith("y");
    * SOPL.accept(condition3.test(25,"Topicsky"));
    * SOPL.accept(condition3.negate().test(25,"Topicsky"));
    * 结果
    * false
    * true
    * */
    default BiPredicate<T,U> negate(){
        return (T t,U u)->!test(t,u);
    }

    /*
    * 同时满足需要的两个过程函数接口范例
    * BiPredicate<Integer, String> condition1 = (i,s)-> i>20 && s.startsWith("T");
    * BiPredicate<Integer, String> condition2 = (i,s)-> i<100 && s.endsWith("y");
    * SOPL.accept(condition1.and(condition2).test(25,"Topicsky"));
    * 结果
    * true
    * */
    default BiPredicate<T,U> and(BiPredicate<? super T,? super U> other){
        Objects.requireNonNull(other);
        return (T t,U u)->test(t,u)&&other.test(t,u);
    }

    /*
    * 非必要满足需要的两个过程函数接口范例
    * BiPredicate<Integer, String> condition2 = (i,s)-> i<100 && s.endsWith("y");
    * BiPredicate<Integer, String> condition3 = (i,s)-> i>100 && s.endsWith("y");
    * SOPL.accept(condition2.or(condition3).test(25,"Topicsky"));
    * 结果
    * true
    * */
    default BiPredicate<T,U> or(BiPredicate<? super T,? super U> other){
        Objects.requireNonNull(other);
        return (T t,U u)->test(t,u)||other.test(t,u);
    }
}

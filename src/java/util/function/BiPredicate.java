package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiPredicate<T,U>{
    boolean test(T t,U u);

    /*
    * ����ȡ�÷�ֵ
    * BiPredicate<Integer, String> condition3 = (i,s)-> i>100 && s.endsWith("y");
    * SOPL.accept(condition3.test(25,"Topicsky"));
    * SOPL.accept(condition3.negate().test(25,"Topicsky"));
    * ���
    * false
    * true
    * */
    default BiPredicate<T,U> negate(){
        return (T t,U u)->!test(t,u);
    }

    /*
    * ͬʱ������Ҫ���������̺����ӿڷ���
    * BiPredicate<Integer, String> condition1 = (i,s)-> i>20 && s.startsWith("T");
    * BiPredicate<Integer, String> condition2 = (i,s)-> i<100 && s.endsWith("y");
    * SOPL.accept(condition1.and(condition2).test(25,"Topicsky"));
    * ���
    * true
    * */
    default BiPredicate<T,U> and(BiPredicate<? super T,? super U> other){
        Objects.requireNonNull(other);
        return (T t,U u)->test(t,u)&&other.test(t,u);
    }

    /*
    * �Ǳ�Ҫ������Ҫ���������̺����ӿڷ���
    * BiPredicate<Integer, String> condition2 = (i,s)-> i<100 && s.endsWith("y");
    * BiPredicate<Integer, String> condition3 = (i,s)-> i>100 && s.endsWith("y");
    * SOPL.accept(condition2.or(condition3).test(25,"Topicsky"));
    * ���
    * true
    * */
    default BiPredicate<T,U> or(BiPredicate<? super T,? super U> other){
        Objects.requireNonNull(other);
        return (T t,U u)->test(t,u)||other.test(t,u);
    }
}

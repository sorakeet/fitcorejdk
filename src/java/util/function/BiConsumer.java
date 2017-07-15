package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiConsumer<T,U>{
    /*
    * 组合方法
    * 参数 T U 相对继承自参数源
    * Map<Integer,String> map = new HashMap<>();
    * map.put(1, "A");
    * map.put(2, "B");
    * map.put(3, "C");
    * map.put(1, "A");
    * map.put(2, "B");
    * map.put(3, "C");
    * BiConsumer<Integer,String> biConsumer1 = (key,value) ->System.out.println("Key:"+ key+" Value:"+ value);
    * BiConsumer<Integer,String> biConsumer2 = (key,value) ->System.out.println("Value:"+ value+" Key:"+ key);
    * map.forEach(biConsumer1.andThen(biConsumer2));
    * 结果
    * Key:1 Value:A
    * Value:A Key:1
    * Key:2 Value:B
    * Value:B Key:2
    * Key:3 Value:C
    * Value:C Key:3
    * */
    default BiConsumer<T,U> andThen(BiConsumer<? super T,? super U> after){
        /*
        * 例行判断
        * */
        Objects.requireNonNull(after);
        return (l,r)->{
            accept(l,r);
            after.accept(l,r);
        };
    }

    void accept(T t,U u);
}

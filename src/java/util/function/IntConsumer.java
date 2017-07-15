package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntConsumer{
    /*
    * 这里的默认实现 实现轮调，参数推进，组合函数
    * */
    default IntConsumer andThen(IntConsumer after){
        /*
        * 例行判断是否存在
        * */
        Objects.requireNonNull(after);
        /*
        * 这里需要注意的是，调用 andThen 的实例 t 参数来源
        * 首先 本身是 IntConsumer 被调用参数，也就是 this
        * 这是组合函数
        * */
        return (int t)->{
            //this.accept(t);
            accept(t);
            after.accept(t);
        };
    }

    void accept(int value);
}

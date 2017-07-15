package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntConsumer{
    /*
    * �����Ĭ��ʵ�� ʵ���ֵ��������ƽ�����Ϻ���
    * */
    default IntConsumer andThen(IntConsumer after){
        /*
        * �����ж��Ƿ����
        * */
        Objects.requireNonNull(after);
        /*
        * ������Ҫע����ǣ����� andThen ��ʵ�� t ������Դ
        * ���� ������ IntConsumer �����ò�����Ҳ���� this
        * ������Ϻ���
        * */
        return (int t)->{
            //this.accept(t);
            accept(t);
            after.accept(t);
        };
    }

    void accept(int value);
}

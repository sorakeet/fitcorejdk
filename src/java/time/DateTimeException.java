package java.time;

public class DateTimeException extends RuntimeException{
    private static final long serialVersionUID=-1632418723876261839L;

    /**
     * ʱ�����쳣���̳� RuntimeException ����ʱ�쳣
     * */
    public DateTimeException(String message){
        super(message);
    }
    /**
     * ʱ�����쳣���̳� RuntimeException ����ʱ�쳣
     * */
    public DateTimeException(String message,Throwable cause){
        super(message,cause);
    }
}

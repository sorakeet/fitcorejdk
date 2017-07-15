package java.time;

public class DateTimeException extends RuntimeException{
    private static final long serialVersionUID=-1632418723876261839L;

    /**
     * 时间类异常，继承 RuntimeException 运行时异常
     * */
    public DateTimeException(String message){
        super(message);
    }
    /**
     * 时间类异常，继承 RuntimeException 运行时异常
     * */
    public DateTimeException(String message,Throwable cause){
        super(message,cause);
    }
}

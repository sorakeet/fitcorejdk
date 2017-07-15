package java.sql;

public interface Wrapper{
    <T> T unwrap(Class<T> iface) throws SQLException;

    boolean isWrapperFor(Class<?> iface) throws SQLException;
}

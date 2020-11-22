package xyz.xiezc.ioc.orm.common;


public class YaoMybatisException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public YaoMybatisException(String message) {
        super(message);
    }

    public YaoMybatisException(Throwable throwable) {
        super(throwable);
    }

    public YaoMybatisException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

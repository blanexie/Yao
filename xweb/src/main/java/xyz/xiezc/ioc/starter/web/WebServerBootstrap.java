package xyz.xiezc.ioc.starter.web;

public interface WebServerBootstrap {

    void startWebServer(boolean ssl, int port,String staticPath) throws Exception;

}

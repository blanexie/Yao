package xyz.xiezc.ioc.starter.web;

public interface WebServerBootstrap {

    void startWebServer(boolean ssl, int port) throws Exception;

}

package xyz.xiezc.ioc.starter.web;

import xyz.xiezc.ioc.starter.web.common.XWebProperties;

public interface WebServerBootstrap {

    void startWebServer(XWebProperties xWebProperties) throws Exception;

}

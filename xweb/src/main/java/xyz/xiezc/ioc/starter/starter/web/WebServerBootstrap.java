package xyz.xiezc.ioc.starter.starter.web;

import xyz.xiezc.ioc.starter.starter.web.common.XWebProperties;

public interface WebServerBootstrap {

    void startWebServer(XWebProperties xWebProperties) throws Exception;

}

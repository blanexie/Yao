package xyz.xiezc.ioc.starter.starter.web.common;

import lombok.Data;
import xyz.xiezc.ioc.starter.annotation.Component;
import xyz.xiezc.ioc.starter.annotation.Value;

@Component
@Data
public class XWebProperties {

    @Value("xweb.server.ssl.enable")
    boolean ssl = false;

    /**
     * 证书链文件， .crt后缀的文件
     */
    @Value("xweb.server.ssl.certChainFile.Path")
    String certChainFilePath;
    /**
     * 私钥文件， .pem后缀的文件
     */
    @Value("xweb.server.ssl.privatekeyFile.Path")
    String privatekeyFilePath;


    @Value("xweb.server.port")
    int port = 8443;
    @Value("xweb.static.path")
    String staticPath = "/static";
    @Value("xweb.websocket.path")
    String websocketPath = "/websocket";


}

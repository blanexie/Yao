package xyz.xiezc.ioc.starter.orm.xml;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.io.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xyz.xiezc.ioc.Xioc;
import xyz.xiezc.ioc.starter.orm.util.DocumentUtil;
import xyz.xiezc.ioc.starter.orm.util.StringUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MybatisConfigDefine {
    @Setter
    @Getter
    Document configDocument;
    @Setter
    @Getter
    List<DocumentMapperDefine> mapperDefines;

    String mybatisConfigPath;

    public MybatisConfigDefine(String mybatisConfigPath) {
        this.mybatisConfigPath = mybatisConfigPath;
    }


    public void checkMybatisConfig() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentUtil.getDocumentBuilder(true);
        InputStream resourceAsStream = Resources.getResourceAsStream(mybatisConfigPath);
        configDocument = documentBuilder.parse(resourceAsStream);
        //获得配置的envName
        checkEnvironments();
        checkMappers();
    }

    /**
     * 更新配置， 之后就可以正常的mybatis进程
     */
    public void dealMappers() {
        NodeList mappers1 = configDocument.getElementsByTagName("mappers");
        int length = mappers1.getLength();
        for (int i = 0; i < length; i++) {
            Node item = mappers1.item(i);
            item.getParentNode().removeChild(item);
        }
        Element mappers = configDocument.createElement("mappers");
        configDocument.getDocumentElement().appendChild(mappers);
        for (DocumentMapperDefine documentMapperDefine : mapperDefines) {
            Element mapper1 = configDocument.createElement("mapper");
            mapper1.setAttribute("resource", documentMapperDefine.getNameSpace().replaceAll("\\.", "/"));
            mappers.appendChild(mapper1);
        }
    }


    private void checkMappers() {
        Set<String> paths = new HashSet<>();
        //如果用户配置了
        NodeList mappers = configDocument.getElementsByTagName("mappers");
        int length = mappers.getLength();
        if (length == 0) {
            return;
        }
        if (length > 1) {
            throw new RuntimeException("请配置一个mappers节点就够了");
        }
        Node item = mappers.item(0);
        NodeList childNodes = item.getChildNodes();
        int length1 = childNodes.getLength();
        for (int i = 0; i < length1; i++) {
            Node item1 = childNodes.item(i);
            String nodeName = item1.getNodeName();
            if (Objects.equals("#text", nodeName)) {
                continue;
            }
            if (Objects.equals(nodeName, "mapper")) {
                Node resource = item1.getAttributes().getNamedItem("resource");
                String nodeValue = resource.getNodeValue();
                int i1 = nodeValue.lastIndexOf("/");
                String substring = nodeValue.substring(0, i1);
                paths.add(substring);
            }
            if (Objects.equals(nodeName, "package")) {
                //还有url和class属性
                //TODO
                Node nameNode = item1.getAttributes().getNamedItem("name");
                String nodeValue = nameNode.getNodeValue();
                paths.add(nodeValue.replaceAll("\\.", "/"));
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : paths) {
            stringBuffer.append(s).append(",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        Xioc.getApplicationContext().getSetting().put("mybatis.mappers.xml.config",stringBuffer.toString());
    }

    /**
     * 获得默认的 配置环境的 id
     */
    private void checkEnvironments() {
        String envName = "development";
        NodeList environments = configDocument.getElementsByTagName("environments");
        int length = environments.getLength();
        if (length == 0) {
            Element environments1 = createEnviorments();
            configDocument.appendChild(environments1);
            return;
        }
        if (length > 1) {
            throw new RuntimeException("请配置一个environments 就够了, 请注意environments 和environment 的区别");
        }

        Node item = environments.item(0);
        Node development = item.getAttributes().getNamedItem("default");
        if (StringUtil.isNullOrEmpty(envName) || Objects.equals(development.getNodeValue(), envName)) {
            return;
        } else {
            Element environments1 = (Element) item;
            environments1.setAttribute("default", envName);
        }
    }


    private Element createEnviorments() {
        Element environments1 = configDocument.createElement("environments");
        environments1.setAttribute("default", "development");
        configDocument.appendChild(environments1);
        Element environment = configDocument.createElement("environment");
        environment.setAttribute("id", "development");
        environments1.appendChild(environment);
        Element transactionManager = configDocument.createElement("transactionManager");
        transactionManager.setAttribute("type", "JDBC");
        environment.appendChild(transactionManager);
        Element dataSource = configDocument.createElement("dataSource");
        environment.appendChild(dataSource);
        dataSource.setAttribute("type", "xyz.xiezc.blade.mybatis.common.MziDataSourceFactory");
        Element property = configDocument.createElement("property");
        dataSource.appendChild(property);
        property.setAttribute("name", "driver");
        property.setAttribute("value", "${mybatis.driver}");
        Element property1 = configDocument.createElement("property");
        dataSource.appendChild(property1);
        property1.setAttribute("name", "url");
        property1.setAttribute("value", "${mybatis.url}");
        Element property2 = configDocument.createElement("property");
        dataSource.appendChild(property2);
        property2.setAttribute("name", "username");
        property2.setAttribute("value", "${mybatis.username}");
        Element property3 = configDocument.createElement("property");
        dataSource.appendChild(property3);
        property3.setAttribute("name", "password");
        property3.setAttribute("value", "${mybatis.password}");
        return environments1;
    }

}

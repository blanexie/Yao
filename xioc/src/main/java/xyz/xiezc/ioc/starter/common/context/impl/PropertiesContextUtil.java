package xyz.xiezc.ioc.starter.common.context.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import xyz.xiezc.ioc.starter.common.context.PropertiesContext;

import java.net.URL;

public class PropertiesContextUtil implements PropertiesContext {

    Log log = LogFactory.get(PropertiesContextUtil.class);

    private Setting setting;

    @Override
    public void addSetting(Setting setting) {
        if (this.setting == null) {
            this.setting = setting;
        } else {
            this.setting.addSetting(setting);
        }
    }

    @Override
    public Setting getSetting() {
        if (this.setting == null) {
            this.setting = new Setting();
        }
        return setting;
    }

    /**
     * 框架只认5个配置文件的名称， 如果需要导入其他的配置文件， 需要在这5个主配置文件中指明
     * 加载配置文件
     */
    @Override
    public void loadProperties() {
        //读取classpath下的Application.setting，不使用变量
        //优先加载框架主要的配置文件
        try {
            URL resourceObj = ResourceUtil.getResource("app.setting");
            this.addSetting(new Setting(resourceObj, CharsetUtil.CHARSET_UTF_8, true));
            log.info("已经加载配置文件app.setting");
        } catch (Exception e) {
            log.info("未加载到配置文件app.setting");
        }
        try {
            URL resourceObj = ResourceUtil.getResource("app.properties");
            this.addSetting(new Setting(resourceObj, CharsetUtil.CHARSET_UTF_8, true));
            log.info("已经加载配置文件app.properties");
        } catch (Exception e) {
            log.info("未加载到配置文件app.properties");
        }
        //加载关联的配置文件
        String s = this.getSetting().get("properties.import.path");
        if (StrUtil.isNotBlank(s)) {
            String[] split = s.split(",");
            for (String s1 : split) {
                try {
                    URL resourceObj = ResourceUtil.getResource(s1);
                    this.addSetting(new Setting(resourceObj, CharsetUtil.CHARSET_UTF_8, true));
                    log.info("已经加载配置文件{}", s1);
                } catch (Exception e) {
                    log.info("未加载到配置文件{}", s1);
                }
            }
        }
    }
}

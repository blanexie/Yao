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
        URL appSetting = ResourceUtil.getResource("app.setting");
        if (appSetting != null) {
            this.addSetting(new Setting(appSetting, CharsetUtil.CHARSET_UTF_8, true));
        }
        URL appProperties = ResourceUtil.getResource("app.properties");
        if (appProperties != null) {
            this.addSetting(new Setting(appProperties, CharsetUtil.CHARSET_UTF_8, true));
        } //加载关联的配置文件
        String s = this.getSetting().get("properties.import.path");
        if (StrUtil.isNotBlank(s)) {
            String[] split = s.split(",");
            for (String s1 : split) {
                URL s1Res = ResourceUtil.getResource(s1);
                if (s1Res != null) {
                    this.addSetting(new Setting(s1Res, CharsetUtil.CHARSET_UTF_8, true));
                }
            }
        }
    }
}

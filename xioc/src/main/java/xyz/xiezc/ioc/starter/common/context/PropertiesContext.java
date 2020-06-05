package xyz.xiezc.ioc.starter.common.context;

import cn.hutool.setting.Setting;

public interface PropertiesContext {

    void addSetting(Setting setting);

    Setting getSetting();

    /**
     * 加载默认的配置文件
     *
     * @return
     */
    void loadProperties();
}

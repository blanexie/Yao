package xyz.xiezc.ioc.common.context;

import cn.hutool.setting.Setting;

public interface PropertiesContext {

    void addSetting(Setting setting);

    Setting getSetting();

}

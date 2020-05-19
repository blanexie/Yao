package xyz.xiezc.ioc.common.context.impl;

import cn.hutool.setting.Setting;
import xyz.xiezc.ioc.common.context.PropertiesContext;


public class PropertiesContextUtil implements PropertiesContext {

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
}

package xyz.xiezc.ioc.common.context.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.common.context.PropertiesContext;
import xyz.xiezc.ioc.common.event.ApplicationEvent;

import java.io.File;

import static xyz.xiezc.ioc.enums.EventNameConstant.loadPropertie;

@Component
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

    /**
     * 框架只认5个配置文件的名称， 如果需要导入其他的配置文件， 需要在这5个主配置文件中指明
     * 加载配置文件
     */
    @Override
    public void loadProperties() {
        //读取classpath下的Application.setting，不使用变量
        //优先加载框架主要的配置文件
        File file1 = FileUtil.file("yao.setting");
        if (FileUtil.exist(file1)) {
            this.addSetting(new Setting(file1.getPath(), true));
        }
        File file2 = FileUtil.file("xioc.setting");
        if (FileUtil.exist(file2)) {
            this.addSetting(new Setting(file2.getPath(), true));
        }
        File file3 = FileUtil.file("xweb.setting");
        if (FileUtil.exist(file3)) {
            this.addSetting(new Setting(file3.getPath(), true));
        }
        File file5 = FileUtil.file("app.setting");
        if (FileUtil.exist(file5)) {
            this.addSetting(new Setting(file5.getPath(), true));
        }

        File file6 = FileUtil.file("app.properties");
        if (FileUtil.exist(file6)) {
            this.addSetting(new Setting(file6, CharsetUtil.CHARSET_UTF_8, true));
        }

        //加载关联的配置文件
        String s = this.getSetting().get("properties.import.path");
        if (StrUtil.isNotBlank(s)) {
            String[] split = s.split(",");
            for (String s1 : split) {
                File file4 = FileUtil.file(s1);
                if (FileUtil.exist(file4)) {
                    this.addSetting(new Setting(file4.getPath(), true));
                }
            }
        }
    }
}

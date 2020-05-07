package xyz.xiezc.ioc.starter.web.entity;

import lombok.Data;

@Data
public class HttpContent {
    public enum HttpDataType {
        Attribute, FileUpload, ApplicationJson
    }

    HttpDataType httpDataType;

    String name;

    String value;

    FileUpload fileUpload;

}

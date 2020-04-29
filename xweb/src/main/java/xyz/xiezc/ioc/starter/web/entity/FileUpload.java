package xyz.xiezc.ioc.starter.web.entity;

import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Data
public class FileUpload {
    String fileName;
    String contentType;
    String name;
    int size;
    Charset charset;
    ByteBuffer byteBuffer;
}
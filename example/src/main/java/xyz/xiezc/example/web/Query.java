package xyz.xiezc.example.web;

import cn.hutool.core.net.multipart.UploadFile;
import lombok.Data;

@Data
public class Query {
    UploadFile filesss;
    Integer per;
}
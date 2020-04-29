package xyz.xiezc.ioc.starter.web.entity;

import lombok.Data;
import xyz.xiezc.ioc.starter.web.enums.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class HttpResponse {

    List<HttpHeader> headers=new ArrayList<>();

    Object body;


    HttpResponseStatus httpResponseStatus;
}

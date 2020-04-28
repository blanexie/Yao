package xyz.xiezc.ioc.starter.web.entity;

import lombok.Data;

import java.util.List;

@Data
public class HttpResponse {

    List<HttpHeader> headers;

    Object body;

}

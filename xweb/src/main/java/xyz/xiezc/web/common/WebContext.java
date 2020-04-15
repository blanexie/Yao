package xyz.xiezc.web.common;

import lombok.Data;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

@Data
public class WebContext {

    ServletRequest request;

    ServletResponse response;

    HttpSession httpSession;

}

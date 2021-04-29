package com.leyou.filter;

import com.leyou.auth.common.pojo.UserInfo;
import com.leyou.auth.common.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.item.configuration.FilterProperties;
import com.leyou.item.configuration.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class LoginFilter extends ZuulFilter {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 9;
    }

    @Override
    public boolean shouldFilter() {
        //get context
        RequestContext context = RequestContext.getCurrentContext();
        //get request
        HttpServletRequest request = context.getRequest();
        //get requesturl
        String url = request.getRequestURL().toString();

        //   get white list
        //LOOP THROUGH
     for(  String path:filterProperties.getAllowPaths()){
         //if contains
         if(url.contains(path)){
             // in white list do not go run method release
             return false;
         }
     }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //get context
        RequestContext context = RequestContext.getCurrentContext();
        //get request
        HttpServletRequest request = context.getRequest();
        // get token by cookievalue
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        //validate token(userinfo)
        try {
            //no exception  RELEASE
        JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            // An exception occurs    return 401 unauthorized
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
        }
        return null;
    }
}

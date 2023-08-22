package com.forest.joker.filter;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.utils.JwtUtil;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.utils.UrlPermitUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: dwh
 **/
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final String TokenName = "X-Token";

    @Resource
    private UrlPermitUtil urlPermitUtil;

    @Resource
    JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
            return;
        }

        String token = httpServletRequest.getHeader(TokenName);
        String url = httpServletRequest.getRequestURI();
        // 验证url是否需要验证
        if (!urlPermitUtil.isPermitUrl(url)) {
            Claims claims = null;
            try {
                claims = jwtUtil.parseToken(token);
            } catch (Exception e) {
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                PrintWriter out = httpServletResponse.getWriter();
                out.write(ResultUtil.TokenInvalid().toJSONString());
                out.flush();
                out.close();
                return;
            }
            // 设置用户信息
            Map<String, Object> map = new HashMap<>();
            claims.entrySet().stream().forEach(e -> map.put(e.getKey(), e.getValue()));
            httpServletRequest.setAttribute("userinfo", map);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}

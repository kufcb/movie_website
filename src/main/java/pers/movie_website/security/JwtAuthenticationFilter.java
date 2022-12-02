package pers.movie_website.security;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ObjectUtils;
import pers.movie_website.utils.JwtUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    private JwtUtils jwtUtils;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String jwt = request.getHeader(jwtUtils.getHeader());
        if(StrUtil.isBlankOrUndefined(jwt)){
            chain.doFilter(request,response);
            return;
        }
        Claims claim = jwtUtils.getClaimByToken(jwt);
        if(ObjectUtils.isEmpty(claim)){
            throw new JwtException("token 异常");
        }
        if(jwtUtils.isTokenExpired(claim)){
            throw new JwtException("token已经过期");
        }
        String username = claim.getSubject();
        //获取用户的权限信息
//        UsernamePasswordAuthenticationToken token =
//                new UsernamePasswordAuthenticationToken(username,null);
//        SecurityContextHolder.getContext().setAuthentication(token);
        chain.doFilter(request,response);
    }

}

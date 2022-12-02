package pers.movie_website.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pers.movie_website.security.*;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class securityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoginFailureHandler loginFailureHandler;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Autowired
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Resource
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager());
        return jwtAuthenticationFilter;
    }


    private static final String[] URL_WHITELIST = {
            "/user/userLogin",
            "/user/logout",
            "/user/userRegister",
    };
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
        //登录配置
        .formLogin()
        .successHandler(loginSuccessHandler)
        .failureHandler(loginFailureHandler)

        //禁用session
        .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        //配置拦截规则
        .and()

                .authorizeRequests()
                .antMatchers(URL_WHITELIST)
                .permitAll()
              //  .anyRequest().authenticated() //其他页面都要拦截

        //异常处理器
        .and()
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

        //配置自定义的过滤器
        .and()
            .addFilter(jwtAuthenticationFilter())
        ;
    }

}

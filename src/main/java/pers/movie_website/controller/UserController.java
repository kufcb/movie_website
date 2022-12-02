package pers.movie_website.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import pers.movie_website.pojo.HttpResult;
import pers.movie_website.service.UserService;

import javax.annotation.Resource;

@RestController
public class UserController {
    @Resource
    private UserService userService;

    //登录
    @PostMapping("/user/userLogin")
    public HttpResult userLogin(String user, String pass) {
        return userService.userLogin(user, pass,0);
    }

    //注册
    @PostMapping("/user/userRegister")
    public HttpResult userRegister(String user, String pass) {
        return userService.registerUser(user, pass);
    }

    //获得推荐电影
    @GetMapping("/user/recommendMovie")
    public HttpResult recommendMovie(@RequestHeader("Authorization") String token){
        return userService.recommendMovie(token);
    }
    //获取收藏
    @GetMapping("/user/seleceUserCollection")
    public HttpResult seleceUserCollection(@RequestHeader(value = "Authorization") String token){
        return userService.seleceUserCollection(token);
    }

    //取消收藏
    @PostMapping("/user/deleteUserCollection")
    public HttpResult deleteUserCollection(@RequestHeader(value = "Authorization") String token,int movieId){
        return userService.deleteUserCollection(token,movieId);
    }

    //用户评分
    @PostMapping("/user/userRating")
    public HttpResult userMovieScore(@RequestHeader(value = "Authorization",required = false) String token, int movieId,int movieRating){
        return userService.userMovieScore(token,movieId,movieRating);
    }

    //删除用户
    @PostMapping("/user/deleteUser")
    public HttpResult deleteUserController (@RequestHeader(value = "Authorization",required = false) String token){
        return userService.deleteUser(token);
    }

    //查询用户评分
    @GetMapping("/user/selectUserRating")
    public HttpResult selectUserRating(@RequestHeader(value = "Authorization",required = false) String token){
        return userService.selectUserRating(token);
    }

    //修改电影简介
    @GetMapping("/user/modifyMovie")
    public HttpResult modifyMovie(int movieId,String movieText){
        return userService.modifyMovie(movieId,movieText);
    }


}

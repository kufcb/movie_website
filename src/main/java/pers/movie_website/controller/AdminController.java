package pers.movie_website.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.movie_website.pojo.HttpResult;
import pers.movie_website.service.UserService;

import javax.annotation.Resource;

@RestController
public class AdminController {

    @Resource
    private UserService userService;

    //管理员登录
    @PostMapping("/admin/adminUserLogin")
    public HttpResult adminUserLogin(String user, String pass) {
        return userService.userLogin(user, pass,1);
    }

    @GetMapping("/admin/upMovieNum")
    public HttpResult upMovieNum(){
        return userService.upMovieNum();
    }

    @GetMapping("/admin/getUserNum")
    public HttpResult getUserNum(){
        return userService.getUserNum();
    }


    //管理员获取用户信息
    @GetMapping("/admin/getUserInfo")
    public HttpResult getUserInfo(String username){
        return userService.getUserInfo(username);
    }

    @GetMapping("/admin/getMovieImg")
    public HttpResult getMovieImg(){
        return userService.getMovieImg();
    }


    @PostMapping("/admin/deleteUser")
    public HttpResult deleteUser(String userName){
        return userService.deleteUser(userName);
    }

    @GetMapping("/admin/adminSearchMovie")
    public HttpResult adminSearchMovie(String movieName){
        return userService.adminSearchMovie(movieName);
    }


    @PostMapping("/admin/deleteMovie")
    public HttpResult deleteMovie(String movieName){
        return userService.deleteMovie(movieName);
    }


    @GetMapping("/admin/userWatchDate")
    public HttpResult userWatchDate(){
        return userService.userWatchDate();
    }

    @GetMapping("/admin/userCollDate")
    public HttpResult userWatchTongJi(){
        return userService.userWatchTongJi();
    }

    @GetMapping("/admin/getBestWatchMoviePie")
    public HttpResult getBestWatchMoviePie(){
        return userService.getBestWatchMoviePie();
    }

    @GetMapping("/admin/getBestCollMoviePie")
    public HttpResult getBestCollMoviePie(){
        return userService.getBestCollMoviePie();
    }
}

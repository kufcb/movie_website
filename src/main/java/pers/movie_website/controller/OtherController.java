package pers.movie_website.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pers.movie_website.pojo.HttpResult;
import pers.movie_website.service.UserService;
import pers.movie_website.utils.CsvImportUtil;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@RestController
public class OtherController {
    @Resource
    private UserService userService;
    //用户信息
    @GetMapping("/api/upUserInfo")
    public HttpResult upUserInfo(@RequestHeader("Authorization") String token) {
        return userService.userInfo(token);
    }

    @PostMapping("/api/grabMovie")
    public HttpResult grabMovie() {
        return userService.toGrab();
    }

    @PostMapping("/api/upLoadCsvFile")
    public HttpResult upLoadCsvFile(@RequestParam MultipartFile file){
        File csvFile = CsvImportUtil.uploadFile(file);
        List<String[]> lists = CsvImportUtil.readCSV(csvFile.getPath(),10);
        csvFile.delete();
        return userService.csvInput(lists);
    }

    @GetMapping("/api/upMovieNum")
    public HttpResult apiUpMovieNum(){
        return userService.upMovieNum();
    }

    //获取所有电影
    @GetMapping("/movieAll/getAllMovie")
    public HttpResult getAllMovie(int pageNum){
        return userService.getAllMovie(pageNum);
    }

    //根据id获取
    @GetMapping("/api/getMovieInfo")
    public HttpResult getMovieInfo(@RequestHeader(value = "Authorization",required = false) String token, int movieId){
        return userService.getMovieInfo(token,movieId);
    }
    //搜索电影
    @GetMapping("/api/searchMovie")
    public HttpResult searchMovie(String movieName){
        return userService.searchMovie(movieName);
    }


    @GetMapping("api/getAllMovieType")
    public HttpResult getAllMovieType(){
        return userService.getAllMovieType();
    }

    @GetMapping("/api/getTypeMovie")
    public HttpResult getTypeMovie(String movieType,int num){
        return userService.getTypeMovie(movieType,num);
    }

    @GetMapping("/api/getMovieWatchWebsite")
    public HttpResult getMovieWatchWebsite(@RequestHeader(value = "Authorization",required = false) String token,String movieName){
        return userService.getMovieWatchWebsite(token,movieName);
    }
}

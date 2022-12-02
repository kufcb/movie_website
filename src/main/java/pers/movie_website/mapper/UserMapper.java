package pers.movie_website.mapper;


import org.apache.ibatis.session.RowBounds;
import pers.movie_website.pojo.Movie;
import pers.movie_website.pojo.MoviePie;
import pers.movie_website.pojo.User;
import pers.movie_website.pojo.UserRating;

import java.util.List;

/**
 * @author 18028
 * 数据库查询操作
 */
//@Mapper
public interface UserMapper {
    /**
     * 是否存在用户
     *
     * @return
     */
    List<User> selectUser(User user);
    List<User> selectAdmin(User user);
    User existsAdmin(String account_number);
    //注册用户
    Integer insertUser(User user);
    //注册时候查找是否有此用户
    User existsUser(User user);
    User existsUserFromStr(String account_number);
    //抓取电影写入数据库
    Integer inserMovie(Movie movie);
    //电影数量
    Integer selectMovieNum();
    //用户数量
    Integer selectUserNum();
    //分页返回所有电影
    List<Movie> selectMovieAll(int pageNum, int pageEnd);
    // 使用 rowbounds分页
    List<Movie> selectMovieFromRowBounds(RowBounds rowBounds);
    //所有电影
    List<Movie> selectMovie();
    //电影图片
    List<Movie> selectMovieImg();
    //更新电影图片
    Integer updateMovieImg(Movie movie);
    //用户id
    Integer selectUserId(String account_number);
    //查找用户观看记录表,根据用户id查询,返回的是电影id
    List<Integer> selectUserWatch(int id);
    //根据用户id和电影id查询
    Integer seleceUserAndMovidWatch(int userId,int movieId);
    //用id查询电影
    Movie selectMovieFromId(int movieId);
    //用名字查询电影
    Movie selectMovieFromName(String movieName);
    //写入用户观看表
    Integer inserUserWatch(int uid,int mid,String datetime);
    //更新观看时间
    Integer updateUserWatchDate(int uid,int mid,String datetime);
    //写入收藏
    Integer inserUserCollection(int uid,int mid,String datetime);
    //查询是否收藏
    Integer selectUserCollection(int uid,int mid);
    //更新收藏时间
    Integer updateUserCollection(int uid,int mid,String datetime);
    //根据用户查看收藏
    List<Integer> selectCollectionFromId(int uid);
    //取消收藏
    Integer deleteCollection(int uid,int mid);
    //删除用户
    Integer deleteUser(String userName);
    //删除电影
    Integer deleteMovie(String movieName);
    //查询电影简介为空的电影
    List<Movie> selectMovieText();
    //插入电影简介
    Integer UpdateMovieText(String movieText,int movieId);
    //用户评分
    Integer inserUserRating(int uid,int mid,double movieRating,String datetime);
    //查询是否评分
    Integer selectUserRating(int uid,int mid);
    //读取所有用户电影评分
    List<UserRating> selectAllRating();
    //读取对应用户电影评分
    List<UserRating> selectRatingFromId(int id);
    //查看用户是否打分
    Integer selectUserRatingFrom(int id);
    //查看所有收藏记录
    List<UserRating> selectAllColl();
    //统计观看最多的电影
    Integer selectBestMovieWatch();
    //统计观看最多的电影
    List<MoviePie> selectBestMovieWatchPie();
    //统计收藏最多的电影
    Integer selectBestMovieColl();
    //统计观看最多的电影
    List<MoviePie> selectBestMovieCollPie();
    //查询所有电影名字
    List<String> selectAllMovieName();

}

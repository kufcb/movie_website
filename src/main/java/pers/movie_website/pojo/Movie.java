package pers.movie_website.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Movie implements Comparable<Movie> {
    private Integer id;
    private String name;
    private String date; //上线时间
    private String type; //分类
    private String score; //评分
    private String movieUrl; //豆瓣网址首页
    private String movieImg;
    private String movieText;


    @Override
    public int compareTo(Movie o) {
        //降序
        return (int) (Double.parseDouble(o.score) - Double.parseDouble(this.score));
        //升序
        //return (int) (Double.parseDouble(this.score) - Double.parseDouble(o.score));
    }
}

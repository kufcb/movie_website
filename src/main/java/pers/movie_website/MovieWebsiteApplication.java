package pers.movie_website;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 18028
 * System.setProperties("spring.devtools.restart.enabled","false");
 */
@SpringBootApplication
@MapperScan("pers.movie_website.mapper")
public class MovieWebsiteApplication {
    public static void main(String[] args) {

        SpringApplication.run(MovieWebsiteApplication.class, args);
    }
}

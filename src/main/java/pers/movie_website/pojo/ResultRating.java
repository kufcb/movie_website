package pers.movie_website.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ResultRating {
    private String movieName;
    private Double rating;
    private String dateTime;

}

package pers.movie_website.pojo;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserRating {
    private int uid;
    private int mid;
    private Double Rating;
    private String datetime;

}

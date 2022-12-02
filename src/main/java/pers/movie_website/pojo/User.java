package pers.movie_website.pojo;


import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author 18028
 */
@Component
@Data
public class User {
    private Integer id; // id
    private String nickName; //昵称
    private String accountNumber; //账号
    private String passWord; //密码
    private String userImg; //头像
}

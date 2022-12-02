package pers.movie_website.pojo;


/**
 * 返回枚举
 *
 * @author 18028
 */
public enum ResultCodeEnum {
    SUCCESS(200, "请求成功"),
    USER_EXISTS(200, "用户已经存在"),
    BAD_LOGIN(400,"用户名或密码错误"),
    NOT_LOGIN(400,"请先登录"),
    USER_NOT_LOGIN(200, "该用户没有注册"),
    USER_BAD_PASS(200, "错误的密码"),
    //重定向
    REDIRECT(301, "重定向"),
    NOT_FOUND(404, "没有找到资源"),
    SERVER_ERROR(500, "服务器错误"),
    TOKEN_OUT(401, "token异常"),
    Not_Acceptable(406,"请求内容错误");

    private Integer code;
    private String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}

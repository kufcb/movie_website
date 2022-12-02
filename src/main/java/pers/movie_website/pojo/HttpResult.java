package pers.movie_website.pojo;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author 18028
 */
@Component
public class HttpResult<T> implements Serializable {
    private Boolean success;
    private Integer code;
    private String msg;
    private T data;

    private HttpResult() {
        this.code = 200;
        this.success = true;
    }

    private HttpResult(T obj) {
        this.code = 200;
        this.msg = "成功执行";
        this.data = obj;
        this.success = true;
    }

    private HttpResult(T obj, String msg) {
        this.code = 200;
        this.msg = msg;
        this.data = obj;
        this.success = true;
    }
    private HttpResult(Integer code,T obj, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = obj;
        this.success = false;
    }

    private HttpResult(ResultCodeEnum resultCodeEnum) {
        this.success = false;
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
    }

    public static <T> HttpResult<T> success() {
        return new HttpResult<>();
    }

    public static <T> HttpResult<T> success(T data) {
        return new HttpResult<T>(data);
    }

    public static <T> HttpResult<T> success(T data, String msg) {
        return new HttpResult<T>(data, msg);
    }

    // 请求失败
    public static <T> HttpResult<T> failure(ResultCodeEnum resultCodeEnum) {
        return new HttpResult<T>(resultCodeEnum);
    }
    public static <T> HttpResult<T> failure(String resultStr) {
        return new HttpResult<T>(400,null,resultStr);
    }


    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "success=" + success +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

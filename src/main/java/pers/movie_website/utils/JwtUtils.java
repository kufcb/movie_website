package pers.movie_website.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author
 * @create: 2022-02-04
 */
@Data
@Component
public class JwtUtils {

    private long expire = 604800;
    private String secret = "weuiuiweuiwue";
    private String header = "Authorization";
    //生成  JWT
    public String generateToken(String username){
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + 1000 * expire);
        return Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)//7天逾期
                .signWith(SignatureAlgorithm.HS256,secret)
                .compact();
    }
    //解析JWT
    public Claims getClaimByToken(String jwt){
        try{
            return   Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(jwt)
                    .getBody();
        }catch (Exception e){
            return null;
        }
    }
    //JWT 是否过期的方法
    public boolean isTokenExpired(Claims claims){
        return claims.getExpiration().before(new Date());
    }
}







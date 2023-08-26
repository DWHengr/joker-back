package com.forest.joker.utils;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author: dwh
 **/
@Component
public class JwtUtil implements Serializable {
    private static final long serialVersionUID = -5625635588908941275L;

    // 令牌秘钥
    private static String secret = "jocke-E7Ymu64s";

    // 令牌有效期（默认3600分钟）
    private static int expireTime = 60 * 6;

    /**
     * 获取token
     *
     * @param claims
     * @return
     */
    public static String createToken(Map<String, Object> claims) {
        long now = System.currentTimeMillis() + (expireTime * 60 * 1000);
        return Jwts.builder()
                .setIssuer("joker")
                .addClaims(claims)
                .setExpiration(new Date(now))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }


    /**
     * 解析token
     *
     * @param token
     * @return
     */
    public static Claims parseToken(String token) {
        JwtParser jwtParser = Jwts.parser().setSigningKey(secret);
        Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        Claims body = claimsJws.getBody();
        return body;
    }

}

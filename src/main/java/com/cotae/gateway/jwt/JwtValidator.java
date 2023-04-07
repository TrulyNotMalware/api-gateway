package com.cotae.gateway.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Gateway Checks JWT Token is validate.
@Slf4j
@Component
public class JwtValidator implements Serializable {

    private static final long serialVersionUID = -4519442075729599949L;

    @Value("${token.secret}")
    private String key;

    public Map<String, Object> getUserParseInfo(String jwtTokenString){
        Claims userInfo = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtTokenString).getBody();
        Map<String, Object> userParseInfo = new ConcurrentHashMap<>();
        //Input user parse info.
        userParseInfo.put("userId",userInfo.getSubject());
        userParseInfo.put("roles",userInfo.get("roles", List.class));
        userParseInfo.put("isExpired", !userInfo.getExpiration().before(new Date()));
        return userParseInfo;
    }

    /**
     * Gateway 는 JWT 토큰에 대해서 기본적인 정보를 체크하고,
     * 정보가 틀리면 Authentication Server 에게 요청. Update Token, Refresh Token을 생성하지는 않음.
     * @param jwtTokenString JwtToken
     * @return Boolean
     */
    public boolean isValidate(String jwtTokenString){
        try{
            Map<String, Object> info = getUserParseInfo(jwtTokenString);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }
}

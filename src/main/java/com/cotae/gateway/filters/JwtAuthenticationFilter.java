package com.cotae.gateway.filters;

import com.cotae.gateway.jwt.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 이 클래스는 Login, Register 를 제외한 모든 요청의 Default Filter 로 작동한다.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config>{

    private final JwtValidator jwtValidator;

    //Constructor Injection.
    @Autowired
    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Config config){
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){ // Http Header 에 AUTHORIZATION 이 없으면
                return onError(exchange, "Header Authorization not found.", HttpStatus.UNAUTHORIZED); // FIXME UnAuthorization 말고, Login페이지로 Redirect 필요.
            }
            String headers = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            log.debug("headers : {}",headers);
            //Jwt Token 판별.
            String token = headers.replace("Bearer","");
            if(this.jwtValidator.isValidate(token)){
                Map<String, Object> userParseInfo = jwtValidator.getUserParseInfo(token);
                return chain.filter(exchange.mutate().request(
                        request.mutate().header("X-AUTH-TOKEN", headers)//Original Headers.
                                .header("userRole", userParseInfo.get("roles").toString())
                        .header("userId", (String) userParseInfo.get("userId")).build()//우선 Header 에 투입.
                ).build());
            }else{
                return onError(exchange, "Not a Valid Token", HttpStatus.UNAUTHORIZED);
            }
        });
    }
    private Mono<Void> onError(ServerWebExchange exchange, String e, HttpStatus status){
        log.error(e);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {

    }
}

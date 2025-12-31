package com.pharmacy.purchase_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public Claims parse(String token){
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims c){
        return (List<String>) c.get("roles");
    }

    public Long userId(Claims c){
        Object v = c.get("userId");
        if (v == null) return null;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Long l) return l;
        if (v instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("Invalid userId claim type: " + v.getClass());
    }
}

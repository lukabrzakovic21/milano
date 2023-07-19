package com.master.milano.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private static String SECRET = "somesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkeysomesecretkey";

    private Key getSignKey() {
        var bytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(bytes);
    }

    public Claims retrieveAllClaims(String token) {

        return Jwts.parserBuilder()
             .setSigningKey(getSignKey())
             .build()
             .parseClaimsJws(token)
             .getBody();
    }

}

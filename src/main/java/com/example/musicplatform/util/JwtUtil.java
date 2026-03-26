package com.example.musicplatform.util;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec; // 需要导入这个
import javax.crypto.SecretKey;          // 需要导入这个

@Component
public class JwtUtil {
    // 密钥保持不变
    private static final String SECRET_KEY_STRING = "MySuperSecretKeyForLoginModule_2026";


    // 【关键修改】将字符串密钥转换为 byte[]，避免库内部错误地进行 Base64 解码
    private static final byte[] SECRET_KEY_BYTES = SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8);

    // 也可以显式构建 SecretKey 对象 (更规范)
    private static final SecretKey SECRET_KEY = new SecretKeySpec(SECRET_KEY_BYTES, SignatureAlgorithm.HS256.getJcaName());

    private static final long EXPIRATION_TIME = 30 * 60 * 1000; // 30分钟过期

    // 生成 Token
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                // 【修改点 1】传入我们准备好的 SecretKey 对象 (或者 byte[])
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 Token 获取 UserId
    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if(claims != null) return Long.parseLong(claims.getSubject());
        else return null;
    }
    //获得用户名username
    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claims.get("username").toString();
        }
        return null;
    }

    //
    public Date getExpirationDateFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claims.getExpiration();
        }
        return null;
    }



    // 验证 Token 是否有效
    public boolean validateToken(String token) {
        try {
            // 1. 解析 Token。如果签名无效，这里会抛出异常。
            // 2. 解析成功后，我们可以获取 claims，进而获取过期时间。
            getExpirationDateFromToken(token);
            // 如果能成功获取到过期时间，说明签名是有效的

            // 3. 检查是否过期
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.after(new Date());

        } catch (ExpiredJwtException e) {
            // Token 已过期
            System.out.println("JWT token expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            // 不支持的 JWT 类型
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (MalformedJwtException e) {
            // Token 格式错误
            System.out.println("JWT token is malformed: " + e.getMessage());
        } catch (SignatureException e) {
            // 签名验证失败
            System.out.println("JWT signature does not match: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token 为空或 null
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false; // 出现任何异常，都认为 Token 无效
    }




   //解析出所有Claims的工具
    private Claims extractAllClaims(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (Exception e) {
            // 这里可以记录日志，例如 token 无效、签名错误等
            System.out.println("Could not parse JWT: " + e.getMessage());
            return null;
        }
    }

    public long getRemainingTime(String token) {
        Claims claims = extractAllClaims(token);
        if (claims != null) {
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        }throw new IllegalArgumentException("Token里面怎么没有时间戳?!");
    }
}
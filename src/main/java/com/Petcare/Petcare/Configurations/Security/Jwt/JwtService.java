package com.Petcare.Petcare.Configurations.Security.Jwt;

import com.Petcare.Petcare.Models.User.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio para manejar tokens JWT (JSON Web Tokens) en la aplicación PodStream.
 * Proporciona funcionalidades para generar, validar y extraer información de tokens JWT usados para autenticación.
 */
@Service
public class JwtService {

    private final String secretKey;
    private final long expirationTime;
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.verification-expiration:3600000}") // 1 hora por defecto
    private long verificationExpirationTime;

    private static final String VERIFICATION_AUDIENCE = "email_verification";

    /**
     * Constructor de JwtService con validación de la clave secreta.
     *
     * @param secretKey      La clave secreta JWT codificada en Base64, inyectada vía configuración.
     * @param expirationTime El tiempo de expiración de los tokens en milisegundos, inyectado vía configuración.
     * @throws IllegalArgumentException Si la clave secreta es nula, vacía, no está codificada en Base64 o tiene menos de 256 bits.
     */
    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration:86400000}") long expirationTime
    ) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key must not be null or empty");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT secret key must be a valid Base64-encoded string");
        }
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits) for HS256");
        }
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    /**
     * Genera un token JWT para el usuario especificado sin reclamos adicionales.
     *
     * @param userDetails El objeto UserDetails que representa al usuario.
     * @return El token JWT generado como una cadena.
     */
    public String getToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        // Get the roles from the UserDetails object and add them to the claims
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        extraClaims.put("authorities", roles);

        return getToken(extraClaims, userDetails);
    }

    /**
     * Genera un token JWT para el usuario especificado con reclamos adicionales.
     *
     * @param extraClaims Un mapa de reclamos adicionales para incluir en el token.
     * @param user        El objeto UserDetails que representa al usuario.
     * @return El token JWT generado como una cadena.
     */
    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtiene la clave de firma utilizada para operaciones con JWT.
     *
     * @return El objeto Key para firmar/verificar tokens JWT.
     */
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario (subject) del token JWT proporcionado.
     *
     * @param token El token JWT a analizar.
     * @return El nombre de usuario como una cadena.
     * @throws IllegalArgumentException Si el token es inválido.
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Valida el token JWT verificando si coincide con el usuario y si no ha expirado.
     *
     * @param token       El token JWT a validar.
     *
     * @return Verdadero si el token es válido, falso en caso contrario.
     */
    public boolean isTokenValid(String token) {
        try {
            getClaim(token, Claims::getSubject);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Analiza el token JWT y devuelve todos los reclamos.
     *
     * @param token El token JWT a analizar.
     * @return Un objeto Claims que contiene todos los reclamos del token.
     * @throws IllegalArgumentException Si el token es inválido.
     */
    public Claims getAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
        }
    }

    /**
     * Extrae un reclamo específico del token JWT usando una función de resolución.
     *
     * @param token          El token JWT a analizar.
     * @param claimsResolver Una función para extraer el reclamo deseado del objeto Claims.
     * @param <T>            El tipo del reclamo a extraer.
     * @return El reclamo extraído.
     * @throws IllegalArgumentException Si el token es inválido.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token El token JWT a analizar.
     * @return La fecha de expiración del token.
     * @throws IllegalArgumentException Si el token es inválido.
     */
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica si el token JWT ha expirado.
     *
     * @param token El token JWT a verificar.
     * @return Verdadero si el token ha expirado, falso en caso contrario.
     * @throws IllegalArgumentException Si el token es inválido.
     */
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    /**
     * Genera un token JWT específico para la verificación de email.
     *
     * @param user El usuario para quien se genera el token.
     * @return El token de verificación JWT.
     */
    public String generateVerificationToken(User user) {
        logger.debug("Generando token de verificación para: {}", user.getEmail());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId()); // Añadir el ID del usuario es una buena práctica

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setAudience(VERIFICATION_AUDIENCE) // Claim para identificar el propósito
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + verificationExpirationTime))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida un token de verificación y extrae los claims.
     *
     * @param token El token de verificación.
     * @return Los claims del token si es válido.
     * @throws JwtException si el token es inválido o no es para verificación.
     */
    public Claims getClaimsFromVerificationToken(String token) throws JwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .requireAudience(VERIFICATION_AUDIENCE) // Asegura que el token sea para verificación
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Opcional: Validar que el token no ha expirado (aunque .parseClaimsJws ya lo hace)
        if (claims.getExpiration().before(new Date())) {
            throw new JwtException("El token de verificación ha expirado");
        }

        return claims;
    }
}
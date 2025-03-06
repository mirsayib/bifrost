package com.bifrost.security;



import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class JwtUtil {
	private JwtUtil() {}


	private static final String SECRET_KEY = System.getenv("JWT_SECRET");
	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	private static Algorithm getAlgorithm() {
		if(Objects.isNull(SECRET_KEY) || SECRET_KEY.isEmpty()) {
			logger.warn("Invalid JWT Secret Key");
			return null;
		}
		return Algorithm.HMAC256(SECRET_KEY);
	}
	public static boolean validateJwt(String token) {
		try{
			Algorithm algorithm = getAlgorithm();

			if(Objects.isNull(algorithm)) {
				return false;
			}
			JWTVerifier verifier = JWT.require(algorithm)
					.build();
			verifier.verify(token);
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}

	}

}

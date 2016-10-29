package wisc.drivesense.utility;

import android.util.Log;

import java.security.Key;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.impl.DefaultClaims;

/**
 * Created by peter on 10/28/16.
 */

public class DriveSenseToken {
    public DriveSenseToken(String jwtString) {
        //JwtBuilder builder = Jwts.builder().claim("firstname",null).claim("lastname", null).claim("email", null);
        int i = jwtString.lastIndexOf('.');
        String[] splitToken = jwtString.split("\\.");
        Jwt parsedToken = Jwts.parser().parse(splitToken[0] + "." + splitToken[1] + ".");
        Log.d("dink", ((DefaultClaims) parsedToken.getBody()).get("firstname", String.class));
    }
}
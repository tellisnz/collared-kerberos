package nz.tellis.collared.kerberos.httpclient;

import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.SPNegoScheme;
import org.ietf.jgss.*;

public class CollaredSPNegoScheme extends SPNegoScheme {

    private final byte[] token;


    public CollaredSPNegoScheme(boolean stripPort, byte[] token) {
        super(stripPort, true);
        this.token = token;
    }

    @Override
    protected byte[] generateToken(final byte[] input, String authServer) throws GSSException {
        return token;
    }

    @Override
    protected byte[] generateToken(byte[] input, String authServer, Credentials credentials) throws GSSException {
        return token;
    }
}

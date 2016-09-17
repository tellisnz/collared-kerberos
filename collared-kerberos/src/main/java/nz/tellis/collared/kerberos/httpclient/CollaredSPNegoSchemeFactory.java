package nz.tellis.collared.kerberos.httpclient;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@SuppressWarnings("deprecation")
public class CollaredSPNegoSchemeFactory implements AuthSchemeFactory, AuthSchemeProvider {

    private final boolean stripPort;
    private final byte[] token;

    public CollaredSPNegoSchemeFactory(final boolean stripPort, byte[] token) {
        this.stripPort = stripPort;
        this.token = token;
    }

    public CollaredSPNegoSchemeFactory(byte[] token) {
        this(true, token);
    }

    public boolean isStripPort() {
        return stripPort;
    }

    public AuthScheme newInstance(final HttpParams params) {
        return new CollaredSPNegoScheme(this.stripPort, token);
    }

    public AuthScheme create(final HttpContext context) {
        return new CollaredSPNegoScheme(this.stripPort, token);
    }

}
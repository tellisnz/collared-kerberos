package nz.tellis.collared.kerberos;

import org.ietf.jgss.*;

import javax.security.auth.Subject;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class CollaredKerberosTokenFactory {

    private static final Oid SPNEGO_OID;
    static {
        try {
            SPNEGO_OID = new Oid("1.3.6.1.5.5.2");
        } catch (GSSException e) {
            throw new IllegalStateException("Couldn't create SPNEGO Oid.", e);
        }
    }

    public static byte[] createToken(GSSCredential delegationCredential, String targetUrl) throws PrivilegedActionException {
        Subject subject = Subject.getSubject(AccessController.getContext());

        return Subject.doAs(subject, new PrivilegedExceptionAction<byte[]>() {
            @Override
            public byte[] run() throws Exception {
                final GSSManager manager = GSSManager.getInstance();
                final GSSName serverName = manager.createName("HTTP@" + new URL(targetUrl).getHost(),
                        GSSName.NT_HOSTBASED_SERVICE);
                final GSSContext gssContext = manager.createContext(
                        serverName.canonicalize(SPNEGO_OID), SPNEGO_OID, delegationCredential, GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
                return gssContext.initSecContext(new byte[0], 0, 0);
            }
        });
    }
}

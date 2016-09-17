package nz.tellis.collared.kerberos.springsecuritykerberos;

import org.ietf.jgss.GSSCredential;
import org.springframework.security.kerberos.authentication.KerberosTicketValidation;

public class CollaredKerberosTicketValidation extends KerberosTicketValidation {

    private final GSSCredential delegationCredential;

    public CollaredKerberosTicketValidation(KerberosTicketValidation initialTicketValidation,
                                            GSSCredential delegationCredential) {
        super(initialTicketValidation.username(),
                initialTicketValidation.subject().getPrincipals().iterator().next().getName(),
                initialTicketValidation.responseToken(),
                initialTicketValidation.getGssContext());
        this.delegationCredential = delegationCredential;
    }

    public GSSCredential getDelegationCredential() {
        return delegationCredential;
    }
}

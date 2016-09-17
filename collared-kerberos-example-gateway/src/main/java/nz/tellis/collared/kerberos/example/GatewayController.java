package nz.tellis.collared.kerberos.example;

import nz.tellis.collared.kerberos.CollaredKerberosTokenFactory;
import nz.tellis.collared.kerberos.springsecuritykerberos.CollaredKerberosTicketValidation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.security.PrivilegedActionException;
import java.util.Base64;

@Controller
public class GatewayController {

    @Value("${app.kerberised-target-url}")
    private String kerberisedTargetUrl;

    @Value("${app.hop-target-url}")
    private String hopTargetUrl;

    @RequestMapping("/")
    public String delegateCallToRemote(Model model) throws PrivilegedActionException {
        KerberosServiceRequestToken authentication = (KerberosServiceRequestToken)
                SecurityContextHolder.getContext().getAuthentication();

        CollaredKerberosTicketValidation ticketValidation = (CollaredKerberosTicketValidation)
                authentication.getTicketValidation();

        byte[] token = CollaredKerberosTokenFactory.createToken(ticketValidation.getDelegationCredential(),
                kerberisedTargetUrl);

        String tokenString = Base64.getEncoder().encodeToString(token);

        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("KERBEROS_TOKEN", tokenString);
        HttpEntity entity = new HttpEntity(headers);

        long start = System.nanoTime();
        HttpEntity<String> response = template.exchange(hopTargetUrl, HttpMethod.GET, entity, String.class);
        long end = System.nanoTime();
        model.addAttribute("output", response.getBody());
        model.addAttribute("outputMillis", (end - start) / 1000000);
        return "home";
    }
}
package nz.tellis.collared.kerberos.example;

import nz.tellis.collared.kerberos.httpclient.CollaredHttpClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

@Controller
public class HopContoller {

    @Value("${app.service-principal}")
    private String servicePrincipal;

    @Value("${app.service-keytab-location}")
    private String keytabLocation;

    @Value("${app.kerberised-target-url}")
    private String targetUrl;


    @RequestMapping("/")
    @ResponseBody
    public String delegateCallToRemote(HttpServletRequest request) throws IOException {
        byte[] token = Base64.getDecoder().decode(request.getHeader("KERBEROS_TOKEN"));

        KerberosRestTemplate template = new KerberosRestTemplate(keytabLocation, servicePrincipal,
                        CollaredHttpClientFactory.build(token));

        return template.getForObject(targetUrl, String.class);
    }
}
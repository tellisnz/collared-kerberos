# collared-kerberos
A couple of helper classes for and an example of using Constrained Delegation with Kerberos.

## What is the Kerberos Double Hop problem and Kerberos Constrained Delegation?
The Kerberos Double Hop problem occurs when you have a kerberised service A that needs to talk to another kerberised service B on behalf of a user. Service A can not by default request a new service ticket for service B without constrained delegation (or protocol translation). A good write up can be found [here](http://searchwindowsserver.techtarget.com/feature/Advanced-Kerberos-topics-Delegation-of-authentication).

## What is in this repo?
### collared-kerberos
Helper classes that can be used to perform constrained delegation using spring-security-kerberos and S4U2Proxy.
* springsecuritykerberos package - Contains the changes found in [this pull request](https://github.com/spring-projects/spring-security-kerberos/pull/27) as well as a tweak to the Sun JAAS config to have isInitiator=true. Necessary to get Krb5ProxyCredential as described in the comment [here](https://github.com/openjdk-mirror/jdk/blob/jdk8u/jdk8u/master/src/share/classes/sun/security/jgss/krb5/Krb5Context.java#L542).
* CollaredKerberosTokenFactory - An example/helper for creating SPNEGO tokens using the obtained Krb5ProxyCredential.
* httpclient package - A simple HttpClient SPNEGO scheme that can be used in a KerberosRestTemplate and simply returns the provided token during the SPNEGO sequence.

### The Example
I set this example up to fulfil my particular use case at the time around the Hadoop stack - that a request from a user would hit a Kerberised service, get passed through a couple of insecure services, and finally hit another kerberised service that required the end users credentials. So collared-kerberos-example-gateway is a kerberised service and collared-kerberos-example-hop isn't, although it makes a kerberised call to a downstream service. It works by generating a service ticket token for the kerberised end service during the gateway interaction, and passing that service ticket as a header to the hop service. The hop service then simply uses that token and KerberosRestTemplate to call the downstream kerberised service. The meat of the constrained delegation is in the gateway example.

#### collared-kerberos-example-gateway
The gateway service configured with [spring security kerberos](http://projects.spring.io/spring-security-kerberos/) that also uses helper classes found in collared-kerberos. Key points to note:
* WebSecurityConfig - Your stock spring security setup with a SPNEGO authentication filter, using the tweaked SunKerberosJaasTicketValidator.
* GatewayController - Obtains the delegated credential after the SPNEGO sequence, puts it in a header and calls the configured hop service.

#### collared-kerberos-example-hop
* HopController - gets the token out of the header and calls the target kerberised service using KerberosRestTemplate, with a special HttpClient that uses the token at the right point of the SPNEGO sequence.

## Setting up a Test AD Environment

### Prerequisites
* Virtualbox
* Patience
* Googling ability - I can't guarantee these instructions will work. 

### Set Up a Host Only Network in Virtualbox
1. File -> Preferences -> Network -> Host-only Networks. Make sure there is an adapter.
1. Edit it and note down the address.
1. Some people get this working using static IPs for the following hosts, but I used a DHCP server and made sure the hosts always got the same IP.

### Set Up Active Directory Server
#### Create Server
1. Download an evaluation Windows Server 2012 R2 from [here](https://www.microsoft.com/en-GB/evalcenter/evaluate-windows-server-2012-r2)
1. In virtualbox, click new.
1. Give your server a good name, leave the defaults, and click create.
1. Right click your new server, click on settings, and click on Storage. Click the 'Empty' disk in the 'Storage Tree' section, then click the disc on the far right. Select Choose Virtual Optical Disk File and then find where you downloaded your ISO and select it.
1. Click on Network, Adapter 2, Enable Network Adapter. Select Host-Only Adapter in the Attached to drop down and click OK.
1. Double click to power it up. Go through the install process making sure to select 'Windows Server 2012 R2 Standard Evaluation (Server with a GUI)'. Do a 'Custom' install and finish up the install.
1. After install, give it a password and log in.
1. Open up the Server Manager, and click Add Roles and Features. Next, Next, Next. Select Active Directory Domain Services and DNS Server. Next through to the end. You may get a warning about not having a static IP - I couldn't quite figure out how to do this and it worked with DHCP so I left it. Install and then close.
1. Click on the orange exclaimation mark and click promote this server to Domain Controller. Select add a new forest and give it a name, e.g. MYCOOLDOMAIN.COM
1. Next through the rest of the options and Install. Server will restart.
1. 

#### Set Up User Accounts

### Set Up A Linux Host for the Gateway & Hop
1. Download Ubuntu from [here](http://www.ubuntu.com/download/desktop).
1. TODO get keytab and place on server.

### Set Up A Linux Host for the downstream example Kerberised Service
1. Create another Virtualbox Unbutu VM as in the Gateway/Hop one above.
1. TODO get keytab and place on server.
1. Clone the hadoop-auth example from [here](https://github.com/apache/hadoop/tree/trunk/hadoop-common-project/hadoop-auth).
1. build it.
1. Download [apache Tomcat](http://tomcat.apache.org/download-80.cgi) and extract.
1. 

### Set Up a Windows Host to be the End User
1. 
1. 

### Common Problems

## Why is this called Collared Kerberos? 
Because I'm terrible at naming things: Kerberos being a mythological three headed dog, and a collar being a sort of 'constraint' on it. Yeah I know. Terrible.

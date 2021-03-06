# collared-kerberos
A couple of helper classes for and an example of using Constrained Delegation with Kerberos.

## What is the Kerberos Double Hop problem and Kerberos Constrained Delegation?
The Kerberos Double Hop problem occurs when you have a kerberised service 'A' that needs to talk to another kerberised service 'B' on behalf of a user. Service A can not by default request a new service ticket for service B without constrained delegation (or protocol transition). A good write up can be found [here](http://searchwindowsserver.techtarget.com/feature/Advanced-Kerberos-topics-Delegation-of-authentication).

## What is in this repo?
### collared-kerberos
Contains helper classes that can be used to perform constrained delegation using spring-security-kerberos and S4U2Proxy.
* springsecuritykerberos package - Contains the changes found in [this pull request](https://github.com/spring-projects/spring-security-kerberos/pull/27) as well as a tweak to the Sun JAAS config to have `isInitiator=true`. This is necessary to get Krb5ProxyCredential as described in the comment [here](https://github.com/openjdk-mirror/jdk/blob/jdk8u/jdk8u/master/src/share/classes/sun/security/jgss/krb5/Krb5Context.java#L542).
* CollaredKerberosTokenFactory - An example/helper for creating SPNEGO tokens using the obtained Krb5ProxyCredential.
* httpclient package - A simple HttpClient SPNEGO scheme that can be used in a KerberosRestTemplate and simply returns the provided token during the SPNEGO sequence.

### The Example
I set this example up to fulfil my particular use case at the time around the Hadoop stack - that a request from a user would hit a Kerberised service, get passed through a couple of insecure services, and finally hit another kerberised service that required the end users credentials. So collared-kerberos-example-gateway is a kerberised service and collared-kerberos-example-hop isn't, although it makes a kerberised call to a downstream service. It works by generating a service ticket token for the kerberised end service during the gateway interaction, and passing that service ticket as a header to the hop service. The hop service then simply uses that token and KerberosRestTemplate to call the downstream kerberised service. The meat of the constrained delegation is in the gateway example.

#### collared-kerberos-example-gateway
The gateway service configured with [spring security kerberos](http://projects.spring.io/spring-security-kerberos/) that also uses helper classes found in collared-kerberos. Key points to note:
* WebSecurityConfig - Your stock spring security setup with a SPNEGO authentication filter, using the tweaked SunKerberosJaasTicketValidator.
* GatewayController - Obtains the delegated credential after the SPNEGO sequence, puts it in a header and calls the configured hop service.

#### collared-kerberos-example-hop
* HopController - gets the token out of the header and calls the target kerberised service using KerberosRestTemplate, with a custom HttpClient that uses the token at the right point of the SPNEGO sequence.

## Setting up a Test AD Environment

### Prerequisites
* Virtualbox
* Patience
* Googling ability - I can't guarantee these instructions will work and will happily accept PRs to fix them, but you'll likely need to work through some problems.

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

#### Set Up User Accounts
1. Once the server has restarted click start, start typing active and then open Active Directory Users and Groups. Expand your domain and click on Managed Service Accounts. Right click in the white space and click new -> User.
1. Give a name for your delegating service like MYCOOLSERVICE and type the same in the User Login Name. Click next and then untick User must change password on next login and tick password never expires and then create.
1. Repeat above for another user called DOWNSTREAM - this will be our downstream kerberised service B.
1. Repeat above for our end user, e.g. Tom. This will be the user that logs into Windows and uses a browser to call our first kerberised service.
1. Create a SPNEGO service principal name (SPN) for MYCOOLSERVICE by opening powershell and running `setspn -s HTTP/mycoolservice.mycooldomain.com MYCOOLSERVICE`.
1. Create a Keytab for the service by running `ktpass -out C:\Users\Administrator\mycoolservice.keytab -princ HTTP/mycoolservice.mycooldomain.com@MYCOOLDOMAIN.COM -mapUser MYCOOLSERVICE -mapOp set +rndpass -crypto RC4-HMAC-NT -pType KRB5_NT_PRINCIPAL`.
1. Add the MYCOOLSERVICE user principal to the keytab using the command `ktpass -in C:\Users\Administrator\mycoolservice.keytab -out C:\Users\Administrator\mycoolservice.keytab -princ MYCOOLSERVICE@MYCOOLDOMAIN.COM -mapUser MYCOOLSERVICE -mapOp set +rndpass -crypto RC4-HMAC-NT -pType KRB5_NT_PRINCIPAL`. Ignore the warning.
1. Repeat the above for the DOWNSTREAM service, but this time for the SPNEGO principal use HTTP/downstream.mycooldomain.com (although for this example you just need the SPNEGO SPN).
1. Configure constrained delegation for the MYCOOLSERVICE user by going back to Active Directory Users and Computers and clicking View -> Advanced Features. Right click on the MYCOOLSERVICE user and click properties. There is now a Delegation tab. Click it and click Trust the user for delegation to specified services only. Click Users or Computers and type downstream and check names and then OK. Select the HTTP downstream.mycooldomain.com service and click OK and OK.
1. Get the keytabs of the server for later, e.g. with a share or something. 

### Set Up A Linux Host for the Gateway & Hop
1. Download Ubuntu from [here](http://www.ubuntu.com/download/desktop).
1. Follow the process as above for creating the virtualbox image.
1. Install Ubuntu.
1. Get the mycoolservice keytab and place on server.
1. Install jdk8 and krb5-workstation.
1. Configure krb5.conf.
1. Configure NTP and Hosts.
1. Clone collared kerberos example projects.
1. Configure projects.
1. Build and start them.

### Set Up A Linux Host for the downstream example Kerberised Service
1. Create another Virtualbox Unbutu VM as in the Gateway/Hop one above.
1. Get downstream keytab and place on server.
1. Install krb5-workstation.
1. Configure krb5.conf.
1. Configure NTP and Hosts.
1. Clone the hadoop-auth example from [here](https://github.com/apache/hadoop/tree/trunk/hadoop-common-project/hadoop-auth).
1. Configure as per hadoop auth example instructions to point to downstream principal and keytab.
1. Build hadoop auth example
1. Download [apache Tomcat](http://tomcat.apache.org/download-80.cgi) and extract and start it.
1. Put built web app in webapps.

### Set Up a Windows Host to be the End User
1. Get a Windows VM from [here](https://developer.microsoft.com/en-us/microsoft-edge/tools/vms/)
1. As per the other virtualbox VMs, create one for Windows.
1. Log in as end user.
1. Add it to mycooldomain.com.
1. Edit IE settings to supply user/password.
1. Go to http://mycoolservice.mycooldomain.com:8080/ - If everything works the service should say you're Tom/whatever your end user you set up was.

### Common Problems

#### Clock Skew

#### Couldn't contact KDC

#### Slow Responses

## Why is this called Collared Kerberos? 
Because I'm terrible at naming things: Kerberos being a mythological three headed dog, and a collar being a sort of 'constraint' on it. Yeah I know. Terrible.

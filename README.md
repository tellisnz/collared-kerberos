# collared-kerberos
A couple of helper classes for and an example of using Constrained Delegation with Kerberos.

## What is the Kerberos Double Hop problem and Kerberos Constrained Delegation?
The Kerberos Double Hop problem occurs when you have a kerberised service A that needs to talk to another kerberised service B on behalf of a user. Service A can not by default request a new service ticket for service B without constrained delegation (or protocol translation). A good write up can be found [here](http://searchwindowsserver.techtarget.com/feature/Advanced-Kerberos-topics-Delegation-of-authentication).

## What is in this repo?
### collared-kerberos
Helper classes that can be used to perform constrained delegation via S4U2Proxy.
* springsecuritykerberos package - Contains the changes found in [this pull request](https://github.com/spring-projects/spring-security-kerberos/pull/27) as well as a tweak to the Sun JAAS config to have isInitiator=true. Necessary to get Krb5ProxyCredential as described in the comment [here](https://github.com/openjdk-mirror/jdk/blob/jdk8u/jdk8u/master/src/share/classes/sun/security/jgss/krb5/Krb5Context.java#L542).
* CollaredKerberosTokenFactory - An example/helper for creating SPNEGO tokens using the obtained Krb5ProxyCredential.
* httpclient package - A simple HttpClient SPNEGO scheme that simple returns the provided token during the SPNEGO sequence.

### collared-kerberos-example-gateway
An example app configured with [spring security kerberos](http://projects.spring.io/spring-security-kerberos/) that also uses helper clases found in 

## Setting up a Test AD Environment

## Why is this called Collared Kerberos? 
Because I'm terrible at naming things: Kerberos being a mythological three headed dog, and a collar being a sort of 'contraint' on it. Yeah I know.

# TLS

This is a rudimentary implementation of [TLS 1.3](https://datatracker.ietf.org/doc/html/rfc8446) for Ktor raw socket server and client.
Only a very specific set of cipher suites are supported.

## Minecraft Protocol Adaptation
While the original idea of wrapping the Minecraft Protocol in TLS was to secure proxy to subserver connections
this could also benefit players with the features of TLS. For that reason here is the definition that is used for this project
when you want to write your own client modification to support this protocol adaptation.

For compatibility with vanilla clients the server can differentiate between a Minecraft handshake and a TLS handshake.
Because of that the same port can be used so Minecraft over TLS has the same default port of 25565.
Still the client side must be sure that the server understands TLS and does not just receive malformed Minecraft packets.

### Advertising TLS support
There are two ways a client can be informed of TLS support. The server must do both but one is enough to enable TLS.

#### Server List Ping
In the status response of a server list ping, an additional JSON field `tls` where the presence indicates tls support.
This allows the client to display the server's certificate in the server list without establishing a connection or performing any verification.
To actually verify the servers certificate another/subsequent server list pings can be performed over TLS.

> ```json5
> {
>   // regular status response json fields...
>   "version": {  },
>   "players": {  },
>   "description": {   },
>   "favicon": "",
>   "tls": { // tls field is present -> server must support TLS
>     // PEM-formatted X.509 certificate chain. as with webservers (mandatory)
>     "certificate": "",
>     // the minimum tls version supported. this should never be lower than 1.3 (mandatory)
>     "minVersion": "1.3",
>     // list of supported cipher suites. a client could fall back early from using TLS (optional)
>     "ciphers": [ "TLS_AES_256_GCM_SHA384" ]
>   }
> }
> ```

Since a server must accept unencrypted connections, the server list ping can be sent without TLS.

#### Plugin Message
A server that supports TLS should send a Plugin Message in the `configuration` connection state,
or as soon as possible in the `play` state before if the client's version is 1.20.2 or older.  
The plugin message has the channel `moonshine:tls` and has a UTF-8 encoded JSON object as described here.  

> ```json5
> {
>   // PEM-formatted X.509 certificate chain. as with webservers (mandatory)
>   "certificate": "",
>   // the minimum tls version supported. this should never be lower than 1.3 (mandatory)
>   "minVersion": "1.3",
>   // list of supported cipher suites. a client could fall back early from using TLS (optional)
>   "ciphers": [ "TLS_AES_256_GCM_SHA384" ]
> }
> ```

This JSON object is the same as the tls field in the [Server List Ping](#server-list-ping).  
With the Plugin Message, the client must reconnect in order to enable TLS.

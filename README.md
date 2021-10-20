### Distributed-Online-Banking-System

- The bank System is consisted by a stream server which accepts connections from clients and by a security code echo a response message back to client over the TCP connection.

- The server support multiple threads, which allows the multiple client connection to the server. 

- The server will manipulating the synchronized bank's database corresponding by the message received from client.

- Features Support:
    - Synchronized database
    - Creating unique accounts to server
    - Retriving an account in the synchronized DB
    - Deposit to an account in server
    - WithDraw from an account

#### Programming Stack:
- Java (Server)
- Perl & Python (Client)
- MySQL (Database)

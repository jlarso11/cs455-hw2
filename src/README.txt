CS455 - Project 2
By: Joseph Larson

Please run server with the following command: 

       java cs455.scaling.server.Server [portnum] [thread-pool-size]

       
       
       
Please run Client with the following command: 

      java cs455.scaling.client.Client [server-host] [server-port] [message-rate]
      
      
    
    
Assumptions made: 

Only received hashes that are in the LinkedList should be counted as a "received message".
The port passed into the Server is correct -- program will gracefully fail if passed a used port.
The ip/host and port number passed into the Client are correct -- program will gracefully fail if passed wrong credentials for the Server.

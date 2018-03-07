CS455 - Project 2
By: Joseph Larson

Please run server with the following command: 

       java cs455.scaling.server.Server [portnum] [thread-pool-size]

       
       
       
Please run Client with the following command: 

      java cs455.scaling.client.Client [server-ip] [server-port] [message-rate]
      

If you would like to use the startup script.  Please edit it to replace the ip and port number with the ones outputed from the server.  I was not going to submit this file because I do not want to be graded on my script writing skills but figured it may help you in the grading process. 

./startup.sh
      
    
    
Assumptions made: 

Only received hashes that are in the LinkedList should be counted as a "received message".
The port passed into the Server is correct -- program will gracefully fail if passed a used port.
The ip/host and port number passed into the Client are correct -- program will gracefully fail if passed wrong credentials for the Server.
I only tested by connecting to the IP.  I am not sure what will happen if you try to connect via host name. 

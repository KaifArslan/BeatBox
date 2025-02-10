import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.net.*;

public class Music_Server{
  private final List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();
  
  public static void main(String[] args){
    new Music_Server().go();
  }

  void go(){

    try{
        ServerSocket serverSocket = new ServerSocket(5000);
        ExecutorService myThreadPool = Executors.newCachedThreadPool();

      while(!serverSocket.isClosed()){
        Socket socket = serverSocket.accept();
        ObjectOutputStream thisOutputStream= new ObjectOutputStream(socket.getOutputStream());
        clientOutputStreams.add(thisOutputStream);
        System.out.println("Got a connection");
        ClientHandler clientHandler = new ClientHandler(socket);
        myThreadPool.execute(clientHandler);

      }
    }catch(IOException e){
      e.printStackTrace();
    }
  }

  void tellEveryone(Object userNameAndMessage, Object userBeatPattern){
    try{
    for (ObjectOutputStream clientOutputStream: clientOutputStreams){
      clientOutputStream.writeObject(userNameAndMessage);
      clientOutputStream.writeObject(userBeatPattern);
    }
    System.out.println("Message & Object sent");
  }catch(IOException e){
    e.printStackTrace();
  }
  }

  class ClientHandler implements Runnable{
    
    ObjectInputStream thisInputStream;

    public ClientHandler(Socket socket){
      try{
      thisInputStream= new ObjectInputStream(socket.getInputStream());
    }catch(IOException e){
      e.printStackTrace();
    }
  }

   public void run(){
      Object userNameAndMessage, userBeatPattern;
      try{
        while((userNameAndMessage = thisInputStream.readObject()) != null){
          userBeatPattern = thisInputStream.readObject();
          tellEveryone(userNameAndMessage, userBeatPattern);  
        }
      
      }catch(IOException| ClassNotFoundException e){
        e.printStackTrace();
      }

    }
  }
}
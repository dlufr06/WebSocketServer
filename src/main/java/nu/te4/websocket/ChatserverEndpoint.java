/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.te4.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Daniel Lundberg
 */
@ServerEndpoint("/chatserver")
public class ChatserverEndpoint {

    static Set<Session> sessions = new HashSet<>();
    
    @OnOpen
    public void open(Session session){
        sessions.add(session); //lägg till användare
    }
    
    @OnClose
    public void close(Session session) throws IOException{
        String username = (String)session.getUserProperties().get("username");
        sessions.remove(session); //ta bort användare
          Iterator<Session> iterator = sessions.iterator();
            while(iterator.hasNext()){
                Session user = iterator.next();
                user.getBasicRemote()
                        .sendText(buildJson("System",username +" has left the chatroom."));
                user.getBasicRemote()
                        .sendText(buildJsonUsers()); //uppdatera lista
            }
    }
    
    @OnMessage
    public void onMessage(String message, Session user) throws IOException {
        String username = (String)user.getUserProperties()
                                  .get("username");
        //om användarnamn saknas
        if(username == null){
            //använd message som användarnamn
            user.getUserProperties().put("username", message);
            user.getBasicRemote().sendText(buildJson("system", "your are connected as "+message));
            //uppdatera användarlista
               Iterator<Session> iterator = sessions.iterator();
            while(iterator.hasNext()){
                iterator.next().getBasicRemote()
                        .sendText(buildJsonUsers());
            }
        }else{//användaren är reggad
            //Iterator kommer vi titta på senare
            Iterator<Session> iterator = sessions.iterator();
            while(iterator.hasNext()){
                iterator.next().getBasicRemote()
                        .sendText(buildJson(username, message));
            }
        }
      
    }
    private String buildJson(String from, String message){
        JsonObject jsonMessage = Json.createObjectBuilder()
                                    .add("from", from)
                                    .add("message", message)
                                    .build();
        return jsonMessage.toString(); //gör om till text
    }
    
    private String buildJsonUsers(){
        //skapa builder
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
          Iterator<Session> iterator = sessions.iterator();
            while(iterator.hasNext()){
                //hämta användarnamnety
             String username =(String)iterator.next().getUserProperties().get("username");
                //skapa json-objekt
                 JsonObject jsonObject = Json.createObjectBuilder()
                         .add("username", username).build();
                 //lägg till i arrayen
                 jsonArrayBuilder.add(jsonObject);
            }
            return jsonArrayBuilder.build().toString();              
    }
    
}

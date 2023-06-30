package MainServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class MainServer {

    private Vector<ClientHandler> clientHandlers;

    public void start() {
        ServerSocket server;
        Socket socket;


        clientHandlers = new Vector<>();

        try {
            AuthServer.connect();

            server = new ServerSocket(8181);
            System.out.println("Сервер запущен");

            while (true) {
                //System.out.println(AuthServer.getNickByLoginPass("login1", "pass1"));
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            sendToAll("/end");
        }
        AuthServer.disconnect();
    }

    public void sendToAll(String msg){
        for (ClientHandler client:
        clientHandlers){
            client.sendMsg(msg);
        }
    }

    public void sendOnlineUsers() {
        StringBuilder sb = new StringBuilder(" ");
        List<String> list = clientHandlers.stream().map(ClientHandler::getNickname).collect(Collectors.toList());

        for (String s:
             list) {
            sb.append(s);
            sb.append(" ");
        }
        sendToAll("/show " + sb.toString().trim());
    }

    public void subScribe(ClientHandler client){
        clientHandlers.add(client);
    }

    public void unSubScribe(ClientHandler client) {
        sendToAll("Пользователь " + client.getNickname() + " отключился");
        clientHandlers.remove(client);
        sendOnlineUsers();
    }

    public boolean isNickFree(String nick){
        if (clientHandlers.isEmpty()) return true;

        for (ClientHandler client:
             clientHandlers) {
            if (client.getNickname().equals(nick)){
                return false;
            }
        }return true;
    }
}

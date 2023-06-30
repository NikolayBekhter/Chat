package MainServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    Socket socket;
    MainServer server;
    DataOutputStream out;
    DataInputStream in;

    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Socket socket, MainServer serverMain) {
        this.socket = socket;
        this.server = serverMain;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while (true){
                            String str = in.readUTF();
                            if (str.startsWith("/auth")){
                                String[] creds = str.split(" ");
                                nickname = AuthServer.getNickByLoginPass(creds[1], creds[2]);

                                if(isUserCorrect(nickname, server)){
                                    break;
                                }

                                /*if (nickname != null){
                                    sendServiceMsg("/authok " + "Вы залогинены под ником: " + nickname);
                                    server.subscribe(ClientHandler.this);
                                    server.sendOnlineUsers();
                                    break;
                                } else {
                                    sendMsg("Wrong Login/Password");
                                }*/
                            }
                        }

                        while (true) {
                            String str;
                            str = in.readUTF();
                            if (str.equals("/end")) {
                                out.writeUTF("/end");
                                break;
                            }
                            if (str.startsWith("/show")){
                                server.sendOnlineUsers();
                            }
                            serverMain.sendToAll(nickname + ": " + str);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            out.writeUTF("/end");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    server.unSubScribe(ClientHandler.this);
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserCorrect(String nickname, MainServer server) {
        if (nickname != null) {
            if (server.isNickFree(nickname)) {
                server.subScribe(ClientHandler.this);
                sendServiceMsg("/authok " + "Вы залогинены под ником: " + nickname);
                server.sendOnlineUsers();
                return true;
            } else {
                sendMsg("Wrong Login/Password");
                return false;
            }
        } else {
            sendMsg("Такого пользователя. Зарегистрируйтесь!");
            return false;
        }
    }

    public void sendMsg(String msg){
        System.out.println("Client send message: " + msg);
        try {
            out.writeUTF(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServiceMsg(String msg){
        System.out.println("Client send message: " + msg);
        try {
            out.writeUTF(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

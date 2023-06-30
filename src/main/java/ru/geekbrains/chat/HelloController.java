package ru.geekbrains.chat;

import MainServer.AuthServer;
import MainServer.MainServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class HelloController {

    @FXML
    TextArea textArea;
    @FXML
    Button buttonSend;
    @FXML
    TextField textField;
    @FXML
    ListView<String> clientList;
    @FXML
    Button changeNickButton;

    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button enter;
    @FXML
    Button signUpButton;
    @FXML
    TextArea textAreaView;

    @FXML
    HBox upperPanel;
    @FXML
    HBox buttonPanel;
    @FXML
    HBox signUpPanel;
    @FXML
    HBox changePanel;

    @FXML
    TextField getLoginField;
    @FXML
    PasswordField getPasswordField;
    @FXML
    TextField getNickName;
    @FXML
    TextArea textAreaMsgAuth;
    @FXML
    Button loginSignUpButton;

    @FXML
    TextField getLoginChange;
    @FXML
    PasswordField getPasswordChange;
    @FXML
    TextField getOldNick;
    @FXML
    TextField getNewNick;
    @FXML
    Button changeNickname;
    @FXML
    TextArea textAreaChange;

    Socket socket;
    static DataOutputStream out;
    DataInputStream in;

    String IP_ADDRESS = "localhost";
    int PORT = 8181;

    public static void onStageClose(){
        try {
            out.writeUTF("/end");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setActive(String scene) {

        if (scene.equals("begin")){
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            buttonPanel.setVisible(false);
            buttonPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
            signUpPanel.setVisible(false);
            signUpPanel.setManaged(false);
            changePanel.setVisible(false);
            changePanel.setManaged(false);
        } else if (scene.equals("chat")){
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            buttonPanel.setVisible(true);
            buttonPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
            signUpPanel.setVisible(false);
            signUpPanel.setManaged(false);
            changePanel.setVisible(false);
            changePanel.setManaged(false);
        } else if(scene.equals("signUp")){
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            buttonPanel.setVisible(false);
            buttonPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
            signUpPanel.setVisible(true);
            signUpPanel.setManaged(true);
            changePanel.setVisible(false);
            changePanel.setManaged(false);
        } else if (scene.equals("change")){
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            buttonPanel.setVisible(false);
            buttonPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
            signUpPanel.setVisible(false);
            signUpPanel.setManaged(false);
            changePanel.setVisible(true);
            changePanel.setManaged(true);
        }
    }

    @FXML
    public void onButtonSignUp(){
        setActive("signUp");
    }

    public void sendMessage(){
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void keyListener(KeyEvent event) {
        if (event.getCode().getCode() == 10) {
            sendMessage();
        }
    }

    @FXML
    public void changeNicknameButton(){
        setActive("change");
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true){
                        try {
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                setActive("chat");
                                textArea.appendText(str + "\n");
                                break;
                            } else {
                                textAreaView.appendText(str + "\n");
                            }
                        } catch (SocketException e) {
                            System.out.println("Сервер не отвечает");
                            break;
                        }
                    }
                    while (true) {
                        try {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.startsWith("/show")) {
                                    String[] nicknames = str.split(" ");

                                    Platform.runLater(() -> {
                                        clientList.getItems().clear();

                                        for (int i = 1; i < nicknames.length; i++) {
                                            clientList.getItems().add(nicknames[i]);
                                        }
                                    });
                                }

                                if (str.equals("/end")) {
                                    break;
                                }
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        } catch (SocketException e) {
                            System.out.println("Сервер не отвечает");
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
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
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth(){
        if (loginField.getText().isBlank() || passwordField.getText().isBlank()){
            textAreaView.appendText("Введите Login/Password\n");
            return;
        }
        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void signUp() {
        if (getLoginField.getText().isBlank() || getPasswordField.getText().isBlank() || getNickName.getText().isBlank()) {
            textAreaMsgAuth.clear();
            textAreaMsgAuth.appendText("Введите Login/Password/Nickname");
            return;
        }

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            if (AuthServer.checkUser(getLoginField.getText(), getNickName.getText())) {
                AuthServer.signUpUser(getLoginField.getText(), getPasswordField.getText(), getNickName.getText());
                textAreaView.clear();
                textAreaView.appendText("Вы успешно зарегистрировались!!!");
                setActive("begin");
            } else {
                textAreaMsgAuth.clear();
                textAreaMsgAuth.appendText("Такие логин и никнейм уже заняты, выберете другие!");
                getLoginField.clear();
                getPasswordField.clear();
                getNickName.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeNickname(){
        if (getLoginChange.getText().isBlank() || getPasswordChange.getText().isBlank()
                || getOldNick.getText().isBlank() || getNewNick.getText().isBlank()) {
            textAreaChange.clear();
            textAreaChange.appendText("Введите Login/Password/Nickname");
        }

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            if (AuthServer.checkUserForChange(getLoginChange.getText(), getPasswordChange.getText())){
                if (AuthServer.checkUser(getNewNick.getText())) {
                    if (AuthServer.changeNickname(getNewNick.getText(), getOldNick.getText())) {
                        textArea.appendText("Вы успешно сменили nickname\n");
                        setActive("chat");
                    } else {
                        textAreaChange.clear();
                        textAreaChange.appendText("Запрос на смену nickname не удался, попробуйте снова");
                        textArea.appendText("Запрос на смену nickname не удался, попробуйте снова\n");
                        setActive("chat"); //возвращаемся в окно чата, чтобы не остаться в окне смены ника
                    }
                } else {
                    textAreaChange.clear();
                    textAreaChange.appendText("Новый nickname занят другим пользователем, выберете другой");
                }
            } else {
                textAreaChange.clear();
                textAreaChange.appendText("Ошибка при вводе. Пожалуйста повторите!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
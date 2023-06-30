module ru.geekbrains.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.kordamp.bootstrapfx.core;
    requires org.xerial.sqlitejdbc;

    opens ru.geekbrains.chat to javafx.fxml;
    exports ru.geekbrains.chat;
}
module lk.ijse.chatapplicationexample {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.ijse.chatapplicationexample to javafx.fxml;
    exports lk.ijse.chatapplicationexample;
}
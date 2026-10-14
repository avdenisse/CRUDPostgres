module com.example.crud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;




    opens com.example.crud to javafx.fxml;
    opens com.example.crud.model to javafx.base;
    opens com.example.crud.controller to javafx.fxml;
    exports com.example.crud.controller to javafx.fxml;
    exports com.example.crud;

}
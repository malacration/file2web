module br.andrew.cnabworker {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.net.http;
    requires jdk.crypto.ec;
    requires jdk.crypto.cryptoki;


    opens br.andrew.cnabworker to javafx.fxml, javafx.graphics;
    exports br.andrew.cnabworker;

    opens br.andrew.cnabworker.controllers to javafx.fxml;
    exports br.andrew.cnabworker.controllers;

}
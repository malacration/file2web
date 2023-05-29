module br.andrew.file2web {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.net.http;
    requires jdk.crypto.ec;
    requires jdk.crypto.cryptoki;


    opens br.andrew.file2web to javafx.fxml, javafx.graphics;
    exports br.andrew.file2web;

    opens br.andrew.file2web.controllers to javafx.fxml;
    exports br.andrew.file2web.controllers;

}
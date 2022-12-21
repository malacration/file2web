package br.andrew.cnabworker

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class StartUpApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(StartUpApplication::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load(), 800.0, 600.0)

        scene.getStylesheets().add(
            StartUpApplication::class.java.getResource("log-view.css").toExternalForm()
        );
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(StartUpApplication::class.java)
}
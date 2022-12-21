package br.andrew.cnabworker.controllers

import br.andrew.cnabworker.nodes.LogNode
import br.andrew.cnabworker.service.SendScheduledTask
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.util.*


class MainController {

    private var dirSaida: File?  = null
    private var dirEntrada: File? = null

    val time = Timer()
    var task : SendScheduledTask? = null

    @FXML
    var buttonEntrada : Button? = null
    @FXML
    var buttonSaida : Button? = null
    @FXML
    var excToggle : ToggleButton? = null;
    @FXML
    var urlFild : TextField? = null
    @FXML
    var content : VBox? = null

    @FXML
    fun initialize(){
        content?.children?.add(LogNode())
    }

    @FXML
    fun entradaButton(){
        dirEntrada = getDir()
        buttonEntrada?.text = "Selecionando: "+dirEntrada.toString()
    }

    @FXML
    fun saidaButton(){
        dirSaida = getDir()
        buttonSaida?.text = "Selecionando: "+dirEntrada.toString()
    }

    fun getDir() : File {
        val janela = Stage()
        val directoryChooser = DirectoryChooser()
        return directoryChooser.showDialog(janela)
    }

    @FXML
    fun toggle(){
        if(excToggle?.isSelected != true){
            LogNode.logger.info("Pausando Execução")
            if(task?.cancel() == true){
                LogNode.logger.info("Tarefa pausada iniciando purge")
                time.purge()
            }
        }else if(dirEntrada == null || dirSaida == null || urlFild?.text?.isNotBlank() != true){
            val alert = Alert(AlertType.ERROR)
            alert.setTitle("Erro");
            alert.setHeaderText("Porfavor preencha todos os campos");
            alert.show()
            excToggle?.isSelected = false
        } else{
            LogNode.logger.info("Iniciando agendamento")
            task = SendScheduledTask(urlFild!!.text,dirEntrada!!,dirSaida!!)
            time.schedule(task, 0, 1000)
        }
    }
}
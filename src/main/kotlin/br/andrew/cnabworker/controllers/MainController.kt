package br.andrew.cnabworker.controllers

import br.andrew.cnabworker.Config
import br.andrew.cnabworker.ConfigRepository
import br.andrew.cnabworker.Diretorio
import br.andrew.cnabworker.nodes.LogNode
import br.andrew.cnabworker.service.SendScheduledTask
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.util.*


class MainController {

    private var dirSaida: File?  = null

    val time = Timer()
    var task : SendScheduledTask? = null

    @FXML
    var buttonSaida : Button? = null
    @FXML
    var excToggle : ToggleButton? = null;
    @FXML
    var urlFild : TextField? = null
    @FXML
    var tableEntrada : TableView<Diretorio>? = null
    @FXML
    var content : VBox? = null

    @FXML
    fun initialize(){
        content?.children?.add(LogNode())
        if(tableEntrada != null){
            val dir = TableColumn<Diretorio,String>("Diretorio").also { it.setCellValueFactory(PropertyValueFactory("pathDir")); it.minWidth = 280.0}
            val delete = TableColumn<Diretorio,String>("Excluir?").also { it.setCellFactory(Diretorio.test) }
            tableEntrada!!.columns.setAll(listOf(dir,delete))
            val config = ConfigRepository().carregar()
            if(config != null){
                urlFild!!.text = config.url
                tableEntrada!!.items.addAll(config.getDirs())
                dirSaida = File(config.saida)
                buttonSaida?.text = "Selecionando: "+dirSaida.toString()
            }
        }
    }

    @FXML
    fun entradaButton(){
        tableEntrada!!.items.add(Diretorio(getDir().toString()))// = FXCollections.observableArrayList(Diretorio(dirEntrada.toString()))
    }

    @FXML
    fun saidaButton(){
        dirSaida = getDir()
        buttonSaida?.text = "Selecionando: "+dirSaida.toString()
    }

    fun getDir() : File {
        val janela = Stage()
        val directoryChooser = DirectoryChooser()
        return directoryChooser.showDialog(janela)
    }

    @FXML
    fun toggle(){
        if(excToggle?.isSelected != true){
            LogNode.logger.info("Pausando Execu????o")
            if(task?.cancel() == true){
                LogNode.logger.info("Tarefa pausada iniciando purge")
                time.purge()
            }
        }else if(!valido()){
            val alert = Alert(AlertType.ERROR)
            alert.setTitle("Erro");
            alert.setHeaderText("Porfavor preencha todos os campos");
            alert.show()
            excToggle?.isSelected = false
        } else{
            salvar()
            LogNode.logger.info("Iniciando agendamento")
            task = SendScheduledTask(urlFild!!.text, this.tableEntrada!!.items.map { File(it.pathDir) },dirSaida!!)
            time.schedule(task, 5000, 1000)
        }
    }

    fun salvar() {
        if(valido()){
            ConfigRepository().salvar(Config(urlFild!!.text, this.tableEntrada!!.items.map { it.pathDir },dirSaida!!.toString()))
            LogNode.logger.info("Configura????o salva com sucesso")
        }
    }

    private fun valido(): Boolean {
        return this.tableEntrada!!.items.size > 0 || dirSaida == null || urlFild?.text?.isNotBlank() != true
    }
}
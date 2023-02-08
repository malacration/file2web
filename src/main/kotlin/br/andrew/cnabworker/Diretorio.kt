package br.andrew.cnabworker

import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class Diretorio(val pathDir : String) {


    companion object {
        val test = object : Callback<TableColumn<Diretorio, String>, TableCell<Diretorio, String>> {
            override fun call(param: TableColumn<Diretorio, String>): TableCell<Diretorio, String> {
                return object : TableCell<Diretorio, String>() {
                    private val btn: Button = Button("Remover")
                    init {
                        btn.setOnAction { event: ActionEvent? ->
                            tableView.items.remove(this.tableView.items.get(index))
                        }
                    }

                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty) {
                            setGraphic(null)
                        } else {
                            setGraphic(btn)
                        }
                    }
                }
            }
        }
    }
}
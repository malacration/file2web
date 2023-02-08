package br.andrew.cnabworker

import java.io.File
import java.io.Serializable

class Config(val url: String, val entradas: List<String>, val saida: String) : Serializable{
    fun getDirs(): List<Diretorio> {
        return entradas.map { Diretorio(it) }
    }

}
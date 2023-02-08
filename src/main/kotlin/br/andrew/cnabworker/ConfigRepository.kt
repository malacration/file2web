package br.andrew.cnabworker

import br.andrew.cnabworker.nodes.LogNode
import java.io.*


class ConfigRepository {

    val nomeArquivo = "config"

    fun carregar() : Config?{
        LogNode.logger.info("Carregando configs")
        var lista : Config? = null
        try {
            val arq = File(nomeArquivo)
            if (arq.exists()) {
                val objInput = ObjectInputStream(FileInputStream(arq))
                lista = objInput.readObject() as Config
                objInput.close()
            }
        } catch (erro1: IOException) {
            erro1.printStackTrace()
            LogNode.logger.error("Erro: %s "+erro1.message)
        } catch (erro2: ClassNotFoundException) {
            erro2.printStackTrace()
            LogNode.logger.error("Erro: %s "+erro2.message)
        }
        return lista
    }

    fun salvar(config : Config){
        val arq = File(nomeArquivo)
        try {
            arq.delete()
            arq.createNewFile()
            val objOutput = ObjectOutputStream(FileOutputStream(arq))
            objOutput.writeObject(config)
            objOutput.close()
        } catch (erro: IOException) {
            LogNode.logger.error("Erro: %s "+erro.message)
            erro.printStackTrace()
            arq.delete()
        }
    }
}
package br.andrew.cnabworker.service

import br.andrew.cnabworker.nodes.LogNode
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.*

class SendScheduledTask(url : String, val entradas : List<File>, val saida : File, val fakeHttp: Boolean = false) : TimerTask() {

    val uri: URI = URI(url)
    val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

    override fun run() {
        entradas.forEach { entrada ->
            Files.newDirectoryStream(entrada.toPath()).forEach {
                LogNode.logger.info("Enviando arquivo " + it.fileName)
                val bytes = Files.readAllBytes(it)
                val post = HttpRequest.newBuilder(uri)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build()
                val response = httpClient.send(post, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 200) {
                    LogNode.logger.info("Arquivo recebido pelo servidor " + it.fileName)
                    if (it.toFile().renameTo(File(saida.toString() + File.separator + it.fileName))) {
                        LogNode.logger.error("Não foi possivel mover o arquivo " + it.fileName)
                    }
                } else {
                    LogNode.logger.error("Falha ao enviar documento ao servidor " + it.fileName)
                    LogNode.logger.error(response.body())
                }
            }
        }
    }
}
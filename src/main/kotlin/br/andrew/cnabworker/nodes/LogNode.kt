package br.andrew.cnabworker.nodes

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.css.PseudoClass
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.Duration
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class LogNode : HBox(10.0) {

    companion object{
        val logger : Logger = Logger(Log(), "main")
    }

    init{
        val logView = LogView(logger)
        logView.prefWidth = 500.0
        val filterLevel = ChoiceBox(
            FXCollections.observableArrayList(
                *Level.values()
            )
        )

        filterLevel.selectionModel.select(Level.DEBUG)
        logView.filterLevelProperty().bind(
            filterLevel.selectionModel.selectedItemProperty()
        )

        val showTimestamp = ToggleButton("Show Timestamp")
        logView.showTimeStampProperty().bind(showTimestamp.selectedProperty())

        val tail = ToggleButton("Tail")
        logView.tailProperty().bind(tail.selectedProperty())

        val pause = ToggleButton("Pause")
        logView.pausedProperty().bind(pause.selectedProperty())

        val rate = Slider(0.1, 60.0, 60.0)
        logView.refreshRateProperty().bind(rate.valueProperty())

        val rateLabel = Label()
        rateLabel.textProperty().bind(Bindings.format("Update: %.2f fps", rate.valueProperty()))
        rateLabel.style = "-fx-font-family: monospace;"
        val rateLayout = VBox(rate, rateLabel)
        rateLayout.alignment = Pos.CENTER

        val controls = HBox(
            10.0,
            filterLevel,
            showTimestamp,
            tail,
            pause,
            rateLayout
        )
        controls.minHeight = HBox.USE_PREF_SIZE
        val layout = VBox(
            10.0,
            controls,
            logView
        )
        VBox.setVgrow(logView, Priority.ALWAYS)
        this.children.addAll(layout)
    }
}



class Log {
    private val log: BlockingDeque<LogRecord> = LinkedBlockingDeque(
        MAX_LOG_ENTRIES
    )

    fun drainTo(collection: MutableCollection<LogRecord>) {
        log.drainTo(collection)
    }

    fun offer(record: LogRecord) {
        log.offer(record)
    }

    companion object {
        private const val MAX_LOG_ENTRIES = 1000000
    }
}

class Logger(val log: Log, val context: String) {
    fun log(record: LogRecord) {
        log.offer(record)
    }

    fun debug(msg: String) {
        log(LogRecord(Level.DEBUG, context, msg))
    }

    fun info(msg: String) {
        log(LogRecord(Level.INFO, context, msg))
    }

    fun warn(msg: String) {
        log(LogRecord(Level.WARN, context, msg))
    }

    fun error(msg: String) {
        log(LogRecord(Level.ERROR, context, msg))
    }
}

enum class Level {
    DEBUG, INFO, WARN, ERROR
}

class LogRecord(level: Level, context: String, message: String) {
    private val timestamp: Date
    val level: Level
    private val context: String
    val message: String

    init {
        timestamp = Date()
        this.level = level
        this.context = context
        this.message = message
    }

    fun getTimestamp(): Date? {
        return timestamp
    }

    fun getContext(): String? {
        return context
    }
}

internal class LogView(logger: Logger) : ListView<LogRecord>() {
    private val showTimestamp: BooleanProperty = SimpleBooleanProperty(false)
    private val filterLevel: ObjectProperty<Level> = SimpleObjectProperty(null)
    private val tail: BooleanProperty = SimpleBooleanProperty(false)
    private val paused: BooleanProperty = SimpleBooleanProperty(false)
    private val refreshRate: DoubleProperty = SimpleDoubleProperty(60.0)
    private val logItems: ObservableList<LogRecord> = FXCollections.observableArrayList()

    fun showTimeStampProperty(): BooleanProperty {
        return showTimestamp
    }

    fun filterLevelProperty(): ObjectProperty<Level> {
        return filterLevel
    }

    fun tailProperty(): BooleanProperty {
        return tail
    }

    fun pausedProperty(): BooleanProperty {
        return paused
    }

    fun refreshRateProperty(): DoubleProperty {
        return refreshRate
    }

    init {
        styleClass.add("log-view")
        val logTransfer = Timeline(
            KeyFrame(
                Duration.seconds(1.0),
                { event: ActionEvent? ->
                    logger.log.drainTo(logItems)
                    if (logItems.size > MAX_ENTRIES) {
                        logItems.remove(0, logItems.size - MAX_ENTRIES)
                    }
                    if (tail.get()) {
                        scrollTo(logItems.size)
                    }
                }
            )
        )
        logTransfer.cycleCount = Timeline.INDEFINITE
        logTransfer.rateProperty().bind(refreshRateProperty())
        pausedProperty().addListener { observable: ObservableValue<out Boolean>, oldValue: Boolean, newValue: Boolean ->
            if (newValue && logTransfer.status == Animation.Status.RUNNING) {
                logTransfer.pause()
            }
            if (!newValue && logTransfer.status == Animation.Status.PAUSED && parent != null) {
                logTransfer.play()
            }
        }

        parentProperty().addListener { observable: ObservableValue<out Parent?>?, oldValue: Parent?, newValue: Parent? ->
            if (newValue == null) {
                logTransfer.pause()
            } else {
                if (!paused.get()) {
                    logTransfer.play()
                }
            }
        }
        filterLevel.addListener { observable: ObservableValue<out Level?>?, oldValue: Level?, newValue: Level? ->
            items = FilteredList(
                logItems
            ) { logRecord: LogRecord ->
                logRecord.level.ordinal >=
                        filterLevel.get()!!.ordinal
            }
        }
        filterLevel.set(Level.DEBUG)

        cellFactory =
            Callback { param: ListView<LogRecord?>? ->
                object : ListCell<LogRecord?>() {
                    init {
                        showTimestamp.addListener { observable: Observable? ->
                            updateItem(
                                item,
                                this.isEmpty
                            )
                        }
                    }

                    override fun updateItem(item: LogRecord?, empty: Boolean) {
                        super.updateItem(item, empty)
                        pseudoClassStateChanged(debug, false)
                        pseudoClassStateChanged(info, false)
                        pseudoClassStateChanged(warn, false)
                        pseudoClassStateChanged(error, false)
                        if (item == null || empty) {
                            text = null
                            return
                        }
                        val context = if (item.getContext() == null) "" else item.getContext() + " "
                        text = if (showTimestamp.get()) {
                            val timestamp =
                                if (item.getTimestamp() == null) "" else timestampFormatter.format(item.getTimestamp()) + " "
                            timestamp + context + item.message
                        } else {
                            context + item.message
                        }
                        when (item.level) {
                            Level.DEBUG -> pseudoClassStateChanged(debug, true)
                            Level.INFO -> pseudoClassStateChanged(info, true)
                            Level.WARN -> pseudoClassStateChanged(warn, true)
                            Level.ERROR -> pseudoClassStateChanged(error, true)
                        }
                    }
                }
            }
    }

    companion object {
        private const val MAX_ENTRIES = 10000
        private val debug = PseudoClass.getPseudoClass("debug")
        private val info = PseudoClass.getPseudoClass("info")
        private val warn = PseudoClass.getPseudoClass("warn")
        private val error = PseudoClass.getPseudoClass("error")
        private val timestampFormatter = SimpleDateFormat("HH:mm:ss.SSS")
    }
}

class Lorem {
    private var idx = 0
    private val random = Random(42)
    @Synchronized
    fun nextString(): String {
        val end = Math.min(idx + MSG_WORDS, IPSUM.size)
        val result = StringBuilder()
        for (i in idx until end) {
            result.append(IPSUM[i]).append(" ")
        }
        idx += MSG_WORDS
        idx = idx % IPSUM.size
        return result.toString()
    }

    @Synchronized
    fun nextLevel(): Level {
        val v = random.nextDouble()
        if (v < 0.8) {
            return Level.DEBUG
        }
        if (v < 0.95) {
            return Level.INFO
        }
        return if (v < 0.985) {
            Level.WARN
        } else Level.ERROR
    }

    companion object {
        private val IPSUM =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque hendrerit imperdiet mi quis convallis. Pellentesque fringilla imperdiet libero, quis hendrerit lacus mollis et. Maecenas porttitor id urna id mollis. Suspendisse potenti. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras lacus tellus, semper hendrerit arcu quis, auctor suscipit ipsum. Vestibulum venenatis ante et nulla commodo, ac ultricies purus fringilla. Aliquam lectus urna, commodo eu quam a, dapibus bibendum nisl. Aliquam blandit a nibh tincidunt aliquam. In tellus lorem, rhoncus eu magna id, ullamcorper dictum tellus. Curabitur luctus, justo a sodales gravida, purus sem iaculis est, eu ornare turpis urna vitae dolor. Nulla facilisi. Proin mattis dignissim diam, id pellentesque sem bibendum sed. Donec venenatis dolor neque, ut luctus odio elementum eget. Nunc sed orci ligula. Aliquam erat volutpat.".split(
                " ".toRegex()
            ).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        private const val MSG_WORDS = 8
    }
}
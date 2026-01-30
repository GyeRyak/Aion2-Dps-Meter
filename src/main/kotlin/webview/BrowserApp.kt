package com.tbread.webview

import com.tbread.DpsCalculator
import com.tbread.entity.DpsData
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.HostServices
import javafx.concurrent.Worker
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.web.WebView
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import javafx.application.Platform
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.system.exitProcess

import com.tbread.packet.PcapCapturer
import com.tbread.packet.PropertyHandler
import netscape.javascript.JSObject
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class BrowserApp(private val dpsCalculator: DpsCalculator) : Application() {

    private val logger = LoggerFactory.getLogger(BrowserApp::class.java)

    class JSBridge(private val stage: Stage,private val dpsCalculator: DpsCalculator,private val hostServices: HostServices,) {
        fun moveWindow(x: Double, y: Double) {
            stage.x = x
            stage.y = y
        }

        fun resetDps(){
            dpsCalculator.resetDataStorage()
        }
        fun openBrowser(url: String) {
            try {
                hostServices.showDocument(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun exitApp() {
          Platform.exit()     
          exitProcess(0)       
        }

        fun getNetworkInterfaces(): String {
            val devices = PcapCapturer.getAllDevices()
            val list = devices.map { device ->
                mapOf(
                    "name" to device.name,
                    "description" to (device.description ?: "Unknown"),
                    "addresses" to device.addresses.mapNotNull { it.address?.hostAddress }
                )
            }
            return Json.encodeToString(list)
        }

        fun saveNetworkInterface(name: String) {
            PropertyHandler.setProperty("network.interface", name)
        }

        fun getCurrentInterface(): String {
            return PropertyHandler.getProperty("network.interface") ?: "Any"
        }

        fun restartApp() {
            try {
                val javaBin = System.getProperty("java.home") + "/bin/java"
                val currentJar = java.io.File(BrowserApp::class.java.protectionDomain.codeSource.location.toURI())

                if (!currentJar.name.endsWith(".jar")) {
                     logger.warn("Not running from a jar, restart might fail or behave unexpectedly.")
                     // 개발 환경(IDE)일 수 있음. 그냥 종료만 하거나, gradle command를 실행할 수도 있지만
                     // 여기서는 단순히 종료 후 사용자가 다시 켜게 하거나, 
                     // 베스트 에포트로 같은 커맨드를 시도해봄.
                }

                val command = ArrayList<String>()
                command.add(javaBin)
                command.add("-jar")
                command.add(currentJar.path)

                val builder = ProcessBuilder(command)
                builder.start()
                exitApp()
            } catch (e: Exception) {
                logger.error("Failed to restart", e)
                // 실패시 그냥 종료 시도 혹은 에러 메시지
                exitApp()
            }
        }
    }

    @Volatile
    private var dpsData: DpsData = dpsCalculator.getDps()

    private val debugMode = false

    private val version = "0.2.4"


    override fun start(stage: Stage) {
        stage.setOnCloseRequest {
            exitProcess(0)
        }
        val webView = WebView()
        val engine = webView.engine
        engine.load(javaClass.getResource("/index.html")?.toExternalForm())

        val bridge = JSBridge(stage, dpsCalculator, hostServices)
        engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                val window = engine.executeScript("window") as JSObject
                window.setMember("javaBridge", bridge)
                window.setMember("dpsData", this)
            }
        }


        val scene = Scene(webView, 1600.0, 1000.0)
        scene.fill = Color.TRANSPARENT

        try {
            val pageField = engine.javaClass.getDeclaredField("page")
            pageField.isAccessible = true
            val page = pageField.get(engine)

            val setBgMethod = page.javaClass.getMethod("setBackgroundColor", Int::class.javaPrimitiveType)
            setBgMethod.isAccessible = true
            setBgMethod.invoke(page, 0)
        } catch (e: Exception) {
            logger.error("리플렉션 실패",e)
        }

        stage.initStyle(StageStyle.TRANSPARENT)
        stage.scene = scene
        stage.isAlwaysOnTop = true
        stage.title = "Aion2 Dps Overlay"

        stage.show()
        Timeline(KeyFrame(Duration.millis(500.0), {
            dpsData = dpsCalculator.getDps()
        })).apply {
            cycleCount = Timeline.INDEFINITE
            play()
        }
    }

    fun getDpsData(): String {
        return Json.encodeToString(dpsData)
    }

    fun isDebuggingMode(): Boolean {
        return debugMode
    }

    fun getBattleDetail(uid:Int):String{
        return Json.encodeToString(dpsData.map[uid]?.analyzedData)
    }

    fun getVersion():String{
        return version
    }

}

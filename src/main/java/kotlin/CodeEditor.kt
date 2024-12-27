package com.gide

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class CodeEditor : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(CodeEditor::class.java.getResource("code-editor.fxml"))
        val scene = Scene(fxmlLoader.load(), 800.0, 600.0)
        stage.title = "Code Editor"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(CodeEditor::class.java)
}
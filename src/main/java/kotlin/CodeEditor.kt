package com.gide

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.TextInputDialog
import javafx.stage.Stage
import javafx.util.Duration
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import java.io.File
import java.util.regex.Pattern

class CodeEditor : Application() {
    private lateinit var codeArea: CodeArea
    private var currentFile: File? = null

    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(CodeEditor::class.java.getResource("code-editor.fxml"))
        val root = fxmlLoader.load<VBox>()
        codeArea = CodeArea()
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea))
        codeArea.richChanges().subscribe { _ -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.text)) }
        root.children.add(codeArea)

        val scene = Scene(root, 800.0, 600.0)
        scene.stylesheets.add(CodeEditor::class.java.getResource("code-editor.css").toExternalForm())
        stage.title = "Code Editor"
        stage.scene = scene
        stage.show()

        // Load a file into the editor
        loadFile(stage)

        // Show find and replace dialog
        showFindReplaceDialog(stage)

        // Initialize auto-save
        initializeAutoSave()
    }

    private fun loadFile(stage: Stage) {
        val fileChooser = FileChooser()
        fileChooser.title = "Open Resource File"
        val file = fileChooser.showOpenDialog(stage)
        file?.let {
            currentFile = it
            codeArea.replaceText(it.readText())
        }
    }

    private fun saveFile() {
        currentFile?.let {
            it.writeText(codeArea.text)
        }
    }

    private fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
        val pattern = Pattern.compile(
            "(?<KEYWORD>\\b(if|else|for|while|return|fun|class|val|var)\\b)"
        )
        val matcher = pattern.matcher(text)
        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        var lastKwEnd = 0
        while (matcher.find()) {
            val styleClass = when {
                matcher.group("KEYWORD") != null -> "keyword"
                else -> null
            }
            spansBuilder.add(emptyList(), matcher.start() - lastKwEnd)
            spansBuilder.add(listOfNotNull(styleClass), matcher.end() - matcher.start())
            lastKwEnd = matcher.end()
        }
        spansBuilder.add(emptyList(), text.length - lastKwEnd)
        return spansBuilder.create()
    }

    private fun showFindReplaceDialog(stage: Stage) {
        val findDialog = TextInputDialog()
        findDialog.title = "Find and Replace"
        findDialog.headerText = "Find and Replace"
        findDialog.contentText = "Find:"

        val findResult = findDialog.showAndWait()
        findResult.ifPresent { findText ->
            val replaceDialog = TextInputDialog()
            replaceDialog.title = "Replace"
            replaceDialog.headerText = "Replace"
            replaceDialog.contentText = "Replace with:"

            val replaceResult = replaceDialog.showAndWait()
            replaceResult.ifPresent { replaceText ->
                findAndReplace(findText, replaceText)
            }
        }
    }

    private fun findAndReplace(findText: String, replaceText: String) {
        val text = codeArea.text
        val newText = text.replace(findText, replaceText)
        codeArea.replaceText(newText)
    }

    private fun initializeAutoSave() {
        val timeline = Timeline(KeyFrame(Duration.minutes(1.0), { saveFile() }))
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }
}

fun main() {
    Application.launch(CodeEditor::class.java)
}
package com.github.sblzdddd.oicoderplugin.services

import com.github.sblzdddd.oicoderplugin.Constants
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

class OiCreateCppFileAction : AnAction() {
    private val createFile = OiCreateCppFile()
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        createFile.createCppFile0(project)
    }
}
class OiCreateCppFile {
    public fun createCppFile0(project: Project, fileNameParam: String="") {
        // Show directory selection dialog
        val descriptor = FileChooserDescriptor(
            false, true, false, false, false, false
        ).withTitle("Select Directory to Save C++ File")

        FileChooser.chooseFiles(descriptor, project, null) { files ->
            if (files.isNotEmpty()) {
                val directory = files[0]
                var fileName = fileNameParam;

                while (fileName == "" || !fileName.isNotBlank()) {
                    fileName = Messages.showInputDialog(project, "Enter C++ file name (without extension):",
                        "Create C++ File", Messages.getQuestionIcon()).toString()
                }
                createCppFile(directory, "$fileName.cpp", project)
            }
        }

    }

    private fun createCppFile(directory: VirtualFile, fileName: String, project: Project) {
        ApplicationManager.getApplication().runWriteAction {
            try {
                val file = directory.createChildData(this, fileName)
                WriteCommandAction.runWriteCommandAction(project) {
                    file.setBinaryContent(Constants.CPP_CONTENT.toByteArray())
                }
                Messages.showInfoMessage("C++ file created successfully!", "Success")
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to create C++ file: ${e.message}", "Error")
            }
        }
    }
}
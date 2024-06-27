package com.github.sblzdddd.oicoderplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.sblzdddd.oicoderplugin.MyBundle
import com.github.sblzdddd.oicoderplugin.toolWindow.OiToolWindowFactory
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.messages.MessageBusConnection

@Service(Service.Level.PROJECT)
class OiCoderService(project: Project) {

    init {
        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {

            // Listen for Window Close
            override fun stateChanged(toolWindowManager: ToolWindowManager) {
                val toolWindow = toolWindowManager.getToolWindow("OICoder")
                if (toolWindow != null && !toolWindow.isVisible) {
                    // Tool window is closing
                    val factory = toolWindow.contentManager.factory as? OiToolWindowFactory<Any?>
                    factory?.getBrowser()?.jbCefClient?.dispose()
                    factory?.getBrowser()?.dispose()
                    thisLogger().info("Dispose on window close")
                }
            }
        })
        thisLogger().info(MyBundle.message("projectService", project.name))
    }

    fun getRandomNumber() = (1..100).random()
}

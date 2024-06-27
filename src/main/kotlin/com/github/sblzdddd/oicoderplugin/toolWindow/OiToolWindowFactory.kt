package com.github.sblzdddd.oicoderplugin.toolWindow

import com.github.sblzdddd.oicoderplugin.Constants
import com.github.sblzdddd.oicoderplugin.services.OiCoderService
import com.github.sblzdddd.oicoderplugin.services.OiCreateCppFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities


class OiToolWindowFactory<DataContext> : ToolWindowFactory {

    init {
    }
    private var browser: JBCefBrowser? = null
    private var jsQuery: JBCefJSQuery? = null
    private val fileAction = OiCreateCppFile();

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val oiViewWindow = OiViewWindow(toolWindow)

        // Create JCEF Browser instance
        browser = JBCefBrowser()

        configureBrowser(browser!!, project)

        browser!!.loadURL("http://oi.beecent.cn")

        // Create a panel with BorderLayout
        val panel = JPanel(BorderLayout())
        panel.add(browser!!.component, BorderLayout.CENTER)

        // Create a panel for the buttons with FlowLayout
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val backButton = JButton("<<")
        val homeButton = JButton("Home")
        val reloadButton = JButton("reload")
        val devToolsButton = JButton("DevTools")
        val forwButton = JButton(">>")
        buttonPanel.add(backButton)
        buttonPanel.add(homeButton)
        buttonPanel.add(reloadButton)
        buttonPanel.add(devToolsButton)
        buttonPanel.add(forwButton)

        backButton.addActionListener {
            browser!!.cefBrowser.executeJavaScript(
                "history.back();",
                "", 0)
        }
        forwButton.addActionListener {
            browser!!.cefBrowser.executeJavaScript(
                "history.forward();",
                "", 0)
        }

        homeButton.addActionListener {
            browser!!.loadURL("http://oi.beecent.cn")
        }
        reloadButton.addActionListener {
            browser!!.cefBrowser.reload();
        }
        devToolsButton.addActionListener {
            SwingUtilities.invokeLater {
                browser!!.openDevtools()
            }
        }

        // Add the button panel to the bottom of the main panel
        panel.add(buttonPanel, BorderLayout.SOUTH)


        val content = ContentFactory.getInstance().createContent(
            panel,
            null,
            false
        )

        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class OiViewWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<OiCoderService>()

    }

    private fun configureBrowser(browser: JBCefBrowser, project: Project) {
        thisLogger().info("configure browser")
        val jbCefClient: JBCefClient = browser.jbCefClient
        jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {

            override fun onLoadEnd(b: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame != null && frame.isMain) {
                    val url = browser.cefBrowser.url
                    frame?.executeJavaScript(Constants.COMMON_JS, "", 0);
                    frame.executeJavaScript("console.log('Page $url Has been Loaded!')", url, 0)

                    if (url.startsWith(Constants.HOME_URL)) {
                        frame.executeJavaScript(Constants.HOME_JS, url, 0);
                    }
                    if (url.startsWith(Constants.LOGIN_URL)) {
                        frame.executeJavaScript(Constants.LOGIN_JS, url, 0);
                    }
                    if (url.startsWith(Constants.QUIZ_URL)) {
                        frame.executeJavaScript(Constants.QUIZ_JS, url, 0);
                    }
                    if (url.startsWith(Constants.SUMMARY_URL)) {
                        frame.executeJavaScript(Constants.SUBMIT_JS, url, 0);
                    }
                    if (url.startsWith(Constants.COURSE_URL)) {
                        frame.executeJavaScript(Constants.COURSE_JS, url, 0);
                    }
                    disposeJsQuery()
                    createQuery(browser, project)

                    thisLogger().warn("Page $url has loaded with status code: $httpStatusCode")

                }
            }
        }, browser.cefBrowser)
    }
    private fun createQuery(browser: JBCefBrowser, project: Project) {
        jsQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
        jsQuery!!.addHandler { query: String? ->
            when {
                query == "getCode" -> {
                    val code = getCurrentFileContent(project)
                    browser.cefBrowser.executeJavaScript(
                        "window.receiveCode(`${code.toJsonString()}`)",
                        browser.cefBrowser.url, 0
                    )
                    JBCefJSQuery.Response("success")
                }

                query?.startsWith("createCppFile") == true -> {
                    ApplicationManager.getApplication().invokeLater {
                        val parts = query.split(":")
                        var fileName = ""
                        if (parts.size == 2) {
                            fileName = parts[1]
                        }
                        fileAction.createCppFile0(project, fileName)
                        JBCefJSQuery.Response("success")
                    }
                }

                else -> JBCefJSQuery.Response("unknown query")
            }
            null
        }
        val getCode = jsQuery!!.inject("'getCode'");
        val createCppFile = jsQuery!!.inject("'createCppFile:'+fileName")
        browser.cefBrowser.executeJavaScript(
            "window.getCode = function() { $getCode };" +
            "window.createCppFile = function(fileName) { $createCppFile };",
            browser.cefBrowser.url, 0
        )
        thisLogger().warn("Created Query")
    }

    private fun disposeJsQuery() {
        jsQuery?.dispose()
        jsQuery = null
    }

    private fun getCurrentFileContent(project: Project): String {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        return editor?.document?.text ?: "No active editor found"
    }


    private fun String.toJsonString(): String {
        return replace("\"", "\\\"")
    }
    fun getBrowser(): JBCefBrowser? {
        return browser;
    }
}

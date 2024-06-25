package com.github.sblzdddd.oicoderplugin.toolWindow

import com.github.sblzdddd.oicoderplugin.Constants
import com.github.sblzdddd.oicoderplugin.services.OiCoderService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities


class OiToolWindowFactory : ToolWindowFactory {

    init {
    }
    private var browser: JBCefBrowser? = null

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val oiViewWindow = OiViewWindow(toolWindow)

        // Create JCEF Browser instance
        browser = JBCefBrowser()

        configureBrowser(browser!!)

        browser!!.loadURL("http://oi.beecent.cn")

        // Create a panel with BorderLayout
        val panel = JPanel(BorderLayout())
        panel.add(browser!!.component, BorderLayout.CENTER)

        // Create a panel for the buttons with FlowLayout
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val backButton = JButton("<<")
        val homeButton = JButton("Reload")
        val devToolsButton = JButton("DevTools")
        val forwButton = JButton(">>")
        buttonPanel.add(backButton)
        buttonPanel.add(homeButton)
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

    private fun configureBrowser(browser: JBCefBrowser) {
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

                    thisLogger().warn("Page $url has loaded with status code: $httpStatusCode")

                }
            }
        }, browser.cefBrowser)
    }
    fun getBrowser(): JBCefBrowser? {
        return browser;
    }
}

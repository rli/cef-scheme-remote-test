package com.github.rli.cefschemeremotetest.toolWindow

import com.github.rli.cefschemeremotetest.toolWindow.AssetResourceHandler.AssetResourceHandlerFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowserBuilder
import org.cef.CefApp


class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow {
        fun getContent() = JBCefBrowserBuilder()
            .build()
            .apply {
                CefApp.getInstance()
                    .registerSchemeHandlerFactory(
                        "http",
                        "localscheme",
                        AssetResourceHandlerFactory(),
                    )
                // language=HTML
                loadHTML("""
                    <html>
                    <body>
                        <h1>H1</h1>
                        <img src='http://localscheme/image.png' alt='' />
                    </body>
                    </html>
                """.trimIndent())
            }
            .component
    }
}

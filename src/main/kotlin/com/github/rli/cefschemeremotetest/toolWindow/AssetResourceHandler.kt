package com.github.rli.cefschemeremotetest.toolWindow

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefCallback
import org.cef.callback.CefSchemeHandlerFactory
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.IOException
import java.net.URLConnection

open class AssetResourceHandler(var data: ByteArray) : CefResourceHandler {
    /**
     * Factory class for [AssetResourceHandler]. Ignores any request that doesn't begin with
     * [AssetResourceHandler.LOCAL_RESOURCE_URL_PREFIX]
     */
    open class AssetResourceHandlerFactory : CefSchemeHandlerFactory {
        override fun create(
            browser: CefBrowser?,
            frame: CefFrame?,
            schemeName: String?,
            request: CefRequest?,
        ): AssetResourceHandler? {
            val resourceUri = request?.url ?: return null
            if (!resourceUri.startsWith(LOCAL_RESOURCE_URL_PREFIX)) return null

            val resource = resourceUri.replace(LOCAL_RESOURCE_URL_PREFIX, "/assets/")
            val resourceInputStream = this.javaClass.getResourceAsStream(resource)

            try {
                resourceInputStream.use {
                    if (resourceInputStream != null) {
                        return AssetResourceHandler(resourceInputStream.readAllBytes())
                    }
                    return null
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private var offset = 0
    private var resourceUri: String? = null
    override fun processRequest(cefRequest: CefRequest, cefCallback: CefCallback): Boolean {
        resourceUri = cefRequest.url
        cefCallback.Continue()
        return true
    }

    override fun getResponseHeaders(cefResponse: CefResponse, intRef: IntRef, stringRef: StringRef) {
        intRef.set(data.size)
        cefResponse.setHeaderByName("Access-Control-Allow-Origin", "*", true)
        val mimeType = if (resourceUri?.endsWith(".wasm") == true) "application/wasm" else URLConnection.getFileNameMap().getContentTypeFor(resourceUri)
        if (mimeType != null) cefResponse.mimeType = mimeType
        cefResponse.status = 200
    }

    override fun readResponse(
        outBuffer: ByteArray,
        bytesToRead: Int,
        bytesRead: IntRef,
        cefCallback: CefCallback,
    ): Boolean {
        if (offset >= data.size) {
            cefCallback.cancel()
            return false
        }
        var lenToRead = Math.min(outBuffer.size, bytesToRead)
        lenToRead = Math.min(data.size - offset, lenToRead)
        System.arraycopy(data, offset, outBuffer, 0, lenToRead)
        bytesRead.set(lenToRead)
        offset = offset + lenToRead
        return true
    }

    override fun cancel() {}

    companion object {
        private const val LOCAL_RESOURCE_URL_PREFIX = "http://localscheme/"
    }
}

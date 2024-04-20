package cn.netdiscovery.compose.imageloader.log

/**
 *
 * @FileName:
 *          cn.netdiscovery.compose.imageloader.log.LoggerProxy
 * @author: Tony Shen
 * @date:  2024/4/20 16:20
 * @version: V1.0 <描述当前版本功能>
 */
interface Logger {
    fun i(msg: String, tag: String? = "imageloader")
    fun v(msg: String, tag: String? = "imageloader")
    fun d(msg: String, tag: String? = "imageloader")
    fun w(msg: String, tag: String? = "imageloader", tr: Throwable?)
    fun e(msg: String, tag: String? = "imageloader", tr: Throwable?)
}

object DefaultLogger: Logger {
    override fun i(msg: String, tag: String?) {
        println("$tag $msg")
    }

    override fun v(msg: String, tag: String?) {
        println("$tag $msg")
    }

    override fun d(msg: String, tag: String?) {
        println("$tag $msg")
    }

    override fun w(msg: String, tag: String?, tr: Throwable?) {
        tr?.printStackTrace()
        println("$tag $msg")
    }

    override fun e(msg: String, tag: String?, tr: Throwable?) {
        tr?.printStackTrace()
        System.err.println("$tag $msg")
    }
}

object LoggerProxy {

    private lateinit var mLogger: Logger

    fun initLogger(logger: Logger) {
        mLogger = logger
    }

    fun getLogger(): Logger {
        return if (this::mLogger.isInitialized) {
            mLogger
        } else {
            DefaultLogger
        }
    }
}

fun String.logI(tag:String = "imageloader") = LoggerProxy.getLogger().i(this, tag)

fun String.logE(tag:String = "imageloader") = LoggerProxy.getLogger().e(this, tag = tag, tr = null)
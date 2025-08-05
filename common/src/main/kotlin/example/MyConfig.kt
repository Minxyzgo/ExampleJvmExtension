package example

import io.github.rwpp.config.Config
import kotlinx.serialization.Serializable

// 对于一个Config，所有字段必须保持可变，必须实现[Config]接口，必须是@Serializable注解的data class， 必须有初始字段
@Serializable
data class MyConfig(
    var count: Int = 0,
    var showBriefing: Boolean = true,
) : Config
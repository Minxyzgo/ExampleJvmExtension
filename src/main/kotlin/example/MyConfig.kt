package example

import io.github.rwpp.config.Config
import kotlinx.serialization.Serializable

@Serializable
data class MyConfig(
    val count: Int,
) : Config
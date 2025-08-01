package example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.rwpp.AppContext
import io.github.rwpp.appKoin
import io.github.rwpp.config.Config
import io.github.rwpp.external.ExtensionLauncher
import io.github.rwpp.logger
import io.github.rwpp.ui.ComposeWidget
import io.github.rwpp.widget.RWTextButton

class Main : ExtensionLauncher() {
    // 定义一个MutableState, 它可受到Compose函数的观察, 并在更改时重组UI达到更新画面的效果
    private var count by mutableStateOf(1)
    private lateinit var config: Config
    override fun init() {
        logger.info("Hello World")
        Test.init()
        // 通过定义可序列化的data class来设置配置 (仅kotlin可用)
        config = getConfig(MyConfig::class) ?: MyConfig(1)
        // 也可直接获取单一配置 (java可用)
        // count = getSingleConfig("count")?.toIntOrNull() ?: 1
        extension.settingPanel.add(object : ComposeWidget() {
            @Composable
            override fun Content() {
                // 获取assets内的资源, 实际建议在src/composeResources/drawable下存放需要显示的图片, 具体可参照compose官方文档
                val bytes = openInputStream("icon.png")!!.readBytes()

                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(bytes, modifier = Modifier.padding(5.dp).size(30.dp), contentDescription = "Image")

                    androidx.compose.material3.Text(
                        "This is a Compose Widget Count: $count",
                        modifier = Modifier.padding(5.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.secondary
                    )

                    //添加一个计数器
                    RWTextButton("Add") {
                        count++
                    }
                }
            }
        })

        // koin 是依赖注入框架, 通过get方法获取KoinComponent实例, AppContext是其中一个定义的模块, 可通过此方法获取AppContext实例
        appKoin.get<AppContext>().onExit {
            // 设置单一配置
            // saveSingleConfig("count", count.toString())
            saveConfig(config)
        }
    }
}
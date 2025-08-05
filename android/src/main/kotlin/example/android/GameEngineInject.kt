package example.android

import example.Main
import io.github.rwpp.inject.Inject
import io.github.rwpp.inject.InjectClass
import io.github.rwpp.inject.InjectMode
import io.github.rwpp.inject.InterruptResult

// 类型别名防止名称冲突
typealias GameEngine = com.corrodinggames.rts.gameFramework.k

@InjectClass(GameEngine::class)
@Suppress("unused")
/**
 * [InjectClass] 的示例，访问package-private的class可用[io.github.rwpp.inject.InjectClassByString]
 *
 * 其它的注解: [io.github.rwpp.inject.SetInterfaceOn] 此注解将接口设置在指定类上，可用于添加字段，访问器等
 */
object GameEngineInject {
    @Inject("a", InjectMode.InsertBefore)
    // 任何InsertBefore模式的注入必须返回Any?或者不返回，不返回默认方法会继续执行
    fun showBriefing(str: String, bu: com.corrodinggames.rts.game.units.custom.bu): Any? {
        return if (Main.config.showBriefing && str == "Briefing") {
            InterruptResult() // InterruptResult 表示中断方法执行，并返回指定结果
        } else {
            Unit // Unit 表示方法继续执行
        }
    }
}

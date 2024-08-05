package cn.chahuyun.session.command

import cn.chahuyun.session.HuYanSession
import cn.chahuyun.session.data.cache.CacheFactory
import cn.chahuyun.session.data.entity.ManySession
import cn.chahuyun.session.data.entity.Permission
import cn.chahuyun.session.data.entity.SingleSession
import cn.chahuyun.session.data.entity.TimingSession
import cn.chahuyun.session.data.factory.DataFactory
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand


class SessionCommand : CompositeCommand(
    HuYanSession.INSTANCE, "hys",
    description = "HuYanSession-3 Command"
) {


    @SubCommand("v")
    @Description("查询当前壶言会话3版本")
    suspend fun CommandSender.version() {
        sendMessage("当前壶言会话3版本 ${HuYanSession.VERSION}")
    }

    @SubCommand("owner")
    @Description("设置主人")
    suspend fun CommandSender.owner(owner: Long) {
        HuYanSession.pluginConfig.owner = owner
        sendMessage("已将主人设置为:${owner}")
    }

    @SubCommand("ref")
    @Description("刷新缓存")
    suspend fun CommandSender.refresh() {

        DataFactory.getInstance().dataService.refresh()
        sendMessage("缓存刷新成功！")
    }

}
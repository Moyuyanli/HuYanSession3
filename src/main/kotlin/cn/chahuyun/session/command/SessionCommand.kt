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

    @SubCommand("ref")
    @Description("刷新缓存")
    suspend fun CommandSender.refresh() {

        val dataService = DataFactory.getInstance().dataService

        val singleSessions = dataService.selectListEntity(SingleSession::class.java, "from SingleSession")
        val manySessions = dataService.selectListEntity(ManySession::class.java, "from ManySession")
        val timingSessions = dataService.selectListEntity(TimingSession::class.java, "from TimingSession")
        val permissions = dataService.selectListEntity(Permission::class.java, "from Permission")

        val cacheService = CacheFactory.getInstall().cacheService

        singleSessions.forEach { cacheService.putSession(it) }
        manySessions.forEach { cacheService.putSession(it) }
        timingSessions.forEach { cacheService.putSession(it) }
        permissions.forEach { cacheService.putPermission(it) }

        sendMessage("缓存刷新成功！")
    }

}
package cn.chahuyun.session.send;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.constant.Constant;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 用于本地化缓存的消息
 *
 * @author Moyuyanli
 * @date 2024/2/26 11:26
 */
@Slf4j(topic = Constant.LOG_TOPIC)
public class LocalMessage {

    private final Image reply;
    private final Contact subject;

    public LocalMessage(Image reply, Contact subject) {
        this.reply = reply;
        this.subject = subject;
    }

    public Image replace() {
        if (this.reply == null) {
            log.error("图片消息为空");
            return null;
        }

        String imageId = reply.getImageId();

        Path dataFolderPath = HuYanSession.INSTANCE.getDataFolderPath();

        Path resolve = dataFolderPath.resolve("cache").resolve(imageId);

        if (!Files.exists(resolve)) {
            log.error("缓存图片不存在!");
            return reply;
        }

        return Contact.uploadImage(subject, resolve.toFile());
    }

    /**
     * 进行本地缓存
     *
     * @param messages 消息链
     * @return 缓存结果
     */
    public static boolean localCacheImage(MessageChain messages) {
        checkCacheFile();

        Path dataFolderPath = HuYanSession.INSTANCE.getDataFolderPath();

        Path cache = dataFolderPath.resolve("cache");

        for (SingleMessage message : messages) {
            if (message instanceof Image) {
                Image image = (Image) message;

                Path imageId = cache.resolve(image.getImageId());

                HttpRequest httpRequest = HttpUtil.createGet(Image.queryUrl(image));
                httpRequest.setReadTimeout(10 * 1000);
                httpRequest.setConnectionTimeout(5 * 1000);

                try (HttpResponse httpResponse = httpRequest.execute()) {
                    if (!httpResponse.isOk()) {
                        log.error("本地缓存图片下载图片失败!");
                        return false;
                    }

                    try (InputStream inputStream = httpResponse.bodyStream();

                         OutputStream outputStream = Files.newOutputStream(imageId)) {

                        byte[] buffer = new byte[4096]; // 定义缓冲区大小
                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                    } catch (IOException e) {
                        log.error("保存图片到本地缓存时发生错误:", e);
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private static void checkCacheFile() {
        Path dataFolderPath = HuYanSession.INSTANCE.getDataFolderPath();

        Path cache = dataFolderPath.resolve("cache");

        if (!Files.exists(cache)) {
            FileUtil.mkdir(cache.toFile());
        }
    }

}

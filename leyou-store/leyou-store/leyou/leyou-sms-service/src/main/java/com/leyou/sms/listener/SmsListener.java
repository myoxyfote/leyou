package com.leyou.sms.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.config.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU-SMS-QUEUE", durable = "true"),
            exchange = @Exchange(value = "LEYOU-SMS-EXCHANGE",
                                 ignoreDeclarationExceptions = "true"),
            key = {"sms.verifycode"}))
    public void sendMessage(Map<String, String> msg) throws Exception {
        if (CollectionUtils.isEmpty(msg)) {
            // 放弃处理
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if (StringUtils.isBlank(phone)||StringUtils.isBlank(code)) {
            // 放弃处理
            return;
        }
        // 发送消息
        SendSmsResponse resp = this.smsUtils.sendSms(phone, code,
                                                     prop.getSignName(),
                                                     prop.getVerifyCodeTemplate());

    }
}
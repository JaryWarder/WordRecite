package example.controller;

import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 验证码缓存，key = username
    static final Map<String, VerifyCode> codeCache = new ConcurrentHashMap<>();

    // 60 秒发送冷却，key = username，value = 上次发送时间戳
    private static final Map<String, Long> cooldownCache = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 60 * 1000L;

    static class VerifyCode {
        String code;
        long expireTime;

        VerifyCode(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    @GetMapping("/send_email/{user}")
    public Result<String> sendEmail(@PathVariable String user) {
        log.info("sendEmail(), user: {}", user);
        try {
            // 60 秒冷却检查
            Long lastSent = cooldownCache.get(user);
            if (lastSent != null && System.currentTimeMillis() - lastSent < COOLDOWN_MS) {
                long remaining = (COOLDOWN_MS - (System.currentTimeMillis() - lastSent)) / 1000;
                return Result.error(429, "请求过于频繁，请 " + remaining + " 秒后再试");
            }

            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error("User not found");
            }

            String toEmail = u.getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                return Result.error("User email not set");
            }

            // 生成 6 位随机数字验证码，记录冷却时间
            String code = String.format("%06d", new Random().nextInt(1000000));
            cooldownCache.put(user, System.currentTimeMillis());
            long expireTime = System.currentTimeMillis() + 5 * 60 * 1000L;
            codeCache.put(user, new VerifyCode(code, expireTime));

            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("WordRecite 密码重置验证码");
                message.setText("您的密码重置验证码为：" + code + "\n\n该验证码 5 分钟内有效，请勿泄露给他人。");
                mailSender.send(message);
                log.info("Verification code sent to {}", toEmail);
            } else {
                log.warn("JavaMailSender not configured, code={}", code);
            }

            return Result.success("验证码已发送");
        } catch (RuntimeException e) {
            log.error("sendEmail error", e);
            return Result.error("Failed to send email");
        }
    }
}

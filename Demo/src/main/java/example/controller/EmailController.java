package example.controller;

import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false) // 如果未配置邮箱，不强制要求注入
    private JavaMailSender mailSender;

    @GetMapping("/send_email/{user}")
    public Result<String> sendEmail(@PathVariable String user) {
        log.info("sendEmail(), user: {}", user);
        try {
            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error("User not found");
            }

            String toEmail = u.getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                return Result.error("User email not set");
            }

            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("your_email@example.com"); // 需在 yml 中配置真实发件人
                message.setTo(toEmail);
                message.setSubject("Password Reset");
                message.setText("Your password has been reset to: 123456");
                mailSender.send(message);
                log.info("Email sent successfully to {}", toEmail);
            } else {
                log.warn("JavaMailSender is not configured, skipping email send");
            }

            // 重置密码为 123456
            u.setPsw("123456");
            userMapper.updateById(u);

            return Result.success("success");
        } catch (RuntimeException e) {
            log.error("sendEmail error", e);
            return Result.error("Failed to send email");
        }
    }
}

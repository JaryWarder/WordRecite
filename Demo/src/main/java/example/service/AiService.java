package example.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import example.common.Result;
import example.dto.ProgressDashboardDto;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Autowired
    private ProgressAnalyticsService progressAnalyticsService;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public Result<String> analyze(String username, String origin) {
        // 1. 获取学习大盘数据
        Result<ProgressDashboardDto> dashResult = progressAnalyticsService.dashboard(username, origin, 30);
        if (dashResult.getCode() != 200 || dashResult.getData() == null) {
            return Result.error(dashResult.getCode(), dashResult.getMsg());
        }
        ProgressDashboardDto data = dashResult.getData();

        // 2. 组装具备“灵魂”的生动化 Prompt
        int bookSize = data.getBookSize();
        int learned = data.getLearnedUnique();
        int mastered = data.getMasteredCount();
        int due = data.getDueCount();
        double progress = bookSize > 0 ? (learned * 100.0 / bookSize) : 0;

        String prompt = String.format(
                "你是一个热血的英语学习教官！🥊 请根据以下战报数据，为用户生成一份极具活力的学情诊断报告。\n\n" +
                        "### 实时战报数据：\n" +
                        "- 目标词书：[%s]\n" +
                        "- 攻克进度：已学 %d 词 / 共 %d 词 (完成度 %.1f%%)\n" +
                        "- 核心战斗力：已精通 %d 词\n" +
                        "- 预警：有 %d 个单词正遭到“遗忘曲线”的伏击！\n\n" +
                        "### 任务要求：\n" +
                        "1. 请直接开始你的表演，禁止输出任何前置客套话或标题。\n" +
                        "2. 必须使用 Markdown 格式，多用加粗和分段，多穿插 Emoji，让视觉层次感拉满。\n" +
                        "3. 内容必须包含以下三个生动模块：\n" +
                        "   📊 【战况诊断】：一句话犀利总结进度。\n" +
                        "   💡 【通关秘籍】：针对复习压力给出具体的战术建议。\n" +
                        "   🔥 【战斗宣言】：极具鼓动性的热血口号。",
                data.getOrigin(), learned, bookSize, progress, mastered, due);

        // 3. 构造 OpenAI 格式请求体
        JSONObject body = new JSONObject();
        body.put("model", "deepseek-chat");
        JSONArray messages = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("role", "user");
        msg.put("content", prompt);
        messages.add(msg);
        body.put("messages", messages);

        RequestBody requestBody = RequestBody.create(
                body.toJSONString(),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 4. 发送请求并解析响应
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                // 重点：如果请求失败，打印出 DeepSeek 的具体错误详情（如欠费、Key错误等）
                log.error("AI API 访问失败 | 状态码: {} | 错误详情: {}", response.code(), responseBody);
                return Result.error(502, "AI教官掉线了(错误码:" + response.code() + ")");
            }

            JSONObject json = JSON.parseObject(responseBody);
            String text = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            return Result.success(text);
        } catch (Exception e) {
            log.error("AI 分析链路发生严重异常", e);
            return Result.error(500, "AI 分析失败：" + e.getMessage());
        }
    }
}
package example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import example.common.Result;
import example.dao.DailyMapper;
import example.dao.UserMapper;
import example.dao.WordEntryMapper;
import example.pojo.Daily;
import example.pojo.User;
import example.pojo.WordEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/memorize")
public class MemorizeController {
    private static final Logger log = LoggerFactory.getLogger(MemorizeController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordEntryMapper wordEntryMapper;

    @Autowired
    private DailyMapper dailyMapper;

    @GetMapping("/get_server_date")
    public Result<Map<String, String>> getServerDate() {
        Map<String, String> result = new HashMap<>();
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar now = Calendar.getInstance(timeZone);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE);
        sdf.setTimeZone(timeZone);

        result.put("date", sdf.format(now.getTime()));
        result.put("timestamp", String.valueOf(now.getTimeInMillis()));
        return Result.success(result);
    }

    @GetMapping("/memorize")
    public Result<Map<String, String>> memorizeWord(@RequestParam String obj) {
        try {
            JSONObject json = JSON.parseObject(obj);
            String user = json.getString("user");
            String studying = json.getString("studying");
            int num = json.getIntValue("num");
            int indexOfTotal = json.getIntValue("indexOfTotal");

            log.info("------------- 背单词请求: {} -------------", user);

            User u = userMapper.selectById(user);
            if (u == null)
                return Result.error("User not found");

            int plan = u.getPlan();
            int studied = u.getStudied();
            int totalNum = (studied + plan > num) ? (num - studied) : plan;

            Map<String, String> result = new HashMap<>();
            if (indexOfTotal == totalNum + 1) {
                result.put("info", "end");
                return Result.success(result);
            }

            int targetIndex = studied + indexOfTotal;

            // 直接查 MySQL (无 Redis)
            WordEntry entry = wordEntryMapper.selectByIdAndTable(targetIndex, studying);
            if (entry == null) {
                return Result.error("Word not found");
            }

            result.put("totalNum", String.valueOf(totalNum));
            result.put("studying", studying);
            result.put("word", entry.getWord());
            result.put("phonetic", entry.getPhonetic());
            result.put("poses", entry.getPoses());
            result.put("id", String.valueOf(entry.getId()));
            result.put("info", "success");

            return Result.success(result);

        } catch (Exception e) {
            log.error("memorizeWord error", e);
            return Result.error("Failed to fetch word");
        }
    }

    @GetMapping("/submit_list")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> submitList(@RequestParam String obj) {
        try {
            JSONObject json = JSON.parseObject(obj);
            String user = json.getString("user"); // 假设前端传回了user
            int totalNum = json.getIntValue("totalNum");
            JSONArray yes = json.getJSONArray("yes");
            JSONArray no = json.getJSONArray("no");

            List<Daily> yesList = new ArrayList<>();
            List<Daily> noList = new ArrayList<>();
            drawList(yes, yesList, user);
            drawList(no, noList, user);

            TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
            Calendar now = Calendar.getInstance(timeZone);
            String date = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-"
                    + now.get(Calendar.DAY_OF_MONTH);

            User u = userMapper.selectById(user);
            if (u == null)
                return Result.error("User not found");

            userMapper.updateStudied(user, totalNum);
            String lastTime = u.getMyDate();

            if (lastTime == null || !date.equals(lastTime)) {
                dailyMapper.deleteByUsername(user);
            }

            // 批量插入 (这里简单遍历插入，实际可优化为 MyBatis-Plus 的 IService.saveBatch)
            for (Daily d : yesList)
                dailyMapper.insert(d);
            for (Daily d : noList)
                dailyMapper.insert(d);

            if (lastTime != null) {
                String[] date1 = lastTime.split("-");
                String[] date2 = date.split("-");

                if (!date1[0].equals(date2[0])) {
                    userMapper.clearDays(user);
                } else if (!date1[1].equals(date2[1])) {
                    // 跨月逻辑简化处理 (后续完善图表统计)
                    userMapper.clearDays(user);
                } else {
                    int d1 = Integer.parseInt(date1[2]);
                    int d2 = Integer.parseInt(date2[2]);
                    if (d2 - d1 >= 7) {
                        userMapper.clearDays(user);
                    } else if (d1 != d2) {
                        // TODO: LeftShift
                        userMapper.updateDay7(user, totalNum);
                    } else {
                        userMapper.updateDay7(user, totalNum);
                    }
                }
            } else {
                userMapper.updateDay7(user, totalNum);
            }

            userMapper.updateDate(user, date);
            return Result.success("success");

        } catch (RuntimeException e) {
            log.error("submitList error", e);
            return Result.error("Failed to submit list");
        }
    }

    private void drawList(JSONArray arr, List<Daily> list, String fallbackUser) {
        if (arr == null)
            return;
        for (int i = 0; i < arr.size(); i++) {
            JSONObject entry = arr.getJSONObject(i);
            Daily d = new Daily();
            d.setUsername(entry.getString("username") != null ? entry.getString("username") : fallbackUser);
            d.setStatus(entry.getString("status"));
            d.setWord(entry.getString("word"));
            d.setId(entry.getIntValue("id"));
            list.add(d);
        }
    }
}

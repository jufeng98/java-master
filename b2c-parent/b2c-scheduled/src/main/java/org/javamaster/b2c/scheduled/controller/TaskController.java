package org.javamaster.b2c.scheduled.controller;

import org.javamaster.b2c.scheduled.consts.AppConsts;
import org.javamaster.b2c.scheduled.respsitory.SpringScheduledCronRepository;
import org.javamaster.b2c.scheduled.task.ScheduledOfTask;
import org.javamaster.b2c.scheduled.util.CronUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 管理定时任务(需要做权限控制)
 *
 * @author yudong
 * @date 2019/5/10
 */
@Controller
@RequestMapping("/scheduled/task")
public class TaskController {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private SpringScheduledCronRepository cronRepository;

    /**
     * 查看任务列表
     *
     * @param model
     * @return
     */
    @RequestMapping("/taskList")
    public String taskList(Model model) {
        model.addAttribute("cronList", cronRepository.findAll());
        return "task-list";
    }

    /**
     * 编辑任务cron表达式
     *
     * @param cronKey
     * @param newCron
     * @return
     */
    @ResponseBody
    @RequestMapping("/editTaskCron")
    public Integer editTaskCron(String cronKey, String newCron) {
        if (!CronUtils.isValidExpression(newCron)) {
            throw new IllegalArgumentException("失败,非法表达式:" + newCron);
        }
        cronRepository.updateCronExpressionByCronKey(newCron, cronKey);
        return AppConsts.SUCCESS;
    }

    /**
     * 执行定时任务
     *
     * @param cronKey
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/runTaskCron")
    public Integer runTaskCron(String cronKey) throws Exception {
        ((ScheduledOfTask) context.getBean(Class.forName(cronKey))).execute();
        return AppConsts.SUCCESS;
    }

    /**
     * 启用或禁用定时任务
     *
     * @param status
     * @param cronKey
     * @return
     */
    @ResponseBody
    @RequestMapping("/changeStatusTaskCron")
    public Integer changeStatusTaskCron(Integer status, String cronKey) {
        cronRepository.updateStatusByCronKey(status, cronKey);
        return AppConsts.SUCCESS;

    }

}

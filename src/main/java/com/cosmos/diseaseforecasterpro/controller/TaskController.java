package com.cosmos.diseaseforecasterpro.controller;


import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Result;
import com.cosmos.diseaseforecasterpro.pojo.Task;
import com.cosmos.diseaseforecasterpro.service.ITaskService;
import com.cosmos.diseaseforecasterpro.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ITaskService taskService;
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("addTask")
    public Result addTask(String taskName, String description, HttpServletRequest request) {
        if (StringUtils.isBlank(taskName))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        long userId = userService.getUserId(request);

        Boolean res = taskService.addTask(taskName, description, userId);
        if (!res)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加任务失败");
        return Result.success("任务添加成功");
    }

    @PostMapping("deleteTask")
    public Result deleteTask(@RequestParam("taskId") int taskId) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务ID不合法");
        }


        Boolean res = taskService.deleteTask(taskId);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除任务失败");
        }
        return Result.success("任务删除成功");
    }


    @GetMapping("getTasks")
    public Result getTasks(HttpServletRequest request) {
        long userId = userService.getUserId(request);
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        List<Task> tasks = taskService.getTasks(userId);
        if (tasks == null || tasks.isEmpty()) {
            return Result.success("暂无任务", new ArrayList<>());
        }
        return Result.success("获取任务成功", tasks);
    }


    @GetMapping("getTasksByDate")
    public Result getTasksByDate(
            @RequestParam("date") LocalDate date,
            HttpServletRequest request) {
        if (date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期参数不能为空");
        }
        long userId = userService.getUserId(request);
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        List<Task> tasks = taskService.getTasksByDate(date, userId);
        if (tasks == null || tasks.isEmpty()) {
            return Result.success("暂无任务", new ArrayList<>());
        }
        return Result.success("按日期获取任务成功", tasks);
    }


    @PostMapping("updateTask")
    public Result updateTask(int id, @RequestParam(required = false) String taskName, @RequestParam(required = false) String description, @RequestParam(required = false) int isComplete) {

        Boolean result = taskService.updateTask(id, taskName, description, isComplete);
        if (!result)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新任务失败");
        return Result.success("任务更新成功");
    }


    @PostMapping("addBatchTasks")
    public Result addBatchTasks(@RequestBody Map<String, String> map, HttpServletRequest request) {
        long userId = userService.getUserId(request);
        if (map == null || map.isEmpty())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务列表为空");

        boolean res = taskService.addBatchTasks(map, request);
        if (!res)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "批量添加任务失败");

        return Result.success("批量添加任务成功");

    }


}

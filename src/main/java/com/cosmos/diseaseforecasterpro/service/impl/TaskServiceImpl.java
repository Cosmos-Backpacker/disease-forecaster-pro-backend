package com.cosmos.diseaseforecasterpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cosmos.diseaseforecasterpro.common.ErrorCode;
import com.cosmos.diseaseforecasterpro.exception.BusinessException;
import com.cosmos.diseaseforecasterpro.pojo.Task;
import com.cosmos.diseaseforecasterpro.mapper.TaskMapper;
import com.cosmos.diseaseforecasterpro.service.ITaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import kotlin.jvm.internal.Lambda;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CosmosBackpacker
 * @since 2025-03-20
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements ITaskService {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private UserServiceImpl userService;

    public Boolean addTask(String taskName, @RequestParam(value = "description", required = false) String description, long userId) {

        Task task = new Task();
        task.setTaskName(taskName);
        task.setUserId(userId);
        task.setDescription(description);
        task.setCreateTime(LocalDate.now());
        task.setUpdateTime(LocalDate.now());
        int res = taskMapper.insert(task);
        return res != 0;
    }


    public Boolean deleteTask(int taskId) {

        Task task = taskMapper.selectById(taskId);
        if (task == null)
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        int res = taskMapper.deleteById(taskId);
        return res != 0;
    }


    public Boolean updateTask(int taskId, @RequestParam(required = false) String taskName, @RequestParam(required = false) String description, @RequestParam(required = false) int isComplete) {

        Task oldTask = taskMapper.selectById(taskId);
        if (oldTask == null)
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");

        Task task = new Task();
        task.setTaskName(taskName);
        task.setId(oldTask.getId());
        task.setDescription(description);
        task.setUpdateTime(LocalDate.now());
        task.setIsComplete(isComplete);
        int res = taskMapper.updateById(task);
        return res != 0;
    }

    @Override
    public Boolean addBatchTasks(Map<String, String> map, HttpServletRequest request) {

        long userId = userService.getUserId(request);
        if (map == null || map.isEmpty())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务列表为空");

        List<Task> tasks = map.entrySet().stream()
                .map(entry -> new Task()
                        .setUserId(userId)
                        .setTaskName(entry.getKey())
                        .setDescription(entry.getValue())
                        .setCreateTime(LocalDate.now())
                        .setUpdateTime(LocalDate.now()))
                .toList();


        boolean res = this.saveBatch(tasks);
        if (!res)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "批量添加任务失败");


        return true;
    }


    /**
     * 根据用户id获取任务列表
     *
     * @return 任务列表
     */
    public List<Task> getTasks(long userId) {

        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getUserId, userId);

        return taskMapper.selectList(queryWrapper);

    }


    /**
     * 根据日期获取任务列表
     *
     * @return 任务列表
     */
    public List<Task> getTasksByDate(LocalDate date, long userId) {
        // 创建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getCreateTime, date);
        queryWrapper.eq(Task::getUserId, userId);

        // 执行查询
        return taskMapper.selectList(queryWrapper);
    }


}

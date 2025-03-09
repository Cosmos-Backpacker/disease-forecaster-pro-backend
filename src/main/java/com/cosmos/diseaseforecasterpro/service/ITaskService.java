package com.cosmos.diseaseforecasterpro.service;

import com.cosmos.diseaseforecasterpro.pojo.Task;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public interface ITaskService extends IService<Task> {

    Boolean addTask(String taskName, @RequestParam(value = "description", required = false) String description, long userId);

    Boolean deleteTask(int taskId);

    List<Task> getTasks(long userId);

    List<Task> getTasksByDate(LocalDate date, long userId);

    Boolean updateTask(int taskId, @RequestParam(required = false) String taskName, @RequestParam(required = false) String description, @RequestParam(required = false) int isComplete);

    /**
     * 批量添加任务
     *
     * @param map 任务名和任务描述的映射关系
     * @return
     */
    Boolean addBatchTasks(Map<String, String> map, HttpServletRequest request);


}

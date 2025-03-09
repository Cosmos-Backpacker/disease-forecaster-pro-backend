package com.cosmos.diseaseforecasterpro.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class ChatbotServiceStarter {

    private Process process;

    @PostConstruct
    public void startChatbotService() {
        try {
            // 构造完整的命令，先切换目录再执行 uvicorn，并指定端口号为 8005
            String command = "cmd /c cd /d D:\\PythonProject\\kbmedical\\KBMEDICAL && uvicorn chatbot_service:app --reload --port 8005";

            // 使用 Runtime.exec()
            process = Runtime.getRuntime().exec(command);

            // 打印输出流并检测服务是否启动
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    boolean isStarted = false;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (line.contains("Uvicorn running on")) { // 检测启动日志
                            isStarted = true;
                            System.out.println("Uvicorn 聊天机器人服务已成功启动");
                            break;
                        }
                    }
                    if (!isStarted) {
                        System.err.println("Uvicorn 聊天机器人服务启动失败");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("正在启动 Uvicorn 聊天机器人服务...,请稍等");
        } catch (Exception e) {
            System.err.println("启动 Uvicorn 聊天机器人服务失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopChatbotService() {
        try {
            // 查找包含 "python" 或 "uvicorn" 的进程
            Process findProcess = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq python.exe\" /FO CSV /NH");
            BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("python.exe")) {
                    // 提取PID
                    String pid = line.split(",")[1].trim().replace("\"", "");
                    // 使用 taskkill 命令关闭进程
                    Process killProcess = Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");
                    killProcess.waitFor();
                    System.out.println("Uvicorn 聊天机器人服务已关闭，PID: " + pid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

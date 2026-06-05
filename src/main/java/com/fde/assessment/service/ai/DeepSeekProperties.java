package com.fde.assessment.service.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek API 配置属性
 */
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /** DeepSeek API 密钥 */
    private String apiKey;

    /** API 请求地址 */
    private String apiUrl = "https://api.deepseek.com/v1/chat/completions";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 连接超时（秒） */
    private int connectTimeout = 30;

    /** 读取超时（秒） */
    private int readTimeout = 60;

    /** 生成温度 */
    private double temperature = 0.7;

    /** 最大生成 Token 数 */
    private int maxTokens = 2000;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }

    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
}

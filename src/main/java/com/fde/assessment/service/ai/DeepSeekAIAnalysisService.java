package com.fde.assessment.service.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.exception.ErrorCode;
import com.fde.assessment.model.dto.AIAnalysisResult;
import com.fde.assessment.model.dto.AIAnalysisResult.QuestionSuggestion;
import com.fde.assessment.model.dto.AIAnalysisResult.RiskItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于 DeepSeek 官方 API 的真实 AI 分析服务
 * <p>
 * 使用 @Profile("llm") 激活，需设置 DEEPSEEK_API_KEY 环境变量。
 * 可通过 --spring.profiles.active=llm 参数或 SPRING_PROFILES_ACTIVE=llm 环境变量切换。
 * </p>
 */
@Service
@Profile("llm")
public class DeepSeekAIAnalysisService implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekAIAnalysisService.class);
    private static final String SYSTEM_PROMPT = """
            你是一个专业的软件需求分析AI助手。请对下面的研发工作项进行结构化分析，并以严格的 JSON 格式返回结果。

            必须严格按以下 JSON Schema 返回（不要包含 markdown 代码块标记，仅返回纯 JSON）：

            {
              "summary": "对工作项的整体分析摘要（200字以内）",
              "risks": [
                {
                  "type": "风险类型名称",
                  "description": "风险详细描述",
                  "severity": "HIGH" | "MEDIUM" | "LOW"
                }
              ],
              "acceptanceCriteria": ["验收标准1", "验收标准2", "验收标准3"],
              "clarificationQuestions": [
                {
                  "question": "具体的澄清问题",
                  "severity": "HIGH" | "MEDIUM" | "LOW"
                }
              ],
              "taskSuggestions": ["任务拆解建议1", "任务拆解建议2", "任务拆解建议3"]
            }

            要求：
            1. summary（字符串）：200字以内的分析摘要，涵盖工作项目标、关键点和建议。
            2. risks（数组）：2-4个风险点，分析潜在的技术、业务或流程风险。
            3. acceptanceCriteria（数组）：2-5个验收标准，可量化、可验证。
            4. clarificationQuestions（数组）：2-4个澄清问题，帮助消除需求不确定性。
            5. taskSuggestions（数组）：3-5个具体的任务拆解建议，可落地执行。

            重要：仅返回有效 JSON，不要包含任何额外说明文字。""";

    private final RestTemplate restTemplate;
    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public DeepSeekAIAnalysisService(DeepSeekProperties properties,
                                     RestTemplateBuilder restTemplateBuilder,
                                     ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
                .setReadTimeout(Duration.ofSeconds(properties.getReadTimeout()))
                .build();
    }

    @Override
    public AIAnalysisResult analyze(String title, String description) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isEmpty() || "sk-placeholder".equals(apiKey)) {
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(),
                    "DeepSeek API 密钥未配置。请设置 DEEPSEEK_API_KEY 环境变量或在 application.yml 中配置 deepseek.api-key");
        }

        String userMessage = "标题：" + title + "\n描述：" + (description != null ? description : "");

        try {
            // Build request body
            DeepSeekRequest request = new DeepSeekRequest();
            request.setModel(properties.getModel());
            request.setTemperature(properties.getTemperature());
            request.setMaxTokens(properties.getMaxTokens());
            request.setResponseFormat(Map.of("type", "json_object"));
            request.setMessages(List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userMessage)
            ));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);

            log.info("Calling DeepSeek API: model={}, title={}", properties.getModel(), title);
            ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(
                    properties.getApiUrl(), entity, DeepSeekResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("DeepSeek API returned non-2xx status: {}", response.getStatusCode());
                throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(),
                        "AI 分析服务暂时不可用，请稍后重试");
            }

            // Extract AI response content
            DeepSeekResponse body = response.getBody();
            if (body.getChoices() == null || body.getChoices().isEmpty()
                    || body.getChoices().get(0).getMessage() == null
                    || body.getChoices().get(0).getMessage().getContent() == null) {
                log.error("DeepSeek API response missing expected fields: {}", body);
                throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(),
                        "AI 分析返回了意外的结果格式");
            }

            String content = body.getChoices().get(0).getMessage().getContent();
            log.debug("DeepSeek API raw response content: {}", content);

            // Parse the structured JSON from content
            AIAnalysisResult result = parseResult(content);

            // Log token usage if available
            if (body.getUsage() != null) {
                log.info("DeepSeek API token usage: prompt={}, completion={}, total={}",
                        body.getUsage().getPromptTokens(),
                        body.getUsage().getCompletionTokens(),
                        body.getUsage().getTotalTokens());
            }

            return result;

        } catch (RestClientException e) {
            log.error("DeepSeek API call failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(),
                    "AI 分析服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 解析 DeepSeek 返回的 JSON 内容为 AIAnalysisResult
     */
    private AIAnalysisResult parseResult(String content) {
        // Sanitize: strip markdown code block fences if present
        String sanitized = content.trim();
        if (sanitized.startsWith("```")) {
            int start = sanitized.indexOf('\n');
            if (start != -1) {
                sanitized = sanitized.substring(start + 1);
            }
            int end = sanitized.lastIndexOf("```");
            if (end != -1) {
                sanitized = sanitized.substring(0, end);
            }
            sanitized = sanitized.trim();
        }

        try {
            return objectMapper.readValue(sanitized, AIAnalysisResult.class);
        } catch (Exception e) {
            log.error("Failed to parse DeepSeek response as AIAnalysisResult: {}", sanitized, e);
            // Return a fallback result with partial data
            AIAnalysisResult fallback = new AIAnalysisResult();
            fallback.setSummary("AI 分析结果解析失败，请稍后重试");
            fallback.setRisks(Collections.singletonList(
                    new RiskItem("解析错误", "AI 服务返回的数据格式异常", "HIGH")));
            fallback.setAcceptanceCriteria(List.of(
                    "确保工作项描述完整清晰",
                    "确认需求文档已更新至最新版本"));
            fallback.setClarificationQuestions(Collections.singletonList(
                    new QuestionSuggestion("AI 分析未能生成结构化结果，请检查工作项描述是否完整", "HIGH")));
            fallback.setTaskSuggestions(List.of(
                    "检查工作项描述是否包含足够信息",
                    "重新触发 AI 分析",
                    "如问题持续，请联系管理员"));
            return fallback;
        }
    }

    // ---- DeepSeek API Request/Response DTOs ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeepSeekRequest {
        private String model;
        private List<Map<String, String>> messages;
        @JsonProperty("response_format")
        private Map<String, String> responseFormat;
        private double temperature;
        @JsonProperty("max_tokens")
        private int maxTokens;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<Map<String, String>> getMessages() { return messages; }
        public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
        public Map<String, String> getResponseFormat() { return responseFormat; }
        public void setResponseFormat(Map<String, String> responseFormat) { this.responseFormat = responseFormat; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeepSeekResponse {
        private String id;
        private List<Choice> choices;
        private Usage usage;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private int index;
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Message {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;

        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }
}

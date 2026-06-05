package com.fde.assessment.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.model.dto.AIAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepSeekAIAnalysisServiceTest {

    @Mock
    private DeepSeekProperties properties;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private DeepSeekAIAnalysisService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // ── constructor-required stubs (needed by every test) ──
        when(properties.getApiKey()).thenReturn("sk-test-key");
        when(properties.getConnectTimeout()).thenReturn(30);
        when(properties.getReadTimeout()).thenReturn(60);
        when(restTemplateBuilder.setConnectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        // ── api-call stubs (only consumed by tests that reach the API) ──
        lenient().when(properties.getModel()).thenReturn("deepseek-chat");
        lenient().when(properties.getTemperature()).thenReturn(0.7);
        lenient().when(properties.getMaxTokens()).thenReturn(2000);
        lenient().when(properties.getApiUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");

        service = new DeepSeekAIAnalysisService(properties, restTemplateBuilder, objectMapper);
    }

    @Test
    @DisplayName("正常 API 调用应返回结构化分析结果")
    void shouldReturnStructuredResultOnSuccess() {
        String jsonResponse = """
                {
                  "summary": "工作项「状态流转」旨在建立可追踪的状态机制，确保各阶段合法流转。",
                  "risks": [
                    {"type": "需求边界不清", "description": "缺少对异常流程的定义", "severity": "HIGH"},
                    {"type": "验收标准缺失", "description": "未定义完成判定条件", "severity": "MEDIUM"}
                  ],
                  "acceptanceCriteria": [
                    "所有合法状态流转均可正常执行",
                    "非法状态流转返回明确的错误提示"
                  ],
                  "clarificationQuestions": [
                    {"question": "是否所有的异常路径都已定义？", "severity": "MEDIUM"},
                    {"question": "对下游模块有何影响？", "severity": "LOW"}
                  ],
                  "taskSuggestions": [
                    "定义状态枚举及合法流转规则",
                    "实现状态机引擎核心逻辑",
                    "编写单元测试"
                  ]
                }
                """;

        DeepSeekAIAnalysisService.DeepSeekResponse apiResponse = createApiResponse(jsonResponse);
        ResponseEntity<DeepSeekAIAnalysisService.DeepSeekResponse> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(DeepSeekAIAnalysisService.DeepSeekResponse.class)
        )).thenReturn(responseEntity);

        AIAnalysisResult result = service.analyze("状态流转模块", "实现状态流转机制");

        assertNotNull(result);
        assertNotNull(result.getSummary());
        assertFalse(result.getSummary().isEmpty());
        assertNotNull(result.getRisks());
        assertEquals(2, result.getRisks().size());
        assertEquals("需求边界不清", result.getRisks().get(0).getType());
        assertEquals("HIGH", result.getRisks().get(0).getSeverity());
        assertNotNull(result.getAcceptanceCriteria());
        assertEquals(2, result.getAcceptanceCriteria().size());
        assertNotNull(result.getClarificationQuestions());
        assertEquals(2, result.getClarificationQuestions().size());
        assertEquals("MEDIUM", result.getClarificationQuestions().get(0).getSeverity());
        assertNotNull(result.getTaskSuggestions());
        assertEquals(3, result.getTaskSuggestions().size());
    }

    @Test
    @DisplayName("API 返回带 markdown 代码块的 JSON 时应正确解析")
    void shouldHandleMarkdownCodeBlocks() {
        String jsonWithMarkdown = """
                ```json
                {
                  "summary": "分析摘要",
                  "risks": [
                    {"type": "测试风险", "description": "风险描述", "severity": "HIGH"}
                  ],
                  "acceptanceCriteria": ["标准1"],
                  "clarificationQuestions": [
                    {"question": "测试问题？", "severity": "LOW"}
                  ],
                  "taskSuggestions": ["任务1"]
                }
                ```
                """;

        DeepSeekAIAnalysisService.DeepSeekResponse apiResponse = createApiResponse(jsonWithMarkdown);
        ResponseEntity<DeepSeekAIAnalysisService.DeepSeekResponse> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(DeepSeekAIAnalysisService.DeepSeekResponse.class)
        )).thenReturn(responseEntity);

        AIAnalysisResult result = service.analyze("测试", "测试 markdown 包裹");

        assertNotNull(result);
        assertEquals("分析摘要", result.getSummary());
        assertEquals(1, result.getRisks().size());
        assertEquals("测试风险", result.getRisks().get(0).getType());
    }

    @Test
    @DisplayName("API 密钥未配置时应抛出 BusinessException")
    void shouldThrowExceptionWhenApiKeyNotConfigured() {
        when(properties.getApiKey()).thenReturn("sk-placeholder");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze("测试", "测试"));
        assertTrue(ex.getMessage().contains("API 密钥未配置"));
    }

    @Test
    @DisplayName("API 返回空 choices 时应抛出 BusinessException")
    void shouldThrowExceptionWhenChoicesEmpty() {
        DeepSeekAIAnalysisService.DeepSeekResponse apiResponse = new DeepSeekAIAnalysisService.DeepSeekResponse();
        apiResponse.setChoices(List.of());
        ResponseEntity<DeepSeekAIAnalysisService.DeepSeekResponse> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        mockRestTemplateCall(responseEntity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze("测试", "测试"));
        assertTrue(ex.getMessage().contains("意外的结果格式"));
    }

    @Test
    @DisplayName("API 调用超时应抛出 BusinessException")
    void shouldThrowExceptionOnNetworkFailure() {
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(DeepSeekAIAnalysisService.DeepSeekResponse.class)
        )).thenThrow(new RestClientException("Connection timed out"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze("测试", "测试"));
        assertTrue(ex.getMessage().contains("服务调用失败"));
    }

    @Test
    @DisplayName("API 返回无法解析的 JSON 时应返回 fallback 结果而非抛异常")
    void shouldReturnFallbackOnParseFailure() {
        String invalidJson = "这不是 JSON 格式的内容";
        DeepSeekAIAnalysisService.DeepSeekResponse apiResponse = createApiResponse(invalidJson);
        ResponseEntity<DeepSeekAIAnalysisService.DeepSeekResponse> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        mockRestTemplateCall(responseEntity);

        AIAnalysisResult result = service.analyze("测试", "测试");

        assertNotNull(result);
        assertNotNull(result.getSummary());
        assertTrue(result.getSummary().contains("解析失败"));
        assertEquals(1, result.getRisks().size());
        assertEquals("解析错误", result.getRisks().get(0).getType());
        assertEquals(1, result.getClarificationQuestions().size());
        assertEquals("HIGH", result.getClarificationQuestions().get(0).getSeverity());
    }

    // ---- helpers ----

    private void mockRestTemplateCall(ResponseEntity<DeepSeekAIAnalysisService.DeepSeekResponse> responseEntity) {
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(DeepSeekAIAnalysisService.DeepSeekResponse.class)
        )).thenReturn(responseEntity);
    }

    private DeepSeekAIAnalysisService.DeepSeekResponse createApiResponse(String content) {
        DeepSeekAIAnalysisService.Message message = new DeepSeekAIAnalysisService.Message();
        message.setRole("assistant");
        message.setContent(content);

        DeepSeekAIAnalysisService.Choice choice = new DeepSeekAIAnalysisService.Choice();
        choice.setIndex(0);
        choice.setMessage(message);
        choice.setFinishReason("stop");

        DeepSeekAIAnalysisService.Usage usage = new DeepSeekAIAnalysisService.Usage();
        usage.setPromptTokens(100);
        usage.setCompletionTokens(50);
        usage.setTotalTokens(150);

        DeepSeekAIAnalysisService.DeepSeekResponse response = new DeepSeekAIAnalysisService.DeepSeekResponse();
        response.setId("test-id");
        response.setChoices(List.of(choice));
        response.setUsage(usage);

        return response;
    }
}

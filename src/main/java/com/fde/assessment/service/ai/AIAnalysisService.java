package com.fde.assessment.service.ai;

import com.fde.assessment.model.dto.AIAnalysisResult;

/**
 * AI 分析服务接口 — 预留 LLM 扩展点
 */
public interface AIAnalysisService {

    /**
     * 对工作项进行 AI 辅助分析
     * @param title 工作项标题
     * @param description 工作项描述
     * @return 结构化分析结果
     */
    AIAnalysisResult analyze(String title, String description);
}

package com.commerce.agent.service;

import com.commerce.agent.dto.AgentDtos.FlowLogDto;
import com.commerce.agent.model.AgentFlowLog;
import com.commerce.agent.model.AgentFlowLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowLogService {

    private final AgentFlowLogRepository repo;

    public void save(String sessionId, String requestId,
                     int stepNumber, String stepLabel,
                     String status, String detail, Long durationMs) {
        repo.save(AgentFlowLog.builder()
                .sessionId(sessionId)
                .requestId(requestId)
                .stepNumber(stepNumber)
                .stepLabel(stepLabel)
                .status(status)
                .detail(detail)
                .durationMs(durationMs)
                .build());
    }

    public List<FlowLogDto> getBySession(String sessionId) {
        return repo.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream().map(this::toDto).toList();
    }

    public List<FlowLogDto> getAll() {
        return repo.findTop200ByOrderByCreatedAtDesc()
                .stream().map(this::toDto).toList();
    }

    public void deleteBySession(String sessionId) {
        repo.deleteBySessionId(sessionId);
    }

    public void deleteAll() {
        repo.deleteAll();
    }

    private FlowLogDto toDto(AgentFlowLog l) {
        return new FlowLogDto(
                l.getId(), l.getSessionId(), l.getRequestId(),
                l.getStepNumber(), l.getStepLabel(), l.getStatus(),
                l.getDetail(), l.getDurationMs(), l.getCreatedAt());
    }
}
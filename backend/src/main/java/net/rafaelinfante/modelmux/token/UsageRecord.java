package net.rafaelinfante.modelmux.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import net.rafaelinfante.modelmux.gateway.GatewayResult;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.FinishReason;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderType;

@Entity
@Table(name = "usage_record")
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String correlationId;

    @Column(nullable = false, length = 120)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoutingMode routeMode;

    @Column(nullable = false, length = 64)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProviderType providerType;

    @Column(nullable = false, length = 120)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ModelTier tier;

    @Column(nullable = false)
    private long promptTokens;

    @Column(nullable = false)
    private long completionTokens;

    @Column(nullable = false)
    private long totalTokens;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal costUsd;

    @Column(nullable = false)
    private long latencyMs;

    @Column(nullable = false)
    private boolean cacheHit;

    @Column(nullable = false)
    private boolean failedOver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinishReason finishReason;

    @Column(nullable = false)
    private Instant createdAt;

    protected UsageRecord() {}

    public UsageRecord(GatewayResult result, Instant createdAt) {
        this.correlationId = result.correlationId();
        this.clientId = result.clientId();
        this.routeMode = result.routeMode();
        this.providerId = result.providerId();
        this.providerType = result.providerType();
        this.model = result.model();
        this.tier = result.tier();
        this.promptTokens = result.usage().promptTokens();
        this.completionTokens = result.usage().completionTokens();
        this.totalTokens = result.usage().totalTokens();
        this.costUsd = result.costUsd();
        this.latencyMs = result.latencyMs();
        this.cacheHit = result.cacheHit();
        this.failedOver = result.failedOver();
        this.finishReason = result.finishReason();
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getClientId() {
        return clientId;
    }

    public RoutingMode getRouteMode() {
        return routeMode;
    }

    public String getProviderId() {
        return providerId;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public String getModel() {
        return model;
    }

    public ModelTier getTier() {
        return tier;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public BigDecimal getCostUsd() {
        return costUsd;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public boolean isFailedOver() {
        return failedOver;
    }

    public FinishReason getFinishReason() {
        return finishReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

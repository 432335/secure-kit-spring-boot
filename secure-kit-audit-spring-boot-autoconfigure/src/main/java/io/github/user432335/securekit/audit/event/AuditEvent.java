/*
 * Copyright 2026 the secure-kit-spring-boot authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.user432335.securekit.audit.event;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.function.Function;

/**
 * Immutable audit event.
 *
 * <p>The event does not retain argument or result object references. Argument and result summaries
 * are generated in the constructor with {@link String#valueOf(Object)} and truncated to 512
 * characters. Object fields are not reflected, which avoids lazy-loading side effects, cycles and
 * unexpected {@code toString()} calls later.</p>
 *
 * <p>Values may contain sensitive text. The audit listener applies masking before handing the
 * event to an {@code AuditRepository} when the mask capability is available. This class itself
 * does not guess which text is sensitive.</p>
 */
public final class AuditEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private static final int MAX_SUMMARY_LENGTH = 512;
    private static final String UNPRINTABLE = "<unprintable>";

    private final String operator;
    private final String action;
    private final String targetId;
    private final String methodSignature;
    private final String argsSnapshot;
    private final String resultSummary;
    private final boolean success;
    private final String errorMessage;
    private final Instant timestamp;

    /**
     * Creates an event and immediately snapshots the supplied arguments and result.
     *
     * @param operator        the operator name; may be {@code null}
     * @param action          the action description; may be {@code null}
     * @param targetId        the target identifier; may be {@code null}
     * @param methodSignature the audited method signature; may be {@code null}
     * @param args            the business arguments; may be {@code null}; not retained
     * @param result          the business result; may be {@code null}; not retained
     * @param success         whether the business method completed successfully
     * @param errorMessage    the failure message; may be {@code null}
     * @param timestamp       the event time; {@code null} means "now"
     */
    public AuditEvent(String operator,
                      String action,
                      String targetId,
                      String methodSignature,
                      Object[] args,
                      Object result,
                      boolean success,
                      String errorMessage,
                      Instant timestamp) {
        this(operator,
                action,
                targetId,
                methodSignature,
                summarizeArguments(args),
                summarize(result),
                success,
                truncate(errorMessage),
                timestamp == null ? Instant.now() : timestamp);
    }

    private AuditEvent(String operator,
                       String action,
                       String targetId,
                       String methodSignature,
                       String argsSnapshot,
                       String resultSummary,
                       boolean success,
                       String errorMessage,
                       Instant timestamp) {
        super(AuditEvent.class);
        this.operator = operator;
        this.action = action;
        this.targetId = targetId;
        this.methodSignature = methodSignature;
        this.argsSnapshot = argsSnapshot;
        this.resultSummary = resultSummary;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }

    /**
     * Returns a copy with selected text fields transformed.
     *
     * <p>The listener uses this method to apply masking without mutating the original event. The
     * transform is applied to {@code operator}, {@code targetId}, {@code argsSnapshot} and
     * {@code resultSummary}. The action, method signature, success flag and timestamp are
     * preserved.</p>
     *
     * @param masker the text transform; must not be {@code null}
     * @return a new immutable event
     * @throws NullPointerException when {@code masker} is {@code null}
     */
    public AuditEvent masked(Function<String, String> masker) {
        if (masker == null) {
            throw new NullPointerException("masker must not be null");
        }
        return new AuditEvent(
                apply(masker, operator),
                action,
                apply(masker, targetId),
                methodSignature,
                apply(masker, argsSnapshot),
                apply(masker, resultSummary),
                success,
                errorMessage,
                timestamp);
    }

    /**
     * Returns the operator name.
     *
     * @return the operator name; may be {@code null}
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the action description.
     *
     * @return the action description; may be {@code null}
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the target identifier.
     *
     * @return the target identifier; may be {@code null}
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Returns the audited method signature.
     *
     * @return the method signature; may be {@code null}
     */
    public String getMethodSignature() {
        return methodSignature;
    }

    /**
     * Returns the argument snapshot generated at construction time.
     *
     * @return the argument snapshot; never {@code null}
     */
    public String getArgsSnapshot() {
        return argsSnapshot;
    }

    /**
     * Returns the result summary generated at construction time.
     *
     * @return the result summary; never {@code null}
     */
    public String getResultSummary() {
        return resultSummary;
    }

    /**
     * Returns whether the business method completed successfully.
     *
     * @return {@code true} for a successful invocation
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the truncated failure message.
     *
     * @return the failure message; may be {@code null}
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the event time.
     *
     * <p>This is named differently from {@link ApplicationEvent#getTimestamp()} because that
     * inherited method returns epoch milliseconds.</p>
     *
     * @return the event time; never {@code null}
     */
    public Instant getOccurredAt() {
        return timestamp;
    }

    /**
     * Returns a readable summary of the event.
     *
     * <p>When the listener has applied masking, the stored event's text fields are already masked.
     * Calling this method on a freshly constructed event can still contain the original summaries;
     * apply {@link #masked(Function)} first if the output is going to a less trusted sink.</p>
     *
     * @return a readable summary
     */
    @Override
    public String toString() {
        return "AuditEvent{"
                + "timestamp=" + timestamp
                + ", operator='" + operator + '\''
                + ", action='" + action + '\''
                + ", targetId='" + targetId + '\''
                + ", methodSignature='" + methodSignature + '\''
                + ", success=" + success
                + ", argsSnapshot='" + argsSnapshot + '\''
                + ", resultSummary='" + resultSummary + '\''
                + ", errorMessage='" + errorMessage + '\''
                + '}';
    }

    private static String summarizeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(safeString(args[i]));
        }
        return truncate(builder.toString());
    }

    private static String summarize(Object result) {
        return truncate(safeString(result));
    }

    private static String safeString(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return String.valueOf(value);
        } catch (RuntimeException ignored) {
            return UNPRINTABLE;
        }
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_SUMMARY_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_SUMMARY_LENGTH);
    }

    private static String apply(Function<String, String> masker, String value) {
        if (value == null) {
            return null;
        }
        try {
            return masker.apply(value);
        } catch (RuntimeException ignored) {
            return value;
        }
    }
}

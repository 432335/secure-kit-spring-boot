# secure-kit-spring-boot

[![CI](https://github.com/432335/secure-kit-spring-boot/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/432335/secure-kit-spring-boot/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0-blue.svg)

## 1. 项目简介

一组默认关闭、可独立引入的 Spring Boot 安全能力库：显式脱敏、审计事件、日志出口守卫。

## 2. 30 秒了解

### 脱敏前

```java
String phone = user.getPhone();
return phone;
```

### 脱敏后

```java
String phone = maskTemplate.mask(MaskType.PHONE, user.getPhone());
return phone;
```

### 审计前

```java
public Order queryOrder(String orderId) {
    return orderService.query(orderId);
}
```

### 审计后

```java
@AuditLog(action = "query-order", targetIdExpression = "#p0")
public Order queryOrder(String orderId) {
    return orderService.query(orderId);
}
```

### 日志出口守卫

```text
# 配置前
2026-01-01 10:00:00 INFO  c.e.OrderService - phone=13812345678

# 配置 log-guard 后
2026-01-01 10:00:00 INFO  c.e.OrderService - phone=*
```

## 3. 定位：库不是框架

这是**库**，不是框架。

- 用户主导控制流：不调用就不生效。
- 不接管应用结构：mask 不拦截字段，audit 只提供可选通知点，log-guard 只挂在日志框架扩展点。
- 默认关闭：引入依赖不会自动改变应用行为。
- 不做认证、鉴权、登录、JWT、OAuth2、RBAC、API Key 校验。
- 不实现加密、哈希、签名算法。

和 Spring Security 的关系是**互补，不替代**：

- Spring Security 负责认证、鉴权和安全过滤器链。
- 本项目只提供脱敏、审计事件和日志出口守卫。
- 需要当前登录人时，可以自己实现 `AuditUserProvider`，从 Spring Security 上下文读取。

## 4. 模块表

| 模块 | 作用 | 类型 | 默认开关 | 坐标 |
| --- | --- | --- | --- | --- |
| `secure-kit-core` | 共享 `MaskType` 枚举 | 公共库 | 不适用 | `io.github.432335:secure-kit-core` |
| `secure-kit-mask-spring-boot-starter` | 显式脱敏 | 纯库 | `false` | `io.github.432335:secure-kit-mask-spring-boot-starter` |
| `secure-kit-audit-spring-boot-starter` | 审计事件 | 能力库 + 可选 AOP | `false` | `io.github.432335:secure-kit-audit-spring-boot-starter` |
| `secure-kit-log-guard-spring-boot-starter` | 日志出口脱敏 | 日志扩展 | `false` | `io.github.432335:secure-kit-log-guard-spring-boot-starter` |
| `secure-kit-demo` | 示例与集成测试 | 示例应用 | 不适用 | 不发布 |

### 为什么默认关闭

- 引入依赖不应该改变现有业务行为。
- 安全能力应该由使用者显式开启，而不是自动接管。
- 避免脱敏误伤、日志性能开销和审计队列占用。
- 方便逐步接入：先引入，再按需打开。

## 5. 快速开始

### 前置条件

- JDK 17
- Maven 3.9+
- Spring Boot 3.2.x

### 方式 A：从本地源码安装

```bash
mvn clean install
```

然后在业务项目中使用 `0.1.0` 版本。

### 方式 B：Maven Central 坐标

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-mask-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

如果 Central 上暂时拉不到，先用方式 A 本地安装。

> Maven 坐标使用 `io.github.432335`；Java 包名是 `io.github.user432335.securekit`。两者不要求完全一致。

## 6. 三个能力详情

### 6.1 mask：显式脱敏

#### 引入依赖

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-mask-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### 开启

```yaml
secure-kit:
  mask:
    enabled: true
```

#### API 顺序

`MaskTemplate` 的三个重载顺序是：

```java
mask(MaskType type, String original)
mask(MaskType type, String original, String replacement)
mask(MaskType type, String original, String pattern, String replacement)
```

注意：没有 `mask(type, original, pattern)` 这个重载。自定义正则必须传四个参数。

#### 使用

```java
import io.github.user432335.securekit.core.MaskType;
import io.github.user432335.securekit.mask.core.MaskTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final MaskTemplate maskTemplate;

    public UserService(MaskTemplate maskTemplate) {
        this.maskTemplate = maskTemplate;
    }

    public String phone(String raw) {
        return maskTemplate.mask(MaskType.PHONE, raw);
    }

    public String email(String raw) {
        return maskTemplate.mask(MaskType.EMAIL, raw);
    }

    public String custom(String raw) {
        return maskTemplate.mask(MaskType.CUSTOM, raw, "\\d{4}", "#");
    }
}
```

`@Mask` 在 0.1.0 中只是元数据标记，不会自动拦截字段、参数或方法，也不会自动改 Jackson 序列化。需要手动调用 `MaskTemplate`。

#### 内置类型

| 类型 | 示例输入 | 默认输出 |
| --- | --- | --- |
| `PHONE` | `13812345678` | `138****5678` |
| `ID_CARD` | `110101199001011234` | `110101********1234` |
| `BANK_CARD` | `6222021234567890123` | `6222***********0123` |
| `EMAIL` | `zhangsan@example.com` | `z*******@example.com` |
| `CUSTOM` | `order-1234` + `\d{4}` | `order-*` |

### 6.2 audit：审计事件

#### 引入依赖

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-audit-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### 开启

```yaml
secure-kit:
  audit:
    enabled: true
    async: true
    default-operator: anonymous
    mask-before-save: true
```

#### 使用

```java
import io.github.user432335.securekit.audit.annotation.AuditLog;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @AuditLog(action = "query-order", targetIdExpression = "#p0")
    public String queryOrder(String orderId) {
        return "order:" + orderId;
    }
}
```

#### 接自己的存储

默认实现只输出 DEBUG 日志，不做持久化。生产环境提供自己的 `AuditRepository` Bean：

```java
import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuditConfig {

    @Bean
    public AuditRepository auditRepository() {
        return new AuditRepository() {
            @Override
            public void save(AuditEvent event) {
                // 写入数据库、消息队列或审计平台
            }
        };
    }
}
```

#### 当前操作人

```java
@Bean
public AuditUserProvider auditUserProvider() {
    return () -> "alice";
}
```

#### 说明

- audit 不强制依赖 mask。
- 检测到 mask 时，保存前自动脱敏。
- 没有 mask 时明文保存，并且只输出一次 WARN。
- 审计线程池有界，队列满时丢弃任务，不阻塞、不抛异常到业务线程。
- 默认不持久化，生产环境必须替换 `AuditRepository`。

### 6.3 log-guard：日志出口脱敏

#### 引入依赖

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-log-guard-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### 开启

```yaml
secure-kit:
  log-guard:
    enabled: true
    default-replacement: "*"
```

#### Logback 配置

```xml
<configuration>
    <conversionRule conversionWord="smsg"
                    converterClass="io.github.user432335.securekit.logguard.logback.SensitiveMessageConverter"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %smsg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

#### Log4j2 配置

```xml
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d %-5p %c - %m%n"/>
        </Console>
        <Rewrite name="Rewrite">
            <SensitiveRewritePolicy/>
            <AppenderRef ref="Console"/>
        </Rewrite>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Rewrite"/>
        </Root>
    </Loggers>
</Configuration>
```

#### 说明

- 本模块不会自动修改 `logback.xml` / `log4j2.xml`。
- Converter 和 Policy 由日志框架创建，不能注入 Spring Bean。
- 可选配置用系统属性：

```text
-Dsecure-kit.log-guard.default-replacement=#
-Dsecure-kit.log-guard.patterns=1[3-9]\d{9},\d{17}[\dXx]
```

## 7. 配置项

### mask

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `secure-kit.mask.enabled` | `false` | 是否开启 |
| `secure-kit.mask.default-replacement` | `*` | 替换文本 |
| `secure-kit.mask.patterns.<TYPE>` | 空 | 覆盖内置校验正则 |

### audit

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `secure-kit.audit.enabled` | `false` | 是否开启 |
| `secure-kit.audit.async` | `true` | 是否异步处理 |
| `secure-kit.audit.default-operator` | `anonymous` | 默认操作人 |
| `secure-kit.audit.mask-before-save` | `true` | 保存前是否脱敏 |
| `secure-kit.audit.async-core-pool-size` | `1` | 核心线程数 |
| `secure-kit.audit.async-max-pool-size` | `4` | 最大线程数 |
| `secure-kit.audit.async-queue-capacity` | `256` | 队列容量 |

### log-guard

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `secure-kit.log-guard.enabled` | `false` | 是否开启 |
| `secure-kit.log-guard.default-replacement` | `*` | 替换文本 |
| `secure-kit.log-guard.patterns` | 内置 7 条 | 敏感正则列表 |

## 8. 自定义扩展

### 自定义 Masker

```java
@Bean
public Masker employeeNoMasker() {
    return new Masker() {
        @Override
        public MaskType supportedType() {
            return MaskType.CUSTOM;
        }

        @Override
        public String mask(String original, String pattern) {
            return original == null ? null : original.replaceAll("\\d{6}", "******");
        }
    };
}
```

### 自定义 AuditRepository

```java
@Bean
public AuditRepository auditRepository() {
    return event -> {
        // 必须线程安全；建议快速返回
    };
}
```

### 自定义 AuditUserProvider

```java
@Bean
public AuditUserProvider auditUserProvider() {
    return () -> "alice";
}
```

## 9. FAQ

### 为什么引入依赖没有生效？

所有模块默认 `enabled=false`，需要显式设置 `secure-kit.<module>.enabled=true`。

### 为什么 `@Mask` 没有自动脱敏？

0.1.0 中 `@Mask` 只是元数据，不会自动拦截。请手动调用 `MaskTemplate`。

### audit 默认存到哪里？

默认只输出 DEBUG 日志，不持久化。生产环境请提供自己的 `AuditRepository`。

### 没有引入 mask，audit 能用吗？

能。audit 会明文保存事件，并输出一次 WARN。

### log-guard 开启了但没有效果？

需要同时修改 `logback.xml` 或 `log4j2.xml`，引用 `SensitiveMessageConverter` 或 `SensitiveRewritePolicy`。

### 能用来做认证、鉴权、JWT 吗？

不能。本项目不做这些能力，请使用 Spring Security 等方案。

### 支持 Spring Boot 2.x 吗？

不支持，只支持 Spring Boot 3.2.x。

### Central 上拉不到依赖怎么办？

在项目根目录执行 `mvn clean install`，先使用本地安装的 `0.1.0`。

### 和 Spring Security 冲突吗？

不冲突。本项目不注册任何 Filter/Interceptor，不接管 SecurityContext，可以和 Spring Security 同时使用。

### 性能开销多大？

mask 是纯字符串正则匹配；audit 异步且有界队列；log-guard 每条日志多一次正则 replace，建议只在需要的 appender 上启用。

### 为什么 audit 默认不持久化？

持久化方式差异太大（JDBC/文件/MQ/审计平台），默认只输出 DEBUG，生产环境请实现 `AuditRepository`。

### log-guard 会误伤纯数字订单号吗？

会。银行卡正则 `\d{13,19}` 较宽泛，请通过 `secure-kit.log-guard.patterns` 覆盖为业务精确规则。

## 10. 边界与已知限制

- 不做认证、鉴权、登录、JWT、OAuth2、RBAC。
- 不实现加密、哈希、签名算法。
- mask 是显式调用，忘记调用就不会脱敏。
- audit 默认不持久化；异步队列满时可能丢弃事件。
- log-guard 正则可能误伤纯数字业务字段，请按业务覆盖配置。
- log-guard 是日志出口兜底，不是 DLP 系统。
- 审计事件的 `errorMessage` 不参与脱敏。
- 本项目与 Spring Security 互补，不替代。

## 11. 兼容性

- Java 17
- Spring Boot 3.2.x
- Maven 3.9+
- 使用 Spring Boot 3.x `AutoConfiguration.imports`
- 不兼容 Spring Boot 2.x
- 构建命令：`mvn verify`

## 12. 示例项目

`secure-kit-demo` 是演示模块，包含 mask + audit 的最小集成。

```bash
# 运行 demo 的集成测试
mvn -pl secure-kit-demo -am test

# 启动 demo
mvn -pl secure-kit-demo -am spring-boot:run
```

demo 目前用于验证能力接入，不提供页面或 REST 接口。

## 13. Roadmap

### 0.2 计划

- Jackson 序列化扩展：基于 `@Mask` 的可选自动脱敏。
- 更多内置脱敏类型。
- 可选 JDBC `AuditRepository` 实现。
- 更完整的发布与版本说明。

如果这个项目对你有帮助，欢迎点一个 Star。

## 14. 贡献指南

欢迎提交 Issue 和 Pull Request。

提交前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)，并确保：

```bash
mvn verify
```

通过。

## 15. License

Apache License 2.0，详见 [LICENSE](LICENSE)。

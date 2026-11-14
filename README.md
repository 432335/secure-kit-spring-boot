# secure-kit-spring-boot

一组默认关闭、可独立引入的 Spring Boot 安全能力库：

- **mask**：显式脱敏手机号、身份证、银行卡、邮箱和自定义正则。
- **audit**：通过 `@AuditLog` 生成审计事件，可接自己的存储。
- **log-guard**：在 Logback / Log4j2 出口替换日志里的敏感文本。

三个能力互不强依赖，引入依赖不会自动生效，需要显式开启并按下面的方式使用。

## 模块

| 模块 | 作用 | 引入坐标 |
| --- | --- | --- |
| mask starter | 显式脱敏 | `io.github.user432335:secure-kit-mask-spring-boot-starter:0.1.0-SNAPSHOT` |
| audit starter | 审计事件 | `io.github.user432335:secure-kit-audit-spring-boot-starter:0.1.0-SNAPSHOT` |
| log-guard starter | 日志出口脱敏 | `io.github.user432335:secure-kit-log-guard-spring-boot-starter:0.1.0-SNAPSHOT` |

要求：Java 17、Spring Boot 3.2.x。

---

## 1. 脱敏：mask

### 引入依赖

```xml
<dependency>
    <groupId>io.github.user432335</groupId>
    <artifactId>secure-kit-mask-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 开启

```yaml
secure-kit:
  mask:
    enabled: true
```

### 使用

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

说明：`@Mask` 在 0.1.0 中只是标记，不会自动拦截，也不会自动改 Jackson 序列化，需要手动调用 `MaskTemplate`。

### 内置类型

| 类型 | 示例输入 | 默认输出 |
| --- | --- | --- |
| `PHONE` | `13812345678` | `138****5678` |
| `ID_CARD` | `110101199001011234` | `110101********1234` |
| `BANK_CARD` | `6222021234567890123` | `6222***********0123` |
| `EMAIL` | `zhangsan@example.com` | `z*******@example.com` |
| `CUSTOM` | `order-1234` + `\d{4}` | `order-*` |

---

## 2. 审计：audit

### 引入依赖

```xml
<dependency>
    <groupId>io.github.user432335</groupId>
    <artifactId>secure-kit-audit-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 开启

```yaml
secure-kit:
  audit:
    enabled: true
    async: true
    default-operator: anonymous
    mask-before-save: true
```

### 使用

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

### 接自己的存储

默认实现只输出 DEBUG 日志，不做持久化。生产环境提供一个自己的 `AuditRepository` Bean：

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

如果需要当前登录人，提供 `AuditUserProvider`：

```java
@Bean
public AuditUserProvider auditUserProvider() {
    return () -> "alice";
}
```

说明：

- audit 不强制依赖 mask。检测到 mask 时自动脱敏，没有 mask 时明文保存并输出一次 WARN。
- 审计线程池有界，队列满时丢弃任务，不会阻塞或抛异常到业务线程。

---

## 3. 日志脱敏：log-guard

### 引入依赖

```xml
<dependency>
    <groupId>io.github.user432335</groupId>
    <artifactId>secure-kit-log-guard-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 开启

```yaml
secure-kit:
  log-guard:
    enabled: true
    default-replacement: "*"
```

### Logback 配置

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

### Log4j2 配置

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

说明：

- 本模块不会自动修改你的 `logback.xml` / `log4j2.xml`，必须按上面配置。
- Logback Converter 和 Log4j2 Policy 由日志框架创建，不能注入 Spring Bean，可选配置用系统属性：

```text
-Dsecure-kit.log-guard.default-replacement=#
-Dsecure-kit.log-guard.patterns=1[3-9]\d{9},\d{17}[\dXx]
```

---

## 配置项

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

---

## 自定义 Masker

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

---

## 边界

- 不做认证、鉴权、登录、JWT、OAuth2、RBAC。
- 不实现加密、哈希、签名算法。
- mask 需要手动调用，忘记调用就不会脱敏。
- audit 默认不持久化，队列满时可能丢弃事件。
- log-guard 正则可能误伤纯数字业务字段，请按业务覆盖配置。

## License

Apache-2.0，详见 [LICENSE](LICENSE)。

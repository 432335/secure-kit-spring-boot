# Contributing

感谢你愿意参与 `secure-kit-spring-boot`。

## 环境要求

- JDK 17
- Maven 3.9+
- Git

## 本地构建

```bash
mvn clean verify
```

如果只想编译：

```bash
mvn -DskipTests compile
```

## 提交 Pull Request

1. Fork 本仓库。
2. 从 `main` 创建功能分支。
3. 保持改动聚焦，一个 PR 只做一件事。
4. 新增或修改行为时，补充对应测试。
5. 确保 `mvn verify` 通过。
6. 更新 README、CHANGELOG 或 Javadoc（如适用）。
7. 提交 PR，并说明改动目的、行为和测试方式。

## 代码约定

- 使用 Java 17。
- 不引入 Lombok、Guava、Commons Lang、MapStruct、Fastjson。
- 模块之间保持低耦合，不增加不必要的强依赖。
- 所有 public 类和 public 方法必须有 Javadoc。
- 默认关闭原则：新增能力必须显式开启。
- 不要让库代码接管用户控制流。
- 不要让安全能力的失败影响业务调用。
- 不要在代码、测试或文档中提交真实密钥、Token、密码或个人信息。

## 测试

- 使用 JUnit 5、AssertJ、`ApplicationContextRunner`。
- 覆盖正常路径、边界输入、空值和失败路径。
- 修改自动配置时，验证默认关闭和用户自定义 Bean 的退让行为。

## 文档

- README 面向使用者，优先说明“是什么”和“怎么用”。
- 发布流程见 [PUBLISHING.md](PUBLISHING.md)。
- 变更记录见 [CHANGELOG.md](CHANGELOG.md)。

## Issue

提交 Issue 时请说明：

- 使用的版本
- JDK 和 Spring Boot 版本
- 最小复现步骤
- 期望行为和实际行为

## License

提交贡献即表示同意以 Apache-2.0 协议发布。

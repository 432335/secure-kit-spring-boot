# 发布到 Maven Central

项目坐标：

```text
groupId:    io.github.432335
artifactId: secure-kit-*
version:    0.1.0
```

## 1. 前置条件

- Central Portal 已注册账号。
- 命名空间 `io.github.432335` 已验证。
- 本机已安装 GPG。
- 已生成并发布 GPG 公钥。
- `~/.m2/settings.xml` 已配置 Central token。

## 2. 配置 Central 凭据

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>Central 用户名</username>
            <password>Central Token</password>
        </server>
    </servers>
</settings>
```

`<id>central</id>` 必须与根 `pom.xml` 中的 `publishingServerId` 一致。

## 3. 生成并发布 GPG 公钥

```bash
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

如果 GPG 私钥有密码，发布时使用环境变量：

```bash
export MAVEN_GPG_PASSPHRASE='你的GPG密码'
```

Windows PowerShell：

```powershell
$env:MAVEN_GPG_PASSPHRASE='你的GPG密码'
```

## 4. 构建并发布

先跑普通构建：

```bash
mvn clean verify
```

再执行发布：

```bash
mvn clean deploy -Prelease
```

`release` profile 会：

- 附加 sources jar
- 附加 javadoc jar
- 使用 GPG 签名
- 通过 Central Publishing 插件上传

`autoPublish=true`，上传成功后会等待 Central 完成发布。

## 5. 在 Central Portal 确认

发布完成后登录：

```text
https://central.sonatype.com/
```

在 Deployments 中确认状态。首次发布通常需要等待同步到 Maven Central。

## 6. 发布后验证

等几分钟后访问：

```text
https://repo1.maven.org/maven2/io/github/432335/
```

能看到 `secure-kit-*` 目录和版本文件，就说明发布成功。

## 注意

- 不要发布 `SNAPSHOT` 版本。
- 发布后的版本不能覆盖，修 bug 请升版本号。
- `mvn clean verify` 不需要 GPG；`mvn clean deploy -Prelease` 才需要。

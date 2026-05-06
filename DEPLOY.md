# Java知识点记忆卡 - 部署说明

## 题库目录

项目使用外部化的 `banks` 目录存储题库文件，不依赖于源码内的 `data` 目录。

### 目录结构
```
banks/
├── 默认题库.json      # 默认题库文件
├── 自定义题库.json    # 用户导入的题库
└── banks.json         # 题库配置（记录当前选中的题库）
```

### 路径配置

在 `application.properties` 中配置：
```properties
# 相对路径（相对于应用运行目录）
banks.dir=./banks

# Linux服务器绝对路径示例
# banks.dir=/opt/javaStudyCard/banks
# banks.dir=${HOME}/javaStudyCard/banks
```

也可通过命令行参数覆盖：
```bash
# Windows
java -jar javaStudyCard-0.0.1.jar --banks.dir=C:\data\banks

# Linux
java -jar javaStudyCard-0.0.1.jar --banks.dir=/opt/javaStudyCard/banks
```

或通过环境变量：
```bash
export BANKS_DIR=/opt/javaStudyCard/banks
java -jar javaStudyCard-0.0.1.jar
```

## Linux部署步骤

1. 上传 jar 包到服务器
2. 创建题库目录并设置权限：
   ```bash
   mkdir -p /opt/javaStudyCard/banks
   chmod 755 /opt/javaStudyCard/banks
   ```

3. 放置默认题库（首次部署）：
   将 `src/main/resources/data/card.json` 复制为 `/opt/javaStudyCard/banks/默认题库.json`

4. 启动应用：
   ```bash
   nohup java -jar javaStudyCard-0.0.1.jar \
       --banks.dir=/opt/javaStudyCard/banks \
       --server.port=8080 \
       > app.log 2>&1 &
   ```

5. 验证：
   访问 `http://服务器IP:8080`

## 注意事项

- 题库修改后无需重启应用，页面刷新即可生效
- 通过页面"导入题库"功能新增的题库会自动保存到 `banks` 目录
- 确保运行用户有 `banks` 目录的读写权限

# 知识记忆卡片

用于记忆知识点

支持导入json题库

大半拿AI跑的代码

## JSON 题库格式

题库文件为 JSON 格式，包含一个 `questions` 数组，每个元素是一个题目对象。

### 公共字段

| 字段       | 类型   | 说明                                          |
| ---------- | ------ | --------------------------------------------- |
| `id`       | number | 题目唯一编号                                  |
| `category` | string | 知识分类名称                                  |
| `type`     | string | 题型：`MULTIPLE_CHOICE` / `ESSAY` / `SORTING` |
| `question` | string | 题目内容                                      |

### 多选题 (MULTIPLE_CHOICE)

```json
{
    "id": 1,
    "category": "集合框架",
    "type": "MULTIPLE_CHOICE",
    "question": "以下哪些是Java Collection Framework的接口？",
    "options": ["List", "Set", "Map", "Queue", "Array"],
    "correctAnswers": [0, 1, 3]
}
```

| 字段             | 类型     | 说明                            |
| ---------------- | -------- | ------------------------------- |
| `options`        | string[] | 选项列表                        |
| `correctAnswers` | number[] | 正确选项的索引数组（从 0 开始） |

其他字段设为 `null`。

### 问答题 (ESSAY)

```json
{
    "id": 2,
    "category": "集合框架",
    "type": "ESSAY",
    "question": "请简述ArrayList和LinkedList的区别及使用场景。",
    "answer": "ArrayList基于动态数组实现..."
}
```

| 字段     | 类型   | 说明                      |
| -------- | ------ | ------------------------- |
| `answer` | string | 参考答案，支持换行符 `\n` |

其他字段设为 `null`。

### 排序题 (SORTING)

```json
{
    "id": 3,
    "category": "集合框架",
    "type": "SORTING",
    "question": "请将ArrayList添加元素的底层操作按执行顺序排列：",
    "items": ["获取size变量", "检查是否需要扩容", "元素赋值", "size自增"],
    "correctOrder": [0, 1, 2, 3]
}
```

| 字段           | 类型     | 说明                            |
| -------------- | -------- | ------------------------------- |
| `items`        | string[] | 待排序的项，按任意顺序给出      |
| `correctOrder` | number[] | 正确顺序的索引数组（从 0 开始） |

其他字段设为 `null`。

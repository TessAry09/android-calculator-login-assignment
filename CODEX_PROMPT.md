# Codex 执行任务：Android 登录 + 计算器 + 结果列表综合作业（完整实现、验证、截图、GitHub 交付）

你是可以直接操作当前项目文件、Android SDK/模拟器、命令行和 Git/GitHub 的开发代理。请从当前工作目录开始，**一次性完成可运行 Android Studio 项目、真实测试、真实截图、提交说明文档与 GitHub 上传**。不是只写思路或代码片段。能自主决定的技术细节直接实现，不必反复向我确认；遇到实际权限、账号、环境缺失等不可绕过的问题，明确说明并完成其余工作。出现编译、运行、UI 或测试失败时主动修复重试。不要虚构执行结果、截图或 GitHub 链接。

## 一、依据与优先级

1. `references/assignment_requirements.png` 是这次作业最终 8 条文字要求，`references/ui_examples.png` 是登录/计算器/ListView 的视觉参考。请先查看这两张图；若看不清某处，可结合下列文字说明，不得自行猜测相反要求。
2. 老师 PPT《5.0 控件.pptx》第 31 页有本题较早版本：创建按下/抬起图片资源、登录 LinearLayout、计算器 GridLayout、学号密码、广播与鉴权、失败 AlertDialog、成功后 10 秒后台过程加进度、计算器初始显示学号、结果广播回登录。**本次截图后补充的“计算结果先打开 HistoryActivity 的 ListView，关闭它时才广播返回登录”优先于旧版 PPT 中计算后立刻广播的描述。**
3. 老师 PPT《4.0 布局.pptx》第 30 页是计算器前置要求：按钮资源使用灰/黑/黄系、GridLayout 水平排列、`+/-` 取反、`AC` 复位、`A op B = ?` 整数运算、按下按钮背景变色。这些也要满足。
4. 课程另有明确的提交要求：**“将执行过程、结果及代码截图”**。请提供真实截图并整理归档，不能仅有源代码或示意图。
5. 其他课堂参考：`3.1 广播及接收` 的自定义广播/Activity 收集和重新登录示例；`5.0 控件` 的 AlertDialog、ProgressBar/ProgressDialog、ListView；`4.0 布局` 的 GridLayout；`2.1 Activity` 的 Intent/Activity；`8.1 后台服务` 的后台线程 + UI 主线程更新。这些是实现参考，并非要求引入所有组件。

## 二、最终必须实现的 8 条任务（不得省略）

1. 创建相关图片/drawable 资源；计算器按钮按下、抬起有明显不同状态，灰/黑/黄色或橙黄色风格，与参考 UI 相近。
2. **登录页面根布局用 LinearLayout，计算器按键区真正使用 GridLayout**（XML 可检查，不能用纯 Compose 或其他布局冒充）。登录界面包含标题/图标、学号输入、密码输入、登录按钮；计算器接近图示黑底、灰色数字键、橙黄运算键、顶部大数字显示，尺寸适配模拟器。
3. 可以输入学号、密码，密码按密码输入框标准掩码显示。
4. 使用 Android **真实 BroadcastReceiver 机制**发送/接收有关正确账号的鉴权信息及返回结果；另外最终计算结果命名为 `M`，要作为广播附加数据传回登录界面。为消除原题表达歧义，优先采用两个明确动作：`ACTION_AUTH`（在应用内部发送鉴权广播并获得成功/失败结果，可以用有序广播返回码/result extras）与 `ACTION_CALC_FINISHED`（HistoryActivity 关闭时 `sendBroadcast` 传递原学号和计算值 `M`，由登录页的 BroadcastReceiver 接收）。使用自定义 action，限制在应用包内，Android 新版本注册兼容；不使用 LocalBroadcastManager 冒充系统广播，不向其他应用泄漏密码。
5. 鉴权失败时显示真正的 **AlertDialog 消息对话框**，说明原因，不启动进度/计算器。题目未提供固定正确密码：可以定义可测试、可在 README 中说明的**本地演示账号**（例如 `2010813299` / `123456`，纯虚构教学演示数据），覆盖空值、错误密码和格式错误；不可让任意非空学号密码都成功，也不接入不必要的真实服务器。
6. 鉴权成功时显示真实进度条或进度对话框，模拟约 **10 秒**的后台工作，进度从 0 到 100 连续变化，期间主线程可响应、不会冻结或 ANR；不能瞬间跳转、不能只放一张进度条图片。使用后台 Executor/线程和主线程 Handler/UI 更新等可靠方案，防重复登录、取消和生命周期问题。完成后进入计算器，**第一次显示的是当前学号**；首次按数字键应正确开始新计算，不把学号误拼接到输入操作数上。
7. 计算器能真实进行 `A op B = ?`，至少加减乘除四则整数运算，并正确处理 `AC` 清零、`+/-` 符号反转、按压反馈；与图片一致的 `%`、`.` 尽量一并实现，处理除零、格式错误、连续操作符、重复等号、长数字和小数显示等边界情况。每条结果记录由真实计算产生（保存算式 + 数值），**按 `=` 得到结果 M 后打开新的 HistoryActivity，以真正的 `ListView` 显示本次已经计算得到的所有结果**。如果支持连续运算，则保留中间有效结果供展示；绝不能用 1～100 的假数据冒充计算历史。HistoryActivity 的第一条至少应包含刚刚得到的 `M`。
8. **用户关闭 HistoryActivity（页面明确的“关闭/完成”按钮或系统返回键）时**，由它发送一次 `ACTION_CALC_FINISHED` 广播，登录页收到广播后清理之前的活动栈、回到全新的登录初始状态，但保留本次输入的原学号和密码（密码仍掩码，不进入日志或公共广播），学号框可见 `原学号-M`，示例 `2010813299-100`。保证不出现重复广播、无限重新启动、重复追加 `-M` 或退回旧计算器的情况。可使用 `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` 重建登录页并安全传回原输入；不要仅 `finish()` 历史页后露出计算器冒充完成。

**行为序列：** 登录填写 → 鉴权失败 AlertDialog / 鉴权成功 10 秒进度 → CalculatorActivity 初始显示学号 → 用户输入并完成真实计算 → HistoryActivity 的 ListView 展示结果 → 关闭 HistoryActivity 才发 Android 广播 → LoginActivity 接收并清栈重建 → 学号显示 `原学号-M` 且密码保留。

## 三、技术与结构要求

- 优先 **Java + XML Views**，与课堂的 Activity/LinearLayout/GridLayout/ListView/BroadcastReceiver 实践一致；新建标准可导入 Android Studio 的 Gradle 工程，如工作目录已有项目则原地修复和扩展，先检查再修改，不无故推倒已有正确实现。使用环境已有/可获取的稳定 SDK、AGP、Gradle/JDK 配对；不要硬编码无法安装的版本。
- 至少 `LoginActivity`、`CalculatorActivity`、`HistoryActivity`，可辅以 `AuthReceiver`、计算逻辑类、结果模型、工具类。Manifest 正确注册，适配 Android 12+ 的 `android:exported` 及较新系统的广播注册限制。
- 按钮使用 `res/drawable` 的真正 selector/shape 或 PNG 状态资源；不能只靠点击回调临时改文字声称实现了按压资源。可以自行制作简单图标/PNG，无需联网下载有版权的素材。
- 计算逻辑与 UI 尽量分离（方便单元测试），计算异常应可控且有用户可理解的提示；历史数据通过 Intent extras / Parcelable / 小型内存模型安全传递，不要求额外数据库。用 `BigDecimal` 或合理数字模型避免常见精度错误，同时保证整数输入得整数结果。
- 10 秒过程是模拟后台运算，无需真正远程网络、真实账号系统或多余的 Service。生命周期中断、旋转、重复点击至少不能导致崩溃或多次打开计算器。
- 账号/密码测试数据仅为本地演示；不要把个人真实密码、GitHub token、签名密钥、`local.properties`、机器路径或敏感日志提交 GitHub。

## 四、实际执行与自动测试（必须亲自运行）

1. 检查 Android Studio/Gradle/SDK、`adb`、模拟器或真机、Git 与 `gh` 是否可用；选择现有可运行设备。没有模拟器时尝试合理安装/创建/启动；环境问题如实报告，不能宣称已截图或运行成功。
2. 实现代码并运行构建命令，例如 `./gradlew assembleDebug testDebugUnitTest`（按现有环境适配）；处理全部编译和运行错误。安装到设备启动应用，观察 `adb logcat`，排除 Crash/ANR。
3. 尽量编写并实际运行单元测试和自动 UI 验收（Espresso、UIAutomator 或 adb 操作均可）。至少覆盖：错误密码弹框、正确密码、10 秒进度、计算器初始学号、`50 + 50 = 100`、`AC`、`+/-`、四则运算、除零、ListView 真实结果、关闭历史广播、回到登录并显示 `2010813299-100` 且原密码仍在输入框中（掩码）、返回键路径、重复广播保护。提供具体 PASS/FAIL 日志；不能把“写了测试代码”当作“执行成功”。
4. 进度过程有真实起止时间记录，正常执行应约 10 秒；截图至少涵盖进度中途和结束后进入计算器。截图可能暴露 demo 密码时也必须只显示掩码。
5. 每次排除问题后重跑关键路径，再提交。

## 五、截图与提交材料（Codex 自行完成）

创建 `evidence/screenshots/`，用 **实际 Android 模拟器/真机截图**（如 `adb exec-out screencap -p`）捕获至少以下文件，清晰、完整、无模拟伪造：

- `01_login_initial.png`：初始登录页与输入框；
- `02_auth_failed_dialog.png`：密码错误触发真实 AlertDialog；
- `03_auth_loading.png`：真实约 10 秒后台进度的中间状态；
- `04_calculator_initial.png`：鉴权成功，计算器初始显示学号；
- `05_calculator_operation.png`：有实际输入/运算状态；
- `06_history_listview.png`：计算完成后新 Activity 的真实 ListView 结果；
- `07_login_return_suffix.png`：关闭历史界面后，登录页学号出现 `-M`，密码仍保留掩码；
- 如有必要增加 `08_more_results.png`：多条真实计算记录。

创建 `evidence/code/`，提供能看清**真实源代码与对应文件名/行号**的代码截图，至少覆盖登录鉴权+广播、10 秒后台进度、计算器逻辑、GridLayout/XML+selector、HistoryActivity+ListView、广播返回+重置。优先截真实 Android Studio/编辑器屏幕；如果无 GUI 截图能力，可使用 Pygments 等将**真实项目源码**渲染为带行号、文件名、语法高亮的 PNG，并在文档标注“源码渲染截图”，不能伪造 IDE 界面。截图绝不允许用 AI 绘制界面代替运行证明。

同时编写以下文件：

- `README.md`：项目概述、技术栈、Android Studio 导入/构建/运行步骤、演示账号密码、全部关键功能、截图索引、测试与局限说明；
- `docs/作业报告.md`：逐条映射老师 8 条要求；设计与主要类/资源清单；执行步骤、实际结果、测试表和真实证据图片链接；关键代码截图嵌入；10 秒进度说明；广播数据/处理流程说明；
- `docs/验证记录.md`：实际执行的构建、测试、安装、截图命令及结果，成功与失败如实区分。

尽量保留最终可安装的 Debug APK 于适当的 `artifacts/`（体积不合适则可只提供构建方法，并说明）；关键源代码、Gradle wrapper、资源、测试、说明与截图必须随项目上传。不要提交 `.gradle/`、`build/`、本机配置或秘钥。确保报告中的相对图片链接在 GitHub 页面可直接浏览。

## 六、GitHub 上传与最终回复

1. 在验证完成后运行 `git status`、合理 `.gitignore`、`git add`、`git commit`。检查需要交付的代码、图片、报告都已追踪。
2. 若当前仓库已经有我可用的 GitHub `origin`，先检查远端，保护已有提交，推送到适当分支；否则使用已认证的 `gh` 创建**可供老师直接访问的公开仓库**（例如 `android-calculator-login-assignment`），确保不包含任何真实账号秘密，完成推送。只在 GitHub 授权确实不可用时才停在授权环节并给出具体阻碍，不要凭空生成 URL。
3. 完成后用 `git remote -v`、`git rev-parse HEAD`、`git ls-remote` 或 `gh repo view` 确认远端确实有最新提交，并核验 README 和截图已推送。
4. 最终只在**真实完成后**汇报：GitHub 完整仓库链接、推送分支/commit SHA、构建与测试结果、模拟器实际运行情况、截图目录/截图数量、作业报告路径、APK 路径（如果有），以及所有未解决问题。不要声称未实际执行的任务已完成。

**现在直接检查工作目录、参考图片和本机环境，开始实现、执行、截图、整理并推送，不需要先向我解释计划。**

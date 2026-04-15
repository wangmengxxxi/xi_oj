-- ----------------------------
-- xi oj 题目示例数据（共 12 道）
-- 入门 2 道 / 简单 6 道 / 中等 4 道
-- ⚠️ 注意：所有参考答案的 Java 代码均通过 args[] 读取输入
--    代码沙箱将 judgeCase.input 按空格拆分后作为命令行参数传入
--    例如 input="5 1 2 3 4 5" → args[0]="5", args[1]="1", ..., args[5]="5"
-- userId 使用 admin 用户：2037025038146736130
-- ----------------------------

INSERT INTO `question`
  (`title`, `content`, `tags`, `answer`, `submitNum`, `acceptedNum`,
   `judgeCase`, `judgeConfig`, `thumbNum`, `favourNum`, `userId`)
VALUES

-- ============================================================
-- 1. A+B 问题（入门）
-- ============================================================
(
  'A+B 问题',
  '## 题目描述\n\n计算两个整数 A 和 B 的和。\n\n## 输入格式\n\n一行，包含两个整数 A 和 B，用空格分隔。\n\n## 输出格式\n\n输出 A + B 的值。\n\n## 示例\n\n**输入**\n```\n1 2\n```\n\n**输出**\n```\n3\n```\n\n## 数据范围\n\n- $-10^9 \\leq A, B \\leq 10^9$',
  '["入门", "模拟"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        long a = Long.parseLong(args[0]);\n        long b = Long.parseLong(args[1]);\n        System.out.println(a + b);\n    }\n}\n```\n\n## 思路\n\n直接读取 args[0]、args[1] 并相加。注意两数均为 $10^9$ 时结果超 int 范围，使用 `long`。',
  1523, 1287,
  '[{"input":"1 2","output":"3"},{"input":"-5 10","output":"5"},{"input":"0 0","output":"0"},{"input":"1000000000 999999999","output":"1999999999"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  45, 23,
  2037025038146736130
),

-- ============================================================
-- 2. 回文字符串判断（入门）
-- ============================================================
(
  '回文字符串判断',
  '## 题目描述\n\n给定一个只含小写字母的字符串 S，判断它是否为回文字符串（正读反读相同）。\n\n## 输入格式\n\n一行，字符串 S（只含小写英文字母，不含空格）。\n\n## 输出格式\n\n若是回文输出 `YES`，否则输出 `NO`。\n\n## 示例\n\n**输入 1**\n```\nabcba\n```\n**输出 1**\n```\nYES\n```\n\n**输入 2**\n```\nhello\n```\n**输出 2**\n```\nNO\n```\n\n## 数据范围\n\n- $1 \\leq |S| \\leq 10^5$',
  '["字符串", "双指针", "入门"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        String s = args[0];\n        String rev = new StringBuilder(s).reverse().toString();\n        System.out.println(s.equals(rev) ? "YES" : "NO");\n    }\n}\n```\n\n## 思路\n\nargs[0] 即为字符串，利用 StringBuilder.reverse() 反转后比较，O(n) 时间。',
  987, 823,
  '[{"input":"abcba","output":"YES"},{"input":"hello","output":"NO"},{"input":"a","output":"YES"},{"input":"abba","output":"YES"},{"input":"abcd","output":"NO"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  32, 18,
  2037025038146736130
),

-- ============================================================
-- 3. 斐波那契数列（简单 · 递推）
-- ============================================================
(
  '斐波那契数列',
  '## 题目描述\n\n斐波那契数列定义：$F(1)=1$，$F(2)=1$，$F(n)=F(n-1)+F(n-2)$（$n \\geq 3$）。\n\n给定正整数 n，求 $F(n)$。\n\n## 输入格式\n\n一行，一个正整数 n。\n\n## 输出格式\n\n输出 $F(n)$。\n\n## 示例\n\n**输入**\n```\n10\n```\n**输出**\n```\n55\n```\n\n## 数据范围\n\n- $1 \\leq n \\leq 40$\n\n> 提示：$F(40) = 102334155$，在 int 范围内，无需取模。',
  '["数学", "递推", "动态规划"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        if (n <= 2) { System.out.println(1); return; }\n        long a = 1, b = 1;\n        for (int i = 3; i <= n; i++) {\n            long c = a + b;\n            a = b;\n            b = c;\n        }\n        System.out.println(b);\n    }\n}\n```\n\n## 思路\n\n迭代（滚动数组）：两个变量 a、b 交替更新，O(n) 时间 O(1) 空间。\n\n⚠️ 不要用递归，F(40) 约需 $2^{40}$ 次递归调用，必然 TLE。',
  2156, 1723,
  '[{"input":"1","output":"1"},{"input":"2","output":"1"},{"input":"10","output":"55"},{"input":"30","output":"832040"},{"input":"40","output":"102334155"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  67, 41,
  2037025038146736130
),

-- ============================================================
-- 4. 爬楼梯（简单 · DP）
-- ============================================================
(
  '爬楼梯',
  '## 题目描述\n\n你正在爬一个 n 阶楼梯，每次可以爬 **1 阶**或 **2 阶**，问有多少种不同方案爬到顶？\n\n## 输入格式\n\n一行，一个正整数 n（楼梯总阶数）。\n\n## 输出格式\n\n一个整数，表示方案总数。\n\n## 示例\n\n**输入**\n```\n5\n```\n**输出**\n```\n8\n```\n\n> 8 种方案：1+1+1+1+1 / 1+1+1+2 / 1+1+2+1 / 1+2+1+1 / 2+1+1+1 / 1+2+2 / 2+1+2 / 2+2+1\n\n## 数据范围\n\n- $1 \\leq n \\leq 45$',
  '["动态规划", "数学"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        if (n <= 2) { System.out.println(n); return; }\n        long a = 1, b = 2;\n        for (int i = 3; i <= n; i++) {\n            long c = a + b;\n            a = b;\n            b = c;\n        }\n        System.out.println(b);\n    }\n}\n```\n\n## 思路\n\n状态转移：$dp[i] = dp[i-1] + dp[i-2]$，边界 $dp[1]=1, dp[2]=2$。\n\n本质是斐波那契数列从第 2 项开始的版本，滚动变量实现 O(1) 空间。',
  1876, 1453,
  '[{"input":"1","output":"1"},{"input":"2","output":"2"},{"input":"3","output":"3"},{"input":"5","output":"8"},{"input":"10","output":"89"},{"input":"45","output":"1836311903"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  89, 56,
  2037025038146736130
),

-- ============================================================
-- 5. 有效的括号（简单 · 栈）
-- ============================================================
(
  '有效的括号',
  '## 题目描述\n\n给定只含括号字符 `(`、`)`、`[`、`]`、`{`、`}` 的字符串 S，判断括号是否合法。\n\n合法要求：每个左括号以正确顺序被相同类型右括号闭合。\n\n## 输入格式\n\n一行，字符串 S（只含括号，不含空格）。\n\n## 输出格式\n\n合法输出 `YES`，否则输出 `NO`。\n\n## 示例\n\n**输入 1**\n```\n()[]{}\n```\n**输出 1**\n```\nYES\n```\n\n**输入 2**\n```\n([)]\n```\n**输出 2**\n```\nNO\n```\n\n## 数据范围\n\n- $1 \\leq |S| \\leq 10^4$',
  '["字符串", "栈"]',
  '## 参考答案（Java，args传参）\n\n```java\nimport java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        String s = args[0];\n        Deque<Integer> stack = new ArrayDeque<>();\n        for (int i = 0; i < s.length(); i++) {\n            int c = s.charAt(i);\n            // 左括号：( = 40  [ = 91  { = 123\n            if (c == 40 || c == 91 || c == 123) {\n                stack.push(c);\n            } else {\n                if (stack.isEmpty()) { System.out.println("NO"); return; }\n                int top = stack.pop();\n                // ) = 41  ] = 93  } = 125\n                if ((c == 41 && top != 40) ||\n                    (c == 93 && top != 91) ||\n                    (c == 125 && top != 123)) {\n                    System.out.println("NO"); return;\n                }\n            }\n        }\n        System.out.println(stack.isEmpty() ? "YES" : "NO");\n    }\n}\n```\n\n## 思路\n\n用栈模拟：遇左括号压栈，遇右括号弹出栈顶检查是否匹配。使用 ASCII 码值比较，避免字符字面量。时间复杂度 O(n)。',
  1654, 1132,
  '[{"input":"()","output":"YES"},{"input":"()[]{}","output":"YES"},{"input":"(]","output":"NO"},{"input":"([)]","output":"NO"},{"input":"{[]}","output":"YES"},{"input":"(","output":"NO"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  73, 48,
  2037025038146736130
),

-- ============================================================
-- 6. 寻找数组最大值（简单 · 数组）
-- ============================================================
(
  '寻找数组最大值',
  '## 题目描述\n\n给定一个长度为 n 的整数数组，找出其中的最大值。\n\n## 输入格式\n\nargs[0] 为数组长度 n，args[1..n] 为数组元素。\n\n一行示例：`5 3 1 4 1 5`（n=5，数组为 [3,1,4,1,5]）\n\n## 输出格式\n\n输出数组最大值。\n\n## 示例\n\n**输入**\n```\n5 3 1 4 1 5\n```\n**输出**\n```\n5\n```\n\n## 数据范围\n\n- $1 \\leq n \\leq 10^5$\n- $-10^9 \\leq$ 元素 $\\leq 10^9$',
  '["数组", "模拟", "简单"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        long max = Long.MIN_VALUE;\n        for (int i = 1; i <= n; i++) {\n            long x = Long.parseLong(args[i]);\n            if (x > max) max = x;\n        }\n        System.out.println(max);\n    }\n}\n```\n\n## 思路\n\nargs[0] 是 n，args[1..n] 是数组元素。遍历一次更新最大值，O(n)。',
  1203, 1089,
  '[{"input":"5 3 1 4 1 5","output":"5"},{"input":"3 -1 -3 -2","output":"-1"},{"input":"1 42","output":"42"},{"input":"4 10 20 30 5","output":"30"},{"input":"3 0 0 0","output":"0"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  28, 15,
  2037025038146736130
),

-- ============================================================
-- 7. 二分查找（简单 · 二分）
-- ============================================================
(
  '二分查找',
  '## 题目描述\n\n给定一个**严格升序**且**各不相同**的整数数组和目标值 target，找出 target 在数组中的位置（**下标从 1 开始**）。\n\n若不存在，输出 -1。\n\n## 输入格式\n\nargs[0] 为数组长度 n，args[1..n] 为升序数组元素，args[n+1] 为 target。\n\n示例：`5 1 3 5 7 9 5`（n=5，数组=[1,3,5,7,9]，target=5）\n\n## 输出格式\n\n若找到输出其 1-based 下标，否则输出 -1。\n\n## 示例\n\n**输入**\n```\n5 1 3 5 7 9 5\n```\n**输出**\n```\n3\n```\n\n## 数据范围\n\n- $1 \\leq n \\leq 10^5$\n- $-10^9 \\leq$ 元素、target $\\leq 10^9$',
  '["数组", "二分查找"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        int[] arr = new int[n];\n        for (int i = 0; i < n; i++) arr[i] = Integer.parseInt(args[i + 1]);\n        int target = Integer.parseInt(args[n + 1]);\n\n        int lo = 0, hi = n - 1;\n        while (lo <= hi) {\n            int mid = lo + (hi - lo) / 2;\n            if (arr[mid] == target) { System.out.println(mid + 1); return; }\n            else if (arr[mid] < target) lo = mid + 1;\n            else hi = mid - 1;\n        }\n        System.out.println(-1);\n    }\n}\n```\n\n## 思路\n\n经典二分：用 `lo + (hi - lo) / 2` 防溢出。每轮折半，时间复杂度 O(log n)。\n\n⚠️ target 是 args[n+1]，别漏掉偏移。',
  2089, 1765,
  '[{"input":"5 1 3 5 7 9 5","output":"3"},{"input":"5 1 3 5 7 9 6","output":"-1"},{"input":"1 1 1","output":"1"},{"input":"6 -5 -3 0 2 7 10 7","output":"5"},{"input":"4 2 4 6 8 1","output":"-1"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  58, 37,
  2037025038146736130
),

-- ============================================================
-- 8. 最大子数组和（中等 · Kadane / DP）
-- ============================================================
(
  '最大子数组和',
  '## 题目描述\n\n给定一个整数数组 nums，找到和最大的**连续子数组**（至少含 1 个元素），返回其最大和。\n\n## 输入格式\n\nargs[0] 为数组长度 n，args[1..n] 为数组元素。\n\n示例：`9 -2 1 -3 4 -1 2 1 -5 4`\n\n## 输出格式\n\n输出最大子数组和。\n\n## 示例\n\n**输入**\n```\n9 -2 1 -3 4 -1 2 1 -5 4\n```\n**输出**\n```\n6\n```\n> 最大子数组为 [4,-1,2,1]，和为 6。\n\n## 数据范围\n\n- $1 \\leq n \\leq 10^5$\n- $-10^4 \\leq$ nums[i] $\\leq 10^4$',
  '["数组", "动态规划", "分治"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        long cur = Long.MIN_VALUE, maxSum = Long.MIN_VALUE;\n        for (int i = 1; i <= n; i++) {\n            long x = Long.parseLong(args[i]);\n            cur = Math.max(x, cur + x);\n            maxSum = Math.max(maxSum, cur);\n        }\n        System.out.println(maxSum);\n    }\n}\n```\n\n## 思路（Kadane 算法）\n\n设 `cur` 为以当前元素结尾的最大子数组和：\n- `cur = max(x, cur + x)`\n  - 若 `cur + x < x`，说明之前积累的子数组是累赘，从当前重新开始。\n\n`maxSum` 记录全局最大值。O(n) 时间，O(1) 空间。',
  2543, 1234,
  '[{"input":"9 -2 1 -3 4 -1 2 1 -5 4","output":"6"},{"input":"1 1","output":"1"},{"input":"5 5 4 -1 7 8","output":"23"},{"input":"4 -3 -2 -1 -4","output":"-1"},{"input":"6 1 -1 1 -1 1 -1","output":"1"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  112, 78,
  2037025038146736130
),

-- ============================================================
-- 9. 买卖股票的最佳时机（简单 · 贪心）
-- ============================================================
(
  '买卖股票的最佳时机',
  '## 题目描述\n\n给定数组 prices，第 i 个元素为第 i 天的股票价格。只能选一天买入并在**未来某一天**卖出，求可以获取的最大利润。无法获利则返回 0。\n\n## 输入格式\n\nargs[0] 为天数 n，args[1..n] 为每天价格。\n\n## 输出格式\n\n最大利润（无法获利则输出 0）。\n\n## 示例\n\n**输入 1**\n```\n6 7 1 5 3 6 4\n```\n**输出 1**\n```\n5\n```\n> 第 2 天买（价格 1），第 5 天卖（价格 6），利润 = 5。\n\n**输入 2**\n```\n5 7 6 4 3 1\n```\n**输出 2**\n```\n0\n```\n\n## 数据范围\n\n- $1 \\leq n \\leq 10^5$\n- $0 \\leq$ prices[i] $\\leq 10^4$',
  '["数组", "动态规划", "贪心"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        int minPrice = Integer.MAX_VALUE, maxProfit = 0;\n        for (int i = 1; i <= n; i++) {\n            int price = Integer.parseInt(args[i]);\n            if (price < minPrice) {\n                minPrice = price;\n            } else if (price - minPrice > maxProfit) {\n                maxProfit = price - minPrice;\n            }\n        }\n        System.out.println(maxProfit);\n    }\n}\n```\n\n## 思路\n\n**贪心**：维护历史最低价 `minPrice`，对每个价格计算利润。\n\n只要当前价格高于历史最低价，就是潜在的最大利润。O(n) 时间，O(1) 空间。',
  1987, 1345,
  '[{"input":"6 7 1 5 3 6 4","output":"5"},{"input":"5 7 6 4 3 1","output":"0"},{"input":"3 1 2 3","output":"2"},{"input":"4 3 1 4 2","output":"3"},{"input":"1 5","output":"0"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  98, 65,
  2037025038146736130
),

-- ============================================================
-- 10. 移动零（简单 · 双指针）
-- ============================================================
(
  '移动零',
  '## 题目描述\n\n给定整数数组 nums，将所有 0 移动到数组末尾，同时保持**非零元素的相对顺序**不变。\n\n**原地修改**数组，输出修改后的数组（空格分隔）。\n\n## 输入格式\n\nargs[0] 为数组长度 n，args[1..n] 为数组元素。\n\n## 输出格式\n\n输出处理后的数组，元素用空格分隔。\n\n## 示例\n\n**输入**\n```\n5 0 1 0 3 12\n```\n**输出**\n```\n1 3 12 0 0\n```\n\n## 数据范围\n\n- $1 \\leq n \\leq 10^4$\n- $-2^{31} \\leq$ nums[i] $\\leq 2^{31}-1$',
  '["数组", "双指针"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        int[] arr = new int[n];\n        for (int i = 0; i < n; i++) arr[i] = Integer.parseInt(args[i + 1]);\n\n        // 双指针：pos 指向下一个非零元素应放的位置\n        int pos = 0;\n        for (int x : arr) if (x != 0) arr[pos++] = x;\n        while (pos < n) arr[pos++] = 0;\n\n        StringBuilder sb = new StringBuilder();\n        for (int i = 0; i < n; i++) {\n            if (i > 0) sb.append('' '');\n            sb.append(arr[i]);\n        }\n        System.out.println(sb);\n    }\n}\n```\n\n## 思路\n\n双指针：第一遍把所有非零元素依次填到前面，第二遍把剩余位置补 0。O(n) 时间，O(1) 额外空间。',
  1432, 1156,
  '[{"input":"5 0 1 0 3 12","output":"1 3 12 0 0"},{"input":"3 0 0 0","output":"0 0 0"},{"input":"3 1 2 3","output":"1 2 3"},{"input":"4 0 1 2 0","output":"1 2 0 0"},{"input":"2 0 1","output":"1 0"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  61, 43,
  2037025038146736130
),

-- ============================================================
-- 11. 最长公共子序列（中等 · DP）
-- ============================================================
(
  '最长公共子序列',
  '## 题目描述\n\n给定两个字符串 text1 和 text2，返回它们的**最长公共子序列（LCS）**的长度。\n\n子序列是从字符串中删去若干字符（可不删）但不改变字符顺序后得到的新字符串。\n\n若无公共子序列，返回 0。\n\n## 输入格式\n\nargs[0] 为 text1，args[1] 为 text2（均为小写字母，不含空格）。\n\n## 输出格式\n\nLCS 的长度。\n\n## 示例\n\n**输入**\n```\nabcde ace\n```\n**输出**\n```\n3\n```\n> LCS 为 "ace"，长度为 3。\n\n## 数据范围\n\n- $1 \\leq |\\text{text1}|, |\\text{text2}| \\leq 1000$',
  '["字符串", "动态规划"]',
  '## 参考答案（Java，args传参）\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        String s1 = args[0], s2 = args[1];\n        int m = s1.length(), n = s2.length();\n        int[][] dp = new int[m + 1][n + 1];\n        for (int i = 1; i <= m; i++) {\n            for (int j = 1; j <= n; j++) {\n                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {\n                    dp[i][j] = dp[i - 1][j - 1] + 1;\n                } else {\n                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);\n                }\n            }\n        }\n        System.out.println(dp[m][n]);\n    }\n}\n```\n\n## 思路（二维 DP）\n\n$dp[i][j]$：text1 前 i 个字符与 text2 前 j 个字符的 LCS 长度。\n\n状态转移：\n- 字符相同：$dp[i][j] = dp[i-1][j-1] + 1$\n- 字符不同：$dp[i][j] = \\max(dp[i-1][j], dp[i][j-1])$\n\nO(mn) 时间，O(mn) 空间。',
  1876, 987,
  '[{"input":"abcde ace","output":"3"},{"input":"abc abc","output":"3"},{"input":"abc def","output":"0"},{"input":"abcba cba","output":"3"},{"input":"ezupkr ubmrapg","output":"2"}]',
  '{"timeLimit":2000,"memoryLimit":262144,"stackLimit":262144}',
  134, 87,
  2037025038146736130
),

-- ============================================================
-- 12. 数组中第 K 大的元素（中等 · 排序 / 堆）
-- ============================================================
(
  '数组中第 K 大的元素',
  '## 题目描述\n\n给定整数数组 nums 和整数 k，返回数组中第 k 大的元素（**非第 k 个不重复的元素**，而是排序后的第 k 大）。\n\n## 输入格式\n\nargs[0] 为数组长度 n，args[1..n] 为数组元素，args[n+1] 为 k。\n\n示例：`5 3 2 1 5 6 2`（n=5，数组=[3,2,1,5,6]，k=2）\n\n## 输出格式\n\n第 k 大的元素。\n\n## 示例\n\n**输入 1**\n```\n5 3 2 1 5 6 2\n```\n**输出 1**\n```\n5\n```\n> 排序后为 [1,2,3,5,6]，第 2 大 = 5。\n\n**输入 2**\n```\n4 3 2 3 1 1\n```\n**输出 2**\n```\n3\n```\n> 排序后为 [1,2,3,3]，第 1 大 = 3。\n\n## 数据范围\n\n- $1 \\leq k \\leq n \\leq 10^4$\n- $-10^4 \\leq$ nums[i] $\\leq 10^4$',
  '["数组", "排序", "堆（优先队列）", "分治"]',
  '## 参考答案（Java，args传参）\n\n```java\nimport java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        int n = Integer.parseInt(args[0]);\n        int[] arr = new int[n];\n        for (int i = 0; i < n; i++) arr[i] = Integer.parseInt(args[i + 1]);\n        int k = Integer.parseInt(args[n + 1]);\n\n        // 方法一：排序（简洁，O(n log n)）\n        Arrays.sort(arr);\n        System.out.println(arr[n - k]);\n\n        // 方法二（进阶）：最小堆维护前 k 大，O(n log k)\n        // PriorityQueue<Integer> heap = new PriorityQueue<>();\n        // for (int x : arr) {\n        //     heap.offer(x);\n        //     if (heap.size() > k) heap.poll();\n        // }\n        // System.out.println(heap.peek());\n    }\n}\n```\n\n## 思路\n\n**方法一（排序）**：升序排序后返回 `arr[n-k]`，O(n log n)。\n\n**方法二（最小堆，进阶）**：维护大小为 k 的最小堆，堆顶即为第 k 大元素，O(n log k)，适合 n 很大时。',
  1654, 987,
  '[{"input":"5 3 2 1 5 6 2","output":"5"},{"input":"4 3 2 3 1 1","output":"3"},{"input":"3 1 2 3 1","output":"3"},{"input":"5 7 6 5 4 3 3","output":"5"},{"input":"2 1 2 1","output":"2"}]',
  '{"timeLimit":1000,"memoryLimit":262144,"stackLimit":262144}',
  87, 56,
  2037025038146736130
);

content_type: 错题分析
tag: 二分查找
title: 二分查找-WA-边界处理错误

【典型错误】
1. 循环条件写成 left < right，漏掉 left == right 时的判断，导致目标值在右端时找不到
2. 更新边界时写 right = mid 或 left = mid，而非 right = mid - 1 / left = mid + 1，导致死循环
3. mid 计算写成 (left + right) / 2，当 left 和 right 均较大时整数溢出，结果变负

【修正思路】
- 明确区间定义（左闭右闭或左闭右开）后，循环条件和边界更新必须配套使用，不能混用
- 左闭右闭：while (left <= right)，left = mid + 1，right = mid - 1
- left闭右开：while (left < right)，left = mid + 1，right = mid
- mid 统一写成 left + (right - left) / 2

【自查清单】
□ 区间定义是否统一？
□ 边界更新是否与区间定义匹配？
□ mid 是否防溢出？

---
content_type: 错题分析
tag: 二分查找
title: 二分查找-TLE-死循环

【典型错误】
当 left 和 right 相邻时（right = left + 1），若 mid = left，且条件不更新 left，则 left/right 永远不变，陷入死循环。
常见于查找左右边界时写成 left = mid（而非 left = mid + 1）。

【修正思路】
- 检查每次循环结束后 left 或 right 是否一定在缩小范围
- 若使用 right = mid，则 left 必须用 left = mid + 1；若使用 left = mid，则中点要向上取整 mid = left + (right - left + 1) / 2
- 可以在草稿上用 [0,1] 这样的小范围手动模拟一遍

【自查清单】
□ 每轮循环后区间是否严格缩小？
□ mid 取整方向是否与边界更新一致？

---
content_type: 错题分析
tag: 动态规划
title: 动态规划-WA-状态转移遗漏边界或初始值错误

【典型错误】
1. dp 数组初始值设为 0，但实际某些状态应为负无穷（求最大值时）或正无穷（求最小值时）
2. 遍历顺序错误：0-1背包内层应从大到小，写成从小到大等同于完全背包
3. 转移时数组越界：dp[i-1] 但 i 从 0 开始，未特判 i=0 的情况
4. 状态定义不清晰，导致转移方程无法正确描述子问题关系

【修正思路】
- 求最大值时 dp 初始化为 Integer.MIN_VALUE / 2（除以2防止加法溢出），求最小值时用 Integer.MAX_VALUE / 2
- 写出转移方程后先手算 dp[0]、dp[1]、dp[2] 验证
- 背包问题：物品在外层，容量在内层；0-1背包容量从大到小，完全背包从小到大

【自查清单】
□ dp 初始值是否与转移方向匹配？
□ 遍历顺序是否正确？
□ 边界下标是否越界？

---
content_type: 错题分析
tag: 动态规划
title: 动态规划-MLE-未优化空间导致内存超限

【典型错误】
使用二维 dp[n][m] 存储所有状态，当 n 和 m 均较大时（如 n=m=1000）数组达到百万级，超出内存限制。

【修正思路】
- 若 dp[i] 只依赖 dp[i-1]，可用滚动数组压缩为一维
- 0-1背包：一维 dp[j]，内层从大到小遍历
- 最长公共子序列（LCS）：dp[i][j] 只依赖 dp[i-1][j]、dp[i-1][j-1]、dp[i][j-1]，可用两行滚动
- 空间优化后要注意遍历方向，防止覆盖还未被使用的旧值

【自查清单】
□ 当前状态依赖哪些历史状态？是否可以滚动？
□ 滚动后遍历方向是否需要调整？

---
content_type: 错题分析
tag: 递归
title: 递归-RE-栈溢出（StackOverflowError）

【典型错误】
1. 递归缺少终止条件，或终止条件判断有误，导致无限递归
2. 输入规模过大（如 n=10000 的链表递归），递归深度超过 JVM 默认栈深度（约 1000~8000 层）
3. 图/树的 DFS 未标记 visited，在有环图中无限循环

【修正思路】
- 检查所有递归路径是否都能到达终止条件，用小样本手动模拟
- 递归深度过大时改用迭代 + 显式栈（java.util.Deque 模拟调用栈）
- 图的 DFS 必须维护 visited[] 数组，在进入递归前标记而非递归返回后标记

【自查清单】
□ 每条递归路径是否都有终止条件？
□ 递归深度是否可能超过 10^4？
□ 图的 DFS 是否标记了 visited？

---
content_type: 错题分析
tag: 数组
title: 数组-RE-下标越界（ArrayIndexOutOfBoundsException）

【典型错误】
1. 访问 nums[i+1] 或 nums[i-1] 时未判断 i 是否在合法范围
2. 二维数组行列混淆：grid[row][col] 写成 grid[col][row]
3. 字符串 charAt(i) 在循环中 i 可能等于 length()，应用 < 而非 <=
4. 滑动窗口/双指针的右指针越过数组末尾

【修正思路】
- 访问相邻元素前先判断边界：if (i + 1 < n && ...) 
- 二维数组遍历时明确 rows = grid.length，cols = grid[0].length
- 循环条件统一用严格小于：for (int i = 0; i < n; i++)
- 提交前在草稿纸上检查最大下标是否合法

【自查清单】
□ 是否访问了 nums[n] 或 nums[-1]？
□ 二维数组行列是否写反？

---
content_type: 错题分析
tag: 双指针
title: 双指针-WA-移动条件错误导致遗漏解

【典型错误】
1. 对撞指针：在 while 内层移动指针时少写了一次，导致同一对元素被重复计算，或某些解被跳过
2. 去重处理不当：找到一个解后没有跳过重复元素，导致结果集有重复
3. 条件判断顺序错误：应先移动指针再更新答案，或反过来

【修正思路】
三数之和典型去重写法：
Arrays.sort(nums);
for (int i = 0; i < n - 2; i++) {
    if (i > 0 && nums[i] == nums[i-1]) continue;  // 外层去重
    int left = i + 1, right = n - 1;
    while (left < right) {
        int sum = nums[i] + nums[left] + nums[right];
        if (sum == 0) {
            result.add(Arrays.asList(nums[i], nums[left], nums[right]));
            while (left < right && nums[left] == nums[left+1]) left++;   // 内层去重
            while (left < right && nums[right] == nums[right-1]) right--;
            left++; right--;
        } else if (sum < 0) left++;
        else right--;
    }
}

【自查清单】
□ 找到答案后是否正确去重并移动指针？
□ 指针移动后是否还需要更新答案？

---
content_type: 错题分析
tag: 哈希表
title: 哈希表-WA-未处理默认值或equals/hashCode问题

【典型错误】
1. map.get(key) 返回 null 时直接参与运算，抛出 NullPointerException
2. 用数组下标作为字母映射时，未将字符转为正确偏移：ch - 'a' 或 ch - '0'
3. 用自定义对象作为 key 时，未重写 hashCode 和 equals，导致相同内容对象被视为不同 key
4. 统计完频率后遍历 map 时修改了 map，引发 ConcurrentModificationException

【修正思路】
- 统一使用 map.getOrDefault(key, 0) 代替 map.get(key)
- 字母映射用 int[] count = new int[26]，索引为 ch - 'a'，比 HashMap 更高效
- 自定义 key 必须同时重写 hashCode 和 equals（或用记录类 record）
- 遍历时需要修改应先收集 key 到 List，再遍历 List

【自查清单】
□ 是否用了 getOrDefault 防止 NPE？
□ 自定义对象 key 是否重写了 equals 和 hashCode？

---
content_type: 错题分析
tag: 图论
title: 图论-WA-未正确标记已访问节点导致重复遍历或死循环

【典型错误】
1. BFS/DFS 未维护 visited 数组，在有环图中无限循环
2. visited 标记时机错误：在出队时才标记（应在入队时标记，否则同一节点可能被多次入队）
3. 无向图建边时只建了单向边：只加了 graph[u].add(v) 忘记 graph[v].add(u)
4. 求最短路时未初始化 dist 数组为无穷大，导致 0 被误认为已到达

【修正思路】
BFS 正确入队标记时机：
queue.offer(start);
visited[start] = true;  // 入队时立刻标记，而非出队时
while (!queue.isEmpty()) {
    int node = queue.poll();
    for (int next : graph.get(node)) {
        if (!visited[next]) {
            visited[next] = true;  // 入队时标记
            queue.offer(next);
        }
    }
}

【自查清单】
□ visited 是否在入队/入栈时标记？
□ 无向图是否建了双向边？
□ dist 初始值是否正确？

---
content_type: 错题分析
tag: 排序
title: 排序-WA-自定义比较器不满足传递性

【典型错误】
自定义 Comparator 时写了 (a, b) -> a - b 用于 Integer 比较，当两数差值超过 Integer.MAX_VALUE 时整数溢出，产生错误的排序结果。
或比较器逻辑本身不满足传递性（a>b 且 b>c 但 a<=c），Java TimSort 会抛出 IllegalArgumentException: Comparison method violates its general contract!

【修正思路】
- 整数比较不用 a - b，改用 Integer.compare(a, b) 或 (a > b) ? 1 : (a < b) ? -1 : 0
- 多条件排序：先按第一关键字，相同时再按第二关键字，每个条件单独用 Integer.compare 或 String.compareTo
- 设计比较器前先验证：是否满足自反性（a==a 返回0）、反对称性（a>b 则 b<a）、传递性

【自查清单】
□ 是否用了 a - b 比较 Integer？
□ 多条件排序的每个条件是否独立满足传递性？

---
content_type: 错题分析
tag: 整数溢出
title: 整数运算-WA-int溢出导致结果错误

【典型错误】
1. 两个较大 int 相加/相乘时超出 Integer.MAX_VALUE（约 2.1×10^9），结果变负
2. 递推公式中中间值溢出：如 dp[i] = dp[i-1] * 2 当 dp[i-1] 已接近 MAX_VALUE 时溢出
3. 数组长度或字符串长度做乘法时溢出：n * m 当 n=m=50000 时结果超 int 范围
4. 二分查找 (left + right) / 2 溢出（已在二分专项中说明）

【修正思路】
- 凡是涉及乘法或大数相加，优先改用 long：long result = (long) a * b
- 题目要求结果对 1e9+7 取模时，每次运算后立即取模：(a % MOD * b % MOD) % MOD
- 判断是否溢出：若 a > Integer.MAX_VALUE / b 则乘积溢出（乘法前预判）

【自查清单】
□ 中间计算结果是否可能超过 2×10^9？
□ 是否应该用 long 或取模？

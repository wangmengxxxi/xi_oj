content_type: 知识点
tag: 二分查找
title: 二分查找基础模板与核心思想

二分查找用于在有序数组中高效定位目标值，时间复杂度 O(log n)。每次将搜索范围减半。

最常用模板（左闭右闭区间 [left, right]）：
int left = 0, right = n - 1;
while (left <= right) {
    int mid = left + (right - left) / 2;  // 防止 (left+right) 溢出
    if (nums[mid] == target) return mid;
    else if (nums[mid] < target) left = mid + 1;
    else right = mid - 1;
}
return -1;

关键点：
1. 区间定义决定边界：左闭右闭用 <=；左闭右开用 <
2. mid 必须写 left + (right-left)/2，避免整数溢出
3. 循环结束后 left 是第一个大于 target 的位置，可用于插入位置问题

---
content_type: 知识点
tag: 二分查找
title: 二分查找变体-查找左右边界

当有序数组存在重复元素，需找第一个或最后一个等于 target 的位置。

查找左边界（第一个 >= target 的下标）：
int left = 0, right = n;
while (left < right) {
    int mid = left + (right - left) / 2;
    if (nums[mid] >= target) right = mid;
    else left = mid + 1;
}
return left;

查找右边界（最后一个 <= target 的下标）：
int left = 0, right = n;
while (left < right) {
    int mid = left + (right - left) / 2;
    if (nums[mid] <= target) left = mid + 1;
    else right = mid;
}
return left - 1;

应用场景：统计某值出现次数（右边界 - 左边界 + 1）；在旋转排序数组中查找。

---
content_type: 知识点
tag: 动态规划
title: 动态规划基础-状态定义与转移

动态规划（DP）的核心是将大问题分解为重叠子问题，用状态记录子问题的解，避免重复计算。

三步走框架：
1. 定义状态：dp[i] 表示什么（通常以「以第 i 个元素结尾」或「前 i 个元素」为单位）
2. 写转移方程：dp[i] 如何从 dp[i-1]、dp[i-2] 等推来
3. 确定初始值和遍历顺序

经典例子-最长递增子序列（LIS）：
dp[i] = 以 nums[i] 结尾的 LIS 长度
转移：dp[i] = max(dp[j] + 1)，其中 j < i 且 nums[j] < nums[i]
时间复杂度 O(n²)；配合二分可优化到 O(n log n)

常见误区：初始值设置错误；遍历方向反了（背包问题需特别注意）

---
content_type: 知识点
tag: 动态规划
title: 动态规划-0-1背包问题

0-1背包：有 n 件物品，背包容量为 W，每件物品只能选一次，求最大价值。

状态定义：dp[i][j] = 前 i 件物品在容量 j 下的最大价值
转移方程：
  不选第 i 件：dp[i][j] = dp[i-1][j]
  选第 i 件：  dp[i][j] = dp[i-1][j-w[i]] + v[i]  （需 j >= w[i]）
  取最大值：   dp[i][j] = max(两者)

空间优化（滚动数组，压缩为一维）：
for (int i = 0; i < n; i++)
    for (int j = W; j >= w[i]; j--)   // 必须从大到小遍历，防止同一物品选两次
        dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);

完全背包（每件物品可选无数次）：内层循环改为从小到大遍历。

---
content_type: 知识点
tag: 双指针
title: 双指针-对撞指针与快慢指针

双指针是用两个索引在数组或链表上协同移动，通常将 O(n²) 降为 O(n)。

对撞指针（两端向中间）：
适用于有序数组的两数之和、回文判断等。
int left = 0, right = n - 1;
while (left < right) {
    if (满足条件) { 处理; break; }
    else if (需要增大) left++;
    else right--;
}

快慢指针（同向，速度不同）：
适用于链表找环（Floyd 判环）、找链表中点、删除倒数第 k 个节点。
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) { /* 有环 */ }
}

---
content_type: 知识点
tag: 滑动窗口
title: 滑动窗口模板与适用场景

滑动窗口用于求满足条件的最长/最短连续子数组，核心是维护一个动态窗口 [left, right]。

通用模板：
int left = 0, right = 0, result = 0;
while (right < n) {
    // 1. 扩展右边界，将 nums[right] 加入窗口
    window.add(nums[right]);
    right++;
    // 2. 当窗口不满足条件时，收缩左边界
    while (窗口不满足条件) {
        window.remove(nums[left]);
        left++;
    }
    // 3. 更新答案
    result = Math.max(result, right - left);
}

适用场景：
- 无重复字符的最长子串（用 HashSet 维护窗口）
- 长度最小的子数组（和 >= target）
- 字符串的排列（固定窗口大小）

关键：明确「窗口不满足条件」的判断逻辑，以及何时更新答案。

---
content_type: 知识点
tag: 哈希表
title: 哈希表核心用法与常见场景

哈希表（HashMap/HashSet）利用 O(1) 的查找时间，将暴力 O(n²) 优化为 O(n)。

Java 常用操作：
map.getOrDefault(key, 0)       // 不存在时返回默认值，避免 NPE
map.put(key, map.getOrDefault(key, 0) + 1)  // 计数
map.containsKey(key)           // 判断存在

常见场景：
1. 两数之和：遍历时将已见元素存入 map，查找 target - nums[i]
2. 字符频率统计：int[] count = new int[26] 比 HashMap 更高效
3. 前缀和 + 哈希：map 存「前缀和 → 出现次数」，求子数组和等于 k 的个数
4. 记录元素首次出现位置：map 存「元素 → 下标」

注意：HashMap 不保证顺序；需要有序时用 TreeMap；需要插入顺序时用 LinkedHashMap。

---
content_type: 知识点
tag: 二叉树
title: 二叉树遍历-递归与迭代

二叉树三种遍历方式（前序/中序/后序）及层序遍历是高频考点。

递归模板（前序为例）：
void preorder(TreeNode node, List<Integer> res) {
    if (node == null) return;
    res.add(node.val);       // 前序：根-左-右
    preorder(node.left, res);
    preorder(node.right, res);
}
中序：先左后根后右；后序：先左后右后根。

迭代-前序（用栈）：
Deque<TreeNode> stack = new ArrayDeque<>();
stack.push(root);
while (!stack.isEmpty()) {
    TreeNode node = stack.pop();
    res.add(node.val);
    if (node.right != null) stack.push(node.right);  // 先压右再压左
    if (node.left  != null) stack.push(node.left);
}

层序遍历（BFS，用队列）：
Queue<TreeNode> queue = new LinkedList<>();
queue.offer(root);
while (!queue.isEmpty()) {
    int size = queue.size();  // 本层节点数
    for (int i = 0; i < size; i++) {
        TreeNode node = queue.poll();
        if (node.left  != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}

---
content_type: 知识点
tag: 递归与回溯
title: 回溯算法模板与剪枝策略

回溯是穷举 + 剪枝，用于求所有可能的解（排列、组合、子集、路径等）。

通用模板：
void backtrack(路径, 选择列表) {
    if (满足终止条件) {
        结果集.add(路径的拷贝);  // 注意要拷贝，不能直接 add 引用
        return;
    }
    for (选择 in 选择列表) {
        做选择;          // 将选择加入路径
        backtrack(路径, 选择列表);
        撤销选择;        // 恢复现场（回溯的核心）
    }
}

常见剪枝方式：
1. 排序后跳过重复元素：if (i > start && nums[i] == nums[i-1]) continue;
2. 当前路径已超出限制（如路径长度、总和）直接 return
3. used[] 数组标记已使用元素，避免排列中重复使用

---
content_type: 知识点
tag: 图论
title: 图论-BFS与DFS遍历模板

图的遍历是很多问题的基础（岛屿数量、最短路径、拓扑排序等）。

DFS（深度优先，递归）：
boolean[] visited = new boolean[n];
void dfs(int node) {
    visited[node] = true;
    for (int next : graph.get(node)) {
        if (!visited[next]) dfs(next);
    }
}

BFS（广度优先，找最短路径）：
Queue<Integer> queue = new LinkedList<>();
queue.offer(start);
visited[start] = true;
int dist = 0;
while (!queue.isEmpty()) {
    int size = queue.size();
    for (int i = 0; i < size; i++) {
        int node = queue.poll();
        if (node == target) return dist;
        for (int next : graph.get(node)) {
            if (!visited[next]) { visited[next] = true; queue.offer(next); }
        }
    }
    dist++;
}

网格图（二维数组）的 DFS/BFS：用方向数组 int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}} 遍历上下左右四个方向。

---
content_type: 知识点
tag: 单调栈
title: 单调栈-求下一个更大元素

单调栈维护一个单调递增或递减的栈，用于 O(n) 解决「下一个更大/更小元素」类问题。

求每个元素右侧第一个更大元素（单调递减栈）：
int[] result = new int[n];
Arrays.fill(result, -1);
Deque<Integer> stack = new ArrayDeque<>();  // 存下标
for (int i = 0; i < n; i++) {
    // 当前元素比栈顶大，栈顶元素找到了「下一个更大」
    while (!stack.isEmpty() && nums[i] > nums[stack.peek()]) {
        result[stack.pop()] = nums[i];
    }
    stack.push(i);
}

变体：
- 下一个更小元素：改为单调递增栈
- 每日温度、柱状图最大矩形、接雨水等经典题均使用此思路
- 循环数组：遍历两倍长度，用 i % n 取下标

---
content_type: 知识点
tag: 前缀和
title: 前缀和与差分数组

前缀和用于 O(1) 查询任意区间的元素之和，差分数组用于 O(1) 对区间批量加减。

一维前缀和：
int[] prefix = new int[n + 1];
for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + nums[i];
// 区间 [l, r] 的和 = prefix[r+1] - prefix[l]

前缀和 + 哈希（求子数组和等于 k 的个数）：
Map<Integer, Integer> map = new HashMap<>();
map.put(0, 1);  // 前缀和为0出现1次（空数组）
int sum = 0, count = 0;
for (int num : nums) {
    sum += num;
    count += map.getOrDefault(sum - k, 0);
    map.put(sum, map.getOrDefault(sum, 0) + 1);
}

差分数组（区间 [l, r] 所有元素加 val）：
diff[l] += val;
diff[r + 1] -= val;
最后对 diff 求前缀和还原原数组。

---
content_type: 知识点
tag: 贪心
title: 贪心算法-局部最优推全局最优

贪心每步选当前最优解，要求局部最优能推导全局最优（需证明或凭直觉+反例验证）。

经典贪心场景：
1. 区间调度（活动选择）：按结束时间排序，优先选结束最早的，选到的区间数最多
2. 跳跃游戏：维护当前能到达的最远位置 maxReach，遍历时判断能否继续延伸
3. 分糖果：先从左向右保证右边比左边多，再从右向左保证左边比右边多，取两次结果的最大值
4. 买卖股票（不限次数）：只要明天比今天贵就今天买，形成若干正收益的累加

注意事项：
- 贪心只适合特定问题，不能通用
- 与 DP 的区分：贪心每步不回头；DP 考虑所有子问题

---
content_type: 知识点
tag: 并查集
title: 并查集-路径压缩与按秩合并

并查集用于高效处理「连通性」问题（判断两点是否同属一个集合、合并集合）。

标准实现（路径压缩 + 按秩合并）：
int[] parent, rank;
void init(int n) {
    parent = new int[n];
    rank = new int[n];
    for (int i = 0; i < n; i++) parent[i] = i;
}
int find(int x) {
    if (parent[x] != x) parent[x] = find(parent[x]);  // 路径压缩
    return parent[x];
}
void union(int x, int y) {
    int px = find(x), py = find(y);
    if (px == py) return;
    if (rank[px] < rank[py]) { int t = px; px = py; py = t; }
    parent[py] = px;
    if (rank[px] == rank[py]) rank[px]++;
}

应用场景：判断图是否有环、统计连通分量数量、岛屿合并、朋友圈/省份数量。
时间复杂度：接近 O(1)（阿克曼函数反函数，极小）。

---
content_type: 知识点
tag: 排序
title: 常见排序算法复杂度与适用场景

快速排序：平均 O(n log n)，不稳定。OJ 中手写时需随机化 pivot 防止最坏情况（有序数组退化为 O(n²)）。
归并排序：稳定，O(n log n)，额外空间 O(n)。适合链表排序、求逆序对（归并过程中统计）。
堆排序：不稳定，O(n log n)，额外空间 O(1)。
计数/桶/基数排序：适合整数范围有限的场景，O(n + k)，绕过比较排序的 O(n log n) 下界。

Java Arrays.sort() 对基本类型用双轴快排（不稳定），对对象用 TimSort（稳定）。
自定义排序：Arrays.sort(arr, (a, b) -> a[0] - b[0])
注意：比较器必须满足传递性，否则可能抛 IllegalArgumentException。

---
content_type: 知识点
tag: 字符串
title: 字符串常用操作与KMP算法

Java 字符串常用操作：
str.toCharArray()           // 转字符数组，方便逐字符处理
str.charAt(i)               // 取第 i 个字符
str.substring(l, r)         // 截取 [l, r)，注意右开区间
String.valueOf(chars)       // char[] 转 String
new StringBuilder().append().toString()  // 拼接字符串，避免 + 在循环中产生大量对象

KMP 算法（线性时间字符串匹配）：
构建 next 数组（最长公共前后缀长度）：
int[] next = new int[pattern.length()];
for (int i = 1, j = 0; i < pattern.length(); i++) {
    while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = next[j - 1];
    if (pattern.charAt(i) == pattern.charAt(j)) j++;
    next[i] = j;
}
匹配时利用 next 数组避免回退，时间复杂度 O(n + m)。

回文相关：双指针从中心向外扩展，或预处理 Manacher 算法；判断回文用 reverse 或双指针。

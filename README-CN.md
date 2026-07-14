# Carpet RMS Addition

[![Modrinth](https://img.shields.io/modrinth/dt/ij9knTzG?label=Modrinth%E4%B8%8B%E8%BD%BD%E9%87%8F)](https://modrinth.com/project/ij9knTzG)

[English](README.md) | **简体中文**

一个 [Minecraft](https://www.minecraft.net/zh-hans) [Carpet模组](https://github.com/gnembon/fabric-carpet) [扩展](https://github.com/gnembon/fabric-carpet/wiki/List-of-Carpet-extensions).

## 规则

### autoUpdate

控制是否自动应用 Modrinth 上发现的新版本。无论此项如何设置，模组都**始终**在专用服务器上检查更新，并在有新版本时输出日志提醒。设为 `true` 时（仅 Linux 和 macOS），会将新版（校验 SHA-512 哈希后）下载到 mods 目录并备份运行中的 jar，下次重启生效。设为 `false`（默认），或在 Windows / 无法定位或替换 jar 时，提醒中只给出手动下载链接。

* 默认值：`false`
* 选项：`false`、`true`

### endPlatformBreakingBackport

移植 1.21 pre1 版本末地传送门生成黑曜石平台的过程中，在破坏方块时会掉落物品的行为。

* 默认值：`false`
* 选项：`false`，`true`

### enhancedDataGet

使 `/data get` 返回实体的 `inNetherPortal` 和船实体的 `ticksUnderwater`。

* 默认值：`false`
* 选项：`false`，`true`

### fakePlayerOfflineUUID

使用离线 UUID 生成假人，而不是查询 Mojang API。可防止 API 响应缓慢或无法访问时服务器卡顿。

* 可用版本：`<1.21.1`
* 默认值：`false`
* 选项：`false`，`true`

### fallingBlockBackport

将 1.18.2+ 的下落方块行为带回低版本。

* 可用版本：`<1.18.2`
* 默认值：`false`
* 选项：`false`，`true`

### fireballExplosionCreditBackport

移植 1.19.3 及以上版本的火球爆炸行为：在创建爆炸时，爆炸的击杀归因会记在火球自身（以及其拥有者）身上。

* 可用版本：`<1.19.3`
* 默认值：`false`
* 选项：`false`，`true`

### interceptAllPacketEntities

拦截列表中实体发送的所有包。这将会覆盖 `interceptUpdatePacketEntities`。在带宽受限时或许有用。

* 默认值：`[]`
* 示例用法：`[boat,minecraft:creeper]`

### interceptParticlePackets

拦截所有的粒子包。在带宽受限时或许有用。

* 默认值：`false`
* 选项：`false`，`true`

### interceptUpdatePacketEntities

拦截列表中实体发送的更新包（移动包和旋转包）。需注意的是，大多数非生物实体（例如矿车，TNT）也会在客户端被计算，所以启用此规则可能会导致客户端显示错误。在带宽受限时或许有用。

* 默认值：`[]`
* 示例用法：`[boat,minecraft:creeper]`

### naturalSpawnBlacklist

黑名单上的实体将不会自然生成。

* 默认值：`[]`
* 示例用法：`[cat,minecraft:creeper]`

### overrideMonsterBlockLightLevel

覆写传递给怪物生成逻辑的方块光照亮度。在设计光照抑制农场时或许有用。

* 默认值：`false`
* 选项：`false`，`0`，`1`，`2`，`3`，`4`，`5`，`6`，`7`，`8`，`9`，`10`，`11`，`12`，`13`，`14`，`15`

### overrideMonsterSkyLightLevel

覆写传递给怪物生成逻辑的天空光照亮度。在设计光照抑制农场时或许有用。

* 默认值：`false`
* 选项：`false`，`0`，`1`，`2`，`3`，`4`，`5`，`6`，`7`，`8`，`9`，`10`，`11`，`12`，`13`，`14`，`15`

### selfCheckOnPlacement

强制发射器、投掷器和音符盒在玩家放置时检查其方块状态。

> 当这些方块由玩家放置时，它们通常不会检查自己是否被红石信号触发，因此会保持在BUD态。
>
> 在 _trigger_ 模式下，它们在被玩家放置时会接收到一次NC更新，从而触发。
>
> 在 _silent_ 模式下，它们不会被触发，但会修正自己的方块状态。
>
> （在这两种模式下，音符盒的区别是：是否会播放声音。）

* 默认值：`false`
* 选项：`false`、`trigger`、`silent`

### separateMapModWorlds

向 VoxelMap 和 Xaero's Map 发送稳定的世界识别 ID，避免同一个服务器地址、相同种子、镜像服或不同生存服之间共享同一份地图数据。

* 默认值：`false`
* 选项：`false`、`auto`，或自定义世界识别字符串
* `auto`：在世界存档目录下生成并保存 `carpet-rms-addition-map-world-id.txt`
* 自定义值不能为空，且长度不能超过 255 个 UTF-8 字节

### usePortalBlacklist

黑名单上的实体将无法使用传送门。

* 默认值：`[]`

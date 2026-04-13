<template>
  <view class="agreement-container">
    <view class="header">
      <text class="title">{{ title }}</text>
    </view>
    <scroll-view scroll-y class="content-scroll">
      <text class="content-text">{{ content }}</text>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const title = ref('协议与政策')
const content = ref('')

// 模拟的服务协议文本
const serviceText = `【邻里帮帮服务协议】

欢迎您使用“邻里帮帮”社区互助平台！在您使用本服务前，请仔细阅读以下条款：

1. 服务说明
“邻里帮帮”为社区居民提供一个信息交流与互助的平台，包括但不限于跑腿代办、闲置物品流转、邻里提问等服务。本平台仅作为信息发布与对接的媒介。

2. 用户行为规范
(1) 用户注册需提供真实的身份信息及社区住址信息。
(2) 用户在平台发布的任何信息必须符合国家法律法规，不得发布涉黄、涉暴、诈骗等违法信息。
(3) 用户在线下进行互助或交易时，应自行核实对方身份，注意人身与财产安全。

3. 责任限制
对于因用户个人行为导致的任何线下纠纷、财产损失或人身伤害，本平台将尽力协助调查，但依法不承担连带赔偿责任。

4. 协议的修改
平台保留随时修改本协议的权利，修改后的协议将在平台内公示，继续使用本服务即视为接受修改后的条款。`

// 模拟的隐私政策文本
const privacyText = `【邻里帮帮隐私政策】

“邻里帮帮”深知个人信息对您的重要性，我们将按法律法规要求，采取相应安全保护措施，尽力保护您的个人信息安全可控。

1. 我们如何收集和使用您的个人信息
为了向您提供精准的同城/同社区互助服务，在您注册时，我们需要收集您的：手机号码、昵称、头像、所在社区及具体楼栋信息。

2. 位置信息的获取
当您使用“发布需求”或“查看附近的需求”功能时，我们会请求您的地理位置权限。如果您拒绝，将无法使用相关基于位置的匹配功能，但不影响您使用基础浏览功能。

3. 我们如何共享、转让、公开披露您的个人信息
(1) 共享：未经您同意，我们不会向任何第三方公司、组织和个人共享您的个人信息。互助接单成功后，仅向交易对方展示您的联系方式。
(2) 公开披露：我们仅会在法律法规要求或您明确同意的情况下，才会公开披露您的个人信息。

4. 您的权利
您有权随时访问、更正、删除您的个人信息，或注销您的平台账号。注销账号后，我们将停止为您提供服务，并删除您的个人信息。`

onLoad((options) => {
  const type = options.type
  
  if (type === 'service') {
    title.value = '邻里帮帮服务协议'
    content.value = serviceText
  } else if (type === 'privacy') {
    title.value = '邻里帮帮隐私政策'
    content.value = privacyText
  } else {
    // 默认情况（如从设置页点击进来），展示综合协议
    title.value = '用户协议与隐私政策'
    content.value = serviceText + '\n\n' + '--------------------------------' + '\n\n' + privacyText
  }
  
  // 动态设置当前页面的导航栏标题
  uni.setNavigationBarTitle({
    title: title.value
  })
})
</script>

<style scoped>
.agreement-container {
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 40rpx 30rpx 20rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #F0F0F0;
  z-index: 10;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  display: block;
}

.content-scroll {
  flex: 1;
  padding: 30rpx;
  box-sizing: border-box;
  /* 减去头部高度，确保内容可滚动 */
  height: calc(100vh - 120rpx);
}

.content-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  text-align: justify;
  white-space: pre-wrap; /* 保留文本中的换行符 */
  padding-bottom: 60rpx; /* 底部留白 */
}
</style>
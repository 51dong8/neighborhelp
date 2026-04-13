<template>
  <view class="service-detail-container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <view class="time">9:41</view>
      <view class="status-icons">
        <view class="signal-icon">📶</view>
        <view class="wifi-icon">📶</view>
        <view class="battery-icon">🔋</view>
      </view>
    </view>

    <!-- 顶部导航栏 -->
    <view class="top-nav">
      <view class="nav-left" @click="goBack">
        <view class="back-icon">←</view>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-center">
        <text class="page-title">{{ serviceInfo.name }}</text>
      </view>
      <view class="nav-right">
        <view class="help-btn" @click="showHelp">
          <text class="help-icon">❓</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 服务信息卡片 -->
      <view class="service-info-card">
        <view class="service-header">
          <view class="service-icon" :class="serviceInfo.iconClass">
            <text class="service-emoji">{{ serviceInfo.emoji }}</text>
          </view>
          <view class="service-details">
            <text class="service-title">{{ serviceInfo.name }}</text>
            <text class="service-desc">{{ serviceInfo.description }}</text>
          </view>
        </view>
      </view>

      <!-- AI 预填提示 -->
      <view v-if="aiPrefillInfo" class="ai-prefill-banner">
        <view class="ai-prefill-icon">🤖</view>
        <view class="ai-prefill-text">
          <text class="ai-prefill-title">已根据语音自动预填</text>
          <text class="ai-prefill-desc">{{ aiPrefillInfo.summary }}</text>
        </view>
        <view class="ai-prefill-action" @click="clearAIPrefill">清空</view>
      </view>

      <!-- 代收快递表单 -->
      <view v-if="serviceType === 'package'" class="form-section">
        <view class="section-title">快递信息</view>
        
        <!-- 取件位置 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">取件位置</text>
            <text class="required">*</text>
          </view>
          <view class="location-input-wrapper">
            <view class="input-icon">📍</view>
            <input 
              class="location-input"
              v-model="packageForm.pickupLocation"
              placeholder="请输入取件位置"
              @input="clearError('pickupLocation')"
            />
            <view class="location-btn" @click="getCurrentLocation">
              <text class="location-btn-text">获取位置</text>
            </view>
          </view>
          <view v-if="errors.pickupLocation" class="error-message">{{ errors.pickupLocation }}</view>
        </view>

        <!-- 取件码 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">取件码</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">🔢</view>
            <input 
              class="input-field"
              v-model="packageForm.pickupCode"
              placeholder="请输入取件码"
              @input="clearError('pickupCode')"
            />
          </view>
          <view v-if="errors.pickupCode" class="error-message">{{ errors.pickupCode }}</view>
        </view>

        <!-- 取件时间 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">取件时间</text>
            <text class="required">*</text>
          </view>
          <view class="time-picker" @click="showTimePicker">
            <view class="picker-icon">⏰</view>
            <view class="picker-content">
              <text class="picker-label">选择时间</text>
              <text class="picker-value">{{ packageForm.pickupTime || '请选择取件时间' }}</text>
            </view>
            <view class="picker-arrow">→</view>
          </view>
          <view v-if="errors.pickupTime" class="error-message">{{ errors.pickupTime }}</view>
        </view>

        <!-- 物品大小 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">物品大小</text>
            <text class="required">*</text>
          </view>
          <view class="size-options">
            <view 
              class="size-option" 
              v-for="size in sizeOptions" 
              :key="size.id"
              :class="{ active: packageForm.itemSize === size.id }"
              @click="selectSize(size.id)"
            >
              <view class="size-icon">{{ size.icon }}</view>
              <text class="size-text">{{ size.name }}</text>
              <text class="size-desc">{{ size.desc }}</text>
            </view>
          </view>
          <view v-if="errors.itemSize" class="error-message">{{ errors.itemSize }}</view>
        </view>



        <!-- 奖励设置 -->
        <view class="reward-section">
          <view class="section-title">奖励设置</view>
          
          <!-- 奖励类型选择 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">奖励类型</text>
              <text class="required">*</text>
            </view>
            <view class="reward-type-options">
              <view 
                class="reward-type-option" 
                :class="{ active: packageForm.rewardType === 'points' }"
                @click="selectRewardType('points')"
              >
                <view class="reward-type-icon">⭐</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">积分奖励</text>
                  <text class="reward-type-desc">获得积分，可用于兑换礼品</text>
                </view>
              </view>
              <view 
                class="reward-type-option" 
                :class="{ active: packageForm.rewardType === 'money' }"
                @click="selectRewardType('money')"
              >
                <view class="reward-type-icon">💰</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">现金报酬</text>
                  <text class="reward-type-desc">直接获得现金报酬</text>
                </view>
              </view>
            </view>
            <view v-if="errors.rewardType" class="error-message">{{ errors.rewardType }}</view>
          </view>

          <!-- 奖励金额输入 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">{{ packageForm.rewardType === 'points' ? '积分数量' : '报酬金额' }}</text>
              <text class="required">*</text>
            </view>
            <view class="reward-input-wrapper">
              <view class="input-icon">{{ packageForm.rewardType === 'points' ? '⭐' : '💰' }}</view>
              <input 
                class="reward-input"
                v-model="packageForm.rewardAmount"
                :placeholder="packageForm.rewardType === 'points' ? '请输入积分数量' : '请输入报酬金额'"
                type="number"
                @input="clearError('rewardAmount')"
              />
              <view class="reward-unit">{{ packageForm.rewardType === 'points' ? '积分' : '元' }}</view>
            </view>
            <view v-if="errors.rewardAmount" class="error-message">{{ errors.rewardAmount }}</view>
          </view>
        </view>
      </view>

      <!-- 照顾宠物表单 -->
      <view v-else-if="serviceType === 'pet'" class="form-section">
        <view class="section-title">宠物信息</view>
        
        <!-- 动物类型 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">动物类型</text>
            <text class="required">*</text>
          </view>
          <view class="animal-type-picker" @click="showAnimalTypePicker">
            <view class="picker-icon">🐾</view>
            <view class="picker-content">
              <text class="picker-label">选择动物类型</text>
              <text class="picker-value">{{ petForm.animalType || '请选择动物类型' }}</text>
            </view>
            <view class="picker-arrow">→</view>
          </view>
          <view v-if="errors.animalType" class="error-message">{{ errors.animalType }}</view>
        </view>

        <!-- 动物名字 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">动物名字</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">🏷️</view>
            <input 
              class="input-field"
              v-model="petForm.animalName"
              placeholder="请输入宠物的名字"
              @input="clearError('animalName')"
            />
          </view>
          <view v-if="errors.animalName" class="error-message">{{ errors.animalName }}</view>
        </view>

        <!-- 食物类型 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">喂食食物</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">🍽️</view>
            <input 
              class="input-field"
              v-model="petForm.foodType"
              placeholder="请输入喂食的食物类型"
              @input="clearError('foodType')"
            />
          </view>
          <view v-if="errors.foodType" class="error-message">{{ errors.foodType }}</view>
        </view>


        <!-- 收养时间段 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">收养时间段</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">⏰</view>
            <input 
              class="input-field"
              v-model="petForm.adoptionDate"
              placeholder="请输入收养时间段，如：2020年3月15日 14:30-16:00"
              @input="clearError('adoptionDate')"
            />
          </view>
          <view v-if="errors.adoptionDate" class="error-message">{{ errors.adoptionDate }}</view>
        </view>

        <!-- 收养地点 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">收养地点</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">📍</view>
            <input 
              class="input-field"
              v-model="petForm.adoptionLocation"
              placeholder="请输入收养地点，如：阳光宠物店、流浪动物救助站等"
              @input="clearError('adoptionLocation')"
            />
          </view>
          <view v-if="errors.adoptionLocation" class="error-message">{{ errors.adoptionLocation }}</view>
        </view>


        <!-- 主人微信 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">主人微信</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">💬</view>
            <input 
              class="input-field"
              v-model="petForm.ownerWechat"
              placeholder="请输入主人的微信号"
              @input="clearError('ownerWechat')"
            />
          </view>
          <view v-if="errors.ownerWechat" class="error-message">{{ errors.ownerWechat }}</view>
        </view>


        <!-- 奖励设置 -->
        <view class="reward-section">
          <view class="section-title">奖励设置</view>
          
          <!-- 奖励类型选择 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">奖励类型</text>
              <text class="required">*</text>
            </view>
            <view class="reward-type-options">
              <view 
                class="reward-type-option" 
                :class="{ active: petForm.rewardType === 'points' }"
                @click="selectRewardType('points')"
              >
                <view class="reward-type-icon">⭐</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">积分奖励</text>
                  <text class="reward-type-desc">获得积分，可用于兑换礼品</text>
                </view>
              </view>
              <view 
                class="reward-type-option" 
                :class="{ active: petForm.rewardType === 'money' }"
                @click="selectRewardType('money')"
              >
                <view class="reward-type-icon">💰</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">现金报酬</text>
                  <text class="reward-type-desc">直接获得现金报酬</text>
                </view>
              </view>
            </view>
            <view v-if="errors.rewardType" class="error-message">{{ errors.rewardType }}</view>
          </view>

          <!-- 奖励金额输入 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">{{ petForm.rewardType === 'points' ? '积分数量' : '报酬金额' }}</text>
              <text class="required">*</text>
            </view>
            <view class="reward-input-wrapper">
              <view class="input-icon">{{ petForm.rewardType === 'points' ? '⭐' : '💰' }}</view>
              <input 
                class="reward-input"
                v-model="petForm.rewardAmount"
                :placeholder="petForm.rewardType === 'points' ? '请输入积分数量' : '请输入报酬金额'"
                type="number"
                @input="clearError('rewardAmount')"
              />
              <view class="reward-unit">{{ petForm.rewardType === 'points' ? '积分' : '元' }}</view>
            </view>
            <view v-if="errors.rewardAmount" class="error-message">{{ errors.rewardAmount }}</view>
          </view>
        </view>
      </view>

      <!-- 家电维修表单 -->
      <view v-else-if="serviceType === 'repair'" class="form-section">
        <view class="section-title">家电维修信息</view>
        
        <!-- 家电类型 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">家电类型</text>
            <text class="required">*</text>
          </view>
          <view class="appliance-type-picker" @click="showApplianceTypePicker">
            <view class="picker-icon">🔧</view>
            <view class="picker-content">
              <text class="picker-label">选择家电类型</text>
              <text class="picker-value">{{ repairForm.applianceType || '请选择家电类型' }}</text>
            </view>
            <view class="picker-arrow">→</view>
          </view>
          <view v-if="errors.applianceType" class="error-message">{{ errors.applianceType }}</view>
        </view>

        <!-- 损坏情况 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">损坏情况</text>
            <text class="required">*</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="repairForm.damageDescription"
              placeholder="请详细描述家电的损坏情况，如：不制冷、有异响、无法启动等"
              maxlength="300"
              @input="clearError('damageDescription')"
            ></textarea>
            <view class="char-count">{{ repairForm.damageDescription.length }}/300</view>
          </view>
          <view v-if="errors.damageDescription" class="error-message">{{ errors.damageDescription }}</view>
        </view>

        <!-- 购买时间 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">购买时间</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">📅</view>
            <input 
              class="input-field"
              v-model="repairForm.purchaseTime"
              placeholder="如：2022年3月"
              @input="clearError('purchaseTime')"
            />
          </view>
        </view>

        <!-- 品牌型号 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">品牌型号</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">🏷️</view>
            <input 
              class="input-field"
              v-model="repairForm.brandModel"
              placeholder="如：格力空调KFR-35GW"
              @input="clearError('brandModel')"
            />
          </view>
        </view>

        <!-- 微信号 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">微信号</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">💬</view>
            <input 
              class="input-field"
              v-model="repairForm.wechat"
              placeholder="请输入微信号"
              @input="clearError('wechat')"
            />
          </view>
          <view v-if="errors.wechat" class="error-message">{{ errors.wechat }}</view>
        </view>


        <!-- 价格协商 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">价格协商</text>
          </view>
          <view class="price-negotiation">
            <view class="negotiation-info">
              <view class="negotiation-icon">💰</view>
              <view class="negotiation-content">
                <text class="negotiation-title">线下协商价格</text>
                <text class="negotiation-desc">维修师傅上门检查后，根据实际情况协商维修费用</text>
              </view>
            </view>
          </view>
        </view>

        <!-- AR 技能指导（仅前端引导，不调用接口） -->
        <view class="ar-skill-card">
          <view class="ar-skill-header">
            <view class="ar-skill-left">
              <view class="ar-skill-icon">🛠️</view>
              <view class="ar-skill-text">
                <text class="ar-skill-title">AR 技能指导</text>
                <text class="ar-skill-subtitle">用摄像头对准家电，按步骤自助排查</text>
              </view>
            </view>
            <button class="ar-skill-btn" @click="openARGuide">AR 指导</button>
          </view>
          <view class="ar-skill-footer">
            <text class="ar-skill-tip">
              已支持空调、洗衣机、冰箱等常见设备的小故障自助检查（仅体验版）
            </text>
          </view>
        </view>

        <!-- 备注信息 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">备注信息</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="repairForm.notes"
              placeholder="请填写其他需要说明的信息，如：上门时间偏好、特殊要求等"
              maxlength="200"
            ></textarea>
            <view class="char-count">{{ repairForm.notes.length }}/200</view>
          </view>
        </view>

      </view>

      <!-- 帮我买菜表单 -->
      <view v-else-if="serviceType === 'shopping'" class="form-section">
        <view class="section-title">买菜信息</view>
        
        <!-- 所需菜名及数量 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">所需菜名及数量</text>
            <text class="required">*</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="shoppingForm.groceryList"
              placeholder="请详细列出所需菜品及数量，如：&#10;白菜 2斤&#10;土豆 3斤&#10;西红柿 1斤&#10;鸡蛋 1盒"
              maxlength="500"
              @input="clearError('groceryList')"
            ></textarea>
            <view class="char-count">{{ shoppingForm.groceryList.length }}/500</view>
          </view>
          <view v-if="errors.groceryList" class="error-message">{{ errors.groceryList }}</view>
        </view>

        <!-- 客户地址 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">客户地址</text>
            <text class="required">*</text>
          </view>
          <view class="location-input-wrapper">
            <view class="input-icon">📍</view>
            <input 
              class="location-input"
              v-model="shoppingForm.customerAddress"
              placeholder="请输入详细地址"
              @input="clearError('customerAddress')"
            />
            <view class="location-btn" @click="getCurrentLocation">
              <text class="location-btn-text">获取位置</text>
            </view>
          </view>
          <view v-if="errors.customerAddress" class="error-message">{{ errors.customerAddress }}</view>
        </view>

        <!-- 客户电话 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">客户电话</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">📞</view>
            <input 
              class="input-field"
              v-model="shoppingForm.customerPhone"
              placeholder="请输入手机号码"
              @input="clearError('customerPhone')"
            />
          </view>
          <view v-if="errors.customerPhone" class="error-message">{{ errors.customerPhone }}</view>
        </view>

        <!-- 客户微信 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">客户微信</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">💬</view>
            <input 
              class="input-field"
              v-model="shoppingForm.customerWechat"
              placeholder="请输入微信号"
              @input="clearError('customerWechat')"
            />
          </view>
          <view v-if="errors.customerWechat" class="error-message">{{ errors.customerWechat }}</view>
        </view>

        <!-- 交易方式 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">交易方式</text>
          </view>
          <view class="transaction-method">
            <view class="transaction-info">
              <view class="transaction-icon">💰</view>
              <view class="transaction-content">
                <text class="transaction-title">线下协商交易</text>
                <text class="transaction-desc">买菜完成后，根据实际购买情况协商费用，支持现金或微信支付</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 备注信息 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">备注信息</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="shoppingForm.notes"
              placeholder="请填写其他需要说明的信息，如：菜品质量要求、购买时间偏好、特殊要求等"
              maxlength="200"
            ></textarea>
            <view class="char-count">{{ shoppingForm.notes.length }}/200</view>
          </view>
        </view>

        <!-- 奖励设置 -->
        <view class="reward-section">
          <view class="section-title">奖励设置</view>
          
          <!-- 奖励类型选择 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">奖励类型</text>
              <text class="required">*</text>
            </view>
            <view class="reward-type-options">
              <view 
                class="reward-type-option" 
                :class="{ active: shoppingForm.rewardType === 'points' }"
                @click="selectRewardType('points')"
              >
                <view class="reward-type-icon">⭐</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">积分奖励</text>
                  <text class="reward-type-desc">获得积分，可用于兑换礼品</text>
                </view>
              </view>
              <view 
                class="reward-type-option" 
                :class="{ active: shoppingForm.rewardType === 'money' }"
                @click="selectRewardType('money')"
              >
                <view class="reward-type-icon">💰</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">现金报酬</text>
                  <text class="reward-type-desc">直接获得现金报酬</text>
                </view>
              </view>
            </view>
            <view v-if="errors.rewardType" class="error-message">{{ errors.rewardType }}</view>
          </view>

          <!-- 奖励金额输入 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">{{ shoppingForm.rewardType === 'points' ? '积分数量' : '报酬金额' }}</text>
              <text class="required">*</text>
            </view>
            <view class="reward-input-wrapper">
              <view class="input-icon">{{ shoppingForm.rewardType === 'points' ? '⭐' : '💰' }}</view>
              <input 
                class="reward-input"
                v-model="shoppingForm.rewardAmount"
                :placeholder="shoppingForm.rewardType === 'points' ? '请输入积分数量' : '请输入报酬金额'"
                type="number"
                @input="clearError('rewardAmount')"
              />
              <view class="reward-unit">{{ shoppingForm.rewardType === 'points' ? '积分' : '元' }}</view>
            </view>
            <view v-if="errors.rewardAmount" class="error-message">{{ errors.rewardAmount }}</view>
          </view>
        </view>
      </view>

      <!-- 帮我清洁表单 -->
      <view v-else-if="serviceType === 'cleaning'" class="form-section">
        <view class="section-title">清洁信息</view>
        
        <!-- 需要清洁什么 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">需要清洁什么</text>
            <text class="required">*</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="cleaningForm.cleaningItems"
              placeholder="请详细描述需要清洁的内容，如：&#10;客厅：沙发、茶几、电视柜&#10;厨房：灶台、抽油烟机、橱柜&#10;卫生间：马桶、洗手台、淋浴房"
              maxlength="400"
              @input="clearError('cleaningItems')"
            ></textarea>
            <view class="char-count">{{ cleaningForm.cleaningItems.length }}/400</view>
          </view>
          <view v-if="errors.cleaningItems" class="error-message">{{ errors.cleaningItems }}</view>
        </view>


        <!-- 预估价格 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">预估价格</text>
            <text class="required">*</text>
          </view>
          <view class="price-input-wrapper">
            <view class="price-input-container">
              <input 
                class="price-input"
                v-model="cleaningForm.estimatedPrice"
                placeholder="请输入预估价格"
                type="number"
                @input="clearError('estimatedPrice')"
              />
              <text class="price-unit">元/小时</text>
            </view>
            <view class="price-tips">
              <text class="tips-text">💡 建议参考：轻微20-30元/小时，一般30-50元/小时，严重50-80元/小时</text>
            </view>
          </view>
          <view v-if="errors.estimatedPrice" class="error-message">{{ errors.estimatedPrice }}</view>
        </view>

        <!-- 清洁时间 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">清洁时间</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <view class="input-icon">⏰</view>
            <input 
              class="input-field"
              v-model="cleaningForm.cleaningTime"
              placeholder="请输入具体时间，如：2025年9月20日 14:30"
              @input="clearError('cleaningTime')"
            />
          </view>
          <view v-if="errors.cleaningTime" class="error-message">{{ errors.cleaningTime }}</view>
        </view>

        <!-- 备注信息 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">备注信息</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="cleaningForm.notes"
              placeholder="请填写其他需要说明的信息，如：特殊清洁要求、清洁用品偏好、注意事项等"
              maxlength="200"
            ></textarea>
            <view class="char-count">{{ cleaningForm.notes.length }}/200</view>
          </view>
        </view>

        <!-- 奖励设置 -->
        <view class="reward-section">
          <view class="section-title">奖励设置</view>
          
          <!-- 奖励类型选择 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">奖励类型</text>
              <text class="required">*</text>
            </view>
            <view class="reward-type-options">
              <view 
                class="reward-type-option" 
                :class="{ active: cleaningForm.rewardType === 'points' }"
                @click="selectRewardType('points')"
              >
                <view class="reward-type-icon">⭐</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">积分奖励</text>
                  <text class="reward-type-desc">获得积分，可用于兑换礼品</text>
                </view>
              </view>
              <view 
                class="reward-type-option" 
                :class="{ active: cleaningForm.rewardType === 'money' }"
                @click="selectRewardType('money')"
              >
                <view class="reward-type-icon">💰</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">现金报酬</text>
                  <text class="reward-type-desc">直接获得现金报酬</text>
                </view>
              </view>
            </view>
            <view v-if="errors.rewardType" class="error-message">{{ errors.rewardType }}</view>
          </view>

          <!-- 奖励金额输入 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">{{ cleaningForm.rewardType === 'points' ? '积分数量' : '报酬金额' }}</text>
              <text class="required">*</text>
            </view>
            <view class="reward-input-wrapper">
              <view class="input-icon">{{ cleaningForm.rewardType === 'points' ? '⭐' : '💰' }}</view>
              <input 
                class="reward-input"
                v-model="cleaningForm.rewardAmount"
                :placeholder="cleaningForm.rewardType === 'points' ? '请输入积分数量' : '请输入报酬金额'"
                type="number"
                @input="clearError('rewardAmount')"
              />
              <view class="reward-unit">{{ cleaningForm.rewardType === 'points' ? '积分' : '元' }}</view>
            </view>
            <view v-if="errors.rewardAmount" class="error-message">{{ errors.rewardAmount }}</view>
          </view>
        </view>
      </view>

      <!-- 帮我做美食表单 -->
      <view v-else-if="serviceType === 'cooking'" class="form-section">
        <view class="section-title">美食信息</view>
        
        <!-- 美食名称 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">美食名称</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field"
              v-model="cookingForm.dishName"
              placeholder="请输入想要制作的美食名称，如：红烧肉、宫保鸡丁、糖醋排骨等"
              @input="clearError('dishName')"
            />
          </view>
          <view v-if="errors.dishName" class="error-message">{{ errors.dishName }}</view>
        </view>

        <!-- 时间地点 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">时间地点</text>
            <text class="required">*</text>
          </view>
          <view class="time-location-wrapper">
            <view class="time-section">
              <view class="section-label">
                <text class="label-icon">⏰</text>
                <text class="label-text">制作时间</text>
              </view>
              <view class="time-input-container">
                <input 
                  class="time-input"
                  v-model="cookingForm.cookingTime"
                  placeholder="如：今天下午2点"
                  @input="clearError('cookingTime')"
                />
              </view>
            </view>
            <view class="location-section">
              <view class="section-label">
                <text class="label-icon">📍</text>
                <text class="label-text">制作地点</text>
              </view>
              <view class="location-input-container">
                <input 
                  class="location-input"
                  v-model="cookingForm.cookingLocation"
                  placeholder="请输入制作地点"
                  @input="clearError('cookingLocation')"
                />
                <view class="location-btn" @click="getCurrentLocation">
                  <text class="location-btn-text">获取位置</text>
                </view>
              </view>
            </view>
          </view>
          <view v-if="errors.cookingTime" class="error-message">{{ errors.cookingTime }}</view>
          <view v-if="errors.cookingLocation" class="error-message">{{ errors.cookingLocation }}</view>
        </view>

        <!-- 联系电话 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">联系电话</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field"
              v-model="cookingForm.phone"
              placeholder="请输入您的联系电话"
              type="number"
              @input="clearError('phone')"
            />
          </view>
          <view v-if="errors.phone" class="error-message">{{ errors.phone }}</view>
        </view>

        <!-- 微信号 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">微信号</text>
            <text class="required">*</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field"
              v-model="cookingForm.wechat"
              placeholder="请输入您的微信号"
              @input="clearError('wechat')"
            />
          </view>
          <view v-if="errors.wechat" class="error-message">{{ errors.wechat }}</view>
        </view>

        <!-- 预估价格 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">预估价格</text>
            <text class="required">*</text>
          </view>
          <view class="price-input-wrapper">
            <view class="price-input-container">
              <input 
                class="price-input"
                v-model="cookingForm.estimatedPrice"
                placeholder="请输入预估价格"
                type="number"
                @input="clearError('estimatedPrice')"
              />
              <text class="price-unit">元</text>
            </view>
            <view class="price-tips">
              <text class="tips-text">💡 建议参考：家常菜30-50元，特色菜50-100元，宴席菜100-200元</text>
            </view>
          </view>
          <view v-if="errors.estimatedPrice" class="error-message">{{ errors.estimatedPrice }}</view>
        </view>

        <!-- 美食要求 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">美食要求</text>
          </view>
          <view class="textarea-wrapper">
            <textarea 
              class="textarea-field"
              v-model="cookingForm.requirements"
              placeholder="请填写其他要求，如：口味偏好、食材要求、制作方式、特殊需求等"
              maxlength="300"
            ></textarea>
            <view class="char-count">{{ cookingForm.requirements.length }}/300</view>
          </view>
        </view>

        <!-- 奖励设置 -->
        <view class="reward-section">
          <view class="section-title">奖励设置</view>
          
          <!-- 奖励类型选择 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">奖励类型</text>
              <text class="required">*</text>
            </view>
            <view class="reward-type-options">
              <view 
                class="reward-type-option" 
                :class="{ active: cookingForm.rewardType === 'points' }"
                @click="selectRewardType('points')"
              >
                <view class="reward-type-icon">⭐</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">积分奖励</text>
                  <text class="reward-type-desc">获得积分，可用于兑换礼品</text>
                </view>
              </view>
              <view 
                class="reward-type-option" 
                :class="{ active: cookingForm.rewardType === 'money' }"
                @click="selectRewardType('money')"
              >
                <view class="reward-type-icon">💰</view>
                <view class="reward-type-info">
                  <text class="reward-type-name">现金报酬</text>
                  <text class="reward-type-desc">直接获得现金报酬</text>
                </view>
              </view>
            </view>
            <view v-if="errors.rewardType" class="error-message">{{ errors.rewardType }}</view>
          </view>

          <!-- 奖励金额输入 -->
          <view class="input-group">
            <view class="input-label">
              <text class="label-text">{{ cookingForm.rewardType === 'points' ? '积分数量' : '报酬金额' }}</text>
              <text class="required">*</text>
            </view>
            <view class="reward-input-wrapper">
              <view class="input-icon">{{ cookingForm.rewardType === 'points' ? '⭐' : '💰' }}</view>
              <input 
                class="reward-input"
                v-model="cookingForm.rewardAmount"
                :placeholder="cookingForm.rewardType === 'points' ? '请输入积分数量' : '请输入报酬金额'"
                type="number"
                @input="clearError('rewardAmount')"
              />
              <view class="reward-unit">{{ cookingForm.rewardType === 'points' ? '积分' : '元' }}</view>
            </view>
            <view v-if="errors.rewardAmount" class="error-message">{{ errors.rewardAmount }}</view>
          </view>
        </view>
      </view>

      <!-- 其他服务类型表单 -->
      <view v-else class="form-section">
        <view class="section-title">{{ serviceInfo.name }}信息</view>
        <view class="coming-soon">
          <view class="coming-soon-icon">🚧</view>
          <text class="coming-soon-text">该服务类型的详细信息填写功能正在开发中</text>
          <text class="coming-soon-desc">敬请期待！</text>
        </view>
      </view>

    </view>

    <!-- 提交按钮 -->
    <view class="submit-container">
      <button 
        class="submit-btn" 
        :class="{ disabled: !canSubmit, loading: isSubmitting }"
        @click="submitRequest"
        :disabled="!canSubmit || isSubmitting"
      >
        <view class="submit-content">
          <text class="submit-text" v-if="!isSubmitting">发布需求</text>
          <view class="loading-spinner" v-else></view>
          <text class="submit-text" v-if="isSubmitting">发布中...</text>
        </view>
      </button>
    </view>

    <!-- AR 指导浮层 -->
    <view v-if="showARGuide" class="ar-layer">
      <view class="ar-mask" @click="closeARGuide"></view>
      <view class="ar-panel">
        <view class="ar-top">
          <view class="ar-top-left">
            <text class="ar-title">AR 技能指导 · {{ arTutorial?.deviceName || '家电' }}</text>
            <text class="ar-subtitle">
              第 {{ arCurrentStepIndex + 1 }}/{{ arTutorial.steps.length }} 步 · {{ arTutorial.scene }}
            </text>
          </view>
          <view class="ar-close-btn" @click="closeARGuide">✕</view>
        </view>

        <!-- 摄像头取景示意区域（不接真实相机，仅前端占位） -->
        <view class="ar-camera">
          <view class="ar-camera-placeholder">
            <text class="ar-camera-text">
              请将手机对准：{{ currentARStep.cameraTarget }}
            </text>
          </view>

          <!-- 简单高亮框模拟 AR 标注 -->
          <view class="ar-highlight" :class="'ar-highlight-' + currentARStep.highlightArea">
            <text class="ar-highlight-label">{{ currentARStep.highlightLabel }}</text>
          </view>
        </view>

        <!-- 步骤文案 -->
        <view class="ar-step-text">
          <text class="ar-step-title">{{ currentARStep.title }}</text>
          <text class="ar-step-desc">{{ currentARStep.description }}</text>
          <text class="ar-step-hint">{{ currentARStep.hint }}</text>
          <text v-if="arTutorial.safety" class="ar-step-safety">安全提示：{{ arTutorial.safety }}</text>
        </view>

        <!-- 步骤切换按钮 -->
        <view class="ar-actions">
          <button
            class="ar-action ghost"
            :disabled="arCurrentStepIndex === 0"
            @click="prevARStep"
          >
            上一步
          </button>
          <button
            class="ar-action primary"
            @click="nextARStep"
          >
            {{ arCurrentStepIndex === arTutorial.steps.length - 1 ? '完成自查' : '下一步' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { createRequest } from '@/api/request'
import { getServiceTypeDetail } from '@/api/service'

// 响应式数据
const serviceType = ref('')
const isSubmitting = ref(false)
const aiPrefillInfo = ref(null)
const apiServiceDetail = ref(null)

// 代收快递表单数据
const packageForm = ref({
  pickupLocation: '',
  pickupCode: '',
  pickupTime: '',
  itemSize: '',
  rewardType: '',
  rewardAmount: ''
})

// 照顾宠物表单数据
const petForm = ref({
  animalType: '',
  animalName: '',
  foodType: '',
  adoptionDate: '',
  adoptionLocation: '',
  ownerWechat: '',
  rewardType: '',
  rewardAmount: ''
})

// 家电维修表单数据
const repairForm = ref({
  applianceType: '',
  damageDescription: '',
  purchaseTime: '',
  brandModel: '',
  wechat: '',
  notes: ''
})

// 帮我买菜表单数据
const shoppingForm = ref({
  groceryList: '',
  customerAddress: '',
  customerPhone: '',
  customerWechat: '',
  notes: '',
  rewardType: '',
  rewardAmount: ''
})

// 帮我清洁表单数据
const cleaningForm = ref({
  cleaningItems: '',
  estimatedPrice: '',
  cleaningTime: '',
  notes: '',
  rewardType: '',
  rewardAmount: ''
})

// 帮我做美食表单数据
const cookingForm = ref({
  dishName: '',
  cookingTime: '',
  cookingLocation: '',
  phone: '',
  wechat: '',
  estimatedPrice: '',
  requirements: '',
  rewardType: '',
  rewardAmount: ''
})

// ======= 新增：用于存储地图选点的真实经纬度 =======
const locationCoords = ref({
  longitude: null,
  latitude: null
})

// AR 技能指导状态（前端模拟）
const showARGuide = ref(false)
const arTutorial = ref(null) // { deviceName, scene, safety, steps: [...] }
const arCurrentStepIndex = ref(0)

// 错误信息
const errors = ref({
  pickupLocation: '',
  pickupCode: '',
  pickupTime: '',
  itemSize: '',
  animalType: '',
  animalName: '',
  foodType: '',
  adoptionDate: '',
  adoptionLocation: '',
  ownerWechat: '',
  applianceType: '',
  damageDescription: '',
  wechat: '',
  groceryList: '',
  customerAddress: '',
  customerPhone: '',
  customerWechat: '',
  cleaningItems: '',
  estimatedPrice: '',
  cleaningTime: '',
  dishName: '',
  cookingTime: '',
  cookingLocation: '',
  phone: '',
  wechat: '',
  rewardType: '',
  rewardAmount: ''
})

// 服务信息映射
const serviceInfoMap = {
  package: {
    name: '帮我取快递',
    emoji: '📦',
    iconClass: 'package-icon',
    description: '帮忙代收快递包裹，安全保管'
  },
  pet: {
    name: '帮我照顾宠物',
    emoji: '🐕',
    iconClass: 'pet-icon',
    description: '照顾宠物日常护理，陪伴玩耍'
  },
  repair: {
    name: '帮我家电维修',
    emoji: '🔧',
    iconClass: 'repair-icon',
    description: '家电故障维修，专业技术服务'
  },
  shopping: {
    name: '帮我买菜',
    emoji: '🛒',
    iconClass: 'shopping-icon',
    description: '帮忙购买生活用品，新鲜送达'
  },
  cleaning: {
    name: '帮我家清洁',
    emoji: '🧹',
    iconClass: 'cleaning-icon',
    description: '家庭清洁整理，专业家政服务'
  },
  cooking: {
    name: '帮我做美食',
    emoji: '🍳',
    iconClass: 'cooking-icon',
    description: '代做各种美食料理，美味可口'
  }
}

// 物品大小选项
const sizeOptions = ref([
  { id: 'small', name: '小件', icon: '📱', desc: '' },
  { id: 'medium', name: '中件', icon: '📦', desc: '' },
  { id: 'large', name: '大件', icon: '📦', desc: '' },
  { id: 'oversized', name: '超大件', icon: '📦', desc: '' }
])

// 物品重量选项

// 动物类型选项
const animalTypes = ref([
  '猫类',
  '狗类', 
  '乌龟类',
  '小鸟类',
  '鱼类',
  '其他类'
])

// 喂食频率选项

// 家电类型选项
const applianceTypes = ref([
  '空调',
  '冰箱',
  '洗衣机',
  '电视',
  '热水器',
  '微波炉',
  '电饭煲',
  '抽油烟机',
  '燃气灶',
  '其他家电'
])

// 脏的程度选项

// 计算属性
const serviceInfo = computed(() => {
  const base = serviceInfoMap[serviceType.value] || serviceInfoMap.package
  const d = apiServiceDetail.value
  if (!d) return base
  return {
    ...base,
    name: d.name || base.name,
    description: d.description || base.description,
    emoji: typeof d.icon === 'string' && d.icon ? d.icon : base.emoji,
    iconClass: d.iconClass || base.iconClass
  }
})

const currentARStep = computed(() => {
  if (!arTutorial.value || !arTutorial.value.steps?.length) {
    return {
      title: '',
      description: '',
      hint: '',
      cameraTarget: '',
      highlightArea: 'center',
      highlightLabel: ''
    }
  }
  return arTutorial.value.steps[arCurrentStepIndex.value] || arTutorial.value.steps[0]
})

const canSubmit = computed(() => {
  if (serviceType.value === 'package') {
    return packageForm.value.pickupLocation.trim() !== '' &&
           packageForm.value.pickupCode.trim() !== '' &&
           packageForm.value.pickupTime.trim() !== '' &&
           packageForm.value.itemSize !== ''
  } else if (serviceType.value === 'pet') {
    return petForm.value.animalType.trim() !== '' &&
           petForm.value.animalName.trim() !== '' &&
           petForm.value.foodType.trim() !== '' &&
           petForm.value.adoptionDate.trim() !== '' &&
           petForm.value.adoptionLocation.trim() !== '' &&
           petForm.value.ownerWechat.trim() !== ''
  } else if (serviceType.value === 'repair') {
    return repairForm.value.applianceType.trim() !== '' &&
           repairForm.value.damageDescription.trim() !== '' &&
           repairForm.value.wechat.trim() !== ''
  } else if (serviceType.value === 'shopping') {
    return shoppingForm.value.groceryList.trim() !== '' &&
           shoppingForm.value.customerAddress.trim() !== '' &&
           shoppingForm.value.customerPhone.trim() !== '' &&
           shoppingForm.value.customerWechat.trim() !== ''
  } else if (serviceType.value === 'cleaning') {
    return cleaningForm.value.cleaningItems.trim() !== '' &&
           cleaningForm.value.estimatedPrice.trim() !== '' &&
           cleaningForm.value.cleaningTime.trim() !== ''
  } else if (serviceType.value === 'cooking') {
    return cookingForm.value.dishName.trim() !== '' &&
           cookingForm.value.cookingTime.trim() !== '' &&
           cookingForm.value.cookingLocation.trim() !== '' &&
           cookingForm.value.phone.trim() !== '' &&
           cookingForm.value.wechat.trim() !== '' &&
           cookingForm.value.estimatedPrice.trim() !== ''
  }
  return false
})

// 方法
const goBack = () => {
  uni.navigateBack()
}

const showHelp = () => {
  uni.showModal({
    title: '填写帮助',
    content: '请详细填写快递信息，包括取件位置、取件码、时间等，以便邻居更好地帮助你',
    showCancel: false
  })
}

// AR 技能指导：根据家电类型/品牌/故障描述生成本地教程
const buildARTutorial = () => {
  const type = repairForm.value.applianceType || ''
  const brandModel = (repairForm.value.brandModel || '').toLowerCase()
  const desc = repairForm.value.damageDescription || ''

  // 设备名称
  let deviceName = type || '家电'
  if (!deviceName && brandModel.includes('kfr')) deviceName = '空调'

  // 空调场景
  if (type.includes('空调') || brandModel.includes('kfr')) {
    return {
      deviceName: deviceName || '空调',
      scene: '空调不制冷/制冷差',
      safety: '排查前请先确认电源插头牢固，严禁私自拆开电源部分。',
      steps: [
        {
          title: '第一步：检查滤网是否堵塞',
          description: '将摄像头对准空调面板上方出风口，找到面板卡扣位置，打开前面板，观察滤网是否被灰尘严重堵住。',
          hint: '若滤网很脏，可取下用清水清洗并晾干后装回，再测试制冷效果。',
          cameraTarget: '空调正面的出风口与上方面板',
          highlightArea: 'panel',
          highlightLabel: '滤网面板'
        },
        {
          title: '第二步：检查遥控器模式是否为“制冷”',
          description: '将摄像头对准遥控器屏幕区域，确认模式图标是否为雪花（制冷模式），温度设置是否低于室温。',
          hint: '若模式不是雪花，请切换到制冷模式，并将温度调至 24℃ 左右再次尝试。',
          cameraTarget: '空调遥控器的显示屏和模式按键区域',
          highlightArea: 'remote-screen',
          highlightLabel: '模式/温度'
        },
        {
          title: '第三步：观察室外机运行与周边环境',
          description: '站在安全位置，用摄像头远远对准室外机，留意风扇是否转动、是否有明显运行声音，并确认室外机周围没有被杂物挡住。',
          hint: '若室外机完全不转或有明显异响，请停止自助排查，发起专业维修需求。',
          cameraTarget: '室外机整体位置（注意安全，不要攀爬）',
          highlightArea: 'outdoor',
          highlightLabel: '室外机'
        }
      ]
    }
  }

  // 洗衣机场景
  if (type.includes('洗衣机')) {
    return {
      deviceName: '洗衣机',
      scene: '洗衣机不排水/不脱水',
      safety: '请勿在插电状态下触摸裸露金属部位，必要时先断电再排查。',
      steps: [
        {
          title: '第一步：检查排水管是否弯折/堵塞',
          description: '将摄像头对准洗衣机背面或侧面的排水管，确认是否被压住、打结或插得过高。',
          hint: '适当理顺排水管，并确保排水口畅通，再重新尝试排水/脱水程序。',
          cameraTarget: '洗衣机背部或侧面的排水管位置',
          highlightArea: 'pipe',
          highlightLabel: '排水管'
        },
        {
          title: '第二步：检查过滤网/绒毛收集器',
          description: '打开洗衣机门，用摄像头对准桶内或侧边的小滤网位置，查看是否被杂物堵塞。',
          hint: '取出过滤网清理绒毛和杂物，装回后再次尝试运行。',
          cameraTarget: '洗衣机内桶边缘或门框附近的小滤网区域',
          highlightArea: 'filter',
          highlightLabel: '过滤网'
        }
      ]
    }
  }

  // 冰箱场景
  if (type.includes('冰箱')) {
    return {
      deviceName: '冰箱',
      scene: '冰箱制冷效果差/结霜严重',
      safety: '清理时避免用硬物敲打蒸发器，防止冷媒泄漏。',
      steps: [
        {
          title: '第一步：检查温控挡位是否过低',
          description: '将摄像头对准冷藏室内的温控旋钮或温度面板，查看是否被调至“弱/低冷”档。',
          hint: '适当调高一档制冷强度，同时观察 2-3 小时制冷是否恢复正常。',
          cameraTarget: '冷藏室内温控旋钮/电子温控面板',
          highlightArea: 'knob',
          highlightLabel: '温控档位'
        },
        {
          title: '第二步：检查门封是否严密',
          description: '用摄像头环绕对准冰箱门四周，看门封胶条是否老化、破损或有明显缝隙。',
          hint: '可以用纸片夹在门缝测试阻力，若轻易被拉出，说明可能漏冷，建议联系专业人员更换门封。',
          cameraTarget: '冰箱门四周的密封胶条',
          highlightArea: 'door',
          highlightLabel: '门封胶条'
        }
      ]
    }
  }

  // 通用场景
  return {
    deviceName,
    scene: '基础自助排查',
    safety: '如不确定操作是否安全，请立即停止并联系专业人员处理。',
    steps: [
      {
        title: '第一步：拍摄整体外观',
        description: '将摄像头对准设备整体，确认有无明显破损、烧焦痕迹或进水痕迹。',
        hint: '若发现明显烧焦/进水，请不要继续通电，建议直接发起维修需求。',
        cameraTarget: '设备整体正面/顶部位置',
        highlightArea: 'center',
        highlightLabel: '设备外观'
      },
      {
        title: '第二步：检查电源与控制面板',
        description: '将摄像头对准电源插头和控制面板，确认电源指示灯是否亮起、按键是否正常反馈。',
        hint: '可以尝试换一个插座或检查空开是否跳闸，若依然无反应，请联系维修。',
        cameraTarget: '电源插头、插座和控制面板区域',
        highlightArea: 'control',
        highlightLabel: '电源/按键'
      }
    ]
  }
}

// 打开 AR 指导浮层
const openARGuide = () => {
  if (serviceType.value !== 'repair') {
    uni.showToast({
      title: '仅在家电维修场景下可使用 AR 指导',
      icon: 'none'
    })
    return
  }

  if (!repairForm.value.applianceType.trim()) {
    uni.showToast({
      title: '请先选择家电类型',
      icon: 'none'
    })
    return
  }

  const tutorial = buildARTutorial()
  if (!tutorial || !tutorial.steps || !tutorial.steps.length) {
    uni.showToast({
      title: '暂未匹配到对应教程',
      icon: 'none'
    })
    return
  }

  arTutorial.value = tutorial
  arCurrentStepIndex.value = 0
  showARGuide.value = true
}

const closeARGuide = () => {
  showARGuide.value = false
}

const nextARStep = () => {
  if (!arTutorial.value) return
  if (arCurrentStepIndex.value < arTutorial.value.steps.length - 1) {
    arCurrentStepIndex.value += 1
  } else {
    showARGuide.value = false
    uni.showToast({
      title: '已完成自助检查',
      icon: 'success'
    })
  }
}

const prevARStep = () => {
  if (arCurrentStepIndex.value > 0) {
    arCurrentStepIndex.value -= 1
  }
}

const applyAIPrefill = (payload) => {
  if (!payload) return
  const text = payload.text || ''
  const prefill = payload.prefill || {}

  // 确保类型同步
  if (payload.type) {
    serviceType.value = payload.type
  }

  aiPrefillInfo.value = {
    summary: payload.prefillSummary || `已根据语音填充关键字段：${payload.label || ''}`
  }

  // 根据类型写入对应表单
  if (serviceType.value === 'repair') {
    if (prefill.applianceType) repairForm.value.applianceType = prefill.applianceType
    if (prefill.damageDescription || text) repairForm.value.damageDescription = prefill.damageDescription || text
  } else if (serviceType.value === 'package') {
    if (prefill.pickupLocation) packageForm.value.pickupLocation = prefill.pickupLocation
    if (prefill.pickupTime) packageForm.value.pickupTime = prefill.pickupTime
  } else if (serviceType.value === 'shopping') {
    shoppingForm.value.groceryList = prefill.groceryList || text
    if (prefill.customerAddress) shoppingForm.value.customerAddress = prefill.customerAddress
  } else if (serviceType.value === 'cleaning') {
    cleaningForm.value.cleaningItems = prefill.cleaningItems || text
    if (prefill.cleaningTime) cleaningForm.value.cleaningTime = prefill.cleaningTime
  } else if (serviceType.value === 'cooking') {
    if (prefill.dishName) cookingForm.value.dishName = prefill.dishName
    cookingForm.value.requirements = prefill.requirements || text
  } else if (serviceType.value === 'pet') {
    if (prefill.animalType) petForm.value.animalType = prefill.animalType
    if (prefill.animalName) petForm.value.animalName = prefill.animalName
    petForm.value.adoptionLocation = prefill.adoptionLocation || petForm.value.adoptionLocation
    petForm.value.adoptionDate = prefill.adoptionDate || petForm.value.adoptionDate
  }
}

const clearAIPrefill = () => {
  aiPrefillInfo.value = null
}

// 生命周期
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  serviceType.value = options.type || 'package'

  ;(async () => {
    try {
      const res = await getServiceTypeDetail(serviceType.value)
      if (res?.data) {
        apiServiceDetail.value = res.data
      }
    } catch (e) {
      console.warn('加载服务类型详情失败', e)
    }
  })()

  // 读取语音 AI 预填
  if (options.prefillKey) {
    try {
      const cache = uni.getStorageSync(options.prefillKey)
      if (cache) {
        const payload = JSON.parse(cache)
        applyAIPrefill(payload)
        uni.removeStorageSync(options.prefillKey)
      }
    } catch (error) {
      console.error('读取 AI 预填失败', error)
    }
  }
})

const clearError = (field) => {
  errors.value[field] = ''
}

const selectSize = (size) => {
  packageForm.value.itemSize = size
  clearError('itemSize')
}


const selectRewardType = (type) => {
  // 根据当前服务类型更新对应的表单
  if (serviceType.value === 'package') {
    packageForm.value.rewardType = type
  } else if (serviceType.value === 'pet') {
    petForm.value.rewardType = type
  } else if (serviceType.value === 'shopping') {
    shoppingForm.value.rewardType = type
  } else if (serviceType.value === 'cleaning') {
    cleaningForm.value.rewardType = type
  } else if (serviceType.value === 'cooking') {
    cookingForm.value.rewardType = type
  }
  clearError('rewardType')
}


const showTimePicker = () => {
  uni.showActionSheet({
    itemList: ['今天下午', '明天上午', '明天下午', '后天上午', '自定义时间'],
    success: (res) => {
      const timeOptions = ['今天下午', '明天上午', '明天下午', '后天上午', '自定义时间']
      packageForm.value.pickupTime = timeOptions[res.tapIndex]
      clearError('pickupTime')
    }
  })
}

const showAnimalTypePicker = () => {
  uni.showActionSheet({
    itemList: animalTypes.value,
    success: (res) => {
      petForm.value.animalType = animalTypes.value[res.tapIndex]
      clearError('animalType')
    }
  })
}

const showApplianceTypePicker = () => {
  uni.showActionSheet({
    itemList: applianceTypes.value,
    success: (res) => {
      repairForm.value.applianceType = applianceTypes.value[res.tapIndex]
      clearError('applianceType')
    }
  })
}


// ======= 修改：替换为真实的地图选点 API =======
const getCurrentLocation = () => {
  uni.chooseLocation({
    success: (res) => {
      console.log('地图选点结果:', res)
      const { name, address, latitude, longitude } = res
      
      locationCoords.value.latitude = latitude
      locationCoords.value.longitude = longitude
      
      const locationText = name || address 
      
      if (serviceType.value === 'package') {
        packageForm.value.pickupLocation = locationText
        clearError('pickupLocation')
      } else if (serviceType.value === 'shopping') {
        shoppingForm.value.customerAddress = locationText
        clearError('customerAddress')
      } else if (serviceType.value === 'cooking') {
        cookingForm.value.cookingLocation = locationText
        clearError('cookingLocation')
      }
      
      uni.showToast({
        title: '位置获取成功',
        icon: 'success'
      })
    },
    fail: (err) => {
      // 拦截用户主动取消的操作，不提示报错
      if (err.errMsg && err.errMsg.indexOf('cancel') !== -1) {
        console.log('用户主动取消了选择位置')
        return; // 直接 return，不执行下面的弹窗
      }
      
      console.error('选择位置失败:', err)
      uni.showToast({
        title: '未获取到位置',
        icon: 'none'
      })
    }
  })
}
// ==========================================

const validateForm = () => {
  let isValid = true
  
  // 清空错误信息
  Object.keys(errors.value).forEach(key => {
    errors.value[key] = ''
  })
  
  if (serviceType.value === 'package') {
    if (!packageForm.value.pickupLocation.trim()) {
      errors.value.pickupLocation = '请输入取件位置'
      isValid = false
    }
    
    if (!packageForm.value.pickupCode.trim()) {
      errors.value.pickupCode = '请输入取件码'
      isValid = false
    }
    
    if (!packageForm.value.pickupTime.trim()) {
      errors.value.pickupTime = '请选择取件时间'
      isValid = false
    }
    
    if (!packageForm.value.itemSize) {
      errors.value.itemSize = '请选择物品大小'
      isValid = false
    }
  } else if (serviceType.value === 'pet') {
    if (!petForm.value.animalType.trim()) {
      errors.value.animalType = '请选择动物类型'
      isValid = false
    }
    
    if (!petForm.value.animalName.trim()) {
      errors.value.animalName = '请输入动物名字'
      isValid = false
    }
    
    if (!petForm.value.foodType.trim()) {
      errors.value.foodType = '请输入喂食食物'
      isValid = false
    }
    
    
    if (!petForm.value.adoptionDate.trim()) {
      errors.value.adoptionDate = '请输入收养时间段'
      isValid = false
    }
    
    if (!petForm.value.adoptionLocation.trim()) {
      errors.value.adoptionLocation = '请输入收养地点'
      isValid = false
    }
    
    
    if (!petForm.value.ownerWechat.trim()) {
      errors.value.ownerWechat = '请输入主人微信'
      isValid = false
    }
  } else if (serviceType.value === 'repair') {
    if (!repairForm.value.applianceType.trim()) {
      errors.value.applianceType = '请选择家电类型'
      isValid = false
    }
    
    if (!repairForm.value.damageDescription.trim()) {
      errors.value.damageDescription = '请描述损坏情况'
      isValid = false
    }
    
    if (!repairForm.value.wechat.trim()) {
      errors.value.wechat = '请输入微信号'
      isValid = false
    }
    
  } else if (serviceType.value === 'shopping') {
    if (!shoppingForm.value.groceryList.trim()) {
      errors.value.groceryList = '请填写所需菜名及数量'
      isValid = false
    }
    
    if (!shoppingForm.value.customerAddress.trim()) {
      errors.value.customerAddress = '请输入客户地址'
      isValid = false
    }
    
    if (!shoppingForm.value.customerPhone.trim()) {
      errors.value.customerPhone = '请输入客户电话'
      isValid = false
    }
    
    if (!shoppingForm.value.customerWechat.trim()) {
      errors.value.customerWechat = '请输入客户微信'
      isValid = false
    }
  } else if (serviceType.value === 'cleaning') {
    if (!cleaningForm.value.cleaningItems.trim()) {
      errors.value.cleaningItems = '请填写需要清洁什么'
      isValid = false
    }
    
    if (!cleaningForm.value.cleaningTime.trim()) {
      errors.value.cleaningTime = '请输入清洁时间'
      isValid = false
    }
    
    if (!cleaningForm.value.estimatedPrice.trim()) {
      errors.value.estimatedPrice = '请输入预估价格'
      isValid = false
    }
  } else if (serviceType.value === 'cooking') {
    if (!cookingForm.value.dishName.trim()) {
      errors.value.dishName = '请输入美食名称'
      isValid = false
    }
    
    if (!cookingForm.value.cookingTime.trim()) {
      errors.value.cookingTime = '请输入制作时间'
      isValid = false
    }
    
    if (!cookingForm.value.cookingLocation.trim()) {
      errors.value.cookingLocation = '请输入制作地点'
      isValid = false
    }
    
    if (!cookingForm.value.phone.trim()) {
      errors.value.phone = '请输入联系电话'
      isValid = false
    }
    
    if (!cookingForm.value.wechat.trim()) {
      errors.value.wechat = '请输入微信号'
      isValid = false
    }
    
    if (!cookingForm.value.estimatedPrice.trim()) {
      errors.value.estimatedPrice = '请输入预估价格'
      isValid = false
    }
  }
  
  // 验证奖励设置（除家电维修外的服务类型需要）
  let currentForm = null
  if (serviceType.value === 'package') {
    currentForm = packageForm.value
  } else if (serviceType.value === 'pet') {
    currentForm = petForm.value
  } else if (serviceType.value === 'shopping') {
    currentForm = shoppingForm.value
  } else if (serviceType.value === 'cleaning') {
    currentForm = cleaningForm.value
  } else if (serviceType.value === 'cooking') {
    currentForm = cookingForm.value
  }
  
  if (currentForm) {
    if (!currentForm.rewardType) {
      errors.value.rewardType = '请选择奖励类型'
      isValid = false
    }
    
    if (!currentForm.rewardAmount || currentForm.rewardAmount <= 0) {
      errors.value.rewardAmount = currentForm.rewardType === 'points' ? '请输入积分数量' : '请输入报酬金额'
      isValid = false
    }
  }
  
  return isValid
}

const submitRequest = async () => {
  if (!validateForm()) {
    uni.showToast({
      title: '请完善必填信息',
      icon: 'none'
    })
    return
  }

  isSubmitting.value = true

  // 公共字段
  const base = {
    serviceType: serviceType.value,
    title: generateRequestTitle({ serviceType: serviceType.value }),
    description: generateRequestDescriptionByType(),
    rewardType: getCurrentRewardType(),
    rewardAmount: getCurrentRewardAmount(),
    expectedTime: null,
    location: generateLocationByType(),
    urgency: 'normal',
    serviceDetails: buildServiceDetails(),
    // ======= 新增：把刚才选中的经纬度传给后端 =======
    longitude: locationCoords.value.longitude,
    latitude: locationCoords.value.latitude
    // ==========================================
  }

  try {
    await createRequest(base)
    uni.showToast({
      title: '需求发布成功！',
      icon: 'success'
    })
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/service-requests/service-requests?type=${serviceType.value}`
      })
    }, 800)
  } catch (e) {
    console.error('发布需求失败', e)
    uni.showToast({
      title: '发布失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}

// 当前奖励类型/金额
const getCurrentRewardType = () => {
  if (serviceType.value === 'package') return packageForm.value.rewardType
  if (serviceType.value === 'pet') return petForm.value.rewardType
  if (serviceType.value === 'shopping') return shoppingForm.value.rewardType
  if (serviceType.value === 'cleaning') return cleaningForm.value.rewardType
  if (serviceType.value === 'cooking') return cookingForm.value.rewardType
  return 'points'
}

const getCurrentRewardAmount = () => {
  if (serviceType.value === 'package') return Number(packageForm.value.rewardAmount || 0)
  if (serviceType.value === 'pet') return Number(petForm.value.rewardAmount || 0)
  if (serviceType.value === 'shopping') return Number(shoppingForm.value.rewardAmount || 0)
  if (serviceType.value === 'cleaning') return Number(cleaningForm.value.rewardAmount || 0)
  if (serviceType.value === 'cooking') return Number(cookingForm.value.rewardAmount || 0)
  return 0
}

// 根据当前服务类型生成简要描述（用于列表卡片）
const generateRequestDescriptionByType = () => {
  if (serviceType.value === 'package') {
    return `取件码：${packageForm.value.pickupCode}，位置：${packageForm.value.pickupLocation}`
  }
  if (serviceType.value === 'pet') {
    return `宠物：${petForm.value.animalType} ${petForm.value.animalName}，时间：${petForm.value.adoptionDate}`
  }
  if (serviceType.value === 'shopping') {
    return `需要购买：${shoppingForm.value.groceryList}`
  }
  if (serviceType.value === 'repair') {
    return `家电：${repairForm.value.applianceType}，故障：${repairForm.value.damageDescription}`
  }
  if (serviceType.value === 'cleaning') {
    return `清洁内容：${cleaningForm.value.cleaningItems}`
  }
  if (serviceType.value === 'cooking') {
    return `美食：${cookingForm.value.dishName}，时间：${cookingForm.value.cookingTime}`
  }
  return '服务需求'
}

// 生成期望时间（目前交给后端自动填充，这里返回 null）
const generateExpectedTimeByType = () => {
  return null
}

// 根据服务类型选择地点字段
const generateLocationByType = () => {
  if (serviceType.value === 'package') return packageForm.value.pickupLocation || '社区内'
  if (serviceType.value === 'shopping') return shoppingForm.value.customerAddress || '社区内'
  if (serviceType.value === 'pet') return petForm.value.adoptionLocation || '社区内'
  if (serviceType.value === 'cleaning') return cleaningForm.value.cleaningTime || '社区内'
  if (serviceType.value === 'cooking') return cookingForm.value.cookingLocation || '社区内'
  if (serviceType.value === 'repair') return generateLocation(shoppingForm.value, packageForm.value)
  return '社区内'
}

// 构造 serviceDetails，后端会保存为 JSON
const buildServiceDetails = () => {
  if (serviceType.value === 'package') {
    return {
      pickupLocation: packageForm.value.pickupLocation,
      pickupCode: packageForm.value.pickupCode,
      pickupTime: packageForm.value.pickupTime,
      itemSize: packageForm.value.itemSize
    }
  }
  if (serviceType.value === 'pet') {
    return {
      animalType: petForm.value.animalType,
      animalName: petForm.value.animalName,
      foodType: petForm.value.foodType,
      adoptionDate: petForm.value.adoptionDate,
      adoptionLocation: petForm.value.adoptionLocation,
      ownerWechat: petForm.value.ownerWechat
    }
  }
  if (serviceType.value === 'shopping') {
    return {
      groceryList: shoppingForm.value.groceryList,
      customerAddress: shoppingForm.value.customerAddress,
      customerPhone: shoppingForm.value.customerPhone,
      customerWechat: shoppingForm.value.customerWechat
    }
  }
  if (serviceType.value === 'repair') {
    return {
      applianceType: repairForm.value.applianceType,
      damageDescription: repairForm.value.damageDescription,
      purchaseTime: repairForm.value.purchaseTime,
      brandModel: repairForm.value.brandModel,
      wechat: repairForm.value.wechat
    }
  }
  if (serviceType.value === 'cleaning') {
    return {
      cleaningItems: cleaningForm.value.cleaningItems,
      estimatedPrice: cleaningForm.value.estimatedPrice,
      cleaningTime: cleaningForm.value.cleaningTime,
      notes: cleaningForm.value.notes
    }
  }
  if (serviceType.value === 'cooking') {
    return {
      dishName: cookingForm.value.dishName,
      cookingTime: cookingForm.value.cookingTime,
      cookingLocation: cookingForm.value.cookingLocation,
      phone: cookingForm.value.phone,
      wechat: cookingForm.value.wechat,
      estimatedPrice: cookingForm.value.estimatedPrice,
      requirements: cookingForm.value.requirements
    }
  }
  return {}
}

// 生成需求标题
const generateRequestTitle = (requestData) => {
  const serviceTypeMap = {
    package: '帮我取快递',
    pet: '帮我照顾宠物',
    repair: '帮我家电维修',
    shopping: '帮我买菜',
    cleaning: '帮我家清洁',
    cooking: '帮我做美食'
  }
  return serviceTypeMap[requestData.serviceType] || '服务需求'
}

// 生成需求描述
const generateRequestDescription = (requestData) => {
  if (requestData.serviceType === 'shopping') {
    return `需要购买：${requestData.groceryList}`
  } else if (requestData.serviceType === 'package') {
    return `取件码：${requestData.pickupCode}，位置：${requestData.pickupLocation}`
  } else if (requestData.serviceType === 'pet') {
    return `宠物类型：${requestData.animalType}，宠物名字：${requestData.animalName}，收养时间：${requestData.adoptionDate}`
  } else if (requestData.serviceType === 'repair') {
    return `家电类型：${requestData.applianceType}，故障描述：${requestData.damageDescription}`
  } else if (requestData.serviceType === 'cleaning') {
    return `清洁内容：${requestData.cleaningItems}`
  } else if (requestData.serviceType === 'cooking') {
    return `美食类型：${requestData.dishType}，用餐人数：${requestData.peopleCount}人`
  }
  return '详细需求请查看详情'
}

// 生成期望时间
const generateExpectedTime = (requestData) => {
  if (requestData.serviceType === 'shopping') {
    return '尽快完成'
  } else if (requestData.pickupTime) {
    return requestData.pickupTime
  } else if (requestData.cleaningTime) {
    return requestData.cleaningTime
  }
  return '尽快完成'
}

// 生成地址
const generateLocation = (requestData) => {
  if (requestData.serviceType === 'shopping') {
    return requestData.customerAddress || '碧桂园某某栋几单元几号房'
  } else if (requestData.serviceType === 'pet') {
    return requestData.adoptionLocation || '碧桂园某某栋几单元几号房'
  } else if (requestData.pickupLocation) {
    return requestData.pickupLocation
  } else if (requestData.customerAddress) {
    return requestData.customerAddress
  }
  return '碧桂园某某栋几单元几号房'
}
</script>

<style scoped>
.service-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 30rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
}

.time {
  font-size: 28rpx;
  font-weight: 600;
  color: #8D6E63;
}

.status-icons {
  display: flex;
  gap: 12rpx;
  font-size: 24rpx;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 30rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx;
  border-radius: 8rpx;
  transition: all 0.3s ease;
}

.nav-left:active {
  background: rgba(0, 0, 0, 0.05);
}

.back-icon {
  font-size: 32rpx;
  color: #8D6E63;
  font-weight: bold;
}

.back-text {
  font-size: 28rpx;
  color: #8D6E63;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.page-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #8D6E63;
}

.nav-right {
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.help-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 143, 0, 0.3);
  transition: all 0.3s ease;
}

.help-btn:active {
  transform: scale(0.95);
}

.help-icon {
  font-size: 28rpx;
  color: white;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 20rpx;
  overflow: hidden;
}

/* 服务信息卡片 */
.service-info-card {
  margin: 12rpx 0;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.service-header {
  display: flex;
  align-items: center;
  padding: 16rpx;
  gap: 16rpx;
}

.service-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.service-emoji {
  font-size: 40rpx;
}

.service-details {
  flex: 1;
}

.service-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #8D6E63;
  margin-bottom: 8rpx;
  display: block;
}

.service-desc {
  font-size: 24rpx;
  color: #A1887F;
  display: block;
}

/* AI 预填提示 */
.ai-prefill-banner {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: linear-gradient(135deg, #E8F5E9, #E3F2FD);
  border-radius: 16rpx;
  margin: 12rpx 0 0;
  border: 2rpx solid #C5E1A5;
}

.ai-prefill-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #4CAF50;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.ai-prefill-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ai-prefill-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #2E7D32;
}

.ai-prefill-desc {
  font-size: 24rpx;
  color: #558B2F;
}

.ai-prefill-action {
  font-size: 24rpx;
  color: #1976D2;
  padding: 8rpx 12rpx;
  border-radius: 12rpx;
  background: #E3F2FD;
}

/* 服务图标样式 */
.package-icon {
  background: linear-gradient(135deg, #FF8A65, #FFB74D);
}

.pet-icon {
  background: linear-gradient(135deg, #FFD54F, #FFECB3);
}

.shopping-icon {
  background: linear-gradient(135deg, #81C784, #A5D6A7);
}

.repair-icon {
  background: linear-gradient(135deg, #64B5F6, #90CAF9);
}

.cleaning-icon {
  background: linear-gradient(135deg, #BA68C8, #CE93D8);
}

.cooking-icon {
  background: linear-gradient(135deg, #FF7043, #FFAB91);
}

/* 表单区域 */
.form-section {
  margin: 32rpx 0;
  background: white;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #8D6E63;
  margin-bottom: 24rpx;
}

/* 输入组 */
.input-group {
  margin-bottom: 32rpx;
}

.input-label {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.label-text {
  font-size: 26rpx;
  color: #424242;
  font-weight: 600;
}

.required {
  color: #F44336;
  font-size: 24rpx;
  margin-left: 4rpx;
}

/* 输入框样式 */
.input-wrapper {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 16rpx;
}

.input-wrapper:focus-within {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
}

.input-icon {
  font-size: 32rpx;
  color: #8D6E63;
}

.input-field {
  flex: 1;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 26rpx;
  color: #424242;
  outline: none;
}

.input-field::placeholder {
  color: #A1887F;
}

/* 位置输入 */
.location-input-wrapper {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 16rpx;
}

.location-input-wrapper:focus-within {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
}

.location-input {
  flex: 1;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 26rpx;
  color: #424242;
  outline: none;
}

.location-input::placeholder {
  color: #A1887F;
}

.location-btn {
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  border-radius: 12rpx;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.location-btn:active {
  transform: scale(0.95);
  background: linear-gradient(135deg, #45A049, #5CB85C);
}

.location-btn-text {
  font-size: 22rpx;
  color: white;
  font-weight: 600;
}

/* 时间选择器 */
.time-picker {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 16rpx;
}

.time-picker:active {
  border-color: #4CAF50;
  background: white;
  transform: translateY(-2rpx);
}

/* 动物类型选择器 */
.animal-type-picker {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 16rpx;
}

.animal-type-picker:active {
  border-color: #4CAF50;
  background: white;
  transform: translateY(-2rpx);
}

/* 家电类型选择器 */
.appliance-type-picker {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 16rpx;
}

.appliance-type-picker:active {
  border-color: #4CAF50;
  background: white;
  transform: translateY(-2rpx);
}

.picker-icon {
  font-size: 32rpx;
  color: #8D6E63;
}

.picker-content {
  flex: 1;
}

.picker-label {
  font-size: 24rpx;
  color: #424242;
  font-weight: 600;
  display: block;
  margin-bottom: 4rpx;
}

.picker-value {
  font-size: 26rpx;
  color: #666;
  display: block;
}

.picker-arrow {
  font-size: 24rpx;
  color: #4CAF50;
}

/* 大小选择 */
.size-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.size-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 16rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 8rpx;
}

.size-option.active {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #E8F5E8, #F1F8E9);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.size-icon {
  font-size: 32rpx;
}

.size-text {
  font-size: 24rpx;
  color: #424242;
  font-weight: 600;
  text-align: center;
}

.size-desc {
  font-size: 20rpx;
  color: #666;
  text-align: center;
}

/* 重量选择 */

/* 喂食频率选择 */
.feeding-frequency-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.frequency-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 12rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 8rpx;
}

.frequency-option.active {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #E8F5E8, #F1F8E9);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.frequency-icon {
  font-size: 28rpx;
}

.frequency-text {
  font-size: 22rpx;
  color: #424242;
  font-weight: 600;
  text-align: center;
}

.frequency-desc {
  font-size: 18rpx;
  color: #666;
  text-align: center;
}

/* 文本域 */
.textarea-wrapper {
  position: relative;
}

.textarea-field {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx;
  border: 2rpx solid #E0E0E0;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #424242;
  background: #F8F9FA;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.textarea-field:focus {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
}

.char-count {
  position: absolute;
  bottom: 8rpx;
  right: 12rpx;
  font-size: 20rpx;
  color: #A1887F;
}

/* 奖励设置样式 */
.reward-section {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 1rpx solid #E9ECEF;
}

.reward-type-options {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-top: 12rpx;
}

.reward-type-option {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: white;
  border-radius: 12rpx;
  border: 2rpx solid #E9ECEF;
  transition: all 0.3s ease;
}

.reward-type-option.active {
  border-color: #4CAF50;
  background: #F1F8E9;
}

.reward-type-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.reward-type-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.reward-type-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 4rpx;
}

.reward-type-desc {
  font-size: 24rpx;
  color: #7F8C8D;
}

.reward-input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12rpx;
  border: 1rpx solid #E9ECEF;
  padding: 0 16rpx;
  margin-top: 12rpx;
}

.reward-input {
  flex: 1;
  height: 80rpx;
  font-size: 28rpx;
  color: #2C3E50;
  border: none;
  outline: none;
}

.reward-unit {
  font-size: 24rpx;
  color: #7F8C8D;
  margin-left: 8rpx;
}

/* 价格协商 */
.price-negotiation {
  padding: 20rpx;
  background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
  border-radius: 16rpx;
  border: 2rpx solid #FFB74D;
}

.negotiation-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.negotiation-icon {
  font-size: 32rpx;
  color: #FF8F00;
}

.negotiation-content {
  flex: 1;
}

.negotiation-title {
  font-size: 26rpx;
  color: #E65100;
  font-weight: 600;
  margin-bottom: 8rpx;
  display: block;
}

.negotiation-desc {
  font-size: 22rpx;
  color: #BF360C;
  line-height: 1.4;
  display: block;
}

/* AR 技能指导卡片 */
.ar-skill-card {
  margin-top: 20rpx;
  padding: 18rpx 16rpx;
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  border-radius: 16rpx;
  border: 2rpx solid #64B5F6;
  box-shadow: 0 4rpx 16rpx rgba(33, 150, 243, 0.25);
}

.ar-skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}

.ar-skill-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex: 1;
}

.ar-skill-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
}

.ar-skill-text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ar-skill-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1E3A8A;
}

.ar-skill-subtitle {
  font-size: 22rpx;
  color: #1E40AF;
}

.ar-skill-btn {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  border: none;
  background: linear-gradient(135deg, #1D4ED8, #3B82F6);
  color: white;
  font-size: 22rpx;
  font-weight: 600;
}

.ar-skill-footer {
  margin-top: 8rpx;
}

.ar-skill-tip {
  font-size: 20rpx;
  color: #1E3A8A;
}

/* 交易方式 */
.transaction-method {
  padding: 20rpx;
  background: linear-gradient(135deg, #E8F5E8, #F1F8E9);
  border-radius: 16rpx;
  border: 2rpx solid #81C784;
}

.transaction-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.transaction-icon {
  font-size: 32rpx;
  color: #4CAF50;
}

.transaction-content {
  flex: 1;
}

.transaction-title {
  font-size: 26rpx;
  color: #2E7D32;
  font-weight: 600;
  margin-bottom: 8rpx;
  display: block;
}

.transaction-desc {
  font-size: 22rpx;
  color: #388E3C;
  line-height: 1.4;
  display: block;
}

/* AR 指导浮层 */
.ar-layer {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ar-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

.ar-panel {
  position: relative;
  z-index: 1;
  width: 88%;
  max-width: 720rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 20rpx 20rpx 16rpx;
  box-shadow: 0 16rpx 40rpx rgba(15, 23, 42, 0.45);
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.ar-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.ar-top-left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ar-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.ar-subtitle {
  font-size: 22rpx;
  color: #6B7280;
}

.ar-close-btn {
  width: 44rpx;
  height: 44rpx;
  border-radius: 12rpx;
  background: #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #111827;
}

.ar-camera {
  position: relative;
  height: 320rpx;
  border-radius: 18rpx;
  overflow: hidden;
  background: radial-gradient(circle at top, #1F2937, #020617);
  margin-bottom: 10rpx;
}

.ar-camera-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
}

.ar-camera-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
}

.ar-highlight {
  position: absolute;
  border: 3rpx solid rgba(96, 165, 250, 0.9);
  border-radius: 16rpx;
  box-shadow: 0 0 0 4rpx rgba(37, 99, 235, 0.35);
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
}

.ar-highlight-label {
  margin: 6rpx;
  padding: 6rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.95);
  color: white;
  font-size: 22rpx;
}

/* 面板滤网区域高亮 */
.ar-highlight-panel {
  left: 10%;
  right: 10%;
  top: 15%;
  height: 35%;
}

/* 遥控器屏幕区域高亮 */
.ar-highlight-remote-screen {
  left: 30%;
  right: 30%;
  top: 35%;
  height: 30%;
}

/* 室外机区域高亮 */
.ar-highlight-outdoor {
  left: 15%;
  right: 15%;
  bottom: 10%;
  height: 40%;
}

/* 通用中心区域高亮 */
.ar-highlight-center {
  left: 20%;
  right: 20%;
  top: 20%;
  bottom: 20%;
}

/* 通用控制/按键区域高亮 */
.ar-highlight-control {
  left: 20%;
  right: 20%;
  bottom: 12%;
  height: 30%;
}

/* 排水管区域高亮 */
.ar-highlight-pipe {
  left: 10%;
  right: 10%;
  bottom: 8%;
  height: 35%;
}

/* 过滤网区域高亮 */
.ar-highlight-filter {
  left: 20%;
  right: 20%;
  top: 25%;
  height: 30%;
}

/* 门封/门缝区域高亮 */
.ar-highlight-door {
  left: 5%;
  right: 5%;
  top: 10%;
  bottom: 10%;
}

.ar-step-text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  margin-bottom: 8rpx;
}

.ar-step-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
}

.ar-step-desc {
  font-size: 24rpx;
  color: #374151;
}

.ar-step-hint {
  font-size: 22rpx;
  color: #1D4ED8;
}

.ar-step-safety {
  font-size: 20rpx;
  color: #B91C1C;
}

.ar-actions {
  display: flex;
  gap: 12rpx;
}

.ar-action {
  flex: 1;
  height: 72rpx;
  border-radius: 16rpx;
  border: none;
  font-size: 26rpx;
  font-weight: 700;
}

.ar-action.ghost {
  background: #F3F4F6;
  color: #374151;
}

.ar-action.primary {
  background: linear-gradient(135deg, #2563EB, #3B82F6);
  color: white;
}

.ar-action:disabled {
  opacity: 0.6;
}

/* 脏的程度选择 */
.dirt-level-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.dirt-level-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 12rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 8rpx;
}

.dirt-level-option.active {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #E8F5E8, #F1F8E9);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.level-icon {
  font-size: 32rpx;
}

.level-text {
  font-size: 24rpx;
  color: #424242;
  font-weight: 600;
  text-align: center;
}

.level-desc {
  font-size: 20rpx;
  color: #666;
  text-align: center;
}

/* 价格输入 */
.price-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.price-input-container {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 12rpx;
}

.price-input-container:focus-within {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
}

.price-input {
  flex: 1;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 26rpx;
  color: #424242;
  outline: none;
}

.price-input::placeholder {
  color: #A1887F;
}

.price-unit {
  font-size: 24rpx;
  color: #8D6E63;
  font-weight: 600;
}

.price-tips {
  padding: 12rpx 16rpx;
  background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
  border-radius: 12rpx;
  border: 1rpx solid #FFB74D;
}

.tips-text {
  font-size: 22rpx;
  color: #E65100;
  line-height: 1.4;
}

/* 时间偏好选择 */
.time-preference-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.time-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 8rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 8rpx;
}

.time-option.active {
  border-color: #4CAF50;
  background: linear-gradient(135deg, #E8F5E8, #F1F8E9);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.2);
}

.time-icon {
  font-size: 28rpx;
}

.time-text {
  font-size: 22rpx;
  color: #424242;
  font-weight: 600;
  text-align: center;
}

/* 时间地点组合 */
.time-location-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.time-section, .location-section {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.label-icon {
  font-size: 24rpx;
  color: #8D6E63;
}

.time-input-container {
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
}

.time-input-container:focus-within {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
}

.time-input {
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 26rpx;
  color: #424242;
  outline: none;
}

.time-input::placeholder {
  color: #A1887F;
}

.location-input-container {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  gap: 12rpx;
}

.location-input-container:focus-within {
  border-color: #4CAF50;
  background: white;
  box-shadow: 0 0 0 4rpx rgba(76, 175, 80, 0.1);
}

.location-input {
  flex: 1;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 26rpx;
  color: #424242;
  outline: none;
}

.location-input::placeholder {
  color: #A1887F;
}

.location-btn {
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, #4CAF50, #45A049);
  border-radius: 12rpx;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.location-btn:active {
  transform: scale(0.95);
  background: linear-gradient(135deg, #45A049, #3E8E41);
}

.location-btn-text {
  font-size: 22rpx;
  color: white;
  font-weight: 600;
}

/* 错误信息 */
.error-message {
  font-size: 22rpx;
  color: #F44336;
  margin-top: 8rpx;
  padding-left: 8rpx;
}

/* 即将推出 */
.coming-soon {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 20rpx;
  text-align: center;
}

.coming-soon-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.coming-soon-text {
  font-size: 28rpx;
  color: #8D6E63;
  font-weight: 600;
  margin-bottom: 12rpx;
  display: block;
}

.coming-soon-desc {
  font-size: 24rpx;
  color: #A1887F;
  display: block;
}

/* 提交按钮 */
.submit-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  border-radius: 20rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 16rpx rgba(76, 175, 80, 0.4);
}

.submit-btn.disabled {
  background: linear-gradient(135deg, #BDBDBD, #E0E0E0);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transform: none;
  opacity: 0.7;
}

.submit-btn.loading {
  background: linear-gradient(135deg, #81C784, #A5D6A7);
}

.submit-content {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.submit-text {
  font-size: 32rpx;
  font-weight: 600;
  color: white;
}

.submit-btn.disabled .submit-text {
  color: #666666;
}

.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top: 3rpx solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 底部间距 */
.bottom-spacing {
  height: 30rpx;
}

/* 响应式设计 */
@media screen and (max-width: 750rpx) {
  .main-content {
    padding: 0 20rpx;
  }
  
  .form-section {
    padding: 24rpx;
    margin: 24rpx 0;
  }
  
  .size-options,
  .feeding-frequency-options {
    grid-template-columns: 1fr;
    gap: 8rpx;
  }
  
  .size-option,
  .frequency-option {
    padding: 16rpx 12rpx;
  }
}

@media screen and (max-width: 600rpx) {
  .service-header {
    padding: 20rpx;
  }
  
  .service-icon {
    width: 60rpx;
    height: 60rpx;
  }
  
  .service-emoji {
    font-size: 32rpx;
  }
  
  .service-title {
    font-size: 28rpx;
  }
  
  .section-title {
    font-size: 28rpx;
  }
  
  .input-field,
  .location-input,
  .textarea-field {
    font-size: 24rpx;
  }
}
</style>

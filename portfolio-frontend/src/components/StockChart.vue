<template>
  <div ref="chartRef" class="stock-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
// 引入我们刚刚写好的获取真实历史数据的 API
import { getStockHistoricalData } from '../apis/finnhubService.js';

const props = defineProps({
  ticker: String,
  apiSymbol: String, // 接收父组件传来的精准 API 请求代码
  pnl: Number
});

const chartRef = ref(null);
let chartInstance = null;
const chartDataCache = {}; // 依然保留全局缓存，防止重复浪费网络请求

const getChartColor = () => {
  return props.pnl >= 0 ? '#30d158' : '#ff453a'; 
};

// ==========================================
// 获取或生成图表数据 (混合模式)
// ==========================================
const fetchChartData = async () => {
  // 1. 检查缓存
  if (chartDataCache[props.ticker]) {
    return chartDataCache[props.ticker];
  }

  let finalData = [];

  // 2. 如果是静态国债 (我们代码里标记了 'STATIC')，则依然使用平缓的模拟数据
  if (props.apiSymbol === 'STATIC') {
    let basePrice = 100;
    for (let i = 0; i < 30; i++) {
      basePrice += (Math.random() - 0.5) * 0.5; // 国债波动极小
      finalData.push(basePrice.toFixed(2));
    }
  } else {
    // 3. 核心：向 Finnhub 发起真实网络请求！
    const realData = await getStockHistoricalData(props.apiSymbol, 30);
    
    if (realData && realData.length > 0) {
      finalData = realData; // 成功拿到真实收盘价数组！
    } else {
      // 容错机制：如果网络卡顿或 API 额度超限，降级使用模拟数据保证界面不崩溃
      let fallbackPrice = Math.random() * 100 + 50;
      for (let i = 0; i < 30; i++) {
        fallbackPrice += (Math.random() - 0.5) * 5;
        finalData.push(fallbackPrice.toFixed(2));
      }
    }
  }

  // 4. 存入缓存
  chartDataCache[props.ticker] = finalData;
  return finalData;
};

// ==========================================
// 初始化与渲染图表
// ==========================================
const initChart = async () => {
  if (!chartRef.value) return;
  
  chartInstance = echarts.init(chartRef.value);
  const color = getChartColor();

  // 开启优雅的 Apple 风格深色加载动画
  chartInstance.showLoading('default', {
    text: '',
    color: color,
    textColor: '#fff',
    maskColor: 'rgba(0, 0, 0, 0)', // 透明遮罩
    zlevel: 0,
    lineWidth: 3,
    spinnerRadius: 10
  });

  // 等待真实数据返回
  const actualData = await fetchChartData();
  
  // 数据回来后，关闭加载动画
  chartInstance.hideLoading();

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 32, 0.8)',
      borderColor: 'rgba(255,255,255,0.1)',
      textStyle: { color: '#fff' },
      axisPointer: { type: 'line', lineStyle: { color: 'rgba(255,255,255,0.2)' } }
    },
    grid: { left: '0%', right: '0%', top: '5%', bottom: '0%', containLabel: false },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: Array.from({length: actualData.length}, (_, i) => `Day ${i+1}`),
      show: false 
    },
    yAxis: {
      type: 'value',
      scale: true, // 核心：让 Y 轴根据真实数据的最大最小值自动缩放，显示出波动的真实感
      show: false,
      splitLine: { show: false }
    },
    series: [
      {
        name: 'Close Price',
        type: 'line',
        data: actualData,
        smooth: 0.2, // 真实数据不需要过度平滑，保留一点真实的折线感
        symbol: 'none', 
        lineStyle: { color: color, width: 2.5 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${color}80` }, 
            { offset: 1, color: `${color}00` }  
          ])
        }
      }
    ]
  };

  chartInstance.setOption(option);
};

const handleResize = () => {
  if (chartInstance) chartInstance.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  if (chartInstance) chartInstance.dispose();
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.stock-chart {
  width: 100%;
  height: 220px;
}
</style>
<template>
  <div ref="chartRef" class="market-chart"></div>
</template>

<script>
// 这里的变量在整个应用生命周期内只初始化一次。
let globalMarketDataCache = null;
</script>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { getMarketIntradayData } from '../apis/finnhubService.js';

const chartRef = ref(null);
let chartInstance = null;

// 核心：获取大盘分时数据 (带缓存拦截)
const fetchIntradayData = async () => {
  // 1. 检查缓存
  if (globalMarketDataCache) {
    return globalMarketDataCache;
  }

  // 2. 如果没缓存，则走正常的获取流程
  let finalData = [];
  const realData = await getMarketIntradayData('VOO');
  
  if (realData && realData.length > 0) {
    finalData = realData;
  } else {
    // 容错模拟数据
    let price = 485.00;
    for (let i = 0; i < 78; i++) {
      price += (Math.random() - 0.45) * 1.5; 
      finalData.push(price.toFixed(2));
    }
  }

  // 3. 把拿到的数据存入全局缓存，造福后人
  globalMarketDataCache = finalData;
  return finalData;
};

// 渲染图表
const initChart = async () => {
  if (!chartRef.value) return;
  
  chartInstance = echarts.init(chartRef.value);

  // 只有在没有缓存（第一次加载）时，才显示 Loading 动画
  if (!globalMarketDataCache) {
    chartInstance.showLoading('default', {
      text: '', color: '#0a84ff', maskColor: 'rgba(0,0,0,0)', zlevel: 0, lineWidth: 3, spinnerRadius: 10
    });
  }

  const actualData = await fetchIntradayData();
  
  chartInstance.hideLoading();

  // 判断涨跌决定颜色
  const startPrice = parseFloat(actualData[0]);
  const endPrice = parseFloat(actualData[actualData.length - 1]);
  const color = endPrice >= startPrice ? '#30d158' : '#ff453a';

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 32, 0.8)',
      borderColor: 'rgba(255,255,255,0.1)',
      textStyle: { color: '#fff' },
      axisPointer: { type: 'line', lineStyle: { color: 'rgba(255,255,255,0.2)' } }
    },
    grid: { left: '-2%', right: '-2%', top: '5%', bottom: '-5%', containLabel: false },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      show: false 
    },
    yAxis: {
      type: 'value',
      scale: true, 
      show: false,
      splitLine: { show: false }
    },
    series: [
      {
        name: 'S&P 500 (VOO)',
        type: 'line',
        data: actualData,
        smooth: 0.25, 
        symbol: 'none', 
        lineStyle: { color: color, width: 3 }, 
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${color}60` }, 
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
.market-chart {
  width: 100%;
  height: 240px; 
}
</style>
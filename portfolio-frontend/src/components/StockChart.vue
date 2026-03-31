<template>
  <div ref="chartRef" class="stock-chart"></div>
</template>

<script>
// ==========================================
// 【终极修复】：普通 script 标签
// 这里的代码在整个应用运行期间只会执行一次。
// 这个缓存对象现在是“金刚不坏之身”，不会随着图表的收起而销毁！
// ==========================================
const globalChartDataCache = {};
</script>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { getStockHistoricalData } from '../apis/finnhubService.js';

const props = defineProps({
  ticker: String,
  apiSymbol: String, 
  pnl: Number
});

const chartRef = ref(null);
let chartInstance = null;

const getChartColor = () => {
  return props.pnl >= 0 ? '#30d158' : '#ff453a'; 
};

// ==========================================
// 获取或生成图表数据 (使用全局缓存)
// ==========================================
const fetchChartData = async () => {
  // 1. 检查真正的全局缓存
  if (globalChartDataCache[props.ticker]) {
    return globalChartDataCache[props.ticker];
  }

  let finalData = [];

  if (props.apiSymbol === 'STATIC') {
    let basePrice = 100;
    for (let i = 0; i < 30; i++) {
      basePrice += (Math.random() - 0.5) * 0.5;
      finalData.push(basePrice.toFixed(2));
    }
  } else {
    const realData = await getStockHistoricalData(props.apiSymbol, 30);
    
    if (realData && realData.length > 0) {
      finalData = realData; 
    } else {
      let fallbackPrice = Math.random() * 100 + 50;
      for (let i = 0; i < 30; i++) {
        fallbackPrice += (Math.random() - 0.5) * 5;
        finalData.push(fallbackPrice.toFixed(2));
      }
    }
  }

  // 2. 把数据存入全局缓存
  globalChartDataCache[props.ticker] = finalData;
  return finalData;
};

// ==========================================
// 初始化与渲染图表
// ==========================================
const initChart = async () => {
  if (!chartRef.value) return;
  
  chartInstance = echarts.init(chartRef.value);
  const color = getChartColor();

  chartInstance.showLoading('default', {
    text: '',
    color: color,
    textColor: '#fff',
    maskColor: 'rgba(0, 0, 0, 0)',
    zlevel: 0,
    lineWidth: 3,
    spinnerRadius: 10
  });

  const actualData = await fetchChartData();
  
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
      scale: true,
      show: false,
      splitLine: { show: false }
    },
    series: [
      {
        name: 'Close Price',
        type: 'line',
        data: actualData,
        smooth: 0.2, 
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
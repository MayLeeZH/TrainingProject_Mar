<template>
  <div ref="chartRef" class="market-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';
import { getStockHistoricalData, getMarketIntradayData } from '../apis/finnhubService.js';

const props = defineProps({
  compareMode: {
    type: Boolean,
    default: false
  },
  timeframe: {
    type: String,
    default: '1M'
  }
});

const chartRef = ref(null);
let chartInstance = null;

// 生成基于真实标普500曲线的混合图表数据
const generateRealPlusMockData = async (tf) => {
  let spPrices = [];
  try {
    if (tf === '1D') {
      spPrices = await getMarketIntradayData('VOO') || [];
    } else if (tf === '1W') {
      spPrices = await getStockHistoricalData('VOO', 7) || [];
    } else if (tf === '1M') {
      spPrices = await getStockHistoricalData('VOO', 30) || [];
    }
  } catch(e) {
    console.warn("API limit or error, using fallback SP data.");
  }

  // 1. 如果周末 API 没数据、触发限流限制、或缺乏数据，进行优雅 fallback 随机生成大盘
  if (!spPrices || spPrices.length < 2) {
    let points = tf === '1D' ? 78 : (tf === '1W' ? 7 : 30);
    let curSp = 480;
    spPrices = [];
    for(let i=0; i<points; i++) {
      curSp += (Math.random() - 0.45) * (tf === '1D' ? 0.3 : 4);
      spPrices.push(curSp);
    }
  }

  const length = spPrices.length;
  const dates = [];
  const portPrices = [];
  
  // 假定你的初始个人资产本金
  let curPort = 3100000;
  
  for(let i=0; i<length; i++) {
    dates.push(`T-${length - i}`);
    
    if (i === 0) {
      portPrices.push(curPort);
      continue;
    }

    const prevSp = spPrices[i - 1];
    const curSp = spPrices[i];
    const spChangePct = (curSp - prevSp) / prevSp;
    
    // Portfolio drift: 与 S&P 深度相关 (例如 Beta 设定为 1.2)，再加上每日额外的波动因子 (Alpha)
    const alpha = (Math.random() - 0.42) * (tf === '1D' ? 1000 : 8000); 
    const portChange = curPort * (spChangePct * 1.2) + alpha;
    
    curPort += portChange;
    portPrices.push(curPort);
  }

  return { dates, portPrices, spPrices };
};

const updateChart = async () => {
  if (!chartInstance) return;

  chartInstance.showLoading('default', {
    text: '', color: '#0a84ff', maskColor: 'rgba(0,0,0,0)', zlevel: 0, lineWidth: 3, spinnerRadius: 10
  });

  const data = await generateRealPlusMockData(props.timeframe);
  
  chartInstance.hideLoading();

  let option = {};

  if (!props.compareMode) {
    // --- Mode 1: Total Portfolio Value (Area Chart) ---
    const startVal = data.portPrices[0];
    const endVal = data.portPrices[data.portPrices.length - 1];
    const color = endVal >= startVal ? '#30d158' : '#ff453a';

    option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(30, 30, 32, 0.8)',
        borderColor: 'rgba(255,255,255,0.1)',
        textStyle: { color: '#fff' },
        axisPointer: { type: 'line', lineStyle: { color: 'rgba(255,255,255,0.2)' } },
        formatter: (params) => {
          const val = params[0].value;
          return `<div style="font-weight:600">$${val.toLocaleString('en-US', {minimumFractionDigits:2, maximumFractionDigits:2})}</div>`;
        }
      },
      legend: { show: false },
      grid: { left: '2%', right: '2%', top: '5%', bottom: '5%', containLabel: true },
      xAxis: { type: 'category', data: data.dates, show: false },
      yAxis: { type: 'value', scale: true, splitLine: { show: false }, axisLabel: { color: '#a1a1a6' } },
      series: [
        {
          name: 'Portfolio',
          type: 'line',
          data: data.portPrices,
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
  } else {
    // --- Mode 2: Portfolio vs S&P 500 Return % (Dual Line Chart) ---
    const portPct = data.portPrices.map(v => ((v - data.portPrices[0]) / data.portPrices[0]) * 100);
    const spPct = data.spPrices.map(v => ((v - data.spPrices[0]) / data.spPrices[0]) * 100);
    
    option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(30, 30, 32, 0.8)',
        borderColor: 'rgba(255,255,255,0.1)',
        textStyle: { color: '#fff' },
        axisPointer: { type: 'line', lineStyle: { color: 'rgba(255,255,255,0.2)', type: 'solid' } },
        formatter: (params) => {
          let html = `<div style="font-size:0.85rem;margin-bottom:4px;color:#a1a1a6;">Return vs Benchmark</div>`;
          params.forEach(p => {
             const mColor = p.color;
             const mVal = p.value > 0 ? `+${p.value.toFixed(2)}%` : `${p.value.toFixed(2)}%`;
             html += `<div style="display:flex;align-items:center;gap:6px;margin-top:4px;">
                <span style="border-radius:50%;width:8px;height:8px;background:${mColor}"></span>
                <span>${p.seriesName}:</span> 
                <b style="margin-left:auto">${mVal}</b>
             </div>`;
          });
          return html;
        }
      },
      legend: {
        data: ['Portfolio', 'S&P 500'],
        textStyle: { color: '#a1a1a6' },
        bottom: 0,
        icon: 'circle'
      },
      grid: { left: '2%', right: '2%', top: '5%', bottom: '15%', containLabel: true },
      xAxis: { type: 'category', data: data.dates, show: false },
      yAxis: { 
        type: 'value', 
        axisLabel: { formatter: '{value}%', color: '#a1a1a6' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)', type: 'dashed' } }
      },
      series: [
        {
          name: 'Portfolio',
          type: 'line',
          data: portPct,
          smooth: 0.25,
          symbol: 'none',
          lineStyle: { color: '#0a84ff', width: 3 },
          zlevel: 1
        },
        {
          name: 'S&P 500',
          type: 'line',
          data: spPct,
          smooth: 0.25,
          symbol: 'none',
          lineStyle: { color: '#8e8e93', width: 2, type: 'dashed' },
          zlevel: 0
        }
      ]
    };
  }

  chartInstance.setOption(option, true);
};

// Listen for prop changes to re-draw
watch(() => [props.compareMode, props.timeframe], () => {
  updateChart();
});

const handleResize = () => {
  if (chartInstance) chartInstance.resize();
};

onMounted(() => {
  chartInstance = echarts.init(chartRef.value);
  updateChart();
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
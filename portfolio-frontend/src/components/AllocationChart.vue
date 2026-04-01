<template>
  <div ref="chartRef" class="allocation-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

const chartRef = ref(null);
let chartInstance = null;

// 这里为你准备了四种资产的模拟数据（后续你可以把它们改成从后端获取的真实汇总数据）
const allocationData = [
  { value: 45000, name: 'Stock' },
  { value: 25000, name: 'Fund' },
  { value: 20000, name: 'Bond' },
  { value: 10000, name: 'Cash' }
];

const initChart = () => {
  if (!chartRef.value) return;
  
  chartInstance = echarts.init(chartRef.value);

  const option = {
    // 鼠标悬浮时的跟随提示框
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(30, 30, 32, 0.9)',
      borderColor: 'rgba(255,255,255,0.1)',
      textStyle: { color: '#fff' },
      // 自定义提示内容格式：Asset Name: 45% 
      formatter: '{b}: {d}%' 
    },
    // 图例（图表下方的小圆点和文字）
    legend: {
      bottom: '0%',
      left: 'center',
      textStyle: { color: '#a1a1a6', fontSize: 12 },
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 15
    },
    series: [
      {
        name: 'Asset Allocation',
        type: 'pie',
        // 核心：设置内外半径，把它从饼图变成环形图 (Donut)
        radius: ['55%', '75%'], 
        center: ['50%', '45%'], // 稍微向上偏移一点，给底部的图例留出空间
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8, // 让每个扇形的边缘变圆滑 (Apple 细节)
          borderColor: '#000', // 描边颜色设置为黑色背景色，制造出扇形之间的间隙
          borderWidth: 3
        },
        // 默认状态下不显示标签
        label: {
          show: false,
          position: 'center'
        },
        // 核心：鼠标悬浮 (Hover) 时的放大和变粗动画
        emphasis: {
          scaleSize: 10, // 扇形放大的程度
          label: {
            show: true, // 悬浮时，在圆环正中心显示文字
            fontSize: '18',
            fontWeight: 'bold',
            color: '#fff',
            formatter: '{b}\n{d}%' // 换行显示：上面名字，下面百分比
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: false
        },
        data: allocationData,
        // Apple 风格的高级调色盘
        color: [
          '#0a84ff', // 蓝色 (Stock)
          '#5e5ce6', // 紫色 (Fund)
          '#30d158', // 绿色 (Bond)
          '#ffd60a'  // 黄色 (Cash)
        ] 
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
.allocation-chart {
  width: 100%;
  height: 240px; /* 撑满卡片的高度 */
}
</style>
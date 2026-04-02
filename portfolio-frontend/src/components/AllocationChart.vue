<template>
  <div ref="chartRef" class="allocation-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  allocationData: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref(null);
let chartInstance = null;

const buildOption = () => {
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(30, 30, 32, 0.9)',
      borderColor: 'rgba(255,255,255,0.1)',
      textStyle: { color: '#fff' },
      formatter: ({ name, value, percent }) => {
        return `${name}: ${percent}% ($${Number(value).toLocaleString()})`;
      }
    },
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
        radius: ['55%', '75%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#000',
          borderWidth: 3
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          scaleSize: 10,
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold',
            color: '#fff',
            formatter: '{b}\n{d}%'
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
        data: props.allocationData,
        color: [
          '#0a84ff',
          '#5e5ce6',
          '#30d158',
          '#ffd60a',
          '#ff9f0a',
          '#64d2ff'
        ]
      }
    ]
  };
};

const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  chartInstance.setOption(buildOption());
};

const updateChart = () => {
  if (!chartInstance) return;
  chartInstance.setOption(buildOption());
};

const handleResize = () => {
  if (chartInstance) chartInstance.resize();
};

watch(
  () => props.allocationData,
  () => {
    updateChart();
  },
  { deep: true }
);

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.allocation-chart {
  width: 100%;
  height: 240px;
}
</style>

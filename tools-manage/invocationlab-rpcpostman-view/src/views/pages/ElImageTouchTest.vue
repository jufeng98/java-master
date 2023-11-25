<template>
    <div class="main-area" style="text-align: center;">
        <div>
            <h5>原始的el-image组件</h5>
            <real-el-image :src="urls[0]" :preview-src-list="urls" class="image"></real-el-image>
        </div>
        <div style="margin-top: 50px;">
            <h5>改造后带触控支持的el-image组件</h5>
            <el-image :src="urls[0]" :preview-src-list="urls" class="image"></el-image>
        </div>
    </div>
</template>
<script>
    export default {
        name: 'elImageTouchTest',
        data() {
            return {
                urls: [
                    'https://t7.baidu.com/it/u=2168645659,3174029352&fm=193&f=GIF',
                    'https://t7.baidu.com/it/u=2531125946,3055766435&fm=193&f=GIF',
                    'https://t7.baidu.com/it/u=1330338603,908538247&fm=193&f=GIF'
                ]
            }
        },
        methods: {
        },
        async mounted() {
        },
        watch: {

        }
    }
</script>
<style scoped lang="less">
    body {
        width: 300px;
        margin: 0 auto;
        overflow-x: hidden;
    }

    .image {
        width: 200px;
        height: 200px;
    }
</style>
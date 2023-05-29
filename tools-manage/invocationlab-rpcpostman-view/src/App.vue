<template>
    <div id="app" style="overflow-y:hidden">
        <transition name="fade" mode="out-in">
            <router-view></router-view>
        </transition>
    </div>
</template>

<script>
    export default {
        name: 'app',
        components: {}
    }
</script>

<style>
    /*#nprogress .bar {
        background: #00ff81;

        position: fixed;
        z-index: 1031;
        top: 0;
        left: 0;

        width: 100%;
        height: 5px;
    }*/
</style>
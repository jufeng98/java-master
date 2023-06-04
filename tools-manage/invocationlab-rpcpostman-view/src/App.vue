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

</style>
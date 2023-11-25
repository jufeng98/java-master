<template>
    <real-el-image class="clothes-img" ref="elImageRef" v-bind="$attrs" v-on="$listeners" :src="src"
        :preview-src-list="previewSrcList" @click="imageClick" :fit="fit" :lazy="lazy"
        :scrollContainer="scrollContainer" :zIndex="zIndex">
    </real-el-image>
</template>

<script>
    require("hammerjs")

    export default {
        name: "ElImage",
        props: {
            src: String,
            fit: String,
            lazy: Boolean,
            scrollContainer: {},
            previewSrcList: {
                type: Array,
                default: () => []
            },
            zIndex: {
                type: Number,
                default: 2000
            }
        },
        data() {
            return {
            }
        },
        methods: {
            throttle(method, context, timeout = 100) {
                clearTimeout(method.tId);
                method.tId = setTimeout(function () {
                    method.call(context);
                }, timeout);
            },
            tryGetHtmlEle(selector, callback) {
                let ele = document.querySelector(selector)
                if (ele) {
                    callback(ele)
                    return
                }
                setTimeout(() => {
                    this.tryGetHtmlEle(selector, callback);
                }, 200);
            },
            imageClick(e) {
                this.tryGetHtmlEle(".el-image-viewer__canvas", this.addTouchSupport)
            },
            addTouchSupport(ele) {
                this.handleOneTouchMove(ele)
                let hammer = new Hammer(ele);
                hammer.get('pinch').set({ enable: true })
                hammer.on('pinchstart', (e) => {
                    this.handleTwoTouchZoom(e, hammer)
                });
            },
            // 添加单指触控移动图片功能
            handleOneTouchMove(ele) {
                $(ele).on('touchstart', (e) => {
                    let imgVueObj = this.$refs["elImageRef"].$children[0]
                    if (!imgVueObj) {
                        return
                    }
                    const { offsetX, offsetY } = imgVueObj.transform;
                    const startX = e.touches[0].pageX;
                    const startY = e.touches[0].pageY;
                    let _dragHandler = ev => {
                        this.throttle(() => {
                            imgVueObj.transform.offsetX = offsetX + ev.touches[0].pageX - startX;
                            imgVueObj.transform.offsetY = offsetY + ev.touches[0].pageY - startY;
                        })
                    }
                    $(document).on('touchmove', _dragHandler);
                    $(document).on('touchend', ev => {
                        $(document).off('touchmove', _dragHandler);
                    })
                })
            },
            // 添加双指触控放大和缩小图片功能
            handleTwoTouchZoom(e, hammer) {
                let imgVueObj = this.$refs["elImageRef"].$children[0]
                if (!imgVueObj) {
                    return
                }
                let zoomInHandler = (ev) => {
                    this.throttle(() => {
                        imgVueObj.transform.scale = parseFloat((imgVueObj.transform.scale + 0.02).toFixed(3));
                    })
                }
                let zoomOutHandler = (ev) => {
                    this.throttle(() => {
                        if (imgVueObj.transform.scale > 0.2) {
                            imgVueObj.transform.scale = parseFloat((imgVueObj.transform.scale - 0.02).toFixed(3));
                        }
                    })
                }
                hammer.on('pinchin', zoomOutHandler);
                hammer.on('pinchout', zoomInHandler);
                hammer.on('pinchend', (e) => {
                    hammer.off('pinchin', zoomOutHandler);
                    hammer.off('pinchout', zoomInHandler);
                });
            },
            clickHandler() {
                this.$refs.elImageRef.clickHandler()
            }
        },
        mounted() {
            console.log("注意,el-image标签在此被重新定义,添加了触控支持");
        },
    };
</script>
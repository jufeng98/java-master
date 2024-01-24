<template>
    <section class="app-container">
        <el-form label-width="200px" @submit.prevent="onSubmit" style="margin:20px;width:90%;min-width:600px;">
            <el-row>
                <el-col :span="14">
                    <el-form-item label="注册中心：">
                        <el-select v-model="zk" placeholder="请选择注册中心" filterable>
                            <el-option v-for="option in zkList" v-bind:value="option.addr" :key="option.addr"
                                :label="option.addr + '(' + $consts.REGISTRATION_CENTER_TYPE_MAP[option.type] + ')'">
                                {{ option.addr }}({{ $consts.REGISTRATION_CENTER_TYPE_MAP[option.type] }})
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>

            <el-row>
                <el-col :span="13">
                    <el-form-item label="服务名称：">
                        <el-select v-model="zkServiceName" placeholder="服务名称" filterable>
                            <el-option v-for="option in serviceList" v-bind:value="option" :key="option"
                                :label="option">
                                {{ option }}
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>

                <el-col :span="1">
                    <el-form-item label-width="0px">
                        <el-button class="cpLink my-button" plain type="info" v-clipboard:error="onError"
                            v-clipboard:copy="zkServiceName" v-clipboard:success="onCopy">
                            复制
                        </el-button>
                    </el-form-item>
                </el-col>

            </el-row>

            <el-row>
                <el-col :span="14">
                    <el-form-item label="API MAVEN依赖：">
                        <label>推荐直接从nexus复制过来比较准确。version元素可选，此时version将会从私服解析，优先级 SNAPSHOT > Release</label>
                        <el-input type="textarea" :autosize="{ minRows: 7, maxRows: 7}" v-model="dependency">
                        </el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item>
                <a :href="'http://192.168.236.7:8081/nexus/#welcome'" target="_blank" class="el-button el-button--mini">
                    NEXUS地址
                </a>
                <el-button :loading="isCreating" type="success" v-on:click="doCreate">创建</el-button>
            </el-form-item>

        </el-form>

        <el-dialog title="服务创建失败" :visible.sync="dialogVisible" :fullscreen="true" :show-close="false">
            <pre>{{rspMsg}}</pre>
            <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
        </el-dialog>
    </section>
</template>

<script>

    import { getZkServices, upload } from '@/api/create';
    import { getAllZk } from '@/api/common';

    export default {
        name: 'createService',
        data() {
            return {
                dependency: `<dependency>
    <groupId>org.javamaster</groupId>
    <artifactId>service-api</artifactId>
</dependency>`,
                isCreating: false,
                zk: '',
                zkServiceName: '',
                zkList: [],
                serviceList: [],
                rspMsg: '',
                dialogVisible: false
            }
        },
        methods: {
            onSubmit() {
                this.$message.success("创建成功onSubmit");
            },
            doCreate() {
                this.createService()
            },
            async createService() {
                if (this.zk == '' ||
                    this.zkServiceName == '' ||
                    this.dependency == '') {
                    this.$message.error('必须指定:zk,serviceName,dependency');
                    return;
                }
                var encodedZk = encodeURI(this.zk);

                let params = {
                    "zk": encodedZk,
                    "zkServiceName": this.zkServiceName,
                    "dependency": this.dependency
                }

                this.isCreating = true;
                this.$NProgress.start();
                let res = await upload(params);
                this.$NProgress.done();
                this.isCreating = false;
                if (res.status == 200) {
                    if (res.data.code == 0) {
                        this.$notify({
                            title: '创建服务',
                            message: this.zkServiceName + "创建成功," + res.data.data,
                            type: 'success'
                        });
                        //清空页面缓存
                        AppUtils.clearItems();
                        //相当于页面刷新,注意name属性一定时组件的名称
                        this.$store.dispatch('tagsView/delCachedView', { name: 'accessService' }).then(() => {
                            this.$nextTick(() => {
                                this.$router.push({
                                    path: '/redirect' + '/access/index',
                                    query: {
                                        zk: this.zk,
                                        serviceName: this.zkServiceName
                                    }
                                });
                            })
                        })
                    } else {
                        this.rspMsg = res.data.error;
                        this.dialogVisible = true;
                    }
                } else {
                    this.$notify({
                        title: '创建服务',
                        message: '系统错误,请重试或联系管理员,状态码:' + res.status,
                        type: 'error',
                        duration: 0
                    });
                }
            },
            getZkList() {
                let para = {};
                getAllZk(para).then((res) => {
                    let ms = res.data.data;
                    this.zkList = ms;
                    if (this.zkList[0]) {
                        this.zk = this.zkList[0].addr
                    }
                });
            },
            getSelectedServices() {
                let zk = this.zk
                let params = { "zk": zk };
                getZkServices(params).then((res) => {
                    let data = res.data.data;
                    this.serviceList = data;
                });
            },
            // 复制成功
            onCopy(e) {
                this.$message({
                    message: '复制成功！',
                    type: 'success'
                })
            },
            // 复制失败
            onError(e) {
                this.$message({
                    message: '复制失败！',
                    type: 'error'
                })
            },
        },
        watch: {
            zk: function () {
                this.zkServiceName = '';
                this.serviceList = [];
                this.getSelectedServices();
            }
        },
        mounted() {
            this.getZkList();
        }
    }
</script>
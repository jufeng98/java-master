<template>
    <section class="app-container">
        <el-form label-width="200px" @submit.prevent="onSubmit" style="margin:20px;width:90%;min-width:600px;">

            <el-row>
                <el-col :span="8">
                    <el-form-item label="依赖名称：">
                        <el-select v-model="dependencyName" placeholder="选择需要刷新的依赖" filterable>
                            <el-option v-for="option in dependencyList" v-bind:value="option" :key="option"
                                :label="option">
                                {{ option }}
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>

                <el-col :span="1">
                    <el-form-item label-width="0px">
                        <el-button class="cpLink my-button" type="success" @click="refreshDependency" :loading="isRefresh">
                            刷新依赖
                        </el-button>
                    </el-form-item>
                </el-col>

            </el-row>

            <el-row>
                <el-col :span="14">
                    <el-form-item label="MAVEN依赖：">
                        <label>依赖用于Redis JDK序列化方式的键值的展示和修改，推荐直接从nexus复制过来比较准确，version固定从私服解析，优先级 SNAPSHOT >
                            Release</label>
                        <el-input type="textarea" :autosize="{ minRows: 7, maxRows: 7}" v-model="dependency">
                        </el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item>
                <a :href="'http://192.168.236.7:8081/nexus/#welcome'" target="_blank" class="el-button el-button--mini">
                    NEXUS地址
                </a>
                <el-button :loading="isCreating" type="success" v-on:click="doCreate">解析依赖</el-button>
            </el-form-item>

        </el-form>

        <el-dialog title="解析依赖失败" :visible.sync="dialogVisible" :fullscreen="true" :show-close="false">
            <pre>{{rspMsg}}</pre>
            <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
        </el-dialog>
    </section>
</template>

<script>

    import { refreshJdk, uploadJdk } from '@/api/create';
    import { getDependencyList } from '@/api/access';

    export default {
        name: 'createService',
        data() {
            return {
                dependency: `<dependency>
    <groupId>cn.com.bluemoon</groupId>
    <artifactId>mh-service-api</artifactId>
</dependency>`,
                isCreating: false,
                isRefresh: false,
                dependencyName: '',
                dependencyList: [],
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
            async refreshDependency() {
                if (!this.dependencyName) {
                    return
                }
                let params = {
                    "dependencyName": this.dependencyName
                }

                this.isRefresh = true;
                this.$NProgress.start();
                let res = await refreshJdk(params);
                this.$NProgress.done();
                this.isRefresh = false;
                if (res.status == 200) {
                    if (res.data.code == 0) {
                        this.$notify({
                            title: '刷新依赖 ',
                            message: res.data.data,
                            type: 'success'
                        });
                        this.$router.push({
                            path: '/redirect/redis/index',
                            query: {}
                        });
                    } else {
                        this.rspMsg = res.data.error;
                        this.dialogVisible = true;
                    }
                } else {
                    this.$notify({
                        title: '刷新依赖',
                        message: '系统错误,请重试或联系管理员,状态码:' + res.status,
                        type: 'error',
                        duration: 0
                    });
                }
            },
            async createService() {
                if (this.dependency === '') {
                    this.$message.error('必须指定:dependency');
                    return;
                }

                let params = {
                    "dependency": this.dependency
                }

                this.isCreating = true;
                this.$NProgress.start();
                let res = await uploadJdk(params);
                this.$NProgress.done();
                this.isCreating = false;
                if (res.status == 200) {
                    if (res.data.code == 0) {
                        this.$notify({
                            title: '解析依赖 ',
                            message: res.data.data,
                            type: 'success'
                        });
                        this.$router.push({
                            path: '/redirect/redis/index',
                            query: {}
                        });
                    } else {
                        this.rspMsg = res.data.error;
                        this.dialogVisible = true;
                    }
                } else {
                    this.$notify({
                        title: '解析依赖',
                        message: '系统错误,请重试或联系管理员,状态码:' + res.status,
                        type: 'error',
                        duration: 0
                    });
                }
            },
            getDependencyList() {
                let para = {};
                getDependencyList(para).then((res) => {
                    let ms = res.data.data;
                    this.dependencyList = ms;
                });
            },
        },
        watch: {
        },
        mounted() {
            this.getDependencyList();
        }
    }
</script>
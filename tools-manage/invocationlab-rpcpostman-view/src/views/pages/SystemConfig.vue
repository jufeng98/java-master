<template>
    <section class="app-container">
    <div style="margin:20px;width:80%;min-width:600px;">
        <el-row >
            <el-col :offset="2">
                <el-table
                        :data="zkAddress"
                        style="width: 100%">
                    <el-table-column
                            label="IP"
                            width="480">
                        <template slot-scope="scope">
                            <span style="margin-left: 10px">
                              {{ scope.row.addr }}({{ $consts.REGISTRATION_CENTER_TYPE_MAP[scope.row.type] }})
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作">
                        <template slot-scope="scope">
                            <el-button
                                    size="mini"
                                    type="danger"
                                    @click="deleteZk(scope.$index, scope.row)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-col>
        </el-row>
        <el-form label-width="140px" style="margin:20px;width:95%;min-width:600px;">
            <el-row>
                <el-col>
                  <el-form-item label="注册中心类型:">
                    <el-select v-model="type">
                      <el-option
                          v-for="(value,key) in $consts.REGISTRATION_CENTER_TYPE"
                          :key="value"
                          :label="key"
                          :value="value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col>
                    <el-form-item label="注册中心:">
                        <el-input v-model="zk"></el-input>
                    </el-form-item>
                </el-col>
                <el-col>
                    <el-form-item label="密码:">
                        <el-input v-model="password"></el-input>
                    </el-form-item>
                </el-col>
                <el-col>
                    <el-form-item>
                        <el-button type="primary" @click="onSubmit">新 增</el-button>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
    </div>
    </section>
</template>

<script>

    import { addConfig,deleteZk,configs} from '@/api/config';

    export default {
        name:'systemConfig',
        data(){
            return{
                requestCodeMirror:'',
                type: this.$consts.REGISTRATION_CENTER_TYPE.ZK,
                zk:'',
                password:'',
                zkAddress: [],
            }
        },
        methods:{
            onSubmit(){
                let encodedZk = encodeURI(this.zk);
                let params = {
                    "zk":encodedZk,
                    "password":this.password,
                    type:this.type
                };
                const loading = this.$loading({
                    lock: true,
                    text: '正在初始化注册中心配置,请耐心等待......',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                addConfig(params).then((res) => {
                    let code = res.data.code;
                    if(code === 0){
                        this.$message.success("保存成功");
                        AppUtils.clearItems();
                        window.location.reload();
                    }
                    this.getConfigs();

                    setTimeout(() => {
                        loading.close();
                    }, 500);
                });
            },
            getConfigs(){
                configs().then((res) => {
                    let zkAddress = res.data.data;
                    console.log("返回的zk地址:",zkAddress);
                    this.zkAddress = zkAddress;
                });
            },
            deleteZk(index,value){
                let encodedZk = encodeURI(value.addr);
                let params = {
                    "zk":encodedZk,
                    "password":this.password
                };
                deleteZk(params).then((res) => {
                    let code = res.data.code;
                    if(code === 0){
                        this.$message.success("删除成功");
                        AppUtils.clearItems();
                        window.location.reload();
                    }
                    this.getConfigs();
                });
            }
        },
        mounted(){
            this.getConfigs();
        }
    }
</script>
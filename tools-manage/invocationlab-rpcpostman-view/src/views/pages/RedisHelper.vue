<template>
    <el-container style="margin:6px;" v-loading="loading">
        <el-header>
            <el-row :gutter="20">
                <el-col :span="3">
                    <el-select v-model="commonVo.connectId" placeholder="请选择">
                        <el-option v-for="item in connectVos" :key="item.connectId" :label="item.name"
                            :value="item.connectId">
                        </el-option>
                    </el-select>
                </el-col>
                <el-col :span="2">
                    <el-button @click="listDb" type="primary">连接</el-button>
                </el-col>
                <el-col :span="2">
                    <el-button @click="dialogVisible=true" type="primary" icon="el-icon-plus" circle></el-button>
                </el-col>
            </el-row>
        </el-header>
        <el-container style="height: calc(78vh);">
            <div style="height: calc(78vh);width: 300px;">
                <el-header style="overflow: hidden;height: 120px;" width="300px">
                    <el-tooltip class="item" effect="dark" :content="url" placement="bottom">
                        <span>{{url}}</span>
                    </el-tooltip>
                    <el-input v-model="pattern" @keyup.enter.native="patternEnter">
                        <template slot="prepend">pattern</template>
                    </el-input>
                </el-header>
                <el-tree style="height: 420px;overflow-y: auto;" lazy :data="redisDbs" :props="defaultProps"
                    :load="loadKeys" @node-expand="nodeExpand" @node-click="nodeClick"></el-tree>
            </div>
            <el-container style="margin: 10px;">
                <div style="margin: 6px;">
                    <el-row :gutter="40">
                        <el-col :span="12">
                            <el-input v-model="commonVo.redisKey"></el-input>
                        </el-col>
                        <el-col :span="3">
                            <el-button @click="renameKey" type="primary">重命名key</el-button>
                        </el-col>
                        <el-col :span="3">
                            <el-button @click="openAdd" type="primary">新增key</el-button>
                        </el-col>
                        <el-col :span="2">
                            <el-button @click="delKey" type="danger">删除key</el-button>
                        </el-col>
                    </el-row>
                    <el-row :gutter="40" style="margin: 6px;">
                        <el-col :span="2">
                            <el-tag>{{'key类型:'+commonVo.redisKeyType}}</el-tag>
                        </el-col>
                        <el-col :span="3">
                            <el-tag>{{'value大小(bytes):'+redisValueSize}}</el-tag>
                        </el-col>
                        <el-col :span="2">
                            <el-tag>{{'field数量:'+fieldCount}}</el-tag>
                        </el-col>
                        <el-col :span="5">
                            <el-input v-model="commonVo.redisKeyTtl">
                                <template slot="prepend">TTL</template>
                            </el-input>
                        </el-col>
                        <el-col :span="3">
                            <el-button @click="setNewTtl" type="primary">设置TTL</el-button>
                        </el-col>
                        <el-col :span="3">
                            <el-button @click="reloadValue" type="primary">刷新value</el-button>
                        </el-col>
                        <el-col :span="2">
                            <el-button @click="saveValue" type="primary">保存value</el-button>
                        </el-col>
                    </el-row>

                    <el-table v-if="fieldVos.length>0" :data="fieldVos" @row-click="fieldRowClick" border
                        style="width: 98%" height="170">
                        <el-table-column prop="fieldKey" label="field" v-if="commonVo.redisKeyType==='hash'">
                        </el-table-column>
                        <el-table-column prop="fieldValue" label="value" show-overflow-tooltip>
                        </el-table-column>
                        <el-table-column prop="fieldScore" label="score" v-if="commonVo.redisKeyType==='zset'">
                        </el-table-column>
                        <el-table-column fixed="right" label="操作" width="100">
                            <template slot-scope="scope">
                                <el-button @click.native.prevent="dialogFieldVisible=true" type="text" size="small">
                                    新增
                                </el-button>
                                <el-button @click.native.prevent="deleteField(scope.row)" type="text" size="small">
                                    删除
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>

                    <el-row>
                        <el-col :span="19">
                            <codemirror style="width:800px" ref="myCm" v-model="commonVo.redisValue"
                                :options="cmOptions">
                            </codemirror>
                        </el-col>
                        <el-col :span="4">
                            <el-tag type="success">F11全屏</el-tag><br />
                            <el-tag type="success">Ctrl-F搜索</el-tag><br />
                            <el-tag type="success">Ctrl-G跳转到下一个搜索目标</el-tag><br />
                            <el-tag type="success">Alt-G跳转到指定行</el-tag>
                            <el-button @click="valueFormatChange(2)" type="success"
                                style="margin:6px 0;">JSON格式化</el-button>
                            <el-button @click="valueFormatChange(1)" type="success"
                                style="margin: 0;">JSON压缩</el-button>
                        </el-col>
                    </el-row>
                </div>
            </el-container>
        </el-container>

        <el-dialog title="新增连接" :visible.sync="dialogVisible" v-if="dialogVisible">
            <el-form ref="form" :model="connectInfo" label-width="80px">
                <el-form-item label="连接名称">
                    <el-input placeholder="单机or集群" v-model="connectInfo.name"></el-input>
                </el-form-item>
                <el-form-item label="host">
                    <el-input v-model="connectInfo.host"></el-input>
                </el-form-item>
                <el-form-item label="端口">
                    <el-input v-model="connectInfo.port"></el-input>
                </el-form-item>
                <el-form-item label="密码">
                    <el-input v-model="connectInfo.password" show-password></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="pingConnect">测试连接</el-button>
                    <el-button type="primary" @click="saveConnect">保存</el-button>
                    <el-button @click="dialogVisible=false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="新增key" :visible.sync="dialogKeyVisible" v-if="dialogKeyVisible">
            <el-form ref="form1" :model="addFieldObj" label-width="80px">
                <el-form-item label="key">
                    <el-input v-model="commonVo.redisKey"></el-input>
                </el-form-item>
                <el-form-item label="type">
                    <el-select v-model="commonVo.redisKeyType">
                        <el-option v-for="item in redisKeyTypes" :key="item" :label="item" :value="item">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="field" v-if="commonVo.redisKeyType==='hash'">
                    <el-input v-model="addFieldObj.fieldKey"></el-input>
                </el-form-item>
                <el-form-item label="score" v-if="commonVo.redisKeyType==='zset'">
                    <el-input v-model="addFieldObj.score"></el-input>
                </el-form-item>
                <el-form-item label="value">
                    <el-input type="textarea" rows="8" v-model="addFieldObj.redisValue"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="addKey">确定</el-button>
                    <el-button @click="dialogKeyVisible=false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="新增" :visible.sync="dialogFieldVisible" v-if="dialogFieldVisible">
            <el-form ref="form2" :model="addFieldObj" label-width="80px">
                <el-form-item label="score" v-if="commonVo.redisKeyType==='zset'">
                    <el-input v-model="addFieldObj.score"></el-input>
                </el-form-item>
                <el-form-item label="field" v-else-if="commonVo.redisKeyType==='hash'">
                    <el-input v-model="addFieldObj.fieldKey"></el-input>
                </el-form-item>
                <el-form-item label="value">
                    <el-input type="textarea" rows="8" v-model="addFieldObj.redisValue"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="addField">确定</el-button>
                    <el-button @click="dialogFieldVisible=false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

    </el-container>
</template>

<script>
    import request from '@/utils/request'

    export default {
        name: 'redisHelper',
        data() {
            return {
                cmOptions: {
                    mode: 'application/json',

                    styleActiveLine: false,
                    lineNumbers: true,

                    line: true,

                    lint: true,
                    foldGutter: true,
                    lineWrapping: true,
                    gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter", "CodeMirror-lint-markers"],

                    smartIndent: true,
                    indentWithTabs: true,
                    // autofocus: true,
                    matchBrackets: true,
                    scrollbarStyle: null,
                    extraKeys: {
                        "F11": function (cm) {
                            cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                            let ele = cm.display.scroller
                            ele.classList.toggle('CodeMirror-scroll-fullscreen')
                        },
                        "Esc": function (cm) {
                            if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                        },
                    },
                    theme: "eclipse"//monokai eclipse zenburn
                },
                loading: false,
                connectInfo: {
                    connectId: '',
                    nodes: '',
                    name: '',
                    host: '',
                    port: '',
                    password: '',
                },
                dialogVisible: false,
                dialogKeyVisible: false,
                dialogFieldVisible: false,
                url: '---',
                pattern: '*',
                connectVos: [],
                redisDbs: [],
                defaultProps: {
                    children: 'keys',
                    label: 'label',
                    isLeaf: 'isLeaf',
                },
                node_had: null, // 触发 tree 的 :load=loadNode 重复触发  动态更新tree
                resolve_had: null, // 触发 tree 的 :load=loadNode 重复触发  动态更新tree
                commonVo: {
                    connectId: '',
                    oldRedisKey: '',
                    redisKey: '',
                    redisKeyBase64: '',
                    oldRedisValue: '',
                    redisValue: '',
                    fieldKey: '',
                    score: null,
                    redisDbIndex: null,
                    redisKeyTtl: null,
                    redisKeyType: '',
                },
                addFieldObj: {
                    fieldKey: '',
                    redisValue: '',
                    score: null,
                },
                fieldVos: [],
                redisValueSize: '',
                fieldCount: '',
                redisKeyTypes: [
                    'string',
                    'list',
                    'set',
                    'hash',
                    'zset',
                ],
                currentRedisDbIndex: null,
            }
        },
        methods: {
            resetData() {
                this.fieldVos = []
                this.redisValueSize = ''
                this.fieldCount = ''
                this.commonVo.redisKey = ''
                this.commonVo.redisKeyBase64 = ''
                this.commonVo.redisValue = ''
                this.commonVo.redisDbIndex = null
                this.commonVo.redisKeyTtl = null
                this.commonVo.redisKeyType = ''
            },
            valueFormatChange(data) {
                if (data === 2) {
                    this.commonVo.redisValue = JSON.stringify(JSON.parse(this.commonVo.redisValue), null, "  ")
                } else if (data === 1) {
                    this.commonVo.redisValue = JSON.stringify(JSON.parse(this.commonVo.redisValue))
                }
            },
            saveConnect() {
                request({
                    url: '/redis/saveConnect',
                    method: 'post',
                    data: this.connectInfo
                }).then(res => {
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                        this.dialogVisible = false;
                        this.listConnects()
                    }
                })
            },
            pingConnect() {
                request({
                    url: '/redis/pingConnect',
                    method: 'post',
                    data: this.connectInfo
                }).then(res => {
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                    }
                })
            },
            listConnects() {
                request({
                    url: '/redis/listConnects',
                    method: 'post',
                    data: this.connectInfo
                }).then(res => {
                    if (res.data.code === 0) {
                        this.connectVos = res.data.data
                    }
                })
            },
            listDb() {
                if (this.commonVo.connectId === '') {
                    this.$message.warning("请选择连接");
                    return
                }
                this.loading = true
                request({
                    url: '/redis/listDb/' + this.commonVo.connectId,
                    method: 'get',
                }).then(res => {
                    this.loading = false
                    if (res.data.code === 0) {
                        this.resetData()
                        this.redisDbs = res.data.data
                        this.initUrl();
                    }
                })
            },
            loadKeys(node, resolve) {
                if (!this.commonVo.connectId) {
                    return resolve([])
                }
                if (node.level === 1) {
                    this.node_had = node;
                    this.resolve_had = resolve;
                }
            },
            listKeys(node, resolve) {
                this.loading = true
                this.currentRedisDbIndex = node.data.redisDbIndex
                request({
                    url: '/redis/listKeys/' + this.commonVo.connectId + "/" + node.data.redisDbIndex + "?pattern=" + this.pattern,
                    method: 'get',
                }).then(res => {
                    this.loading = false
                    if (res.data.code === 0) {
                        resolve(res.data.data)
                    }
                })
            },
            nodeExpand(data, node) {
                this.listKeys(this.node_had, this.resolve_had);
            },
            nodeClick(data, node) {
                if (node.level === 1) {
                    this.commonVo.redisDbIndex = data.redisDbIndex
                } else if (node.level == 2) {
                    this.commonVo.redisKey = data.label
                    this.commonVo.redisKeyBase64 = data.labelBase64
                    this.commonVo.oldRedisKey = data.label
                    this.commonVo.redisDbIndex = node.parent.data.redisDbIndex
                    this.reloadValue();
                }
            },
            patternEnter() {
                if (!this.node_had) {
                    return
                }
                this.listKeys(this.node_had, this.resolve_had);
            },
            initUrl() {
                let tmp = this.connectVos
                    .filter(conn => {
                        return conn.connectId === this.commonVo.connectId
                    })
                if (tmp[0].nodes !== '') {
                    this.url = tmp[0].nodes
                } else {
                    this.url = tmp[0].host + ":" + tmp[0].port
                }
            },
            renameKey() {
                if (!this.commonVo.redisKey) {
                    return
                }
                this.$confirm(`确定将key ${this.commonVo.oldRedisKey} 重命名为 ${this.commonVo.redisKey}?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loading = true
                    request({
                        url: '/redis/renameKey',
                        method: 'post',
                        data: this.commonVo
                    }).then(res => {
                        this.loading = false
                        if (res.data.code === 0) {
                            this.$message.success("操作成功");
                            this.commonVo.oldRedisKey = res.data.data.redisKey
                            this.listKeys(this.node_had, this.resolve_had);
                        }
                    })
                }).catch(() => {
                });
            },
            reloadValue() {
                if (!this.commonVo.redisKey) {
                    return
                }
                this.loading = true
                request({
                    url: '/redis/getValue',
                    method: 'post',
                    data: this.commonVo
                }).then(res => {
                    this.loading = false
                    if (res.data.code === 0) {
                        let valueVo = res.data.data
                        this.commonVo.redisValue = valueVo.redisValue || ''
                        this.commonVo.redisKeyTtl = valueVo.redisKeyTtl
                        this.commonVo.redisKeyType = valueVo.redisKeyType || ''
                        this.redisValueSize = valueVo.redisValueSize || ''
                        this.fieldCount = valueVo.fieldCount || ''
                        this.fieldVos = valueVo.fieldVos || []
                    }
                })
            },
            openAdd() {
                if (!this.currentRedisDbIndex && this.currentRedisDbIndex !== 0) {
                    return
                }
                this.dialogKeyVisible = true;
            },
            addKey() {
                this.loading = true
                Object.assign(this.commonVo, this.addFieldObj)
                this.commonVo.redisDbIndex = this.currentRedisDbIndex
                request({
                    url: '/redis/addKey',
                    method: 'post',
                    data: this.commonVo
                }).then(res => {
                    this.loading = false
                    if (res.data.code === 0) {
                        this.$message.success("新增成功");
                        this.listKeys(this.node_had, this.resolve_had);
                        this.dialogKeyVisible = false
                    }
                })
            },
            delKey() {
                if (!this.commonVo.redisKey) {
                    return
                }
                this.$confirm(`确定删除key ${this.commonVo.redisKey}?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loading = true
                    request({
                        url: '/redis/delKey',
                        method: 'post',
                        data: this.commonVo
                    }).then(res => {
                        this.loading = false
                        if (res.data.code === 0) {
                            this.$message.success("删除成功");
                            this.commonVo.redisKey = ''
                            this.commonVo.redisValue = ''
                            this.listKeys(this.node_had, this.resolve_had);
                        }
                    })
                }).catch(() => {
                });
            },
            fieldRowClick(row) {
                this.commonVo.fieldKey = row.fieldKey
                this.commonVo.redisValue = row.fieldValue
                this.commonVo.oldRedisValue = row.fieldValue
                this.redisValueSize = row.fieldValueSize

            },
            deleteField(row) {
                this.commonVo.redisValue = row.fieldValue
                this.commonVo.fieldKey = row.fieldKey
                this.$confirm(`确定删除?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loading = true
                    request({
                        url: '/redis/delField',
                        method: 'post',
                        data: this.commonVo
                    }).then(res => {
                        this.loading = false
                        if (res.data.code === 0) {
                            this.$message.success(res.data.data);
                            this.reloadValue()
                        }
                    })
                }).catch(() => {
                });
            },
            addField() {
                Object.assign(this.commonVo, this.addFieldObj)
                this.loading = true
                request({
                    url: '/redis/addField',
                    method: 'post',
                    data: this.commonVo
                }).then(res => {
                    this.loading = false
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                        this.dialogFieldVisible = false
                        this.reloadValue()
                    }
                })
            },
            setNewTtl() {
                if (!this.commonVo.redisKeyTtl) {
                    return
                }
                this.$confirm(`确定将TTL设置为 ${this.commonVo.redisKeyTtl}?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loading = true
                    request({
                        url: '/redis/setNewTtl',
                        method: 'post',
                        data: this.commonVo
                    }).then(res => {
                        this.loading = false
                        if (res.data.code === 0) {
                            this.$message.success(res.data.data);
                            this.reloadValue()
                        }
                    })
                }).catch(() => {
                });
            },
            saveValue() {
                if (!this.commonVo.redisValue) {
                    return
                }
                this.$confirm(`确定要保存value?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    request({
                        url: '/redis/saveValue',
                        method: 'post',
                        data: this.commonVo
                    }).then(res => {
                        this.loading = false
                        if (res.data.code === 0) {
                            this.$message.success('保存成功');
                        }
                        this.reloadValue();
                    })
                }).catch(() => {
                });
            },
        },
        mounted() {
            window.pageVue = this
            this.listConnects();
        }
    }
</script>
<style scoped>
    /deep/ .CodeMirror-scroll {
        height: 290px;
        width: 800px;
        padding: 0 !important;
        margin: 0 !important;
        overflow-x: hidden !important;
        overflow-y: auto !important;
    }

    /deep/ .CodeMirror {
        font-size: 20px;
        border: 1px solid #eee;
        height: auto;
    }

    /deep/ .el-select {
        width: 100%;
    }

    /deep/ .el-cascader {
        width: 100%;
    }

    /deep/ .CodeMirror-fullscreen {
        z-index: 9999 !important;
    }

    /deep/ .cm-s-monokai.CodeMirror {
        height: 500px;
    }

    /deep/ .el-header {
        background-color: #B3C0D1;
        color: #333;
        text-align: center;
        line-height: 60px;
    }

    /deep/ .el-aside {
        background-color: #D3DCE6;
        color: #333;
        text-align: center;
        line-height: 200px;
    }

    /deep/ .el-main {
        background-color: #E9EEF3;
        color: #333;
        text-align: center;
        line-height: 160px;
    }

    /deep/ body>.el-container {
        margin-bottom: 40px;
    }

    /deep/ .el-container:nth-child(5) .el-aside,
    .el-container:nth-child(6) .el-aside {
        line-height: 260px;
    }

    /deep/ .el-container:nth-child(7) .el-aside {
        line-height: 320px;
    }
</style>
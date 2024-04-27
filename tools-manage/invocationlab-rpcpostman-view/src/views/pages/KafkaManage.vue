<template>
    <el-container style="margin:6px;" v-loading="loadings.length">
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
                    <el-button @click="getKafkaTrees" type="primary">连接</el-button>
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
                        <template slot="prepend">过滤</template>
                    </el-input>
                </el-header>
                <el-tree :data="kafkaTrees" :props="defaultProps" @node-click="nodeClick" highlight-current
                    ref="dbTreeRef" :filter-node-method="filterNode"></el-tree>
            </div>
            <el-container style="margin: 10px">
                <div style="margin: 6px;">
                    <el-row :gutter="10">
                        <el-col :span="12">
                            <el-input v-model="commonVo.topic"></el-input>
                        </el-col>
                        <el-col :span="2">
                            <el-button @click="getTopicInfo" type="primary">Topic信息</el-button>
                        </el-col>
                        <el-col :span="2" style="margin-left: 10px;">
                            <el-button @click="openSendTopicMsg" type="primary">发送Topic消息</el-button>
                        </el-col>
                        <el-col :span="2" style="margin-left: 34px;">
                            <el-button @click="openCreateTopicDialog" type="primary">新增Topic</el-button>
                        </el-col>
                    </el-row>

                    <el-form ref="formSearch" :inline="true" :model="searchTopicObj" label-width="40px">
                        <el-form-item label="分区">
                            <el-input-number v-model="searchTopicObj.partition" controls-position="right" size="mini"
                                :min="0"></el-input-number>
                        </el-form-item>
                        <el-form-item label="偏移">
                            <el-input-number v-model="searchTopicObj.offset" controls-position="right" size="mini"
                                :min="0"></el-input-number>
                        </el-form-item>
                        <el-form-item label="时间">
                            <el-date-picker style="width: 180px;" value-format="timestamp"
                                v-model="searchTopicObj.timestamp" type="datetime" placeholder="选择日期时间">
                            </el-date-picker>
                        </el-form-item>
                        <el-form-item label="value">
                            <el-input v-model="searchTopicObj.value" size="mini"></el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-tooltip class="item" effect="dark" content="处于性能考虑,记录数如果超过300,则会截断到300返回"
                                placement="bottom">
                                <el-button @click="getTopicMsgList" type="primary">搜索topic</el-button>
                            </el-tooltip>
                        </el-form-item>
                    </el-form>

                    <el-table @row-click="msgRowClick" :data="topicDetails" border style="width: 98%" height="220"
                        highlight-current-row ref="msgTableRef">
                        <el-table-column type="index" label="行号" width="50">
                        </el-table-column>
                        <el-table-column prop="partition" label="分区" width="70">
                        </el-table-column>
                        <el-table-column prop="offset" label="偏移量" width="120">
                        </el-table-column>
                        <el-table-column prop="timestamp" label="时间" width="140" show-overflow-tooltip>
                        </el-table-column>
                        <el-table-column prop="key" label="key" width="120" show-overflow-tooltip>
                        </el-table-column>
                        <el-table-column prop="value" label="value" show-overflow-tooltip>
                        </el-table-column>
                        <el-table-column prop="headers" label="headers" width="120" show-overflow-tooltip>
                        </el-table-column>
                    </el-table>

                    <el-row>
                        <el-col :span="19">
                            <codemirror style="width:800px" ref="myCm" v-model="kafkaValue" :options="cmOptions">
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

        <el-dialog title="新增连接" :visible.sync="dialogVisible" v-if="dialogVisible" :close-on-click-modal="false">
            <el-form ref="form" :model="connectInfo" label-width="150px" v-loading="loadings.length">
                <el-form-item label="连接名称">
                    <el-input v-model="connectInfo.name"></el-input>
                </el-form-item>
                <el-form-item label="bootstrap servers">
                    <el-input v-model="connectInfo.nodes"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="pingConnect" icon="el-icon-s-promotion"
                        :loading="pingLoading">测试连接</el-button>
                    <el-button type="primary" @click="saveConnect">保存</el-button>
                    <el-button @click="dialogVisible=false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="新增Topic" :visible.sync="createTopicVisible" v-if="createTopicVisible"
            :close-on-click-modal="false" v-loading="loadings.length">
            <el-form ref="form1" :model="addTopicObj" label-width="160px">
                <el-form-item label="topic">
                    <el-input v-model="addTopicObj.topic"></el-input>
                </el-form-item>
                <el-form-item label="partition num">
                    <el-input-number v-model="addTopicObj.numPartitions"></el-input-number>
                </el-form-item>
                <el-form-item label="replication factor">
                    <el-input-number v-model="addTopicObj.replicationFactor"></el-input-number>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="createTopic">确定</el-button>
                    <el-button @click="createTopicVisible=false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="Topic信息" :visible.sync="topicInfosVisible" v-if="topicInfosVisible"
            :close-on-click-modal="false" v-loading="loadings.length">
            <span>订阅者:</span>
            <el-button v-if="topicSubscribesLoad" v-loading="topicSubscribesLoad">加载中</el-button>
            <el-tag v-else-if="topicSubscribes.length === 0">无</el-tag>
            <el-tag v-else v-for="item in topicSubscribes" :key="item">{{item}}</el-tag>

            <span>偏移量:</span>
            <el-input-number v-model="clearOffset" controls-position="right" size="mini" :min="0"></el-input-number>
            <el-tooltip class="item" effect="dark" content="若偏移量为空,则清除Topic所有消息" placement="bottom">
                <el-button type="danger" @click="clearTopicMsg">清除Topic偏移量前消息</el-button>
            </el-tooltip>
            <el-button @click="delTopic" type="danger">删除Topic</el-button>

            <el-table :data="topicInfos" border style="width: 98%" height="380" highlight-current-row>
                <el-table-column prop="partition" label="partition id">
                </el-table-column>
                <el-table-column prop="leader" label="leader" show-overflow-tooltip>
                </el-table-column>
                <el-table-column prop="replicas" label="replicas">
                    <template slot-scope="scope">
                        {{scope.row.replicas.join(",")}}
                    </template>
                </el-table-column>
                <el-table-column prop="isr" label="isr">
                    <template slot-scope="scope">
                        {{scope.row.isr.join(",")}}
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>

        <el-dialog title="发送Topic消息" :visible.sync="sendTopicMsgVisible" v-if="sendTopicMsgVisible"
            :close-on-click-modal="false" v-loading="loadings.length">
            <el-form ref="sendTopicForm" :model="sendTopicObj" label-width="80px">
                <el-form-item label="topic">
                    <el-input v-model="sendTopicObj.topic"></el-input>
                </el-form-item>
                <el-form-item label="分区">
                    <el-input-number v-model="sendTopicObj.partition" controls-position="right" size="mini"
                        :min="0"></el-input-number>
                </el-form-item>
                <el-form-item label="headers">
                    <el-tooltip class="item" effect="dark" content="key1=value1;key2=value2;..." placement="bottom">
                        <el-input v-model="sendTopicObj.headers"></el-input>
                    </el-tooltip>
                </el-form-item>
                <el-form-item label="key">
                    <el-input v-model="sendTopicObj.key"></el-input>
                </el-form-item>
                <el-form-item label="value">
                    <el-input type="textarea" rows="8" v-model="sendTopicObj.value"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="sendTopicMsg">确定</el-button>
                    <el-button @click="sendTopicMsgVisible=false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </el-container>
</template>

<script>
    import request from '@/utils/request'

    export default {
        name: 'kafkaManage',
        data() {
            return {
                scrollTopLeft: 0,
                scrollTopRight: 0,
                scrollTopCm: 0,
                searchTopicObj: {
                    timestamp: null,
                    partition: undefined,
                    offset: undefined,
                    value: null,
                },
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
                loadings: [],
                connectInfo: {
                    connectId: '',
                    nodes: '127.0.0.1:9092,127.0.0.1:9092,127.0.0.1:9092',
                    name: '42-44测试集群',
                },
                pingLoading: false,
                dialogVisible: false,
                createTopicVisible: false,
                sendTopicMsgVisible: false,
                url: '---',
                pattern: '',
                clearOffset: undefined,
                connectVos: [],
                kafkaTrees: [],
                topicDetails: [],
                topicInfos: [],
                topicSubscribes: [],
                topicSubscribesLoad: false,
                topicInfosVisible: false,
                defaultProps: {
                    children: 'children',
                    label: 'label',
                    isLeaf: 'isLeaf',
                },
                commonVo: {
                    connectId: '',
                    topic: '',
                },
                addTopicObj: {
                    topic: '',
                    numPartitions: 6,
                    replicationFactor: 1,
                },
                sendTopicObj: {
                    topic: '',
                    headers: '',
                    key: '',
                    value: '',
                },
                kafkaValue: '',
            }
        },
        watch: {
            pattern(val) {
                this.$refs.dbTreeRef.filter(val);
            }
        },
        methods: {
            resetData() {
                this.kafkaValue = ''
                this.commonVo.topic = ''
            },
            valueFormatChange(data) {
                if (data === 2) {
                    this.kafkaValue = JSON.stringify(JSON.parse(this.kafkaValue), null, "  ")
                } else if (data === 1) {
                    this.kafkaValue = JSON.stringify(JSON.parse(this.kafkaValue))
                }
            },
            saveConnect() {
                this.loadings.push('')
                request({
                    url: '/kafka/saveConnect',
                    method: 'post',
                    data: this.connectInfo
                }).then(res => {
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                        this.dialogVisible = false;
                        this.listConnects()
                    }
                }).finally(() => this.loadings.pop())
            },
            pingConnect() {
                this.pingLoading = true
                request({
                    url: '/kafka/pingConnect',
                    method: 'post',
                    data: this.connectInfo
                }).then(res => {
                    this.pingLoading = false
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                    }
                })
            },
            listConnects() {
                request({
                    url: '/kafka/listConnects',
                    method: 'post',
                    data: this.connectInfo
                }).then(res => {
                    if (res.data.code === 0) {
                        this.connectVos = res.data.data
                        if (this.connectVos.length > 0) {
                            this.commonVo.connectId = this.connectVos[0].connectId
                        }
                    }
                })
            },
            patternEnter() {
                this.getKafkaTrees()
            },
            getKafkaTrees() {
                if (this.commonVo.connectId === '') {
                    this.$message.warning("请选择连接");
                    return
                }
                this.loadings.push('')
                request({
                    url: '/kafka/getKafkaTrees/' + this.commonVo.connectId,
                    method: 'get',
                }).then(res => {
                    if (res.data.code === 0) {
                        this.resetData()
                        this.kafkaTrees = res.data.data
                        this.initUrl()
                        this.$nextTick(() => {
                            if (this.pattern) {
                                this.$refs.dbTreeRef.filter(this.pattern)
                            }
                        })
                    }
                }).finally(() => this.loadings.pop())
            },
            filterNode(value, data) {
                if (!value) return true;
                return data.label.indexOf(value) !== -1;
            },
            nodeClick(data, node) {
                if (node.level !== 2) {
                    return
                }
                if (data.kafkaNodeType === 'Consumer') {
                    this.getSubscribeTopics(data.label)
                    return
                }
                if (data.kafkaNodeType === 'Broker') {
                    return
                }
                this.commonVo.topic = data.label
                this.getTopicMsgList();
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
            getTopicInfo() {
                if (!this.commonVo.topic) {
                    return
                }
                this.loadings.push('')
                request({
                    url: `/kafka/getTopicInfo/${this.commonVo.connectId}/${this.commonVo.topic}`,
                    method: 'get',
                }).then(res => {
                    this.loadings.pop()
                    if (res.data.code === 0) {
                        this.topicInfos = res.data.data
                        this.topicInfosVisible = true
                    }
                })
                this.getTopicSubscribes()
            },
            clearTopicMsg() {
                this.$confirm(`确定清除?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loadings.push('')
                    request({
                        url: `/kafka/clearTopicMsg/${this.commonVo.connectId}/${this.commonVo.topic}?offset=${this.clearOffset || ''}`,
                        method: 'post',
                    }).then(res => {
                        this.loadings.pop()
                        if (res.data.code === 0) {
                            this.$message.success(res.data.data)
                            this.topicInfosVisible = false
                            this.getTopicMsgList()
                        }
                    })
                }).catch(() => {
                });
            },
            getTopicSubscribes() {
                this.topicSubscribes = []
                this.topicSubscribesLoad = true
                request({
                    url: `/kafka/getTopicSubscribes/${this.commonVo.connectId}/${this.commonVo.topic}`,
                    method: 'get',
                }).then(res => {
                    this.loadings.pop()
                    if (res.data.code === 0) {
                        this.topicSubscribes = res.data.data
                        this.topicSubscribesLoad = false
                    }
                })
            },
            getSubscribeTopics(groupId) {
                this.loadings.push('')
                request({
                    url: `/kafka/getSubscribeTopics/${this.commonVo.connectId}/${groupId}`,
                    method: 'get',
                }).then(res => {
                    this.loadings.pop()
                    if (res.data.code === 0) {
                        this.$alert(res.data.data.join("、\n"), groupId + '订阅的所有Topic', {
                            confirmButtonText: '确定',
                        });
                    }
                })
            },
            getTopicMsgList() {
                if (!this.commonVo.topic) {
                    return
                }
                this.topicDetails = []
                this.kafkaValue = ''
                this.loadings.push('')
                request({
                    url: `/kafka/getTopicMsgList/${this.commonVo.connectId}/${this.commonVo.topic}`,
                    method: 'post',
                    data: this.searchTopicObj
                }).then(res => {
                    if (res.data.code === 0) {
                        this.topicDetails = res.data.data
                        this.kafkaValue = ''
                    }
                }).finally(() => this.loadings.pop())
            },
            openCreateTopicDialog() {
                if (!this.commonVo.connectId) {
                    return
                }
                this.createTopicVisible = true;
            },
            createTopic() {
                this.loadings.push('')
                request({
                    url: `/kafka/createTopic/${this.commonVo.connectId}`,
                    method: 'post',
                    data: this.addTopicObj
                }).then(res => {
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                        this.getKafkaTrees();
                        this.createTopicVisible = false
                    }
                }).finally(() => this.loadings.pop())
            },
            delTopic() {
                if (!this.commonVo.topic) {
                    return
                }
                this.$confirm(`确定删除Topic ${this.commonVo.topic}?`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.loadings.push('')
                    request({
                        url: `/kafka/delTopic/${this.commonVo.connectId}/${this.commonVo.topic}`,
                        method: 'post',
                    }).then(res => {
                        if (res.data.code === 0) {
                            this.$message.success(res.data.data)
                            this.commonVo.topic = ''
                            this.topicInfosVisible = false
                            this.getKafkaTrees()
                        }
                    }).finally(() => this.loadings.pop())
                }).catch(() => {
                });
            },
            msgRowClick(row) {
                this.kafkaValue = row.value
            },
            openSendTopicMsg() {
                if (!this.commonVo.connectId) {
                    return
                }
                this.sendTopicObj.topic = this.commonVo.topic
                this.sendTopicMsgVisible = true
            },
            sendTopicMsg() {
                this.loadings.push('')
                request({
                    url: `/kafka/sendTopicMsg/${this.commonVo.connectId}`,
                    method: 'post',
                    data: this.sendTopicObj
                }).then(res => {
                    if (res.data.code === 0) {
                        this.$message.success(res.data.data);
                        this.getTopicMsgList()
                        this.sendTopicMsgVisible = false
                    }
                }).finally(() => this.loadings.pop())
            },
        },
        deactivated() {
            if (this.$refs.dbTreeRef) {
                this.scrollTopLeft = this.$refs.dbTreeRef.$el.scrollTop
            }
            if (this.$refs.msgTableRef) {
                this.scrollTopRight = this.$refs.msgTableRef.$el.getElementsByClassName('el-table__body-wrapper')[0].scrollTop
            }
            this.scrollTopCm = this.$refs.myCm.codemirror.getScrollerElement().scrollTop
        },
        activated() {
            window.pageVue = this
            if (this.$refs.dbTreeRef) {
                this.$refs.dbTreeRef.$el.scrollTo(0, this.scrollTopLeft)
            }
            if (this.$refs.msgTableRef) {
                setTimeout(() => {
                    this.$refs.msgTableRef.$el.getElementsByClassName('el-table__body-wrapper')[0].scrollTo(0, this.scrollTopRight)
                }, 100);
            }
            this.$refs.myCm.codemirror.getScrollerElement().scrollTo(0, this.scrollTopCm)
        },
        mounted() {
            window.pageVue = this
            this.listConnects()
        }
    }
</script>
<style scoped>
    /deep/ .CodeMirror-scroll {
        height: 240px;
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
        height: 300px;
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

    /deep/ .el-tree {
        height: 420px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    /deep/ .el-tree-node__children {
        width: 280px;
        overflow-x: auto;
    }
</style>
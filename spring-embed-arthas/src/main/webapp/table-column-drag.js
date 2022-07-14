Vue.component('el-table-drag', {
    props: {
        id: {
          type: String,
          required: true
        },
        height: {
            type: Number,
            default: null
        },
        border: {
            type: Boolean,
            default: false
        },
        vLoading: {
            type: Boolean,
            default: false
        },
        elementLoadingText: {
            type: String,
            default: "请稍后..."
        },
        data: {
          default: function () {
            return []
          },
          type: Array
        },
        header: {
          default: function () {
            return []
          },
          type: Array
        },
    },
    template: `
        <div :id="id" class="table-drag"
            :class="{'table-drag_moving': dragState.dragging}">
            <el-table
                :data="tableData"
                :border="border"
                :v-loading="vLoading"
                :element-loading-text="elementLoadingText"
                :height="height"
                :header-cell-class-name="headerCellClassName"
                cell-class-name="table-body-cell-style"
                @header-dragend="headerDragend"
                @select-all="toggleSelectionAll">
                <el-table-column v-for="(col, index) in tableHeader"
                    :key="index"
                    :prop="col.prop"
                    :label="col.label"
                    :width="col.width"
                    :sortable="col.sortable"
                    :show-overflow-tooltip="col.showOverflowTooltip"
                    :column-key="index + ''">
                    <template slot="header" slot-scope="scope">
                        <el-checkbox v-if="col.type==='selection'"
                            @change="toggleSelectionAll">
                        </el-checkbox>
                        <div class="thead-cell"
                            v-else
                            @mousedown="handleMouseDown($event, scope.column)"
                            @mouseup="handleMouseUp($event, scope.column)"
                            @mousemove="handleMouseMove($event, scope.column)">
                            <a>{{scope.column.label}}</a>
                        </div>
                    </template>
                    <template slot-scope="scope">
                        <slot :name="col.prop" :row="scope.row">
                            <el-checkbox v-if="col.type==='selection'"
                                v-model="checkData[scope.$index]"
                                @change="checkChange($event, scope.$index)">
                            </el-checkbox>
                            <div v-else>{{scope.row[col.prop]}}</div>
                        </slot>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    `,
    data() {
        return {
            tableHeader: this.header,
            tableData: this.data,
            dragState: {
                // 起始元素的 index
                start: -1,
                // 结束元素的 index
                end: -1,
                // 移动鼠标时所覆盖的元素 index
                move: -1,
                // 是否正在拖动
                dragging: false,
                // 拖动方向
                direction: undefined
            },
            table: null,
            checkData: {},
            selection: [],
        }
    },
    created() {
        this.tableData.forEach((it, index) => {
            this.$set(this.checkData, index, false)
        })
    },
    mounted() {
        this.$nextTick(() => {
            this.table = document.querySelector("#" + this.id)
            // 鼠标移出拖拽元素且松开了左键范围外,需重新初始化状态
            document.onmouseup = this.resetDragIfNecessary
            this.handleCaretLocation()
        })
    },
    methods: {
        toggleSelectionAll(check) {
            if(check){
                this.data.forEach((item,index) => {
                    this.checkData[index] = true
                })
                this.selection.push(...this.data)
            }else{
                this.data.forEach((item,index) => {
                    this.checkData[index] = false
                })
                this.selection.splice(0, this.selection.length)
            }
            this.$emit("select-all", this.selection)
        },
        checkChange(check, index) {
            if(check){
                this.selection.push(this.data[index])
            }else{
                let delIndex
                for (let i = 0; i < this.selection.length; i++) {
                    const element = this.selection[i];
                    if(element === this.data[index]){
                        delIndex = i
                        break;
                    }
                }
                this.selection.splice(delIndex, 1)
            }
            this.$emit("change", this.selection)
        },
        // 调整过列的宽度,将列的新宽度映射回tableHeader
        headerDragend(newWidth, oldWidth, column, event) {
            this.tableHeader.forEach(it => {
                if(it.prop === column.property){
                    it.width = newWidth
                }
            })
        },
        handleCaretLocation() {
            this.$nextTick(() => {
                this.table.querySelectorAll(".is-sortable > .cell").forEach(it => {
                    let caret = it.querySelector(".caret-wrapper")
                    it.querySelector(".thead-cell").appendChild(caret)
                })
            })
        },
        headerCellClassName({column, columnIndex}) {
            let cls = (columnIndex === this.dragState.move ? `drag_active_${this.dragState.direction}` : '')
            return cls +" table-header-cell-style"
        },
        // 按下鼠标开始拖动,记录起始列
        handleMouseDown (e, column) {
            this.dragState.dragging = true
            this.dragState.start = parseInt(column.columnKey)
        },
        // 拖动中
        handleMouseMove (e, column) {
            if (this.dragState.dragging) {
                let index = parseInt(column.columnKey)
                if (index - this.dragState.start !== 0) {
                    // 判断拖动方向
                    this.dragState.direction = index - this.dragState.start < 0 ? 'left' : 'right'
                    this.dragState.move = parseInt(column.columnKey)
                } else {
                    this.dragState.direction = undefined
                }
            }
        },
        // 鼠标放开结束拖动
        handleMouseUp (e, column) {
            // 记录结束列
            this.dragState.end = parseInt(column.columnKey)
            this.dragColumn(this.dragState)
            this.resetDragState()
        },
        // 拖动易位
        dragColumn ({start, end, direction}) {
            if(this.dragState.start !== -1 && this.dragState.start !== this.dragState.end){
                let tempData = []
                let left = direction === 'left'
                let min = left ? end : start - 1
                   let max = left ? start + 1 : end
                for (let i = 0; i < this.tableHeader.length; i++) {
                    if (i === end) {
                        tempData.push(this.tableHeader[start])
                    } else if (i > min && i < max) {
                        tempData.push(this.tableHeader[ left ? i - 1 : i + 1 ])
                    } else {
                        tempData.push(this.tableHeader[i])
                    }
                }
                this.tableHeader = tempData
                // tableHeader变了后内容没有相应发生变化,可能是elementUI的bug,须有这行代码来强制触发内容重渲染
                this.tableData.splice(0, 0)
                this.handleCaretLocation()
            }
        },
        resetDragIfNecessary(e) {
            if(e.buttons === 0) {
                this.resetDragState()
            }
        },
        resetDragState() {
            if(this.dragState.dragging) {
                this.dragState.start = -1
                this.dragState.end = -1
                this.dragState.move = -1
                this.dragState.dragging = false
                this.dragState.direction = undefined
            }
        }
    },
})


/**
 * @author yudong
 * @date   2022/7/14
 * 为elementUI的table组件加上列拖拽功能,用法:
 * this.$nextTick(()=>{
 *     new BmTableDrag({tableRef: this.$refs.tableRef})
 * })
 */
 (function (global, factory) {
    global.BmTableDrag = factory(global)
}(window, function (global) {

    function BmTableDrag(options = {}) {
        if (!(this instanceof BmTableDrag)) {
            throw new Error('BmTableDrag is a constructor and should be called with the `new` keyword')
        }
        this._initParams(options)
    }

    let btd
    BmTableDrag.prototype._initParams = function (options) {
        btd = this
        btd.dragState = {
            // 起始元素的 index
            start: -1, 
            // 结束元素的 index
            end: -1, 
            // 移动鼠标时所覆盖的元素 index
            move: -1, 
            // 是否正在拖动
            dragging: false, 
            // 拖动方向
            direction: undefined 
        }
        btd.tableRef = options.tableRef
        btd.tableDiv = btd.tableRef.$el
        let prev = btd.tableDiv.previousElementSibling
        btd.$wTableDiv = $('<div class="table-drag"></div>')
        btd.$wTableDiv.append(btd.tableDiv).insertAfter(prev)
        btd.columns = btd.tableRef.store.states._columns
        let existSelection = btd.columns[0].type==="selection"
        btd.headThCells = btd.tableDiv.querySelectorAll("div.el-table__header-wrapper .table-header-cell-style .cell")
        btd.headThCells.forEach((it,index) => {
            if(index===0 && existSelection){
                return
            }
            it.addEventListener("mousedown", btd._handleMouseDown)
            it.addEventListener("mousemove", btd._handleMouseMove)
            it.addEventListener("mouseup", btd._handleMouseUp)
        })
        
        document.addEventListener("mouseup", btd.resetDragIfNecessary)
    }

    // 按下鼠标开始拖动
    BmTableDrag.prototype._handleMouseDown = function(e) {
        if($(e.currentTarget).hasClass("noclick")){
            return
        }
        btd.$wTableDiv.addClass("table-drag_moving")
        btd.dragState.dragging = true
        btd.dragState.start = btd._realIndex(e)
    }

    // 拖动中
    BmTableDrag.prototype._handleMouseMove = function(e) {
        if (!btd.dragState.dragging) {
            return
        }
        // 记录起始列
        let index = btd._realIndex(e)
        if (index - btd.dragState.start !== 0) {
            // 判断拖动方向
            btd.dragState.direction = index - btd.dragState.start < 0 ? 'left' : 'right'
            btd.dragState.move = index
        } else {
            btd.dragState.direction = undefined
        }

        if(btd.columns[index].type==="selection"){
            return
        }

        btd._resetHeadThCellCls()
        let targetTh
        let $target = $(e.target)
        if($target.hasClass("sort-caret")){
            targetTh = e.target.parentElement.parentElement.parentElement
        }else if($target.hasClass("caret-wrapper")){
            targetTh = e.target.parentElement.parentElement
        }else{
            targetTh = e.target.parentElement
        }
        $(targetTh).addClass(`drag_active_${btd.dragState.direction}`)
    }

    // 鼠标放开结束拖动
    BmTableDrag.prototype._handleMouseUp = function(e) {
        let key = btd._realIndex(e)
        btd.dragState.end = parseInt(key) 
        btd._dragColumn(btd.dragState)
        // 初始化拖动状态
        btd._resetDragState()
    }

    // 拖动易位
    BmTableDrag.prototype._dragColumn = function({ start, end, direction }) {
        if (btd.dragState.start !== -1 && btd.dragState.start !== btd.dragState.end) {
            let tempData = []
            let left = direction === 'left'
            let min = left ? end : start - 1
            let max = left ? start + 1 : end
            for (let i = 0; i < btd.columns.length; i++) {
                if (i === end) {
                    tempData.push(btd.columns[start])
                } else if (i > min && i < max) {
                    tempData.push(btd.columns[left ? i - 1 : i + 1])
                } else {
                    tempData.push(btd.columns[i])
                }
            }
            btd.tableRef.store.states._columns.splice(0, btd.columns.length)
            btd.tableRef.store.states._columns.push(...tempData)

            btd.tableRef.store.updateColumns()
            btd.tableRef.data.splice(0,0)
        }
    }

    BmTableDrag.prototype._realIndex = function(e){
        let list = btd.columns
        for (let i = 0; i < list.length; i++) {
            const id = list[i].id
            if(id === e.currentTarget.parentElement.classList[0]){
                return i
            }
        }
        return -1
    }

    BmTableDrag.prototype._resetHeadThCellCls = function() {
        btd.headThCells.forEach(it=>{
            $(it.parentElement).removeClass(`drag_active_left drag_active_right`)
        })
    }

    BmTableDrag.prototype.resetDragIfNecessary = function(e) {
        if (e.buttons === 0) {
            btd._resetDragState()
        }
    }

    BmTableDrag.prototype._resetDragState = function() {
        if (btd.dragState.dragging) {
            btd._resetHeadThCellCls()
            btd.$wTableDiv.removeClass("table-drag_moving")
            btd.dragState.start = -1
            btd.dragState.end = -1
            btd.dragState.move = -1
            btd.dragState.dragging = false
            btd.dragState.direction = undefined
        }
    }

    return BmTableDrag
}))
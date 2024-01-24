import React, { Ref, useEffect, useImperativeHandle, useState } from 'react';
import type { TableColumnsType } from 'antd';
import './index.less'
import { Button, Input, Modal, Select, Table, Tooltip } from 'antd';
import { ModalForm } from '@ant-design/pro-components';
import useProjectStore from "@/store/project/useProjectStore";
import * as QueryResultUtils from '@/pages/design/query/component/QueryResultUtils';
import AceEditor from "react-ace";
import "ace-builds/src-noconflict/mode-mysql";
import "ace-builds/src-noconflict/ext-language_tools";
import 'ace-builds/src-noconflict/theme-xcode';

interface DataType {
    key: React.Key;
    index: Number;
    title: string;
    chnname: string;
    expandedTableData: ExpandedDataType[];
    ddl: string;
    ownerModule: string;
}

interface ExpandedDataType {
    key: React.Key;
    index: Number;
    name: string;
    chnname: string;
    dataType: string;
    remark: string;
    pk: string;
    notNull: string;
    autoIncrement: string;
    defaultValue: string;
}

export type MetaDataSearchProps = {
    onRef?: Ref<any>;
};

const MetaDataSearch: React.FC<MetaDataSearchProps> = (props) => {
    const projectDispatch = useProjectStore(state => state.dispatch);

    const [showSearch, setShowSearch] = useState(false)

    const [tableName, setTableName] = useState('')
    const [tableComment, setTableComment] = useState('')
    const [columnName, setColumnName] = useState('')
    const [columnComment, setColumnComment] = useState('')
    const [conditionType, setConditionType] = useState('and')

    const [originTables, setOriginTables] = useState<DataType[]>([])
    const [originTableColumnsObj, setOriginTableColumnsObj] = useState<any>({})
    const [originTableIndexsObj, setOriginTableIndexsObj] = useState<any>({})

    const [tables, setTables] = useState<DataType[]>()

    useImperativeHandle(props.onRef, () => ({
        showSearchForm: (_: any) => {
            setShowSearch(true)
            const modules = projectDispatch.getOriginalModules()
            const obj = getTableAndColumns(modules)

            setOriginTables(obj.tables)
            setOriginTableColumnsObj(obj.tableColumns)
            setOriginTableIndexsObj(obj.tableIndexs)
        },
    }));

    const getTableAndColumns = (modules: any) => {
        const tableColumns = {}
        const tableIndexs = {}
        const tables = modules
            .map((module: any, i: number) => {
                return module.entities
                    .map((entity: any, j: number) => {

                        tableColumns[entity.title] = entity.fields
                        tableIndexs[entity.title] = entity.indexs

                        return {
                            key: i + "-" + j,
                            title: entity.title,
                            chnname: entity.chnname,
                            ddl: entity.originalCreateTableSql,
                            ownerModule: module.name + " " + module.chnname
                        }
                    })
            })
            .flat()
            .sort((t1: any, t2: any) => t1.title.localeCompare(t2.title))
        return { tables, tableColumns, tableIndexs }
    }

    useEffect(() => {

        filterAndSetTables()

    }, [originTables, originTableColumnsObj, tableName, tableComment, columnName, columnComment, conditionType])

    const filterAndSetTables = () => {
        const tmpList = originTables
            .map((it) => {
                const matches: Boolean[] = []
                if (tableName) {
                    matches.push(it.title.includes(tableName))
                }

                if (tableComment) {
                    matches.push(it.chnname.includes(tableComment))
                }

                let columnList = originTableColumnsObj[it.title]
                if (columnName && columnComment) {
                    columnList = originTableColumnsObj[it.title]
                        .filter((innerIt: any) => {
                            if (conditionType === 'and') {
                                return innerIt.name.includes(columnName) && innerIt.chnname.includes(columnComment)
                            } else if (conditionType === 'or') {
                                return innerIt.name.includes(columnName) || innerIt.chnname.includes(columnComment)
                            } else {
                                throw new Error(conditionType)
                            }
                        })
                    matches.push(columnList.length > 0)
                } else if (columnName) {
                    columnList = originTableColumnsObj[it.title]
                        .filter((innerIt: any) => innerIt.name.includes(columnName))
                    matches.push(columnList.length > 0)
                } else if (columnComment) {
                    columnList = originTableColumnsObj[it.title]
                        .filter((innerIt: any) => innerIt.chnname.includes(columnComment))
                    matches.push(columnList.length > 0)
                }

                let match
                if (matches.length === 0) {
                    match = true
                } else {
                    if (conditionType === 'and') {
                        match = matches.every(it => it)
                    } else if (conditionType === 'or') {
                        match = matches.some(it => it)
                    } else {
                        throw new Error(conditionType)
                    }
                }

                if (!match) {
                    return null
                }

                let tmpColumnList = [...columnList]
                tmpColumnList = tmpColumnList
                    .map((innerIt: ExpandedDataType, i: number) => {
                        let obj = { ...innerIt }
                        obj.index = i + 1
                        return obj
                    })
                if (originTableIndexsObj[it.title]) {
                    let tmpIndexList = [...originTableIndexsObj[it.title]]
                    tmpIndexList = tmpIndexList
                        .map((innerIt: any, i: number) => {
                            return {
                                index: i + 1,
                                name: innerIt.name,
                                chnname: innerIt.fields.join(', '),
                                dataType: '',
                                remark: innerIt.isUnique ? '唯一' : '',
                            }
                        })
                    tmpColumnList = tmpColumnList.concat(tmpIndexList)
                }

                let tmpIt: any = { ...it }
                tmpIt.expandedTableData = tmpColumnList
                return tmpIt
            })
            .filter(it => it != null)
            .map((it, i) => {
                it.index = i + 1
                return it
            })

        setTables(tmpList)
    }

    const expandedColumns: TableColumnsType<ExpandedDataType> = [
        {
            title: '列序号', dataIndex: 'index', key: 'index', width: 60, render: (val, record) => {
                if (record.dataType === '') {
                    return '索引' + val
                } else {
                    return '列' + val
                }
            }
        },
        { title: '列名', dataIndex: 'name', key: 'name', width: 200 },
        { title: '列注释', dataIndex: 'chnname', key: 'chnname', width: 200 },
        { title: '数据类型', dataIndex: 'dataType', key: 'dataType', width: 150 },
        { title: '说明', dataIndex: 'remark', key: 'remark', width: 100 },
        { title: '主键', dataIndex: 'pk', key: 'pk', width: 80, render: val => val ? '是' : '' },
        { title: '自增', dataIndex: 'autoIncrement', key: 'autoIncrement', width: 80, render: val => val ? '是' : '' },
        { title: '不为空', dataIndex: 'notNull', key: 'notNull', width: 80, render: val => val ? '是' : '' },
        { title: '默认值', dataIndex: 'defaultValue', key: 'defaultValue', width: 150 },
    ];

    const expandedRowRender = (row: DataType) => {
        return <Table rowKey='name' columns={expandedColumns} dataSource={row.expandedTableData} pagination={false}
            scroll={{ y: 'calc(100vh - 460px)' }} size='small' />;
    };

    const columns: TableColumnsType<DataType> = [
        {
            title: '表序号', dataIndex: 'index', key: 'index', width: 100, render: (text) => {
                return '表' + text
            }
        },
        { title: '表名', dataIndex: 'title', key: 'title' },
        { title: '表注释', dataIndex: 'chnname', key: 'chnname' },
        { title: '所属模块', dataIndex: 'ownerModule', width: 260, key: 'ownerModule' },
        {
            title: '表DDL', dataIndex: 'ddl', key: 'ddl', width: 100, render: (text, record) => {
                return <Button size={"small"} type='link' onClick={() => {
                    Modal.info({
                        title: record.title,
                        content: <AceEditor value={text} width="900px" height="470px" mode='mysql' theme="xcode"
                            showGutter={true} showPrintMargin={false} editorProps={{ $blockScrolling: true }}
                            setOptions={{ readOnly: true, wrap: false }} />,
                        width: 1000,
                        closable: true,
                        mask: false,
                        maskClosable: true,
                        okText: '复制DDL',
                        onOk: () => { QueryResultUtils.copyValue(text) }
                    })
                }}>DDL</Button>
            }
        },
    ];

    return (
        <>
            <ModalForm
                width={1250}
                open={showSearch}
                title="搜索元数据"
                modalProps={{
                    onCancel: () => setShowSearch(false),
                    cancelText: '关闭',
                }}
                onFinish={async (_: any) => {
                    setShowSearch(false)
                    return true
                }}
            >
                <Input addonBefore="表名" style={{ width: 240 }} placeholder='模糊搜索' value={tableName} size='small'
                    onChange={e => setTableName(e.target.value)} />
                <Input addonBefore="表注释" style={{ width: 240, marginLeft: 10 }} placeholder='模糊搜索' value={tableComment} size='small'
                    onChange={e => setTableComment(e.target.value)} />
                <Input addonBefore="列名" style={{ width: 240, marginLeft: 10 }} placeholder='模糊搜索' value={columnName} size='small'
                    onChange={e => setColumnName(e.target.value)} />
                <Input addonBefore="列注释" style={{ width: 240, marginLeft: 10, marginRight: 10 }} placeholder='模糊搜索' value={columnComment} size='small'
                    onChange={e => setColumnComment(e.target.value)} />
                <Tooltip title="搜索条件关系">
                    <Select
                        size='small'
                        style={{ marginRight: 10 }}
                        value={conditionType}
                        onChange={e => { setConditionType(e) }}>
                        <Select.Option value='and' key='1'>且</Select.Option>
                        <Select.Option value='or' key='2'>或</Select.Option>
                    </Select>
                </Tooltip>
                <Button size='small' onClick={() => {
                    setTableName('')
                    setTableComment('')
                    setColumnName('')
                    setColumnComment('')
                    setConditionType('and')
                }}>
                    重置查询
                </Button>
                <Table
                    columns={columns}
                    expandable={{ expandedRowRender, columnWidth: 30, expandRowByClick: true }}
                    dataSource={tables}
                    size='small'
                    scroll={{ y: 'calc(100vh - 310px)', x: "120px" }}
                    pagination={false}
                    style={{ height: 'calc(100vh - 280px)' }}
                />
            </ModalForm>
        </>
    );
};

export default React.memo(MetaDataSearch)

import { EditableProTable, } from "@ant-design/pro-components";
import './index.less';
import { Button, Modal, Pagination, message, } from 'antd';
import * as cache from "@/utils/cache";
import * as QueryResultUtils from './QueryResultUtils';
import React, { Ref, useEffect, useRef, useState } from "react";
import InputContextMenu from "./InputContextMenu";
import AesContextMenu from "./AesContextMenu";
import { ExclamationCircleOutlined, ExportOutlined, ReloadOutlined, SaveOutlined } from "@ant-design/icons";
import { Tooltip } from "@mui/material";

export type QueryResultProps = {
  tableResult: {
    columns: string[], dataSource: any[], total: number, realTotal: number, callFromPagination: boolean, queryKey: number,
    tableName: string, page: number, pageSize: number, showPagination: boolean, primaryKeys: string[], tableColumns: {}
  };
  onRef?: Ref<any>;
  submitQueryTableChange: Function;
  exportSql: Function;
  executeSqlWhenPaginationChange: Function;
  getTableRecordTotal: Function;
};


const QueryResult: React.FC<QueryResultProps> = (props) => {
  const [editableKeys, setEditableRowKeys] = useState<React.Key[]>([]);
  const [dataSource, setDataSource] = useState<any[]>([]);
  const [delBtnStyle, setDelBtnStyle] = useState({});
  const [editBtnStyle, setEditBtnStyle] = useState({});
  const [columns, setColumns] = useState<any>([]);
  const [tableRecordTotal, setTableRecordTotal] = useState<number | null>()
  const [btnLoading, setBtnLoading] = useState(false)

  const tableRef = useRef<any>(null);
  const aesContextMenuRef = useRef<any>(null);

  useEffect(() => {
    setColumns(getColumns())
    setDataSource(props.tableResult.dataSource)
    setEditableRowKeys(props.tableResult.columns)
    setDelBtnStyle({})
    setEditBtnStyle({})
    if (!props.tableResult.callFromPagination) {
      setTableRecordTotal(null)
    }
    if (props.tableResult.realTotal) {
      setTableRecordTotal(props.tableResult.realTotal)
    }
  }, [props.tableResult.queryKey])

  useEffect(() => {
    setColumns((oldColumns: any) => {
      let newColumns = getColumns()
      newColumns.forEach((it, index) => {
        it.width = oldColumns[index].width
      })
      return newColumns
    })
  }, [editBtnStyle, delBtnStyle])

  const getColumns = () => {
    let fieldMapObj: {} = {}
    if (props.tableResult.tableName) {
      // @ts-ignore
      fieldMapObj = props.onRef?.current?.getDbTableFieldsMap(props.tableResult.tableName) || {}
    }
    let noPrimaryKey = props.tableResult.primaryKeys.length === 0 && props.tableResult.columns.length > 0
    const columnsTmp2: any[] = props.tableResult.columns
      .filter(columnName => columnName !== 'erdRowKey')
      .map((columnName: string, index: number) => {
        let field = QueryResultUtils.getSuitField(columnName, props.tableResult.tableColumns, fieldMapObj)
        return {
          title: QueryResultUtils.handleTitle(columnName, field),
          tooltip: QueryResultUtils.handleTooltip(field),
          key: columnName,
          onHeaderCell: (column: any) => {
            let tmpIndex = noPrimaryKey ? index + 1 : index
            return QueryResultUtils.onHeaderCell(column, tmpIndex, setColumns);
          },
          dataIndex: columnName,
          ellipsis: true,
          width: 153,
          render: (_: any, record: any) => {
            if (record[columnName] === null) {
              return <span style={{ fontWeight: '100' }}>{"<null>"}</span>
            }
            if (typeof record[columnName] === 'boolean') {
              return <span>{record[columnName] + ''}</span>;
            }
            return <span style={delBtnStyle[record.erdRowKey] || editBtnStyle[record.erdRowKey]}
              onContextMenu={(e) => {
                aesContextMenuRef?.current?.showContext(e)
                e.preventDefault()
              }}
              onDoubleClick={() => { QueryResultUtils.copyValue(record[columnName]) }}>
              {record[columnName]}
            </span>
          },
          renderFormItem: (row: any) => {
            return <InputContextMenu cellValue={row.entity[row.key]} />
          },
        }
      })
    let columnsTmp
    if (noPrimaryKey) {
      const columnsTmp1: any[] = [
        {
          title: "行号",
          key: "rowNumber",
          dataIndex: "rowNumber",
          width: 50,
          render: (_: any, __: any, rowIndex: number, ___: any) => {
            return <span>{(rowIndex + 1) + ''}</span>;
          }
        }
      ]
      columnsTmp = columnsTmp1.concat(columnsTmp2)
    } else {
      columnsTmp = columnsTmp2
    }
    if (props.tableResult.primaryKeys.length > 0) {
      columnsTmp.push({
        title: '操作',
        valueType: 'option',
        key: 'option',
        width: 150,
        fixed: 'right',
        render: (_: any, record: any, __: number, action: any) => [
          <a
            style={delBtnStyle[record.erdRowKey] || editBtnStyle[record.erdRowKey]}
            key="editable"
            onClick={(e) => {
              if (!record.rowOperationType) {
                record.rowOperationType = 'preUpdate'
              }
              action?.startEditable?.(record.erdRowKey);
            }}
          >
            编辑
          </a>,
          <a
            style={delBtnStyle[record.erdRowKey] || editBtnStyle[record.erdRowKey]}
            key="delete"
            onClick={(_) => {
              setDelBtnStyle((it: {}) => {
                let obj = Object.assign({}, it)
                obj[record.erdRowKey] = { color: "red" }
                return obj
              })
              record.rowOperationType = 'delete'
            }}
          >
            删除
          </a>,
          <a
            style={delBtnStyle[record.erdRowKey] || editBtnStyle[record.erdRowKey]}
            key="add"
            onClick={(_) => {
              let newRecord = JSON.parse(JSON.stringify(record))
              Object.entries(record)
                .forEach(([key, value]) => {
                  if (value === null) {
                    newRecord[key] = '<null>'
                  }
                })
              newRecord.rowOperationType = 'preInsert'
              newRecord.erdRowKey = Math.floor(Math.random() * 100000000)
              tableRef?.current?.addEditRecord(newRecord)
            }}
          >
            复制新增
          </a>,
        ],
      })
    }
    return columnsTmp;
  }

  const paginationOnChange = (current: number, pageSize: number) => {
    props.executeSqlWhenPaginationChange(current, pageSize)
  }

  const getTableRecordTotal = () => {
    setBtnLoading(true)
    props.getTableRecordTotal()
      .then((res: any) => {
        setBtnLoading(false)
        if (!res.data) {
          return
        }
        setTableRecordTotal(res.data)
      })
  }

  const renderTotal = () => {
    if (tableRecordTotal !== null) {
      return tableRecordTotal
    }
    if (props.tableResult.realTotal !== null) {
      return props.tableResult.realTotal
    }
    return props.tableResult.total
  }

  const renderTotalTxt = () => {
    if (tableRecordTotal !== null) {
      return tableRecordTotal + ' 条'
    }
    if (props.tableResult.realTotal !== null) {
      return props.tableResult.realTotal + ' 条'
    }
    return props.tableResult.total + '+ 条'
  }

  return (<>
    <AesContextMenu onRef={aesContextMenuRef} />
    <EditableProTable
      actionRef={tableRef}
      components={QueryResultUtils.components}
      size={'small'}
      scroll={{ x: 1300, y: 'calc(100vh - 540px)' }}
      headerTitle="可编辑表格(双击单元格复制其内容)"
      rowKey="erdRowKey"
      columns={columns}
      value={dataSource}
      bordered
      onChange={(it: any) => setDataSource(it)}
      editable={{
        type: 'multiple',
        editableKeys,
        saveText: '确定',
        onSave: async (_: any, row: any, __) => {
          let style: {}
          if (row.rowOperationType === 'preInsert' || row.rowOperationType === 'insert') {
            row.rowOperationType = 'insert'
            style = { color: "gold" }
          } else {
            if (row.rowOperationType !== 'delete') {
              row.rowOperationType = 'update'
              style = { color: "green" }
            }
          }
          setEditBtnStyle((it: {}) => {
            let obj = Object.assign({}, it)
            obj[row.erdRowKey] = style
            return obj;
          })
        },
        onChange: setEditableRowKeys,
        actionRender: (_, __, defaultDom) => {
          return [
            defaultDom.save,
            defaultDom.cancel,
          ];
        }
      }}
      recordCreatorProps={{
        position: 'bottom',
        creatorButtonText: '新增',
        record: () => ({
          erdRowKey: Math.floor(Math.random() * 100000000),
          rowOperationType: 'preInsert',
        }),
        onClick: () => {
        }
      }}
      search={false}
      options={false}
      dateFormatter="string"
      toolBarRender={() => [
        <Button key="button" size={"small"} icon={<ExportOutlined />} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('json')
        }}>
          导出为json
        </Button>,
        <Button key="button" size={"small"} icon={<ExportOutlined />} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('csv')
        }}>
          导出为csv
        </Button>,
        <Button key="button" size={"small"} icon={<ExportOutlined />} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('xls')
        }}>
          导出为Excel
        </Button>,
        <Button key="button" size={"small"} icon={<ExportOutlined />} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('sqlInsert')
        }}>
          导出为sql(insert)
        </Button>,
        <Button key="button" size={"small"} icon={<ExportOutlined />} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('sqlUpdate')
        }}>
          导出为sql(update)
        </Button>,
        <Button key="button" type="primary" icon={<SaveOutlined />} size={"small"} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          let list = dataSource.filter((row: any) => row.rowOperationType && !row.rowOperationType.includes('pre'))
          // console.log("submit table change:", list);
          if (list.length === 0) {
            message.warning("表格数据没有发生变动!");
            return
          }
          if (cache.isProEnv()) {
            Modal.confirm({
              title: '警告',
              icon: <ExclamationCircleOutlined />,
              content: '确定提交改动?',
              okText: '确认',
              cancelText: '取消',
              onOk: () => {
                props.submitQueryTableChange(list)
              }
            });
          } else {
            props.submitQueryTableChange(list)
          }
        }}>
          提交表格变动回db
        </Button>,
      ]}
    />
    {props.tableResult.showPagination && <div style={{ display: 'flex', float: 'right' }}>
      <Pagination size="small" style={{ textAlign: 'right', marginRight: 6 }} onChange={paginationOnChange}
        current={props.tableResult.page} pageSize={props.tableResult.pageSize}
        total={renderTotal()} pageSizeOptions={[5, 20, 50, 100]} showSizeChanger />
      <span style={{ paddingTop: 3 }}>总数：</span>
      <Tooltip title="点击更新(运行 select count(*) from ...)">
        <Button style={{ height: 26, padding: 2 }} type="text" onClick={getTableRecordTotal} loading={btnLoading}
          icon={<ReloadOutlined />}>
          {renderTotalTxt()}
        </Button>
      </Tooltip>
    </div>}
  </>);
};

export default React.memo(QueryResult)

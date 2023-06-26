import { EditableProTable, } from "@ant-design/pro-components";
import { Button, Pagination, message, } from 'antd';
import React, { Ref, useEffect, useRef, useState } from "react";
import InputContextMenu from "./InputContextMenu";

export type QueryResultProps = {
  tableResult: {
    columns: string[], dataSource: any[], total: number, queryKey: number, tableName: string,
    page: number, pageSize: number, showPagination: boolean, primaryKeys: string[], tableColumns: {}
  };
  onRef?: Ref<any>;
  submitQueryTableChange: Function;
  exportSql: Function;
  executeSqlWhenPaginationChange: Function;
};


const QueryResult: React.FC<QueryResultProps> = (props) => {

  const [editableKeys, setEditableRowKeys] = useState<React.Key[]>(props.tableResult.columns);
  const [dataSource, setDataSource] = useState<any[]>(props.tableResult.dataSource);
  const [delBtnStyle, setDelBtnStyle] = useState<{}>({});
  const [editBtnStyle, setEditBtnStyle] = useState<{}>({});

  const tableRef = useRef<any>(null);

  useEffect(() => {
    setDataSource(props.tableResult.dataSource)
    setEditableRowKeys(props.tableResult.columns)
    setDelBtnStyle({})
    setEditBtnStyle({})
  }, [props.tableResult.queryKey])

  const copyCellValue = (val: string) => {
    const dummy = document.createElement('textarea');
    dummy.style.position = 'absolute';
    dummy.style.left = '-9999px';
    dummy.style.top = `-9999px`;
    document.body.appendChild(dummy);
    dummy.value = val;
    dummy.select();
    document.execCommand('copy');
    document.body.removeChild(dummy);
    message.success('复制单元格内容成功')
  }

  const getColumns = () => {
    let fieldMapObj: {} = {}
    if (props.tableResult.tableName) {
      // @ts-ignore
      fieldMapObj = props.onRef?.current?.getDbTableFieldsMap(props.tableResult.tableName) || {}
    }
    const columns: any[] = props.tableResult.columns
      .map((columnName: any, index: number) => {
        let field
        let tmp = props.tableResult.tableColumns[columnName]
        if (tmp) {
          field = {
            name: tmp.name,
            value: tmp.name,
            remarks: tmp.remarks,
            dataType: tmp.typeName,
            notNull: tmp.isNullable === 'NO',
            autoIncrement: tmp.isAutoincrement === 'YES',
            defaultValue: tmp.def,
            pk: tmp.primaryKey,
          }
        } else {
          field = fieldMapObj[columnName]
        }
        let title: string = "", tooltip: string = ""
        if (columnName !== 'index') {
          if (field?.pk) {
            title = "*" + columnName + "(主键)"
          } else if (field?.notNull) {
            title = "*" + columnName
          } else {
            title = columnName
          }
        } else {
          title = columnName + '(虚拟列)'
        }
        if (field) {
          if (field.pk) {
            tooltip = `${field.remarks} --- ` + (field.autoIncrement ? '(自增)' : '(非自增)') + "(" + field.dataType + ")"
          } else {
            tooltip = `${field.remarks}` + " --- " + "(" + field.dataType + ")" + (field.defaultValue ? `(默认值:${field.defaultValue})` : '')
          }
        }
        return {
          title: title,
          tooltip: tooltip,
          key: columnName,
          dataIndex: columnName,
          ellipsis: true,
          width: 153,
          render: (text: any, record: any, rowIndex: number, action: any) => {
            if (record[columnName] === null) {
              return <span style={{ fontWeight: '100' }}>{"<null>"}</span>
            }
            if (typeof record[columnName] === 'boolean') {
              return <span>{record[columnName] + ''}</span>;
            }
            return <span onDoubleClick={() => { copyCellValue(record[columnName]) }}>
              {record[columnName]}
            </span>
          },
          renderFormItem: () => <InputContextMenu />,
        }
      })
    if (props.tableResult.primaryKeys.length > 0) {
      columns.push({
        title: '操作',
        valueType: 'option',
        key: 'option',
        width: 180,
        fixed: 'right',
        render: (text: any, record: any, rowIndex: number, action: any) => [
          <a
            key="editable"
            style={editBtnStyle[rowIndex]}
            onClick={(e) => {
              if (!record.rowOperationType) {
                record.rowOperationType = 'preEdit'
              }
              action?.startEditable?.(record.index);
            }}
          >
            编辑
          </a>,
          <a
            key="delete"
            style={delBtnStyle[rowIndex]}
            onClick={(e) => {
              message.info('行已打上删除标识')
              setDelBtnStyle((it: {}) => {
                let obj = Object.assign({}, it)
                obj[rowIndex] = { color: "red" }
                return obj;
              })
              record.rowOperationType = 'delete'
            }}
          >
            删除
          </a>,
          <a
            key="add"
            onClick={(e) => {
              let newRecord = JSON.parse(JSON.stringify(record))
              Object.entries(record)
                .forEach(([key, value]) => {
                  if (value === null) {
                    newRecord[key] = '<null>'
                  }
                })
              newRecord.rowOperationType = 'preAdd'
              newRecord.index = dataSource.length
              tableRef?.current?.addEditRecord(newRecord)
            }}
          >
            复制该行
          </a>,
        ],
      })
    }
    return columns;
  }

  const paginationOnChange = (current: number, pageSize: number) => {
    props.executeSqlWhenPaginationChange(current, pageSize)
  }

  return (<>
    <EditableProTable
      actionRef={tableRef}
      size={'small'}
      scroll={{ x: 1300, y: 'calc(100vh - 500px)' }}
      rowKey='index'
      headerTitle="可编辑表格(双击单元格复制其内容)"
      columns={getColumns()}
      value={dataSource}
      bordered
      onChange={(it:any) => setDataSource(it)}
      editable={{
        type: 'multiple',
        editableKeys,
        saveText: '确定',
        onSave: async (rowKey: any, row: any, originRow) => {
          // console.log(rowKey, row, originRow);
          let style: {}
          if (row.rowOperationType === 'preAdd' || row.rowOperationType === 'add') {
            message.info('行已打上新增标识')
            row.rowOperationType = 'add'
            style = { color: "gold" }
          } else {
            message.info('行已打上编辑标识')
            row.rowOperationType = 'edit'
            style = { color: "green" }
          }
          setEditBtnStyle((it: {}) => {
            let obj = Object.assign({}, it)
            obj[row.index] = style
            return obj;
          })
        },
        onChange: setEditableRowKeys,
        actionRender: (row, config, defaultDom) => {
          return [
            defaultDom.save,
            defaultDom.cancel,
          ];
        }
      }}
      recordCreatorProps={{
        position: 'bottom',
        record: () => ({
          index: dataSource.length,
          rowOperationType: 'preAdd',
        }),
        onClick: () => {
        }
      }}
      form={{
        syncToUrl: (values, type) => {
          // console.log("xxxxxxxxx", values, type);
          if (type === 'get') {
            return {
              ...values,
              created_at: [values.startTime, values.endTime],
            };
          }
          return values;
        },
      }}
      search={false}
      options={false}
      dateFormatter="string"
      toolBarRender={() => [
        <Button key="button" size={"small"} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('json')
        }}>
          导出为json
        </Button>,
        <Button key="button" size={"small"} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('csv')
        }}>
          导出为csv
        </Button>,
        <Button key="button" size={"small"} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('xls')
        }}>
          导出为Excel
        </Button>,
        <Button key="button" size={"small"} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('sqlInsert')
        }}>
          导出为sql(insert)
        </Button>,
        <Button key="button" size={"small"} onClick={() => {
          if (dataSource.length === 0) {
            message.warning("没有数据!");
            return
          }
          props.exportSql('sqlUpdate')
        }}>
          导出为sql(update)
        </Button>,
        <Button key="button" type="primary" size={"small"} onClick={() => {
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
          props.submitQueryTableChange(list)
        }}>
          提交表格变动回db
        </Button>,
      ]}
    />
    {props.tableResult.showPagination && <Pagination size="small" style={{ textAlign: 'right' }} onChange={paginationOnChange}
      current={props.tableResult.page} pageSize={props.tableResult.pageSize} showTotal={(total: number) => `总共 ${total} 条`}
      total={props.tableResult.total} pageSizeOptions={[5, 20, 50]} showSizeChanger />}
  </>);
};

export default React.memo(QueryResult)

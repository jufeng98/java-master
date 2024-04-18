import { ProTable } from "@ant-design/pro-components";
import React from "react";
import * as QueryResultUtils from './QueryResultUtils';

export type ExplainResultProps = {
  tableResult: { columns: any, dataSource: any, total: number };
};


const ExplainResult: React.FC<ExplainResultProps> = (props) => {


  const getColumns = () => {
    return props.tableResult.columns
      ?.map((columnName: any) => ({
        title: columnName,
        key: columnName,
        dataIndex: columnName,
        ellipsis: true,
        width: 150,
        render: (text: any, record: any) => {
          const columnValue = record[columnName]
          if (columnValue === null) {
            return <span style={{ fontWeight: '100' }}>{"<null>"}</span>
          } else if (typeof columnValue === 'object') {
            const tmpValue = JSON.stringify(columnValue, null, 4)
            return <span onDoubleClick={() => { QueryResultUtils.copyValue(tmpValue) }}>
              {tmpValue}
            </span>
          } else {
            return <span style={{ fontWeight: '100' }}>
              {columnValue}
            </span>
          }
        }
      }))
  }

  return (<>
    <ProTable
      size={'small'}
      scroll={{ x: 1100, y: 'calc(100vh - 500px)' }}
      dataSource={props.tableResult.dataSource}
      rowKey="id"
      pagination={{
        showQuickJumper: true,
        total: props.tableResult.total
      }}
      columns={getColumns()}
      search={false}
      options={false}
      dateFormatter="string"
    />
  </>);
};

export default React.memo(ExplainResult)

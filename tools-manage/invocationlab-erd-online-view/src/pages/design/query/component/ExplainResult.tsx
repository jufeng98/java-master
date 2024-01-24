import { ProTable } from "@ant-design/pro-components";
import React from "react";

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
          return record[columnName] === null ? <span style={{ fontWeight: '100' }}>{"<null>"}</span> : record[columnName]
        }
      }))
  }

  return (<>
    <ProTable
      size={'small'}
      scroll={{ x: 1300, y: 'calc(100vh - 500px)' }}
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

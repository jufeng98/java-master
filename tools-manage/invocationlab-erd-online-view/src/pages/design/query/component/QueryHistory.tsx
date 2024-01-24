import { ProColumns, ProTable } from "@ant-design/pro-components";
import React, { useEffect, useState } from "react";
import { GET } from "@/services/crud";
import { useSearchParams } from "@@/exports";
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import { Pagination, message } from "antd";

export type QueryHistoryProps = {
  queryId: string | number;
  key: string;
};

type QueryHistoryItem = {
  id: number | string;
  title: string;
  sqlInfo: string;
  dbName: string;
  createTime: string;
  creator: string;
  duration: number;
};


const QueryHistory: React.FC<QueryHistoryProps> = (props) => {
  const columns: ProColumns<QueryHistoryItem>[] = [
    {
      title: 'SQL',
      dataIndex: 'sqlInfo',
      copyable: true,
      ellipsis: true,
    },
    {
      title: '所有参数',
      dataIndex: 'params',
    },
    {
      title: '执行数据库',
      dataIndex: 'dbName',
    },
    {
      title: '耗时(ms)',
      dataIndex: 'duration',
    },
    {
      title: '执行时间',
      dataIndex: 'createTime',
    },
    {
      title: '执行人',
      dataIndex: 'creator',
    },
  ]

  const [searchParams] = useSearchParams();
  let projectId = searchParams.get("projectId") || '';
  if (!projectId || projectId === '') {
    projectId = cache.getItem(CONSTANT.PROJECT_ID) || '';
  }

  const [records, setRecords] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState<any>({ current: 1, pageSize: 50 })

  const queryHistory = (current: number, pageSize: number) => {
    setLoading(true)
    GET('/ncnb/queryHistory', {
      page: current,
      pageSize: pageSize,
      queryId: props.queryId,
    }).then(result => {
      setLoading(false)
      if (result?.code === 200) {
        setRecords(result.data.records)
        setTotal(result.data.total)
      }
    })
  }

  useEffect(() => {
    queryHistory(page.current, page.pageSize)
  }, [props.key])

  const paginationOnChange = (current: number, pageSize: number) => {
    setPage({ current, pageSize })
    queryHistory(current, pageSize)
  }

  return (<>
    <ProTable
      loading={loading}
      size={'small'}
      scroll={{ x: 1300, y: 'calc(100vh - 478px)' }}
      dataSource={records}
      columns={columns}
      search={false}
      options={false}
      dateFormatter="string"
      pagination={false}
    />
    <Pagination size="small" style={{ textAlign: 'right' }} onChange={paginationOnChange}
      current={page.current} pageSize={page.pageSize} showTotal={(total: number) => `总共 ${total} 条`}
      total={total} pageSizeOptions={[5, 20, 50, 100]} showSizeChanger />
  </>);
};

export default React.memo(QueryHistory)

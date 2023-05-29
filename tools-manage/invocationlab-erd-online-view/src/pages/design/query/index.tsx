import React, { useEffect, useRef, useState } from "react";
import './index.less'
import { Button, message, Select, Space, Spin, Drawer } from "antd";
import { ProCard } from "@ant-design/pro-components";
import { Data, HistoryQuery, Plan } from "@icon-park/react";
import CodeEditor from "@/components/CodeEditor";
import QueryResult from "@/pages/design/query/component/QueryResult";
import { BarsOutlined, EyeOutlined, PlayCircleOutlined, SaveOutlined, SmileOutlined } from "@ant-design/icons";
import useQueryStore from "@/store/query/useQueryStore";
import shallow from "zustand/shallow";
import useVersionStore from "@/store/version/useVersionStore";
import _ from "lodash";
import { useSearchParams } from "@@/exports";
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import { format } from "sql-formatter";
import request from "../../../utils/request";
import useProjectStore from "@/store/project/useProjectStore";
import ExplainResult from "@/pages/design/query/component/ExplainResult";
import QueryHistory from "@/pages/design/query/component/QueryHistory";
import ConstructSqlForm from "@/pages/design/query/component/ConstructSqlForm";

const { Option, OptGroup } = Select;
export type QueryProps = {
  id: string | number;
};

const Query: React.FC<QueryProps> = (props) => {

  const { tables, } = useProjectStore(state => ({
    tables: state.tables
  }), shallow);

  //console.log(130, tables);

  const [loading, setLoading] = useState(false);
  const [codeLoading, setCodeLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const [searchParams] = useSearchParams();
  let projectId = searchParams.get("projectId") || '';
  if (!projectId || projectId === '') {
    projectId = cache.getItem(CONSTANT.PROJECT_ID) || '';
  }

  const [tableResult, setTableResult] = useState({
    columns: [],
    dataSource: [],
    total: 0,
    queryKey: 0,
    tableName: '',
    page: 1,
    pageSize: 20,
    showPagination: false,
    primaryKeys: [],
    tableColumns: {},
  });
  const [explainTable, setExplainTable] = useState({
    columns: [],
    dataSource: [],
    total: 0
  });
  const [tab, setTab] = useState('result');
  let [selectDB, setSelectDB] = useState('');
  const [sqlMode, setSqlMode] = useState('mysql');
  const [theme, setTheme] = useState('xcode');
  const [dbNameList, setDbNameList] = useState<[]>([]);

  const { queryDispatch } = useQueryStore(state => ({
    queryDispatch: state.dispatch
  }), shallow);

  const [queryInfo, setQueryInfo] = useState({ sqlInfo: '' });

  const editorRef = useRef(null);
  const btnRunRef = useRef(null);

  useEffect(() => {
    let dbNamesStr: any = sessionStorage.getItem("queryDbNamesSqlKey:" + props.id)
    if (dbNamesStr) {
      let dbNames = JSON.parse(dbNamesStr)
      setDbNameList(dbNames)
      let queryInfo: any = sessionStorage.getItem("querySqlKey:" + props.id)
      let obj = JSON.parse(queryInfo)
      setQueryInfo(obj)
      setSelectDB(obj.selectDB)
      if (obj.selectDB) {
        setSelectDB(obj.selectDB)
      } else {
        setSelectDB(dbNames[0])
      }
      return
    }

    setCodeLoading(true)
    request.post('/ncnb/getDbs', { data: { projectId } })
      .then(res => {
        if (!res.data) {
          return;
        }
        sessionStorage.setItem("queryDbNamesSqlKey:" + props.id, JSON.stringify(res.data))
        setDbNameList(res.data)

        queryDispatch.fetchQueryInfo(props.id)
          .then(r => {
            setCodeLoading(false)
            setQueryInfo(r.data);
            if (r.data.selectDB) {
              setSelectDB(r.data.selectDB)
            } else {
              setSelectDB(res.data[0])
            }
            sessionStorage.setItem("querySqlKey:" + props.id, JSON.stringify(r.data))
          });
      })
  }, [])

  const clearSessionQuery = () => {
    Object.keys(sessionStorage).forEach(key => {
      if (key.includes("SqlKey")) {
        sessionStorage.removeItem(key)
      }
    })
  }

  useEffect(() => {
    window.addEventListener('keydown', onKeyDown);
    window.addEventListener('unload', clearSessionQuery)
    return () => {
      window.removeEventListener('keydown', onKeyDown);
      window.removeEventListener('unload', clearSessionQuery)
    }
  }, [])

  const onKeyDown = (e: KeyboardEvent) => {
    if (e.ctrlKey && e.code === 'KeyC' && e.target && e.target.classList && e.target.classList.contains('ace_text-input')) {
      // @ts-ignore
      if (editorRef?.current?.getSelectValue()) {
        return
      }
      // @ts-ignore
      editorRef?.current?.selectLine()
    }
    if (e.ctrlKey && e.code === 'Enter') {
      // @ts-ignore
      if (!editorRef?.current?.getSelectValue()) {
        // @ts-ignore
        editorRef?.current?.selectLine()
      }
      const event = new MouseEvent("click", {
        bubbles: true,
        cancelable: true
      });
      // @ts-ignore
      btnRunRef?.current?.dispatchEvent(event)
    }
  }

  const executeSql = (sql: string, page: number = 1, pageSize: number = 20) => {
    const params = {
      queryId: props.id,
      projectId,
      sql,
      dbName: selectDB,
      page,
      pageSize
    }
    setLoading(true);
    queryDispatch.exec(params).then(r => {
      setLoading(false);
      if (r?.code === 200) {
        sessionStorage.setItem("executeSqlKey:" + props.id, sql)
        sessionStorage.setItem("executePageSqlKey:" + props.id, r.data.page)
        sessionStorage.setItem("executePageSizeSqlKey:" + props.id, r.data.pageSize)
        setTableResult({
          columns: r?.data.columns,
          dataSource: r.data.tableData.records,
          total: r.data.tableData.total,
          queryKey: r.data.queryKey,
          tableName: r.data.tableName,
          page: r.data.page,
          pageSize: r.data.pageSize,
          showPagination: r.data.showPagination,
          primaryKeys: r.data.primaryKeys,
          tableColumns: r.data.tableColumns,
        });
        setTab("result");
      }
    });
  }

  const executeSqlWhenPaginationChange = (page: number, pageSize: number) => {
    let sql: string = sessionStorage.getItem("executeSqlKey:" + props.id) || ""
    executeSql(sql, page, pageSize)
  }

  const exportSql = (type: string) => {
    let sql: string = sessionStorage.getItem("executeSqlKey:" + props.id) || ""
    let page = sessionStorage.getItem("executePageSqlKey:" + props.id) || 1
    let pageSize = sessionStorage.getItem("executePageSizeSqlKey:" + props.id) || 20
    const params = {
      queryId: props.id,
      projectId,
      sql,
      dbName: selectDB,
      page,
      pageSize,
      type
    }
    setLoading(true);
    queryDispatch.exportSql(params, setLoading)
  }

  const run = () => {
    // @ts-ignore
    let selectValue = editorRef?.current?.getSelectValue();
    // console.log(267, selectValue);
    if (!selectValue) {
      // @ts-ignore
      editorRef?.current?.selectLine()
      // @ts-ignore
      selectValue = editorRef?.current?.getSelectValue();
    }
    if (!selectDB) {
      message.warning("未选中数据源");
      return
    }
    executeSql(selectValue)
  }

  const closeDrawer = (sql: string) => {
    setOpen(false)
    // @ts-ignore
    let editor = editorRef?.current?.getEditor();
    editor.navigateTo(editor.getCursorPosition().row, 99999)
    // @ts-ignore
    editorRef?.current?.setSelectValue("\r\n" + sql);
    run()
  }

  const submitQueryTableChange = (records: []) => {
    let current = tableResult.page
    setLoading(true)
    let sql: string = sessionStorage.getItem("executeSqlKey:" + props.id) || ""
    request.post('/ncnb/execUpdate', {
      data: {
        queryId: props.id,
        sql,
        dbName: selectDB,
        projectId,
        rows: records
      }
    })
      .then(res => {
        setLoading(false)
        if (res.code !== 200) {
          return
        }
        message.success(res.msg)
        executeSql(sql, current)
      })
  }

  const EDITOR_THEME = ['xcode', 'terminal',];
  const actions = <Space direction="vertical">
    <Space wrap>
      <Button loading={codeLoading} size={"small"} icon={<SmileOutlined />} type="primary" onClick={() => {
        setOpen(true);
      }}>动态构建查询SQL</Button>
      <span style={{ marginRight: 8 }}>数据源</span>
      <Select
        key={'db'}
        size="small"
        style={{ width: 150, marginRight: 12 }}
        value={selectDB ? selectDB : "请选择数据源"}
        onSelect={(e: any) => setSelectDB(e)}
        showSearch
      >
        {
          <OptGroup>
            {dbNameList.map((m1: string) => {
              return <Option key={`${m1}`} value={m1}>{m1}</Option>
            }
            )}
          </OptGroup>
        }
      </Select>
      <span style={{ marginRight: 8 }}>模式</span>
      <Select key={'model'} size="small" style={{ width: 90, marginRight: 12 }} value={sqlMode}
        onSelect={(e: any) => setSqlMode(e)}>
        <Option key="mysql" value="mysql">MySQL</Option>
        <Option key="sql" value="sql">SQL</Option>
      </Select>
      <span style={{ marginRight: 8 }}>主题</span>
      <Select key={'topic'} size="small" style={{ marginRight: 16, width: 100 }} value={theme} onSelect={(e: any) => {
        setTheme(e);
      }}>
        {
          EDITOR_THEME.map(v => <Option key={v} value={v}>{v}</Option>)
        }
      </Select>
    </Space>

  </Space>
  return (<>
    <ProCard layout="center" bordered extra={actions} size={'small'}>
      <CodeEditor
        tables={tables}
        selectDB={selectDB}
        onRef={editorRef}
        mode={sqlMode}
        theme={theme}
        value={queryInfo.sqlInfo}
        onChange={(value) => {
          let queryInfoCp = {
            ...queryInfo,
            sqlInfo: value
          }
          setQueryInfo(queryInfoCp);
          sessionStorage.setItem("querySqlKey:" + props.id, JSON.stringify(queryInfoCp))
        }}
      />
    </ProCard>
    <Spin spinning={codeLoading}>
      <Space wrap>
        <Button type="primary" size={"small"} ref={btnRunRef} icon={<PlayCircleOutlined />} onClick={run}>
          运行(Ctrl+Enter)
        </Button>
        <Button icon={<BarsOutlined />} size={"small"} onClick={() => {
          // @ts-ignore
          let selectValue = editorRef?.current?.getSelectValue();
          if (!selectValue) {
            // @ts-ignore
            editorRef?.current?.selectLine()
            // @ts-ignore
            selectValue = editorRef?.current?.getSelectValue();
          }
          // @ts-ignore
          const formatSqlInfo = format(selectValue || '', { language: sqlMode });
          console.log(130, formatSqlInfo);
          // @ts-ignore
          editorRef?.current?.setSelectValue(formatSqlInfo);
        }}>格式化</Button>
        <Button icon={<EyeOutlined />} size={"small"} onClick={() => {
          // @ts-ignore
          let selectValue = editorRef?.current?.getSelectValue();
          console.log(267, selectValue);
          if (!selectValue) {
            // @ts-ignore
            editorRef?.current?.selectLine()
            // @ts-ignore
            selectValue = editorRef?.current?.getSelectValue();
          }
          if (!selectDB) {
            message.warning("未选中数据源");
            return
          }
          const params = {
            queryId: props.id,
            projectId,
            sql: selectValue,
            dbName: selectDB,
          }
          queryDispatch.explain(params).then(r => {
            setExplainTable({
              columns: r?.data.columns,
              dataSource: r?.data.tableData,
              total: r?.data?.tableData?.length
            });
            setTab("plan");
          });
        }}>查看执行计划</Button>
        <Button icon={<SaveOutlined />} size={"small"} onClick={() => {
          queryDispatch.updateSqlInfo({
            id: props.id,
            sqlInfo: queryInfo.sqlInfo,
            selectDB
          });
        }}>保存SQL</Button>
      </Space>
    </Spin>
    <Spin spinning={loading}>
      <ProCard size={'small'} layout="center" bordered style={{ height: 'calc(100vh - 326px)' }}
        wrap={true}
        tabs={{
          activeKey: tab,
          items: [
            {
              label: <span>执行结果</span>,
              key: 'result',
              children: <QueryResult tableResult={tableResult} onRef={editorRef}
                submitQueryTableChange={submitQueryTableChange}
                executeSqlWhenPaginationChange={executeSqlWhenPaginationChange}
                exportSql={exportSql} />,
            },
            {
              label: <span>执行计划</span>,
              key: 'plan',
              children: <ExplainResult tableResult={explainTable} />,
            },
            {
              label: <span>历史记录</span>,
              key: 'history',
              children: <QueryHistory queryId={props.id} key={tab} />,
            },
          ],
          onChange: (key) => {
            setTab(key);
          },
        }}
      >
        Auto
      </ProCard>
    </Spin>
    <Drawer title="动态构建查询SQL" width={800} placement="right" onClose={() => { setOpen(false) }} open={open}>
      <ConstructSqlForm selectDB={selectDB} closeDrawer={closeDrawer} />
    </Drawer>
  </>);
};

export default React.memo(Query)

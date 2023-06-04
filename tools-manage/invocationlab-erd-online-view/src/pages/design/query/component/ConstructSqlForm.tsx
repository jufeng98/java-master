import { Button, Form, Input, Select, Space, Spin, Tag } from 'antd';
import React, { useEffect, useState } from 'react';
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import request from "../../../../utils/request";
import moment from "moment";
import { ArrowDownOutlined, ArrowUpOutlined, MinusCircleOutlined, PlayCircleOutlined, PlusOutlined, SmileOutlined } from '@ant-design/icons';

const { Option, OptGroup } = Select;
const { TextArea } = Input;
const layout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 16 },
};

export type ConstructSqlFormProps = {
  selectDB: string;
  closeDrawer: Function;
};

const ConstructSqlForm: React.FC<ConstructSqlFormProps> = (props) => {
  const [codeLoading, setCodeLoading] = useState(false);
  const [tables, setTables] = useState([]);
  const [sql, setSql] = useState('');
  const [tableColumns, setTableColumns] = useState([]);
  const [form] = Form.useForm();

  useEffect(() => {
    setCodeLoading(true)
    request.post('/ncnb/getTables', { data: { projectId: cache.getItem(CONSTANT.PROJECT_ID), selectDB: props.selectDB } })
      .then(res => {
        setCodeLoading(false)
        form.setFieldValue('conditionsChoose', [])
        form.setFieldValue('tableColumnNamesChoose', [])
        form.setFieldValue('tableNameChoose', '')
        setTableColumns([])
        setSql('')
        if (!res.data) {
          return;
        }
        setTables(res.data)
      })
  }, [props.selectDB])

  const onTableNameChange = (value: string) => {
    setCodeLoading(true)
    request.post('/ncnb/getTableColumns', {
      data: {
        projectId: cache.getItem(CONSTANT.PROJECT_ID),
        selectDB: props.selectDB,
        tableName: value
      }
    })
      .then(res => {
        form.setFieldValue('conditionsChoose', [])
        setCodeLoading(false)
        if (!res.data) {
          return;
        }
        setTableColumns(res.data)
        form.setFieldValue("tableColumnNamesChoose", res.data.map((it: any) => it.name))
        tryConstructSqlWhenChooseTable()
      })
  }

  const constructSql = (conditionsChoose: any, tableColumnNamesChoose: any, tableNameChoose: any): string => {
    if (!tableNameChoose || !tableColumnNamesChoose) {
      return ""
    }
    let columnNamesStr
    if (tableColumnNamesChoose.length === 0 || tableColumnNamesChoose.length === tableColumns.length) {
      columnNamesStr = "*"
    } else {
      columnNamesStr = tableColumnNamesChoose.join(",")
    }
    let sql = `select ${columnNamesStr} from ${tableNameChoose}`
    if (!conditionsChoose || conditionsChoose.length === 0) {
      return sql
    }
    let conditionStr = conditionsChoose
      .filter((conditionChoose: any) => conditionChoose)
      .map((conditionChoose: any, index: number) => {
        let tmpStr = ''
        if (!conditionChoose.querySymbol) {
          return tmpStr
        }
        if (conditionChoose.querySymbol === '=' || conditionChoose.querySymbol === '!=' || conditionChoose.querySymbol === '>'
          || conditionChoose.querySymbol === '>=' || conditionChoose.querySymbol === '<' || conditionChoose.querySymbol === '<=') {
          tmpStr = `${conditionChoose.columnName} ${conditionChoose.querySymbol} ${conditionChoose.columnValue}`
        } else if (conditionChoose.querySymbol.includes('like')) {
          tmpStr = `${conditionChoose.columnName} ${conditionChoose.querySymbol}`.replace('${value}', conditionChoose.columnValue)
        } else if (conditionChoose.querySymbol.startsWith('is')) {
          tmpStr = `${conditionChoose.columnName} ${conditionChoose.querySymbol}`
        } else if (conditionChoose.querySymbol.includes('in')) {
          tmpStr = `${conditionChoose.columnName} ${conditionChoose.querySymbol}`.replace('${values}', conditionChoose.columnValue)
        } else if (conditionChoose.querySymbol.includes('between')) {
          let split = conditionChoose.columnValue.split(",")
          tmpStr = `${conditionChoose.columnName} ${conditionChoose.querySymbol}`.replace('${start}', split[0] || '')
            .replace('${end}', split[1] || '')
        } else {
          throw new Error("wrong " + conditionChoose.querySymbol)
        }

        if (index !== (conditionsChoose.length - 1)) {
          tmpStr += ` ${conditionChoose.conditionJoin}`
        }
        return tmpStr;
      })
      .join(" ")

    let sortStr = ''
    let columnSortsChoose = conditionsChoose
      .filter((conditionChoose: any) => conditionChoose && conditionChoose.columnSort)
      .map((conditionChoose: any, index: number) => {
        return `${conditionChoose.columnName} ${conditionChoose.columnSort}`
      })
    if (columnSortsChoose.length > 0) {
      sortStr = "order by " + columnSortsChoose.join(",")
    }
    sql += ` where ${conditionStr} ${sortStr}`
    return sql
  }

  const tryConstructSqlWhenChooseTable = () => {
    let conditionsChoose = form.getFieldValue('conditionsChoose')
    let tableNameChoose = form.getFieldValue('tableNameChoose')
    let sql = constructSql(conditionsChoose, [], tableNameChoose)
    setSql(sql)
  }

  const tryConstructSql = () => {
    let conditionsChoose = form.getFieldValue('conditionsChoose')
    let tableColumnNamesChoose = form.getFieldValue('tableColumnNamesChoose')
    let tableNameChoose = form.getFieldValue('tableNameChoose')
    let sql = constructSql(conditionsChoose, tableColumnNamesChoose, tableNameChoose)
    setSql(sql)
  }

  const getDefaultValueOfType = (type: string, querySymbol: string = '=') => {
    if (querySymbol.includes('like')) {
      return ""
    }
    if (type.includes("char")) {
      return "''"
    }
    if (type.includes("int")) {
      return "0"
    }
    if (type.includes("datetime") || type.includes('timestamp')) {
      return "'" + moment(new Date()).format("YYYY-MM-DD HH:mm:ss") + "'"
    }
    if (type.includes("date")) {
      return "'" + moment(new Date()).format("YYYY-MM-DD") + "'"
    }
    if (type.includes("time")) {
      return "'" + moment(new Date()).format("HH:mm:ss") + "'"
    }
    return ""
  }

  const onConditionColumnChange = (columnName: string) => {
    let column = tableColumns.filter((column: any) => column.name === columnName)[0]

    let tmpList = form.getFieldValue('conditionsChoose')
    tmpList
      .filter((conditionChoose: any) => conditionChoose.columnName === columnName)
      .forEach((conditionChoose: any) => {
        let val = getDefaultValueOfType(column.typeName, conditionChoose.querySymbol)
        conditionChoose.columnValue = val
      })
  }

  const onQuerySymbolChange = (querySymbol: string, index: number) => {
    let conditionsChoose = form.getFieldValue('conditionsChoose')
    let conditionChoose = conditionsChoose[index]
    if (querySymbol.includes('in')) {
      conditionChoose.columnValue = 'a,b,c'
    } else if (querySymbol.includes('between')) {
      conditionChoose.columnValue = 'a,b'
    }
  }

  const onFinish = (values: any) => {
    props.closeDrawer(sql)
  };

  const conditions = [
    { id: "equals", label: "等于", value: "=" },
    { id: "notEquals", label: "不等于", value: "!=" },
    { id: "moreThen", label: "大于", value: ">" },
    { id: "moreThenOrEquals", label: "大于或等于", value: ">=" },
    { id: "lessThen", label: "小于", value: "<" },
    { id: "lessThenOrEquals", label: "小于或等于", value: "<=" },
    { id: "contains", label: "包含", value: "like '%${value}%'" },
    { id: "notContains", label: "不包含", value: "not like '%${value}%'" },
    { id: "startsWith", label: "以...开始", value: "like '${value}%'" },
    { id: "endsWith", label: "以...结束", value: "like '%${value}'" },
    { id: "isNull", label: "为null", value: "is null" },
    { id: "isNotNull", label: "不为null", value: "is not null" },
    { id: "inList", label: "在列表中", value: "in (${values})" },
    { id: "notInList", label: "不在列表中", value: "not in (${values})" },
    { id: "between", label: "在两者之间", value: "between ${start} and ${end}" },
    { id: "notBetween", label: "不在两者之间", value: "not between ${start} and ${end}" },
  ]

  const conditionJoins = [
    { id: "or", label: "或者", value: "or" },
    { id: "and", label: "并且", value: "and" },
    { id: "orNot", label: "or not", value: "or not" },
    { id: "andNot", label: "and not", value: "and not" },
  ]

  const columnSorts = [
    { id: "asc", label: "升序", value: "asc" },
    { id: "desc", label: "降序", value: "desc" },
  ]

  return (
    <Spin spinning={codeLoading}>
      <Form
        {...layout}
        form={form}
        style={{ maxWidth: 800 }}
        name="control-hooks"
        labelCol={{ span: 4 }}
        onFinish={onFinish}
        onFieldsChange={() => {
          tryConstructSql()
        }}
      >
        <Form.Item name="tableNameChoose" label="选择要查询的表" rules={[{ required: true }]}>
          <Select
            style={{ width: 630 }}
            showSearch
            size='middle'
            placeholder="请选择"
            onChange={onTableNameChange}
          >
            <OptGroup>
              {
                tables.map((table: any) => {
                  return <Option key={table.name} value={table.name}>{table.name + "(" + table.remarks + ")"}</Option>
                })
              }
            </OptGroup>
          </Select>
        </Form.Item>
        <Form.Item name="tableColumnNamesChoose" label="选择要查询的列" rules={[{ required: true }]} >
          <Select
            showSearch
            style={{ width: 630 }}
            size='middle'
            mode="multiple"
            placeholder="请选择"
            allowClear
            options={tableColumns.map((column: any) => ({ label: column.name, value: column.name }))}
          />
        </Form.Item>
        <Form.List name="conditionsChoose">
          {(fields, { add, remove, move }) => (
            <>
              {fields.map(({ key, name, ...restField }) => (
                <Space key={key} align="baseline" style={{ height: 30 }}>
                  <Form.Item
                    noStyle
                    {...restField}
                    name={[name, 'columnName']}
                    rules={[{ required: true, message: '请选择列' }]}
                  >
                    <Select
                      showSearch
                      size='small'
                      style={{ width: 200 }}
                      placeholder="请选择列"
                      onChange={onConditionColumnChange}
                      options={
                        tableColumns.map((column: any) => ({
                          label: `${column.name}  ${column.remarks} ${column.typeName}`,
                          value: column.name
                        }))
                      }
                    />
                  </Form.Item>
                  <Form.Item
                    noStyle
                    shouldUpdate={(prevValues, curValues) =>
                      prevValues.conditions !== curValues.conditions
                    }
                  >
                    {() => (
                      <Form.Item
                        {...restField}
                        name={[name, 'querySymbol']}
                        rules={[{ required: true, message: '请选择符号' }]}
                      >
                        <Select onChange={(e) => { onQuerySymbolChange(e, name) }} style={{ width: 120 }}
                          placeholder="请选择符号" size='small'>
                          {conditions.map(item => (
                            <Option key={item.id} value={item.value}>
                              {item.label}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                    )}
                  </Form.Item>
                  <Form.Item
                    noStyle
                    hidden={false}
                    {...restField}
                    style={{ width: 190 }}
                    name={[name, 'columnValue']}
                    rules={[{ required: true, message: '请输入值' }]}
                  >
                    <Input size='small' style={{ width: 190 }} />
                  </Form.Item>
                  <Form.Item
                    noStyle
                    shouldUpdate={(prevValues, curValues) =>
                      prevValues.conditions !== curValues.conditions
                    }
                  >
                    {() => (
                      <Form.Item
                        noStyle
                        {...restField}
                        name={[name, 'conditionJoin']}
                        rules={[{ required: true, message: '请选择' }]}
                      >
                        <Select style={{ width: 80 }} placeholder="请选择" size='small'>
                          {conditionJoins.map(item => (
                            <Option key={item.id} value={item.value}>
                              {item.label}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                    )}
                  </Form.Item>
                  <Form.Item
                    noStyle
                    shouldUpdate={(prevValues, curValues) =>
                      prevValues.conditions !== curValues.conditions
                    }
                  >
                    {() => (
                      <Form.Item
                        noStyle
                        {...restField}
                        name={[name, 'columnSort']}
                      >
                        <Select allowClear style={{ width: 70 }} placeholder="排序" size='small'>
                          {columnSorts.map(item => (
                            <Option key={item.id} value={item.value}>
                              {item.label}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                    )}
                  </Form.Item>
                  <MinusCircleOutlined onClick={() => {
                    remove(name)

                    tryConstructSql()
                  }} />
                  <ArrowUpOutlined onClick={() => move(name, name - 1)} />
                  <ArrowDownOutlined onClick={() => move(name, name + 1)} />
                </Space>
              ))}
              <Form.Item>
                <Button size="small" type="dashed" onClick={() => {
                  add()
                  let tmpList = form.getFieldValue('conditionsChoose')
                  if (tableColumns.length == 0) {
                    tmpList.forEach((conditionChoose: any, index: number, list: any) => {
                      if (conditionChoose !== undefined) {
                        return
                      }
                      list[index] = {
                        querySymbol: conditions[0].value,
                        conditionJoin: conditionJoins[0].value
                      }
                    })
                    return
                  }
                  let currentIndex = tmpList.length - 1
                  if (currentIndex >= tableColumns.length) {
                    currentIndex = 0
                  }
                  tmpList.forEach((conditionChoose: any, index: number, list: any) => {
                    if (conditionChoose !== undefined) {
                      return
                    }
                    list[index] = {
                      columnName: tableColumns[currentIndex].name,
                      querySymbol: conditions[0].value,
                      columnValue: getDefaultValueOfType(tableColumns[currentIndex].typeName),
                      conditionJoin: conditionJoins[0].value
                    }
                  })

                  tryConstructSql()
                }} block icon={<PlusOutlined />}>
                  添加查询条件
                </Button>
              </Form.Item>
            </>
          )}
        </Form.List>
        <Form.Item>
          <Button icon={<PlayCircleOutlined />} type="primary" size='small' htmlType="submit">
            执行查询
          </Button>
        </Form.Item>
      </Form>
      <Tag icon={<SmileOutlined />} style={{ fontSize: 14 }}>
        实时预览SQL
      </Tag>
      <TextArea rows={6} value={sql} />
    </Spin>
  );
};
export default React.memo(ConstructSqlForm)

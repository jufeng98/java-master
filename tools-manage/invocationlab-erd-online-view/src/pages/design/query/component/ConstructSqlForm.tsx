import { Button, Form, Input, Select, Space, Spin, Tag } from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import request from "../../../../utils/request";
import moment from "moment";
import { ArrowDownOutlined, ArrowUpOutlined, ConsoleSqlOutlined, MinusCircleOutlined, PlayCircleOutlined, PlusOutlined } from '@ant-design/icons';

const { Option, OptGroup } = Select;
const { TextArea } = Input;
const layout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 16 },
};

export type ConstructSqlFormProps = {
  selectDB: string;
  closeDrawer: Function;
  open: Boolean;
};

const ConstructSqlForm: React.FC<ConstructSqlFormProps> = (props) => {
  const [codeLoading, setCodeLoading] = useState(false);
  const [tables, setTables] = useState<any[]>([]);
  const [sql, setSql] = useState('');
  const [tableColumns, setTableColumns] = useState<any[]>([]);
  const [form] = Form.useForm();
  const tableNameSelectRef = useRef<any>()
  const addConditionBtnRef = useRef<any>()

  const conditions = [
    { id: "equals", label: "等于(=)", value: "=" },
    { id: "inList", label: "在列表中(in)", value: "In" },
    { id: "contains", label: "包含(like)", value: "Like" },
    { id: "between", label: "在两者之间(between)", value: "Between" },
    { id: "isNull", label: "为null(is null)", value: "IsNull" },
    { id: "moreThenOrEquals", label: "大于或等于(>=)", value: ">=" },
    { id: "lessThenOrEquals", label: "小于或等于(<=)", value: "<=" },
    { id: "moreThen", label: "大于(>)", value: ">" },
    { id: "lessThen", label: "小于(<)", value: "<" },
    { id: "notEquals", label: "不等于(!=)", value: "!=" },
    { id: "startsWith", label: "以...开始(like xxx%)", value: "LikeLeft" },
    { id: "endsWith", label: "以...结束(like %xxx)", value: "LikeRight" },
    { id: "isNotNull", label: "不为null(is not null)", value: "IsNotNull" },
    { id: "notInList", label: "不在列表中(not in)", value: "NotIn" },
    { id: "notContains", label: "不包含(not like)", value: "NotLike" },
    { id: "notBetween", label: "不在两者之间(not between)", value: "NotBetween" },
  ]

  const conditionJoins = [
    { id: "or", label: "或者", value: "or" },
    { id: "and", label: "并且", value: "and" },
    { id: "orNot", label: "or not", value: "or not" },
    { id: "andNot", label: "and not", value: "and not" },
  ]

  const columnSorts = [
    { id: "desc", label: "降序", value: "desc" },
    { id: "asc", label: "升序", value: "asc" },
  ]

  useEffect(() => {
    getTables()
  }, [props.selectDB])

  const getTables = () => {
    setCodeLoading(true)
    request.post('/ncnb/getTables', { data: { projectId: cache.getItem(CONSTANT.PROJECT_ID), selectDB: props.selectDB } })
      .then(res => {
        setCodeLoading(false)
        if (!res || !res.data) {
          return;
        }
        form.setFieldValue('conditionsChoose', [])
        form.setFieldValue('tableColumnNamesChoose', [])
        form.setFieldValue('tableNameChoose', '')
        setTableColumns([])
        setSql('')
        setTables(res.data)
        tableNameSelectRef.current?.focus()
      })
  }

  useEffect(() => {
    if (tables.length > 0 && props.open) {
      setTimeout(() => {
        tableNameSelectRef.current?.focus()
      }, 0)
    }
  }, [props.open])

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
        const value = getModifiedConditionValue(conditionChoose)
        if (conditionChoose.querySymbol.includes("Like")) {
          tmpStr = `${conditionChoose.columnName} ${value}`
        } else if (conditionChoose.querySymbol.includes("In")) {
          tmpStr = `${conditionChoose.columnName} ${value}`
        } else if (conditionChoose.querySymbol.includes("Between")) {
          tmpStr = `${conditionChoose.columnName} ${value}`
        } else if (conditionChoose.querySymbol.includes("Null")) {
          tmpStr = `${conditionChoose.columnName} ${value}`
        } else {
          tmpStr = `${conditionChoose.columnName} ${conditionChoose.querySymbol} ${value}`
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
      .map((conditionChoose: any) => {
        return `${conditionChoose.columnName} ${conditionChoose.columnSort}`
      })
    if (columnSortsChoose.length > 0) {
      sortStr = "order by " + columnSortsChoose.join(",")
    }
    sql += ` where ${conditionStr} ${sortStr}`
    return sql
  }

  const getModifiedConditionValue = (conditionChoose: any) => {
    let column = tableColumns.filter((column: any) => column.name === conditionChoose.columnName)[0]
    return getColumnValueOfType(column.typeName, conditionChoose.querySymbol, conditionChoose.columnValue)
  }

  const getColumnValueOfType = (type: string, querySymbol: string = '=', currentValue: any) => {
    if (querySymbol === 'Like') {
      return currentValue ? `like '%${currentValue}%'` : "like"
    } else if (querySymbol === 'NotLike') {
      return currentValue ? `not like '%${currentValue}%'` : "not like"
    } else if (querySymbol === 'LikeLeft') {
      return currentValue ? `like '${currentValue}%'` : "like"
    } else if (querySymbol === 'LikeRight') {
      return currentValue ? `like '%${currentValue}'` : "like"
    } else if (querySymbol === 'In' || querySymbol === 'NotIn') {
      let tmp = querySymbol === 'In' ? "in" : "not in"
      if (!currentValue) {
        return tmp
      } else {
        let array = currentValue.split(",")
        for (let i = 0; i < array.length; i++) {
          const str = array[i];
          array[i] = getColumnValueOfType(type, "", str)
        }
        return `${tmp} (${array.join(',')})`
      }
    } else if (querySymbol === 'IsNull' || querySymbol === 'IsNotNull') {
      return querySymbol === 'IsNull' ? "is null" : "is not null"
    } else if (querySymbol === 'Between' || querySymbol === 'NotBetween') {
      let tmp = querySymbol === 'Between' ? "between" : "not between"
      if (!currentValue) {
        return tmp
      } else {
        let array = currentValue.split(",")
        for (let i = 0; i < array.length; i++) {
          const str = array[i];
          array[i] = getColumnValueOfType(type, "", str)
        }
        return `${tmp} ${array[0] || ''} and ${array[1] || ''}`
      }
    }

    if (type.includes("char")) {
      return currentValue ? `'${currentValue}'` : ""
    } else if (type.includes("int")) {
      return currentValue ? `${currentValue}` : "0"
    } else if (type.includes("datetime") || type.includes('timestamp')) {
      return currentValue ? `'${currentValue}'` : moment(new Date()).format("YYYY-MM-DD HH:mm:ss")
    } else if (type.includes("date")) {
      return currentValue ? `'${currentValue}'` : moment(new Date()).format("YYYY-MM-DD")
    } else if (type.includes("time")) {
      return currentValue ? `'${currentValue}'` : moment(new Date()).format("HH:mm:ss")
    } else {
      return ""
    }
  }

  const tryConstructSql = () => {
    let conditionsChoose = form.getFieldValue('conditionsChoose')
    let tableColumnNamesChoose = form.getFieldValue('tableColumnNamesChoose')
    let tableNameChoose = form.getFieldValue('tableNameChoose')
    let sql = constructSql(conditionsChoose, tableColumnNamesChoose, tableNameChoose)
    setSql(sql)
  }

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
        if (!res || !res.data) {
          return;
        }
        form.setFieldValue("tableColumnNamesChoose", res.data.map((it: any) => it.name))
        setTableColumns(res.data)
        addConditionBtnRef?.current?.click()
      })
  }

  const onConditionColumnChange = (columnName: string) => {
    let column = tableColumns.filter((column: any) => column.name === columnName)[0]
    let tmpList = form.getFieldValue('conditionsChoose')
    tmpList
      .filter((conditionChoose: any) => conditionChoose.columnName === columnName)
      .forEach((conditionChoose: any) => {
        let val = getColumnValueOfType(column.typeName, conditionChoose.querySymbol, null)
        conditionChoose.columnValue = val
      })
  }

  const onQuerySymbolChange = (querySymbol: string, index: number) => {
    let conditionsChoose = form.getFieldValue('conditionsChoose')
    let conditionChoose = conditionsChoose[index]
    if (querySymbol.includes('In')) {
      conditionChoose.columnValue = '1,2,3'
    } else if (querySymbol.includes('Between')) {
      conditionChoose.columnValue = '1,2'
    }
  }

  const onFinish = (_: any) => {
    props.closeDrawer(sql)
  };

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
            ref={tableNameSelectRef}
            autoFocus={true}
            defaultOpen={true}
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
                    <Input size='small' autoFocus={true} style={{ width: 190 }} />
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
                <Button size="small" ref={addConditionBtnRef} type="dashed" onClick={() => {
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
                      columnValue: getColumnValueOfType(tableColumns[currentIndex].typeName, "=", null),
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
          <Button ref={el => el && el.style.setProperty('margin-left', '200px', 'important')} size='small' onClick={getTables}>
            重置并刷新
          </Button>
        </Form.Item>
      </Form>
      <Tag icon={<ConsoleSqlOutlined />} style={{ fontSize: 14 }}>
        实时预览SQL
      </Tag>
      <TextArea rows={6} value={sql} />
    </Spin>
  );
};
export default React.memo(ConstructSqlForm)

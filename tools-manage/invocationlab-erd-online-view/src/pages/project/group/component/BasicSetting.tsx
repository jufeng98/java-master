import React from "react";
import { ProForm, ProFormSelect, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import { Divider, message, Space, Typography } from "antd";
import { GET } from "@/services/crud";
import { useSearchParams } from "@@/exports";
import _ from "lodash";
import RemoveGroupProject from "@/pages/project/group/component/RemoveGroupProject";
import { updateGroupProject } from "@/services/group-project";
import { Access, useAccess } from "@@/plugin-access";


const { Title, Text } = Typography;


export type BasicSettingProps = {};
const BasicSetting: React.FC<BasicSettingProps> = (props) => {
  const access = useAccess();
  const [searchParams] = useSearchParams();
  const projectId = searchParams.get("projectId") || '';
  return (<>
    <ProForm
      submitter={{
        // 完全自定义整个区域
        render: (props, dom) => {
          console.log(props);
          return access.canErdProjectGroupEdit ? dom : null;
        },
      }}
      onFinish={async (values) => {
        await updateGroupProject({
          id: projectId,
          projectName: values.projectName,
          description: values.description,
          tags: _.join(values.tags, ',')
        }).then(r => {
          if (!r) {
            return
          }
          if (r.code === 200) {
            message.success('修改成功');
          } else {
            message.error(r.message || '修改失败')
          }
        })
      }}
      params={{ id: '100' }}
      formKey="base-form-use-demo"
      dateFormatter={(value, valueType) => {
        console.log('---->', value, valueType);
        return value.format('YYYY/MM/DD HH:mm:ss');
      }}
      request={async (param) => {
        const result = await GET('/ncnb/project/group/get/' + projectId, {});
        result.data.tags = result.data.tags.split(",")
        return result.data
      }}
      autoFocusFirstInput
    >
      <Title level={4}>基本设置</Title>
      <ProFormText width="md"
        name="projectName"
        label="项目名"
        placeholder="请输入项目名"
        fieldProps={{
          bordered: access.canErdProjectGroupEdit,
          disabled: !access.canErdProjectGroupEdit,
        }}
        formItemProps={{
          rules: [
            {
              required: true,
              message: '不能为空',
            },
            {
              max: 100,
              message: '不能大于 100 个字符',
            },
          ],
        }}
      />
      <ProFormSelect width="md"
        name="tags"
        label="标签"
        placeholder="请输入项目标签,按回车分割"
        disabled={!access.canErdProjectGroupEdit}
        bordered={access.canErdProjectGroupEdit}
        formItemProps={{
          rules: [
            {
              required: true,
              message: '不能为空',
            },
          ],
        }}
        fieldProps={{
          mode: "tags",
          tokenSeparators: [",", ";"]
        }}
      />
      <ProFormTextArea
        width="md"
        name="description"
        label="项目描述"
        placeholder="请输入项目描述"
        fieldProps={{
          bordered: access.canErdProjectGroupEdit,
          disabled: !access.canErdProjectGroupEdit,
        }}
        formItemProps={{
          rules: [
            {
              required: true,
              message: '不能为空',
            },
            {
              max: 100,
              message: '不能大于 100 个字符',
            },
          ],
        }}
      />
      <ProFormText width="md"
        name="createTime"
        label="创建时间"
        fieldProps={{
          bordered: false,
          disabled: true,

        }}
        formItemProps={{
          rules: [
            {
              required: true,
              message: '不能为空',
            },
            {
              max: 100,
              message: '不能大于 100 个字符',
            },
          ],
        }}
      />
      <ProFormText width="md"
        name="updateTime"
        label="最后修改时间"
        placeholder=""
        fieldProps={{
          bordered: false,
          disabled: true,
        }}
        formItemProps={{
          rules: [
            {
              required: true,
              message: '不能为空',
            },
            {
              max: 100,
              message: '不能大于 100 个字符',
            },
          ],
        }}
      />
    </ProForm>


    <Divider />
    <Access
      accessible={access.canErdProjectGroupDel}
      fallback={<></>}
    >
      <Space direction="vertical">
        <Title level={4}>删除项目</Title>
        <Text type="secondary">删除项目全部模型，此操作无法恢复</Text>
        <RemoveGroupProject projectId={projectId} />
      </Space>
    </Access>
  </>);
};
export default React.memo(BasicSetting)

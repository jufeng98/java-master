import React, { useState } from "react";
import { PlusOutlined } from '@ant-design/icons';
import { ModalForm, ProForm, } from '@ant-design/pro-components';
import { Button, message, Input } from 'antd';
import { GET, POST } from "@/services/crud";

const { Search } = Input;

export type AddUserProps = {
  projectId: string;
  roleId: string;
  actionRef: any;
};
const AddUser: React.FC<AddUserProps> = (props) => {
  const [account, setAccount] = useState("")
  const [username, setUsername] = useState("")
  const [okDisabled, setOkDisabled] = useState(true)

  const getUserInfo = async (account: string) => {
    if (!account) {
      return
    }
    const result = await GET('/auth/oauth/getUserInfo', { account });
    if (result.code === 200) {
      setAccount(account)
      setUsername(result.data)
      setOkDisabled(false)
    } else {
      setAccount('')
      setOkDisabled(true)
      message.error(result.msg)
    }
  }

  return (<>
    <ModalForm
      title="添加成员"
      trigger={
        <Button key="add-user" type="primary">
          <PlusOutlined />
          添加成员
        </Button>
      }
      autoFocusFirstInput
      modalProps={{
        destroyOnClose: true,
        onCancel: () => {
          setAccount('')
          setOkDisabled(true)
          setUsername('')
        },
      }}
      submitter={{
        resetButtonProps: {
          type: 'dashed',
        },
        render: (props, doms) => {
          console.log(props);
          return [
            <Button disabled={okDisabled} key="submit" onClick={() => props.form?.submit?.()}>
              提交
            </Button>
          ];
        },
      }}
      submitTimeout={2000}
      onFinish={async (values: any) => {
        console.log(values.user);
        await POST('/ncnb/project/group/role/users', {
          projectId: props.projectId,
          roleId: props.roleId,
          userIds: [account],
        }).then((resp) => {
          console.log(34, props.actionRef);
          if (resp?.code === 200) {
            message.success("保存成功");
          }
          props.actionRef.current?.reload();
          setAccount('')
          setOkDisabled(true)
          setUsername('')
        });
        return true;
      }}
    >
      <ProForm.Group>
        <Search placeholder="请输入" onInput={(it: any) => setAccount(it.target.value)} onSearch={getUserInfo} style={{ width: 200 }}
          onBlur={() => getUserInfo(account)} />
        <Input placeholder="用户名" disabled value={username} />
      </ProForm.Group>
    </ModalForm></>);
};

export default React.memo(AddUser)

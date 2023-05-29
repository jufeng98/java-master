import React, { Ref, useEffect, useImperativeHandle, useState } from 'react';
import { Input, Modal, message } from 'antd';
import request from "@/utils/request";

export type ChangePwdProps = {
    onRef: Ref<any>;
};
const ChangePwd: React.FC<ChangePwdProps> = (props) => {
    const [open, setOpen] = useState(false);
    const [confirmLoading, setConfirmLoading] = useState(false);
    const [newPwd, setNewPwd] = useState('');


    const handleOk = () => {
        setConfirmLoading(true);
        request.post('/auth/oauth/changePwd', { data: { newPwd } })
            .then(res => {
                setConfirmLoading(false);
                message.success(res.data);
                setOpen(false);
            })
    };

    useImperativeHandle(props.onRef, () => ({
        showModal: () => {
            setOpen(true)
        },
      }));

    const handleCancel = () => {
        setOpen(false);
    };

    return (
        <>
            <Modal
                title="修改密码"
                open={open}
                onOk={handleOk}
                confirmLoading={confirmLoading}
                onCancel={handleCancel}
            >
                <Input.Password placeholder="新密码" onChange={(e) => setNewPwd(e.target.value)} value={newPwd} />
            </Modal>
        </>
    );
};
export default React.memo(ChangePwd);
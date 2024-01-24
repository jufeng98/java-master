import React, { useEffect, useRef, useState } from 'react';
import type { MenuProps } from 'antd';
import defaultProps from './_defaultProps';
import ChangePwd from './changePwd'
import { history, Link, Outlet } from "@@/exports";
import { PageContainer, ProCard, ProLayout, ProSettings, WaterMark } from '@ant-design/pro-components';
import { Me } from "@icon-park/react";
import { headRightContent } from "@/layouts/DesignLayout";
import { Button, ConfigProvider, Dropdown, theme } from "antd";
import { logout } from "@/utils/request";
import * as cache from "@/utils/cache";
import { useModel } from "@umijs/max";
import useTabStore from "@/store/tab/useTabStore";
import Theme from "@/components/Theme";


export interface HomeLayoutLayoutProps {
  children: any;
}

const HomeLayout: React.FC<HomeLayoutLayoutProps> = props => {
  const [pathname, setPathname] = useState('/project/recent');
  const { setInitialState } = useModel('@@initialState');
  const { tabDispatch } = useTabStore(state => ({ tabDispatch: state.dispatch }));
  const changePwdRef = useRef(null);

  useEffect(() => {
    // console.log('回首页清空权限');
    tabDispatch.removeAllTab({});
    setInitialState((s: any) => ({ ...s, access: {} }));
  }, [])

  const settings: ProSettings | undefined = {
    "layout": "mix",
    "navTheme": "light",
    "contentWidth": "Fluid",
    "fixSiderbar": true,
    "siderMenuType": "group",
    "fixedHeader": true
  };

  const items: MenuProps['items'] = [
    {
      key: '1',
      label: (<Button onClick={() => {
        setInitialState((s: any) => ({ ...s, access: {} }));
        logout();
      }}>退出登录</Button>),
    },
    // {
    //   key: '2',
    //   label: (<Button onClick={() => {
    //     changePwdRef?.current?.showModal()
    //   }}>修改密码</Button>),
    // },
  ];

  return (
    <WaterMark content={['ERD Online', 'V4.0.7']}>
      <ChangePwd onRef={changePwdRef}/>
      <ProLayout
        logo={"/invocationlab-erd-online-view/logo.svg"}
        title={"ERD Online"}
        {...defaultProps}
        location={{
          pathname,
        }}
        avatarProps={{
          src: <Me theme="filled" size="28" fill="#DE2910" strokeWidth={2} />,
          title: <Dropdown menu={{ items }} placement="bottom"
            arrow={{ pointAtCenter: true }}>
            <div>{cache.getItem('username')}</div>
          </Dropdown>,
        }}
        actionsRender={(props) => {
          if (props.isMobile) return [];
          // return headRightContent;
          return [];
        }}
        menuFooterRender={(props) => {
          if (props?.collapsed) return undefined;
          return (
            <div
              style={{
                textAlign: 'center',
                paddingBlockStart: 12,
              }}
            >
              <div>© 2023 Made with erd</div>
              <div>ERD Online</div>
            </div>
          );
        }}
        onMenuHeaderClick={(e) => history.push("/")}
        menuItemRender={(item, dom) => (
          item.path?.startsWith('http') || item.exact ?
            <a href={item?.path || '/project'} target={'_blank'}>{dom}</a>
            :

            <div
              onClick={() => {
                console.log(153, item);
                setPathname(item.path || '/project');
              }}
            >
              <Link to={item?.path || '/project'}>{dom}</Link>
            </div>
        )}
        {...settings}
      >
        <PageContainer
          title={false}
        >
          <ProCard
            style={{
              minHeight: '85vh',
            }}
          >
            <Theme />
          </ProCard>
        </PageContainer>
      </ProLayout>
    </WaterMark>
  );
}
export default React.memo(HomeLayout);

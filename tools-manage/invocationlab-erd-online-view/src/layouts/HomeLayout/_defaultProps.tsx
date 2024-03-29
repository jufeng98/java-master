import {EveryUser, HomeTwo, Sphere, Timeline, User} from "@icon-park/react";

export default {
  route: {
    path: '/',
    routes: [
      {
        path: '/project/recent',
        name: '最近',
        icon: <Timeline theme="filled" size="18" fill="#DE2910" strokeWidth={2}/>,
      },
      {
        path: '/project/person',
        name: '个人',
        icon: <User theme="filled" size="18" fill="#DE2910" strokeWidth={2}/>,
      },
      {
        path: '/project/group',
        name: '团队',
        icon: <EveryUser theme="filled" size="18" fill="#DE2910" strokeWidth={2}/>,
      },
      {
        path: '/project/home',
        name: '概览',
        icon: <HomeTwo theme="filled" size="18" fill="#DE2910" strokeWidth={2}/>,
      },
      {
        exact: true,
        path: 'https://doc.erdonline.com',
        name: 'ERD Online 论坛',
        icon: <Sphere theme="filled" size="18" fill="#DE2910" strokeWidth={2}/>
      },
    ],
  },
  location: {
    pathname: '/',
  },
  appList: [
    {
      icon: '/invocationlab-erd-online-view/logo.svg',
      title: 'ERD Online',
      desc: '国内第一个开源免费在线建模软件',
      url: 'https://portal.zerocode.net.cn/',
      target: '_blank',
    },
    {
      icon: '/invocationlab-erd-online-view/zerocode.svg',
      title: '零代',
      desc: '国内第一个零代码社区',
      url: 'https://doc.erdonline.com',
      target: '_blank',
    },
    {
      icon: '/invocationlab-erd-online-view/loco.svg',
      title: 'LOCO',
      desc: '类钉钉宜搭的低代码搭建平台',
      url: 'https://loco.zerocode.net.cn/',
      target: '_blank',
    },
    {
      icon: '/invocationlab-erd-online-view/zerocode.svg',
      title: 'Fast Test',
      desc: '接口快速测试平台',
      url: 'https://doc.erdonline.com',
      target: '_blank',
    },
    {
      icon: '/invocationlab-erd-online-view/zerocode.svg',
      title: 'Super BI',
      desc: '超级报表、BI引擎',
      url: 'https://doc.erdonline.com',
      target: '_blank',
    },
  ],
};

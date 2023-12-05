"use strict";(self.webpackChunkInvocationlab_ERD_Online=self.webpackChunkInvocationlab_ERD_Online||[]).push([[6549],{98855:function(te,D){var e={icon:{tag:"svg",attrs:{viewBox:"64 64 896 896",focusable:"false"},children:[{tag:"path",attrs:{d:"M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"}},{tag:"path",attrs:{d:"M464 336a48 48 0 1096 0 48 48 0 10-96 0zm72 112h-48c-4.4 0-8 3.6-8 8v272c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V456c0-4.4-3.6-8-8-8z"}}]},name:"info-circle",theme:"outlined"};D.Z=e},33931:function(te,D,e){var o=e(76813),C=e(16130),m=e(9009),j=e(49507),f=e(38297),N=e(34062),M=e(6702),F=e(98404),I=e(46767),G=e(84613),y=e(36422),K=["children","value","valuePropName","onChange","fieldProps","space","type","transform","convertValue"],x=["children","space","valuePropName"],B={space:f.Z,group:N.Z.Group};function T(n){var t=arguments.length<=1?void 0:arguments[1];return t&&t.target&&n in t.target?t.target[n]:t}var g=function(t){var r=t.children,S=t.value,v=S===void 0?[]:S,h=t.valuePropName,l=t.onChange,i=t.fieldProps,A=t.space,u=t.type,O=u===void 0?"space":u,_=t.transform,V=t.convertValue,R=(0,m.Z)(t,K),ae=(0,j.J)(function(a,s){var c,P=(0,C.Z)(v);P[s]=T(h||"value",a),l==null||l(P),i==null||(c=i.onChange)===null||c===void 0||c.call(i,P)}),z=-1,d=(0,M.Z)(r).map(function(a){if(F.isValidElement(a)){var s,c,P;z+=1;var E=z,Q=(a==null||(s=a.type)===null||s===void 0?void 0:s.displayName)==="ProFormComponent"||(a==null||(c=a.props)===null||c===void 0?void 0:c.readonly),re=Q?(0,o.Z)((0,o.Z)({key:E,ignoreFormItem:!0},a.props||{}),{},{fieldProps:(0,o.Z)((0,o.Z)({},a==null||(P=a.props)===null||P===void 0?void 0:P.fieldProps),{},{onChange:function(){ae(arguments.length<=0?void 0:arguments[0],E)}}),value:v==null?void 0:v[E],onChange:void 0}):(0,o.Z)((0,o.Z)({key:E},a.props||{}),{},{value:v==null?void 0:v[E],onChange:function(Y){var $,b;ae(Y,E),($=(b=a.props).onChange)===null||$===void 0||$.call(b,Y)}});return F.cloneElement(a,re)}return a}),W=B[O],L=(0,G.zx)(R),p=L.RowWrapper,U=(0,F.useMemo)(function(){return(0,o.Z)({},O==="group"?{compact:!0}:{})},[O]),J=(0,F.useCallback)(function(a){var s=a.children;return(0,y.jsx)(W,(0,o.Z)((0,o.Z)((0,o.Z)({},U),A),{},{align:"start",children:s}))},[W,A,U]);return(0,y.jsx)(p,{Wrapper:J,children:d})},q=F.forwardRef(function(n,t){var r=n.children,S=n.space,v=n.valuePropName,h=(0,m.Z)(n,x);return(0,F.useImperativeHandle)(t,function(){return{}}),(0,y.jsx)(g,(0,o.Z)((0,o.Z)((0,o.Z)({space:S,valuePropName:v},h.fieldProps),{},{onChange:void 0},h),{},{children:r}))}),Z=(0,I.G)(q);D.Z=Z},47174:function(te,D,e){var o=e(76813),C=e(9009),m=e(98404),j=e(85016),f=e(36422),N=["fieldProps","proFieldProps"],M=["fieldProps","proFieldProps"],F="text",I=function(x){var B=x.fieldProps,T=x.proFieldProps,g=(0,C.Z)(x,N);return(0,f.jsx)(j.Z,(0,o.Z)({valueType:F,fieldProps:B,filedConfig:{valueType:F},proFieldProps:T},g))},G=function(x){var B=x.fieldProps,T=x.proFieldProps,g=(0,C.Z)(x,M);return(0,f.jsx)(j.Z,(0,o.Z)({valueType:"password",fieldProps:B,proFieldProps:T,filedConfig:{valueType:F}},g))},y=I;y.Password=G,y.displayName="ProFormComponent",D.Z=y},67026:function(te,D,e){e.d(D,{A:function(){return v}});var o=e(76813),C=e(25409),m=e(98404),j=e(51201),f=e(34299),N=e(85765),M=e(37311),F=e(49687),I=e(85383),G=e(27085),y=e(38297),K=e(7421),x=e.n(K),B=e(50322),T=e(84613),g=e(62005),q=function(l){var i;return(0,f.Z)({},l.componentCls,{"&-title":{marginBlockEnd:l.marginXL,fontWeight:"bold"},"&-container":(0,f.Z)({flexWrap:"wrap",maxWidth:"100%"},"> div".concat(l.antCls,"-space-item"),{maxWidth:"100%"}),"&-twoLine":(i={display:"block",width:"100%"},(0,f.Z)(i,"".concat(l.componentCls,"-title"),{width:"100%",margin:"8px 0"}),(0,f.Z)(i,"".concat(l.componentCls,"-container"),{paddingInlineStart:16}),(0,f.Z)(i,"".concat(l.antCls,"-space-item,").concat(l.antCls,"-form-item"),{width:"100%"}),(0,f.Z)(i,"".concat(l.antCls,"-form-item"),{"&-control":{display:"flex",alignItems:"center",justifyContent:"flex-end","&-input":{alignItems:"center",justifyContent:"flex-end","&-content":{flex:"none"}}}}),i)})};function Z(h){return(0,g.Xj)("ProFormGroup",function(l){var i=(0,o.Z)((0,o.Z)({},l),{},{componentCls:".".concat(h)});return[q(i)]})}var n=e(36422),t=m.forwardRef(function(h,l){var i=m.useContext(B.Z),A=i.groupProps,u=(0,o.Z)((0,o.Z)({},A),h),O=u.children,_=u.collapsible,V=u.defaultCollapsed,R=u.style,ae=u.labelLayout,z=u.title,d=z===void 0?h.label:z,W=u.tooltip,L=u.align,p=L===void 0?"start":L,U=u.direction,J=u.size,a=J===void 0?32:J,s=u.titleStyle,c=u.titleRender,P=u.spaceProps,E=u.extra,Q=u.autoFocus,re=(0,F.Z)(function(){return V||!1},{value:h.collapsed,onChange:h.onCollapse}),X=(0,N.Z)(re,2),Y=X[0],$=X[1],b=(0,m.useContext)(G.ZP.ConfigContext),ee=b.getPrefixCls,ue=(0,T.zx)(h),de=ue.ColWrapper,se=ue.RowWrapper,w=ee("pro-form-group"),ne=Z(w),ie=ne.wrapSSR,k=ne.hashId,ve=_&&(0,n.jsx)(M.Z,{style:{marginInlineEnd:8},rotate:Y?void 0:90}),pe=(0,n.jsx)(I.G,{label:ve?(0,n.jsxs)("div",{children:[ve,d]}):d,tooltip:W}),me=(0,m.useCallback)(function(oe){var le=oe.children;return(0,n.jsx)(y.Z,(0,o.Z)((0,o.Z)({},P),{},{className:x()("".concat(w,"-container ").concat(k),P==null?void 0:P.className),size:a,align:p,direction:U,style:(0,o.Z)({rowGap:0},P==null?void 0:P.style),children:le}))},[p,w,U,k,a,P]),fe=c?c(pe,h):pe,Pe=(0,m.useMemo)(function(){var oe=[],le=m.Children.toArray(O).map(function(H,ge){var ce;return m.isValidElement(H)&&H!==null&&H!==void 0&&(ce=H.props)!==null&&ce!==void 0&&ce.hidden?(oe.push(H),null):ge===0&&m.isValidElement(H)&&Q?m.cloneElement(H,(0,o.Z)((0,o.Z)({},H.props),{},{autoFocus:Q})):H});return[(0,n.jsx)(se,{Wrapper:me,children:le},"children"),oe.length>0?(0,n.jsx)("div",{style:{display:"none"},children:oe}):null]},[O,se,me,Q]),he=(0,N.Z)(Pe,2),Ce=he[0],Ee=he[1];return ie((0,n.jsx)(de,{children:(0,n.jsxs)("div",{className:x()(w,k,(0,f.Z)({},"".concat(w,"-twoLine"),ae==="twoLine")),style:R,ref:l,children:[Ee,(d||W||E)&&(0,n.jsx)("div",{className:"".concat(w,"-title ").concat(k),style:s,onClick:function(){$(!Y)},children:E?(0,n.jsxs)("div",{style:{display:"flex",width:"100%",alignItems:"center",justifyContent:"space-between"},children:[fe,(0,n.jsx)("span",{onClick:function(le){return le.stopPropagation()},children:E})]}):fe}),(0,n.jsx)("div",{style:{display:_&&Y?"none":void 0},children:Ce})]})}))});t.displayName="ProForm-Group";var r=t,S=e(9475);function v(h){return(0,n.jsx)(j.I,(0,o.Z)({layout:"vertical",submitter:{render:function(i,A){return A.reverse()}},contentRender:function(i,A){return(0,n.jsxs)(n.Fragment,{children:[i,A]})}},h))}v.Group=r,v.useForm=C.Z.useForm,v.Item=S.Z,v.useWatch=C.Z.useWatch,v.ErrorList=C.Z.ErrorList,v.Provider=C.Z.Provider,v.useFormInstance=C.Z.useFormInstance},85383:function(te,D,e){e.d(D,{G:function(){return q}});var o=e(76813),C=e(34299),m=e(87006),j=e(98404),f=e(98855),N=e(41566),M=function(n,t){return j.createElement(N.Z,(0,m.Z)({},n,{ref:t,icon:f.Z}))},F=j.forwardRef(M),I=e(27085),G=e(45836),y=e(7421),K=e.n(y),x=e(62005),B=function(n){return(0,C.Z)({},n.componentCls,{display:"inline-flex",alignItems:"center",maxWidth:"100%","&-icon":{display:"block",marginInlineStart:"4px",cursor:"pointer","&:hover":{color:n.colorPrimary}},"&-title":{display:"inline-flex",flex:"1"},"&-subtitle ":{marginInlineStart:8,color:n.colorTextSecondary,fontWeight:"normal",fontSize:n.fontSize,whiteSpace:"nowrap"},"&-title-ellipsis":{overflow:"hidden",whiteSpace:"nowrap",textOverflow:"ellipsis",wordBreak:"keep-all"}})};function T(Z){return(0,x.Xj)("LabelIconTip",function(n){var t=(0,o.Z)((0,o.Z)({},n),{},{componentCls:".".concat(Z)});return[B(t)]})}var g=e(36422),q=j.memo(function(Z){var n=Z.label,t=Z.tooltip,r=Z.ellipsis,S=Z.subTitle,v=(0,j.useContext)(I.ZP.ConfigContext),h=v.getPrefixCls,l=h("pro-core-label-tip"),i=T(l),A=i.wrapSSR,u=i.hashId;if(!t&&!S)return(0,g.jsx)(g.Fragment,{children:n});var O=typeof t=="string"||j.isValidElement(t)?{title:t}:t,_=(O==null?void 0:O.icon)||(0,g.jsx)(F,{});return A((0,g.jsxs)("div",{className:K()(l,u),onMouseDown:function(R){return R.stopPropagation()},onMouseLeave:function(R){return R.stopPropagation()},onMouseMove:function(R){return R.stopPropagation()},children:[(0,g.jsx)("div",{className:K()("".concat(l,"-title"),u,(0,C.Z)({},"".concat(l,"-title-ellipsis"),r)),children:n}),S&&(0,g.jsx)("div",{className:"".concat(l,"-subtitle ").concat(u),children:S}),t&&(0,g.jsx)(G.Z,(0,o.Z)((0,o.Z)({},O),{},{children:(0,g.jsx)("span",{className:"".concat(l,"-icon ").concat(u),children:_})}))]}))})},31092:function(te,D,e){e.r(D),e.d(D,{default:function(){return ae}});var o=e(97833),C=e.n(o),m=e(42196),j=e.n(m),f=e(98404),N=e(67026),M=e(47174),F=e(33931),I=e(76813),G=e(9009),y=e(87006),K={icon:{tag:"svg",attrs:{viewBox:"64 64 896 896",focusable:"false"},children:[{tag:"path",attrs:{d:"M400 317.7h73.9V656c0 4.4 3.6 8 8 8h60c4.4 0 8-3.6 8-8V317.7H624c6.7 0 10.4-7.7 6.3-12.9L518.3 163a8 8 0 00-12.6 0l-112 141.7c-4.1 5.3-.4 13 6.3 13zM878 626h-60c-4.4 0-8 3.6-8 8v154H214V634c0-4.4-3.6-8-8-8h-60c-4.4 0-8 3.6-8 8v198c0 17.7 14.3 32 32 32h684c17.7 0 32-14.3 32-32V634c0-4.4-3.6-8-8-8z"}}]},name:"upload",theme:"outlined"},x=K,B=e(41566),T=function(d,W){return f.createElement(B.Z,(0,y.Z)({},d,{ref:W,icon:x}))},g=f.forwardRef(T),q=e(10517),Z=e(26774),n=e(46767),t=e(85452),r=e(36422),S=["fieldProps","action","accept","listType","title","max","icon","buttonProps","onChange","disabled","proFieldProps"],v=function(d,W){var L,p=d.fieldProps,U=d.action,J=d.accept,a=d.listType,s=d.title,c=s===void 0?"\u5355\u51FB\u4E0A\u4F20":s,P=d.max,E=d.icon,Q=E===void 0?(0,r.jsx)(g,{}):E,re=d.buttonProps,X=d.onChange,Y=d.disabled,$=d.proFieldProps,b=(0,G.Z)(d,S),ee=(0,f.useMemo)(function(){var ne;return(ne=b.fileList)!==null&&ne!==void 0?ne:b.value},[b.fileList,b.value]),ue=(0,f.useContext)(t.A),de=($==null?void 0:$.mode)||ue.mode||"edit",se=(P===void 0||!ee||(ee==null?void 0:ee.length)<P)&&de!=="read",w=(a!=null?a:p==null?void 0:p.listType)==="picture-card";return(0,r.jsx)(q.Z,(0,I.Z)((0,I.Z)({action:U,accept:J,ref:W,listType:a||"picture",fileList:ee},p),{},{name:(L=p==null?void 0:p.name)!==null&&L!==void 0?L:"file",onChange:function(ie){var k;X==null||X(ie),p==null||(k=p.onChange)===null||k===void 0||k.call(p,ie)},children:se&&(w?(0,r.jsxs)("span",{children:[Q," ",c]}):(0,r.jsxs)(Z.ZP,(0,I.Z)((0,I.Z)({disabled:Y||(p==null?void 0:p.disabled)},re),{},{children:[Q,c]})))}))},h=(0,n.G)(f.forwardRef(v),{getValueFromEvent:function(d){return d.fileList}}),l=h,i=e(71793),A=e(5049),u=e(75687),O=e(77662),_=e(51927),V=e(56713),R=function(d){var W=i.rV(_.s.PROJECT_ID),L=(0,V.md)(),p=(0,u.ZP)(function(a){var s,c;return{projectDispatch:a.dispatch,profile:(s=a.project)===null||s===void 0||(c=s.projectJSON)===null||c===void 0?void 0:c.profile}},O.Z),U=p.projectDispatch,J=p.profile;return(0,r.jsx)(r.Fragment,{children:(0,r.jsxs)(N.A,{initialValues:J,onFinish:function(){var a=j()(C()().mark(function s(c){return C()().wrap(function(E){for(;;)switch(E.prev=E.next){case 0:return E.next=2,U.updateProfile(c);case 2:return E.abrupt("return",!0);case 3:case"end":return E.stop()}},s)}));return function(s){return a.apply(this,arguments)}}(),children:[(0,r.jsx)(M.Z.Password,{width:"lg",label:"ERD\u79D8\u94A5",extra:"\u4EC5\u7528\u4E8EERD\u5BFC\u5165\u5BFC\u51FA\u52A0\u5BC6\u89E3\u5BC6",name:"erdPassword",placeholder:"\u9ED8\u8BA4\u4E3AERDOnline"}),(0,r.jsx)(M.Z,{width:"lg",name:"sqlConfig",label:"SQL\u5206\u9694\u7B26",extra:"\u5206\u9694\u6BCF\u6761\u5F80\u6570\u636E\u5E93\u6267\u884C\u7684SQL",placeholder:"\u9ED8\u8BA4\u4E3A/*SQL@Run*/",formItemProps:{rules:[{max:100,message:"\u4E0D\u80FD\u5927\u4E8E 100 \u4E2A\u5B57\u7B26"}]}}),(0,r.jsx)(M.Z,{width:"lg",name:"moduleNameFormat",label:"\u5143\u6570\u636E->\u6A21\u5757\u540D\u663E\u793A\u683C\u5F0F",extra:"\u6A21\u578B->\u5143\u6570\u636E\u4E2D\uFF0C\u6A21\u5757\u540D\u79F0\u663E\u793A\u683C\u5F0F\uFF1A{name}\u663E\u793A\u82F1\u6587\u540D\uFF0C{chnname}\u663E\u793A\u4E2D\u6587\u540D\uFF0C{name} {chnname}\u4E3A\u82F1\u6587\u548C\u4E2D\u6587\u7684\u7EC4\u5408\u540D",placeholder:"\u9ED8\u8BA4\u4E3A {name} {chnname}",formItemProps:{rules:[{max:100,message:"\u4E0D\u80FD\u5927\u4E8E 100 \u4E2A\u5B57\u7B26"}]}}),(0,r.jsx)(M.Z,{width:"lg",name:"tableNameFormat",label:"\u5143\u6570\u636E->\u8868\u540D\u663E\u793A\u683C\u5F0F",extra:"\u6A21\u578B->\u5143\u6570\u636E\u4E2D\uFF0C\u8868\u540D\u79F0\u663E\u793A\u683C\u5F0F\uFF1A{title}\u663E\u793A\u82F1\u6587\u540D\uFF0C{chnname}\u663E\u793A\u4E2D\u6587\u540D\uFF0C{title} {chnname}\u4E3A\u82F1\u6587\u548C\u4E2D\u6587\u7684\u7EC4\u5408\u540D",placeholder:"\u9ED8\u8BA4\u4E3A {title} {chnname}",formItemProps:{rules:[{max:100,message:"\u4E0D\u80FD\u5927\u4E8E 100 \u4E2A\u5B57\u7B26"}]}}),(0,r.jsxs)(F.Z,{label:"WORD\u6A21\u677F\u914D\u7F6E",extra:"\u9ED8\u8BA4\u4E3A\u7CFB\u7EDF\u81EA\u5E26\u7684\u6A21\u677F\uFF0C\u5982\u9700\u4FEE\u6539\uFF0C\u8BF7\u5148\u4E0B\u8F7D\uFF0C\u518D\u91CD\u65B0\u4E0A\u4F20\u6A21\u677F\u6587\u4EF6",children:[(0,r.jsx)(V.Nv,{accessible:L.canErdDocUploadwordtemplate,fallback:(0,r.jsx)(r.Fragment,{}),children:(0,r.jsx)(l,{max:1,name:"wordTemplateConfig",fieldProps:{name:"file",headers:{Authorization:"Bearer 1"},onChange:function(s){if(s.file.status=="done")if(s.file.response.code==200)U.updateWordTemplateConfig(s.file.response.data);else{var c;A.ZP.error((c=s.file.response.msg)!==null&&c!==void 0?c:"\u4E0A\u4F20\u5931\u8D25")}else s.file.status=="error"&&A.ZP.error("\u4E0A\u4F20\u5931\u8D25")}},action:"".concat("http://192.168.241.106:8083","/ncnb/doc/uploadWordTemplate/").concat(W)})}),(0,r.jsx)(V.Nv,{accessible:L.canErdDocDownloadwordtemplate,fallback:(0,r.jsx)(r.Fragment,{}),children:(0,r.jsx)(Z.ZP,{title:"\u4E0B\u8F7D\u6A21\u677F",onClick:function(){return U.downloadWordTemplate()},children:"\u4E0B\u8F7D\u6A21\u677F"})})]})]})})},ae=f.memo(R)},10517:function(te,D,e){var o=e(35397),C=e(12416);const m=C.Z;m.Dragger=o.Z,m.LIST_IGNORE=C.E,D.Z=m}}]);
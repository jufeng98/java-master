import { CONSTANT } from "@/utils/constant";
const lStorage = localStorage || {};
const sStorage = sessionStorage || {};
const __cache = {};

export const setCache = (key, value) => {
  __cache[CONSTANT.PREFIX + key] = value;
};

export const getCache = (key) => {
  return __cache[CONSTANT.PREFIX + key];
};

export const deleteCache = (key) => {
  delete __cache[CONSTANT.PREFIX + key];
};

export const getSessionId = () => {
  return lStorage.getItem(CONSTANT.PREFIX + 'SessionId');
};

export const setSessionId = (SessionId) => {
  lStorage.setItem(CONSTANT.PREFIX + 'SessionId', SessionId);
};

export const getUser = () => {
  return JSON.parse(lStorage.getItem(CONSTANT.PREFIX + 'user') || '{}');
};

export const setUser = (user) => {
  lStorage.setItem(CONSTANT.PREFIX + 'user', JSON.stringify(user || {}));
};

export const setItem = (key, value) => {
  if (key === CONSTANT.PROJECT_ID) {
    sStorage.setItem(key, value);
    return
  }
  key = CONSTANT.PREFIX + key
  lStorage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value || {}));
};

export const getItem = (key) => {
  if (key === CONSTANT.PROJECT_ID) {
    return sStorage.getItem(key);
  }
  key = CONSTANT.PREFIX + key
  return lStorage.getItem(key);
};

export const getItem2object = (key) => {
  return JSON.parse(getItem(CONSTANT.PREFIX + key) || '{}');
};

export const clear = () => {
  Object.keys(lStorage)
    .forEach(k => {
      if (k.startsWith(CONSTANT.PREFIX)) {
        lStorage.removeItem(k)
      }
    })
};

export const isProEnv = () => {
  return getItem('env') === 'pro'
}

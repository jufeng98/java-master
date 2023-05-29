import { CONSTANT } from "@/utils/constant";
const lStorage = localStorage || {};
const sStorage = sessionStorage || {};
const __cache = {};

export const setCache = (key, value) => {
  __cache[key] = value;
};

export const getCache = (key) => {
  return __cache[key];
};

export const deleteCache = (key) => {
  delete __cache[key];
};

export const getSessionId = () => {
  return lStorage.getItem('SessionId');
};

export const setSessionId = (SessionId) => {
  lStorage.setItem('SessionId', SessionId);
};

export const getUser = () => {
  return JSON.parse(lStorage.getItem('user') || '{}');
};

export const setUser = (user) => {
  lStorage.setItem('user', JSON.stringify(user || {}));
};

export const setItem = (key, value) => {
  if (key === CONSTANT.PROJECT_ID) {
    sStorage.setItem(key, value);
    return
  }
  lStorage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value || {}));
};

export const getItem = (key) => {
  if (key === CONSTANT.PROJECT_ID) {
    return sStorage.getItem(key);
  }
  return lStorage.getItem(key);
};

export const getItem2object = (key) => {
  return JSON.parse(getItem(key) || '{}');
};

export const clear = () => {
  lStorage.clear();
};

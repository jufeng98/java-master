const prefix = "rpc-postman:"

export default {
    clearItems:() => {
        Object.keys(localStorage)
            .forEach(k => {
                if(k.startsWith(prefix)) {
                    localStorage.removeItem(k)
                }
            })
    },
    setItem:(key, value) => {
        localStorage.setItem(prefix + key, value)
    },
    getItem:(key) => {
        localStorage.getItem(prefix + key)
    },
}
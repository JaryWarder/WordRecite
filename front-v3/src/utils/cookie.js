export function getCookie (name) {
  const cookies = document.cookie.replace(/\s+/g, '')
  const arr = cookies.split(';')
  for (let i = 0; i < arr.length; i++) {
    const a = arr[i].split('=')
    if (a[0] === name) return a[1]
  }
  return null
}

export function setCookie (name, value, expireDays) {
  const expireDate = new Date()
  expireDate.setDate(expireDate.getDate() + expireDays)
  document.cookie =
    name +
    '=' +
    escape(value) +
    (expireDays == null ? '' : ';expires=' + expireDate.toUTCString())
}

export function delCookie (name, expireDays) {
  const expireDate = new Date()
  expireDate.setDate(expireDate.getDate() + expireDays)
  document.cookie =
    name +
    '=' +
    escape('null') +
    (expireDays == null ? '' : ';expires=' + expireDate.toUTCString())
}

function loginApi(data) {
  //页面调用了该方法，该方法即 页面调用axios方法 去请求 新的页面
  return $axios({
    'url': '/employee/login',
    'method': 'post',
    data
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}

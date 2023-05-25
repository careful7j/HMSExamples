package net.c7j.wna.huawei

interface IapApiCallback<T> {

    fun onSuccess(result: T)

    fun onFail(e: Exception?)

}
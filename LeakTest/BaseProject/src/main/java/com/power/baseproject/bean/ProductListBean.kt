package com.power.baseproject.bean

import java.io.Serializable

class ProductListBean<T> :Serializable{
    var current_page: Int? = null
    var data: T? = null
    var first_page_url: String? = null
    var from: Int? = null
    var last_page: Int? = null
    var last_page_url: String? = null
    var next_page_url: String? = null
    var path: String? = null
    var per_page: Int? = null
    var prev_page_url: String? = null
    var to: Int? = null
    var total: Int? = null
    override fun toString(): String {
        return "ProductListBean(current_page=$current_page, data=$data, first_page_url=$first_page_url, from=$from, last_page=$last_page, last_page_url=$last_page_url, next_page_url=$next_page_url, path=$path, per_page=$per_page, prev_page_url=$prev_page_url, to=$to, total=$total)"
    }

}
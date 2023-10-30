package com.tb.moviestools.sfapi

import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *_    .--,       .--,
 *_   ( (  \\.---./  ) )
 *_    '.__/o   o\\__.'
 *_       {=  ^  =}
 *_        >  -  <
 *_       /       \\
 *_      //       \\\\
 *_     //|   .   |\\\\
 *_     \"'\\       /'\"_.-~^`'-.
 *_        \\  _  /--'         `
 *_      ___)( )(___
 *_     (((__) (__)))
 * author      : xue
 * date        : 2023/10/30 13:45
 * description :
 */
interface TfVideoService {
    @POST("/v1/media/page_need_created")
    fun getAllCreatedList(@Body params: RequestBody) : Observable<String>
}
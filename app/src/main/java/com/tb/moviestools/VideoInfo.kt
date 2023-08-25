package com.tb.moviestools

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
 *_     (((__) (__)))    高山仰止,景行行止.虽不能至,心向往之。
 * author      : xue
 * date        : 2023/8/24 13:39
 * description :
 */
class VideoInfo {
    var id: String = ""
    var name: String = ""
    var dataType: Int = 0
    var tvAllEps:List<EpsItem>? = null
}

class SeasonItem{
    var id = ""
    var title = ""
}

class EpsItem{
    var id = ""
    var title = ""
    var epsNum = ""
}

package com.arsvechkarev.common

import core.Networker
import core.datetime.TimeFormatter
import core.model.NewsItemWithPicture
import core.recycler.DifferentiableItem
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber

class NewYorkTimesNewsRepository(
  private val networker: Networker,
  private val formatter: TimeFormatter,
  nytApiKey: String
) {
  
  private val baseUrl = "https://api.nytimes.com/svc/search/v2/" +
      "articlesearch.json?api-key=$nytApiKey&q=coronavirus&fq=headline:" +
      "(%22coronavirus%22%20%22covid-19%22%20%22covid%22)&sort=newest&page="
  
  fun getLatestNews(page: Int): Observable<List<DifferentiableItem>> {
    val url = baseUrl + page.toString()
    Timber.d("Loading page $page")
    return networker.request(url)
        .map { transformJson(it) }
  }
  
  private fun transformJson(json: String): List<DifferentiableItem> {
    val news = ArrayList<NewsItemWithPicture>()
    val outerObject = JSONObject(json)
    val array = outerObject.getJSONObject("response").getJSONArray("docs")
    for (i in 0 until array.length()) {
      val item = array.getJSONObject(i)
      val id = item.getString("_id")
      val title = item.getJSONObject("headline").getString("main")
      val description = item.getString("lead_paragraph")
      val webUrl = item.getString("web_url")
      val date = item.getString("pub_date")
      val formattedDate = formatter.formatPublishedDate(date)
      val multimediaItem = item.getJSONArray("multimedia").optJSONObject(10)
      if (multimediaItem != null) {
        val imagePath = multimediaItem.getString("url")
        val imageUrl = "https://static01.nyt.com/$imagePath"
        Timber.d("$i: title='$title', id='$id',date='$date', imageUrl='$imageUrl'")
        news.add(NewsItemWithPicture(id, title, description, webUrl, formattedDate, imageUrl))
      }
    }
    return news.distinctBy { it.title }
  }
}
package com.arsvechkarev.news.list

import com.arsvechkarev.news.presentation.NewsFragment
import com.arsvechkarev.views.NewsItemView
import com.arsvechkarev.views.RoundedCornersImage
import com.bumptech.glide.Glide
import core.extenstions.assertThat
import core.model.BasicNewsItem
import core.model.NewsItemWithPicture
import core.recycler.BaseListAdapter
import core.recycler.delegate
import core.viewbuilding.Colors
import core.viewbuilding.Fonts
import core.viewbuilding.Styles.NewsTextView

class NewsAdapter(
  fragment: NewsFragment,
  onNewsItemClicked: (BasicNewsItem) -> Unit
) : BaseListAdapter(
  delegate<NewsItemWithPicture> {
    buildView fn@{
      val textTitle = textView().withStyle(base = NewsTextView) {
        typeface = Fonts.SegoeUiBold
        maxLines = 3
      }
      val textDescription = textView().withStyle(base = NewsTextView) {
        maxLines = 1
      }
      val textTime = textView().apply { setTextColor(Colors.TextSecondary) }
      val image = RoundedCornersImage(context)
      return@fn NewsItemView(image, textTitle, textDescription, textTime)
    }
    onInitViewHolder {
      itemView.setOnClickListener { onNewsItemClicked(item) }
    }
    onBind { itemView, element ->
      assertThat(itemView is NewsItemView)
      itemView.textTitle.text = element.title
      itemView.textDescription.text = element.description
      itemView.textTime.text = element.publishedDate
      Glide.with(fragment)
          .load(element.imageUrl)
          .into(itemView.image)
    }
  }
)
package com.arsvechkarev.news.presentation

import android.content.Intent
import android.net.Uri
import android.view.Gravity.CENTER
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import base.BaseFragment
import base.behaviors.HeaderBehavior
import base.behaviors.ScrollingRecyclerBehavior
import base.behaviors.ViewUnderHeaderBehavior
import base.drawables.BaseLoadingStub.Companion.setLoadingDrawable
import base.drawables.GradientHeaderDrawable
import base.drawables.NewsListLoadingStub
import base.extensions.getMessageRes
import base.extensions.ifTrue
import base.extensions.subscribeToChannel
import base.hostActivity
import base.resources.Dimens.ErrorLayoutImageSize
import base.resources.Dimens.ErrorLayoutTextPadding
import base.resources.Dimens.GradientHeaderHeight
import base.resources.Dimens.ImageDrawerMargin
import base.resources.Styles.BoldTextView
import base.resources.Styles.HeaderTextView
import base.resources.TextSizes
import base.views.RetryButton
import com.arsvechkarev.news.R
import com.arsvechkarev.news.di.NewsComponent
import com.arsvechkarev.viewdsl.Size.Companion.MatchParent
import com.arsvechkarev.viewdsl.Size.Companion.WrapContent
import com.arsvechkarev.viewdsl.Size.IntSize
import com.arsvechkarev.viewdsl.animateInvisibleIfNeeded
import com.arsvechkarev.viewdsl.animateVisibleIfNeeded
import com.arsvechkarev.viewdsl.background
import com.arsvechkarev.viewdsl.behavior
import com.arsvechkarev.viewdsl.gone
import com.arsvechkarev.viewdsl.gravity
import com.arsvechkarev.viewdsl.image
import com.arsvechkarev.viewdsl.invisible
import com.arsvechkarev.viewdsl.layoutGravity
import com.arsvechkarev.viewdsl.margins
import com.arsvechkarev.viewdsl.onClick
import com.arsvechkarev.viewdsl.orientation
import com.arsvechkarev.viewdsl.paddings
import com.arsvechkarev.viewdsl.tag
import com.arsvechkarev.viewdsl.text
import com.arsvechkarev.viewdsl.textSize
import com.arsvechkarev.viewdsl.visible
import com.arsvechkarev.viewdsl.withViewBuilder
import core.BaseScreenState
import core.Failure
import core.FailureReason.NO_CONNECTION
import core.FailureReason.TIMEOUT
import core.FailureReason.UNKNOWN
import core.Loading
import core.di.CoreComponent.networkAvailabilityChannel
import timber.log.Timber

class NewsFragment : BaseFragment() {
  
  override fun buildLayout() = withViewBuilder {
    CoordinatorLayout(MatchParent, MatchParent) {
      child<View>(MatchParent, MatchParent) {
        tag(LayoutLoading)
        behavior(ViewUnderHeaderBehavior())
        setLoadingDrawable(NewsListLoadingStub())
      }
      child<RecyclerView>(MatchParent, MatchParent) {
        tag(RecyclerView)
        invisible()
        behavior(ScrollingRecyclerBehavior())
        adapter = newsAdapter
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      }
      child<LinearLayout>(MatchParent, MatchParent) {
        tag(LayoutFailure)
        invisible()
        gravity(CENTER)
        orientation(LinearLayout.VERTICAL)
        behavior(ViewUnderHeaderBehavior())
        child<ImageView>(ErrorLayoutImageSize, ErrorLayoutImageSize) {
          tag(ImageFailure)
          image(R.drawable.image_unknown_error)
          margins(bottom = ErrorLayoutTextPadding)
        }
        child<TextView>(WrapContent, WrapContent, style = BoldTextView) {
          tag(ErrorMessage)
          gravity(CENTER)
          paddings(
            start = ErrorLayoutTextPadding,
            end = ErrorLayoutTextPadding,
            bottom = ErrorLayoutTextPadding
          )
          textSize(TextSizes.H2)
        }
        child<RetryButton>(WrapContent, WrapContent) {
          tag(ButtonRetry)
          onClick { viewModel.retryLoadingData() }
        }
      }
      child<FrameLayout>(MatchParent, IntSize(GradientHeaderHeight + StatusBarHeight)) {
        behavior(HeaderBehavior(context))
        child<View>(MatchParent, MatchParent) {
          paddings(top = StatusBarHeight)
          background(GradientHeaderDrawable())
        }
        child<TextView>(WrapContent, WrapContent, style = HeaderTextView) {
          text(R.string.label_news)
          layoutGravity(CENTER)
        }
        child<ImageView>(WrapContent, WrapContent) {
          margins(start = ImageDrawerMargin, top = ImageDrawerMargin + StatusBarHeight)
          image(R.drawable.ic_drawer)
          onClick { hostActivity.openDrawer() }
        }
      }
    }
  }
  
  private lateinit var viewModel: NewsViewModel
  
  private val newsAdapter = NewsComponent.provideAdapter(this,
    onNewsItemClicked = { newsItem ->
      val intent = Intent(Intent.ACTION_VIEW)
      intent.data = Uri.parse(newsItem.webUrl)
      startActivity(intent)
    },
    onReadyToLoadNextPage = {
      viewModel.tryLoadNextPage()
    },
    onRetryItemClicked = {
      viewModel.retryLoadingNextPage()
    }
  )
  
  override fun onInit() {
    viewModel = NewsComponent.provideViewModel(this).also { model ->
      model.state.observe(this, Observer(::handleState))
      model.startLoadingData()
    }
    hostActivity.enableTouchesOnDrawer()
    subscribeToChannel(networkAvailabilityChannel) { viewModel.onNetworkAvailable() }
  }
  
  override fun onHiddenChanged(hidden: Boolean) {
    if (hidden) hostActivity.enableTouchesOnDrawer()
  }
  
  override fun onOrientationBecameLandscape() {
    view(ImageFailure).gone()
  }
  
  override fun onOrientationBecamePortrait() {
    view(ImageFailure).visible()
  }
  
  private fun handleState(state: BaseScreenState) {
    when (state) {
      is Loading -> renderLoading()
      is LoadedNews -> renderLoadedNews(state)
      is LoadingNextPage -> renderLoadingNextPage(state)
      is LoadedNewNews -> renderLoadedNewNews(state)
      is FailureLoadingNextPage -> renderFailureLoadingNextPage(state)
      is Failure -> renderFailure(state)
    }
  }
  
  private fun renderLoading() {
    showOnly(view(LayoutLoading))
  }
  
  private fun renderLoadedNews(state: LoadedNews) {
    showOnly(view(RecyclerView))
    newsAdapter.submitList(state.news)
  }
  
  private fun renderLoadingNextPage(state: LoadingNextPage) {
    showOnly(view(RecyclerView))
    requireView().post { // Posting in case recycler is currently computing layout
      newsAdapter.submitList(state.list)
      // Notifying last item changed, because it can be the same list with changed AdditionalItem
      newsAdapter.notifyItemChanged(state.list.lastIndex)
    }
  }
  
  private fun renderLoadedNewNews(state: LoadedNewNews) {
    showOnly(view(RecyclerView))
    newsAdapter.submitList(state.news)
  }
  
  private fun renderFailureLoadingNextPage(state: FailureLoadingNextPage) {
    showOnly(view(RecyclerView))
    newsAdapter.submitList(state.list)
    // Notifying last item changed, because it can be the same list with changed AdditionalItem
    newsAdapter.notifyItemChanged(state.list.lastIndex)
  }
  
  private fun renderFailure(state: Failure) {
    showOnly(view(LayoutFailure))
    Timber.e(state.throwable)
    textView(ErrorMessage).text(state.reason.getMessageRes())
    when (state.reason) {
      TIMEOUT, UNKNOWN -> imageView(ImageFailure).image(R.drawable.image_unknown_error)
      NO_CONNECTION -> imageView(ImageFailure).image(R.drawable.image_no_connection)
    }
  }
  
  private fun showOnly(view: View) {
    view(RecyclerView).ifTrue({ it !== view }, { animateInvisibleIfNeeded() })
    view(LayoutFailure).ifTrue({ it !== view }, { animateInvisibleIfNeeded() })
    view(LayoutLoading).ifTrue({ it !== view }, { animateInvisibleIfNeeded() })
    view.animateVisibleIfNeeded()
  }
  
  companion object {
    
    const val LayoutLoading = "NewsLoadingLayout"
    const val LayoutFailure = "NewsErrorLayout"
    const val RecyclerView = "NewsRecyclerView"
    const val ImageFailure = "NewsImageFailure"
    const val ErrorMessage = "TextErrorMessage"
    const val ButtonRetry = "ButtonRetry"
  }
}